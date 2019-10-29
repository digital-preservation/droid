/*
 * Copyright (C) 2005-2015 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package de.schlichtherle.truezip.zip;

import de.schlichtherle.truezip.rof.BufferedReadOnlyFile;
import de.schlichtherle.truezip.rof.IntervalReadOnlyFile;
import de.schlichtherle.truezip.rof.ReadOnlyFile;
import de.schlichtherle.truezip.rof.ReadOnlyFileInputStream;
import de.schlichtherle.truezip.util.Pool;
import de.schlichtherle.truezip.zip.*;
import edu.umd.cs.findbugs.annotations.CreatesObligation;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.annotation.WillCloseWhenClosed;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.Inflater;
import java.util.zip.ZipException;

import static de.schlichtherle.truezip.util.HashMaps.initialCapacity;
import static de.schlichtherle.truezip.zip.Constants.*;
import static de.schlichtherle.truezip.zip.ExtraField.WINZIP_AES_ID;
import static de.schlichtherle.truezip.zip.LittleEndian.*;
import static de.schlichtherle.truezip.zip.WinZipAesEntryExtraField.VV_AE_2;
import static de.schlichtherle.truezip.zip.WinZipAesUtils.overhead;
import static de.schlichtherle.truezip.zip.ZipEntry.*;
import static de.schlichtherle.truezip.zip.ZipParametersUtils.parameters;

/**
 * Provides unsafe (raw) access to a ZIP file using unsynchronized methods and
 * shared {@link ZipEntry} instances.
 * <p>
 * <b>Warning:</b> This class is <em>not</em> intended for public use
 * - its API may change at will without prior notification!
 * <p>
 * Where the constructors of this class accept a {@code charset}
 * parameter, this is used to decode comments and entry names in the ZIP file.
 * However, if an entry has bit 11 set in its General Purpose Bit Flags,
 * then this parameter is ignored and "UTF-8" is used for this entry.
 * This is in accordance to Appendix D of PKWARE's
 * <a href="http://www.pkware.com/documents/casestudies/APPNOTE.TXT">ZIP File Format Specification</a>,
 * version 6.3.0 and later.
 * <p>
 * This class is able to skip a preamble like the one found in self extracting
 * archives.
 *
 * @param  <E> the type of the ZIP entries.
 * @see    RawZipOutputStream
 * @author Christian Schlichtherle
 */
@NotThreadSafe
public abstract class RawZipFile<E extends ZipEntry>
implements Iterable<E>, Closeable {

    private static final int LFH_FILE_NAME_LENGTH_OFF =
            /* Local File Header signature     */ 4 +
            /* Version Needed To Extract       */ 2 +
            /* General Purpose Bit Flags       */ 2 +
            /* Compression Method              */ 2 +
            /* Last Mod File Time              */ 2 +
            /* Last Mod File Date              */ 2 +
            /* CRC-32                          */ 4 +
            /* Compressed Size                 */ 4 +
            /* Uncompressed Size               */ 4;

    /**
     * The default character set used for entry names and comments in ZIP files.
     * This is {@code "UTF-8"} for compatibility with Sun's JDK implementation.
     * Note that you should use &quot;IBM437&quot; for ordinary ZIP files
     * instead.
     */
    public static final Charset DEFAULT_CHARSET = Constants.DEFAULT_CHARSET;

    /** The nullable data source. */
    private @CheckForNull ReadOnlyFile rof;

    /** The total number of bytes in the ZIP file. */
    private long length;

    /** The number of bytes in the preamble of this ZIP file. */
    private long preamble;

    /** The number of bytes in the postamble of this ZIP file. */
    private long postamble;

    private final ZipEntryFactory<E> param;

    /** The charset to use for entry names and comments. */
    private Charset charset;

    /** The encoded file comment. */
    private @CheckForNull byte[] comment;

    /** HashMaps entry names to zip entries. */
    private Map<String, E> entries;

    /** HashMaps offsets specified in the ZIP file to real offsets in the file. */
    private PositionMapper mapper = new PositionMapper();

    /** The number of open resources for reading the entries in this ZIP file. */
    private int open;

    /**
     * Reads the given {@code zip} file in order to provide random access
     * to its entries.
     *
     * @param  zip the ZIP file to be read.
     * @param  param the parameters for reading the ZIP file.
     * @throws ZipException if the file is not compatible to the ZIP
     *         File Format Specification.
     * @throws IOException on any other I/O related issue.
     * @see    #recoverLostEntries()
     */
    @CreatesObligation
    protected RawZipFile(
            @WillCloseWhenClosed ReadOnlyFile zip,
            ZipFileParameters<E> param)
    throws IOException {
        this(new SingleReadOnlyFilePool(zip), param);
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings("OBL_UNSATISFIED_OBLIGATION")
    RawZipFile(
            final Pool<ReadOnlyFile, IOException> source,
            final ZipFileParameters<E> param)
    throws IOException {
        if (null == param)
            throw new NullPointerException();
        final ReadOnlyFile rof = source.allocate();
        try {
            this.rof = rof;
            this.length = rof.length();
            this.param = param;
            this.charset = param.getCharset();
            final ReadOnlyFile
                    brof = new SafeBufferedReadOnlyFile(rof, this.length);
            if (!param.getPreambled())
                checkZipFileSignature(brof);
            final int numEntries = findCentralDirectory(brof, param.getPostambled());
            mountCentralDirectory(brof, numEntries);
            if (this.preamble + this.postamble >= this.length) {
                assert 0 == numEntries;
                if (param.getPreambled()) // otherwise already checked
                    checkZipFileSignature(brof);
            }
            // Do NOT close brof - would close rof as well!
        } catch (IOException ex) {
            source.release(rof);
            throw ex;
        }
        assert null != this.rof;
        //assert null != this.param; // make FindBugs happy
        assert null != this.charset;
        assert null != this.entries;
        assert null != this.mapper;
    }

    private void checkZipFileSignature(final ReadOnlyFile rof)
    throws IOException {
        final byte[] sig = new byte[4];
        rof.seek(this.preamble);
        rof.readFully(sig);
        final long signature = readUInt(sig, 0);
        // Constraint: A ZIP file must start with a Local File Header
        // or a (ZIP64) End Of Central Directory Record iff it's emtpy.
        if (LFH_SIG != signature
                  && ZIP64_EOCDR_SIG != signature
                  && EOCDR_SIG != signature)
            throw new ZipException(
                    "Expected Local File Header or (ZIP64) End Of Central Directory Record!");
    }

    /**
     * Positions the file pointer at the first Central File Header.
     * Performs some means to check that this is really a ZIP file.
     * <p>
     * As a side effect, the following fields will get initialized:
     * <ul>
     * <li>{@link #preamble}
     * <li>{@link #postamble}
     * </ul>
     * <p>
     * The following fields may get updated:
     * <ul>
     * <li>{@link #comment}
     * <li>{@link #mapper}
     * </ul>
     *
     * @throws ZipException If the file is not compatible to the ZIP File
     *         Format Specification.
     * @throws IOException On any other I/O error.
     */
    private int findCentralDirectory(
            final ReadOnlyFile rof,
            final boolean postambled)
    throws IOException {
        final byte[] sig = new byte[4];
        final long max = this.length - EOCDR_MIN_LEN;
        final long min = !postambled && max >= 0xffff ? max - 0xffff : 0;
        for (long eocdrOff = max; eocdrOff >= min; eocdrOff--) {
            rof.seek(eocdrOff);
            rof.readFully(sig);
            if (EOCDR_SIG != readUInt(sig, 0))
                continue;

            // Process End Of Central Directory Record.
            long diskNo;        // number of this disk
            long cdDiskNo;      // number of the disk with the start of the central directory
            long cdEntriesDisk; // total number of entries in the central directory on this disk
            long cdEntries;     // total number of entries in the central directory
            long cdSize;        // size of the central directory
            long cdOffset;      // offset of start of central directory with respect to the starting disk number
            int commentLen;     // .ZIP file comment length
            final byte[] eocdr = new byte[EOCDR_MIN_LEN - sig.length];
            rof.readFully(eocdr);
            int off = 0;
            diskNo = readUShort(eocdr, off);
            off += 2;
            cdDiskNo = readUShort(eocdr, off);
            off += 2;
            cdEntriesDisk = readUShort(eocdr, off);
            off += 2;
            cdEntries = readUShort(eocdr, off);
            off += 2;
            if (0 != diskNo || 0 != cdDiskNo || cdEntriesDisk != cdEntries)
                throw new ZipException(
                        "ZIP file spanning/splitting is not supported!");
            cdSize = readUInt(eocdr, off);
            off += 4;
            cdOffset = readUInt(eocdr, off);
            off += 4;
            commentLen = readUShort(eocdr, off);
            //off += 2;
            if (0 < commentLen) {
                final byte[] comment = new byte[commentLen];
                rof.readFully(comment);
                this.comment = comment;
            }
            this.preamble = eocdrOff;
            this.postamble = this.length - rof.getFilePointer();

            // Check for ZIP64 End Of Central Directory Locator.
            final long eocdlOff = eocdrOff - ZIP64_EOCDL_LEN;
            if (0 > eocdlOff) {
                // Seek and check first CFH, probably using an offset mapper.
                long offset = eocdrOff - cdSize;
                rof.seek(offset);
                offset -= cdOffset;
                if (0 != offset)
                    this.mapper = new OffsetPositionMapper(offset);
                return (int) cdEntries;
            }
            rof.seek(eocdlOff);
            final byte[] zip64eocdl = new byte[ZIP64_EOCDL_LEN];
            rof.readFully(zip64eocdl);
            if (ZIP64_EOCDL_SIG != readUInt(zip64eocdl, 0)) {
                // dito
                long offset = eocdrOff - cdSize;
                rof.seek(offset);
                offset -= cdOffset;
                if (0 != offset)
                    this.mapper = new OffsetPositionMapper(offset);
                return (int) cdEntries;
            }
            final long zip64eocdrDisk;      // number of the disk with the start of the zip64 end of central directory record
            final long zip64eocdrOff;       // relative offset of the zip64 end of central directory record
            final long totalDisks;          // total number of disks
            off = 4; // reuse
            zip64eocdrDisk = readUInt(zip64eocdl, off);
            off += 4;
            zip64eocdrOff = readLong(zip64eocdl, off);
            off += 8;
            totalDisks = readUInt(zip64eocdl, off);
            //off += 4;
            if (0 != zip64eocdrDisk || 1 != totalDisks)
                throw new ZipException(
                        "ZIP file spanning/splitting is not supported!");

            // Read Zip64 End Of Central Directory Record.
            final byte[] zip64eocdr = new byte[ZIP64_EOCDR_MIN_LEN];
            rof.seek(zip64eocdrOff);
            rof.readFully(zip64eocdr);
            off = 0; // reuse
            // zip64 end of central dir
            // signature                       4 bytes  (0x06064b50)
            if (ZIP64_EOCDR_SIG != readUInt(zip64eocdr, off))
                throw new ZipException(
                        "Expected ZIP64 End Of Central Directory Record!");
            off += 4;
            // size of zip64 end of central
            // directory record                8 bytes
            off += 8;
            // version made by                 2 bytes
            off += 2;
            // version needed to extract       2 bytes
            off += 2;
            // number of this disk             4 bytes
            diskNo = readUInt(zip64eocdr, off);
            off += 4;
            // number of the disk with the
            // start of the central directory  4 bytes
            cdDiskNo = readUInt(zip64eocdr, off);
            off += 4;
            // total number of entries in the
            // central directory on this disk  8 bytes
            cdEntriesDisk = readLong(zip64eocdr, off);
            off += 8;
            // total number of entries in the
            // central directory               8 bytes
            cdEntries = readLong(zip64eocdr, off);
            off += 8;
            if (0 != diskNo || 0 != cdDiskNo || cdEntriesDisk != cdEntries)
                throw new ZipException(
                        "ZIP file spanning/splitting is not supported!");
            if (cdEntries < 0 || Integer.MAX_VALUE < cdEntries)
                throw new ZipException(
                        "Total Number Of Entries In The Central Directory out of range!");
            // size of the central directory   8 bytes
            //cdSize = readLong(zip64eocdr, off);
            off += 8;
            // offset of start of central
            // directory with respect to
            // the starting disk number        8 bytes
            cdOffset = readLong(zip64eocdr, off);
            //off += 8;
            // zip64 extensible data sector    (variable size)
            rof.seek(cdOffset);
            this.preamble = zip64eocdrOff;
            return (int) cdEntries;
        }

        // Start recovering file entries from min.
        this.preamble = min;
        this.postamble = this.length - min;
        return 0;
    }

    /**
     * Reads the central directory from the given read only file file and
     * populates the internal tables with ZipEntry instances.
     * <p>
     * The ZipEntrys will know all data that can be obtained from
     * the central directory alone, but not the data that requires the
     * local file header or additional data to be read.
     * <p>
     * As a side effect, the following fields will get initialized:
     * <ul>
     * <li>{@link #entries}
     * </ul>
     * <p>
     * The following fields may get updated:
     * <ul>
     * <li>{@link #preamble}
     * <li>{@link #charset}
     * </ul>
     *
     * @throws ZipException If the file is not compatible to the ZIP File
     *         Format Specification.
     * @throws IOException On any other I/O related issue.
     */
    private void mountCentralDirectory(final ReadOnlyFile rof, int numEntries)
    throws IOException {
        final Map<String, E> entries = new LinkedHashMap<String, E>(
                Math.max(initialCapacity(numEntries), 16));
        final byte[] cfh = new byte[CFH_MIN_LEN];
        for (; ; numEntries--) {
            rof.readFully(cfh, 0, 4);
            // central file header signature   4 bytes  (0x02014b50)
            if (CFH_SIG != readUInt(cfh, 0))
                break;
            rof.readFully(cfh, 4, CFH_MIN_LEN - 4);
            final int gpbf = readUShort(cfh, 8);
            final int nameLen = readUShort(cfh, 28);
            final byte[] name = new byte[nameLen];
            rof.readFully(name);
            // See appendix D of PKWARE's ZIP File Format Specification.
            final boolean utf8 = 0 != (gpbf & GPBF_UTF8);
            if (utf8)
                this.charset = UTF8;
            final E entry = this.param.newEntry(decode(name));
            try {
                int off = 0;
                // central file header signature   4 bytes  (0x02014b50)
                off += 4;
                // version made by                 2 bytes
                entry.setRawPlatform(readUShort(cfh, off) >> 8);
                off += 2;
                // version needed to extract       2 bytes
                off += 2;
                // general purpose bit flag        2 bytes
                entry.setGeneralPurposeBitFlags(gpbf);
                off += 2; // General Purpose Bit Flags
                // compression method              2 bytes
                entry.setRawMethod(readUShort(cfh, off));
                off += 2;
                // last mod file time              2 bytes
                // last mod file date              2 bytes
                entry.setRawTime(readUInt(cfh, off));
                off += 4;
                // crc-32                          4 bytes
                entry.setRawCrc(readUInt(cfh, off));
                off += 4;
                // compressed size                 4 bytes
                entry.setRawCompressedSize(readUInt(cfh, off));
                off += 4;
                // uncompressed size               4 bytes
                entry.setRawSize(readUInt(cfh, off));
                off += 4;
                // file name length                2 bytes
                off += 2;
                // extra field length              2 bytes
                final int extraLen = readUShort(cfh, off);
                off += 2;
                // file comment length             2 bytes
                final int commentLen = readUShort(cfh, off);
                off += 2;
                // disk number start               2 bytes
                off += 2;
                // internal file attributes        2 bytes
                //entry.setEncodedInternalAttributes(readUShort(cfh, off));
                off += 2;
                // external file attributes        4 bytes
                entry.setRawExternalAttributes(readUInt(cfh, off));
                off += 4;
                // relative offset of local header 4 bytes
                long lfhOff = readUInt(cfh, off);
                entry.setRawOffset(lfhOff); // must be unmapped!
                //off += 4;
                // extra field (variable size)
                if (0 < extraLen) {
                    final byte[] extra = new byte[extraLen];
                    rof.readFully(extra);
                    entry.setRawExtraFields(extra);
                }
                // file comment (variable size)
                if (0 < commentLen) {
                    final byte[] comment = new byte[commentLen];
                    rof.readFully(comment);
                    entry.setRawComment(decode(comment));
                }
                // Re-read virtual offset after ZIP64 Extended Information
                // Extra Field may have been parsed, map it to the real
                // offset and conditionally update the preamble size from it.
                lfhOff = this.mapper.map(entry.getOffset());
                if (lfhOff < this.preamble)
                    this.preamble = lfhOff;
            } catch (IllegalArgumentException cause) {
                throw (ZipException) new ZipException(entry.getName()
                        + " (invalid meta data)").initCause(cause);
            }

            // Map the entry using the name that has been determined
            // by the ZipEntryFactory.
            // Note that this name may differ from what has been found
            // in the ZIP file!
            entries.put(entry.getName(), entry);
        }

        // Check if the number of entries found matches the number of entries
        // declared in the (ZIP64) End Of Central Directory header.
        // Sometimes, legacy ZIP32 archives (those without ZIP64 extensions)
        // contain more than the maximum number of entries specified in the
        // ZIP File Format Specification, which is 65535 (= 0xffff, a two byte
        // unsigned integer).
        // In this case, the declared number of entries usually overflows and
        // may get negative (Java does not support unsigned integers).
        // Although beyond the spec, we silently tolerate this in the test.
        // Thanks to Jean-Francois Thamie for this hint!
        if (0 != numEntries % 0x10000)
            throw new ZipException(
                    "Expected " +
                    Math.abs(numEntries) +
                    (numEntries > 0 ? " more" : " less") +
                    " entries in the Central Directory!");

        // Commit map of entries.
        this.entries = entries;
    }

    /**
     * Recovers any lost entries which have been added to the ZIP file after
     * the (last) End Of Central Directory Record (EOCDR).
     * This method should be called immediately after the constructor.
     * It requires a fully initialized object, hence it's not part of the
     * constructor.
     * For example, to recover encrypted entries, it may require
     * {@link #getCryptoParameters() crypto parameters}.
     * <p>
     * This method starts parsing entries at the start of the postamble and
     * continues until it hits EOF or any non-entry data.
     * As a side effect, it will not only add any found entries to its internal
     * map, but will also cut the start of the postamble accordingly.
     * <p>
     * Note that it's very likely that this method terminates with an
     * exception unless the postamble is empty or contains only valid ZIP
     * entries.
     * Therefore it may be a good idea to log or silently ignore any exception
     * thrown by this method.
     * If an exception is thrown, you can check and recover the remaining
     * postamble for post-mortem analysis by calling
     * {@link #getPostambleLength()} and {@link #getPostambleInputStream()}.
     *
     * @throws ZipException if an invalid entry is found.
     * @throws IOException if any other I/O error occurs.
     */
    public void recoverLostEntries() throws IOException {
        final long length = this.length;
        final ReadOnlyFile rof = new SafeBufferedReadOnlyFile(rof(), length);
        while (0 < this.postamble) {
            long fp = length - postamble;
            rof.seek(fp);
            final byte[] lfh = new byte[LFH_MIN_LEN];
            rof.readFully(lfh);
            if (LFH_SIG != readUInt(lfh, 0))
                throw new ZipException("Expected Local File Header!");
            final int gpbf = readUShort(lfh, 6);
            final int nameLen = readUShort(lfh, 26);
            final byte[] name = new byte[nameLen];
            rof.readFully(name);
            // See appendix D of PKWARE's ZIP File Format Specification.
            final boolean utf8 = 0 != (gpbf & GPBF_UTF8);
            if (utf8)
                this.charset = UTF8;
            final E entry = this.param.newEntry(decode(name));
            try {
                int off = 0;
                // local file header signature     4 bytes  (0x04034b50)
                off += 4;
                // version needed to extract       2 bytes
                off += 2;
                // general purpose bit flag        2 bytes
                entry.setGeneralPurposeBitFlags(gpbf);
                off += 2; // General Purpose Bit Flags
                // compression method              2 bytes
                entry.setRawMethod(readUShort(lfh, off));
                off += 2;
                // last mod file time              2 bytes
                // last mod file date              2 bytes
                entry.setRawTime(readUInt(lfh, off));
                off += 4;
                // crc-32                          4 bytes
                entry.setRawCrc(readUInt(lfh, off));
                off += 4;
                // compressed size                 4 bytes
                entry.setRawCompressedSize(readUInt(lfh, off));
                off += 4;
                // uncompressed size               4 bytes
                entry.setRawSize(readUInt(lfh, off));
                off += 4;
                // file name length                2 bytes
                off += 2;
                // extra field length              2 bytes
                final int extraLen = readUShort(lfh, off);
                //off += 2;
                entry.setRawOffset(mapper.unmap(fp));
                // extra field (variable size)
                if (0 < extraLen) {
                    final byte[] extra = new byte[extraLen];
                    rof.readFully(extra);
                    entry.setRawExtraFields(extra);
                }

                // Process entry contents.
                if (entry.getGeneralPurposeBitFlag(GPBF_DATA_DESCRIPTOR)) {
                    // HC SVNT DRACONES!
                    // This is the tough one.
                    // We need to process the entry as if we were unzipping
                    // it because the CRC-32, the compressed size and the
                    // uncompressed size are unknown.
                    // Once we have done this, we compare our findings to
                    // the Data Descriptor which comes next.
                    final long start = fp = rof.getFilePointer();
                    ReadOnlyFile erof = new IntervalReadOnlyFile(rof,
                            fp, length - fp);
                    WinZipAesEntryExtraField field = null;
                    int method = entry.getMethod();
                    if (entry.isEncrypted()) {
                        if (WINZIP_AES != method)
                            throw new ZipException(entry.getName()
                                    + " (encrypted compression method "
                                    + method
                                    + " is not supported)");
                        erof = new WinZipAesEntryReadOnlyFile(erof,
                                new WinZipAesEntryParameters(
                                    parameters(
                                        WinZipAesParameters.class,
                                        getCryptoParameters()),
                                    entry));
                        field = (WinZipAesEntryExtraField)
                                entry.getExtraField(WINZIP_AES_ID);
                        method = field.getMethod();
                    }
                    final int bufSize = getBufferSize(entry);
                    CountingInputStream din = null;
                    InputStream in;
                    switch (method) {
                        case DEFLATED:
                            in = new ZipInflaterInputStream(
                                    new DummyByteInputStream(erof),
                                    bufSize);
                            break;
                        case BZIP2:
                            din = new CountingInputStream(
                                    new ReadOnlyFileInputStream(erof));
                            in = new BZip2CompressorInputStream(din);
                            break;
                        default:
                            throw new ZipException(entry.getName()
                                    + " (compression method "
                                    + method
                                    + " is not supported)");
                    }
                    final CheckedInputStream
                            cin = new CheckedInputStream(in, new CRC32());
                    try {
                        entry.setRawSize(cin.skip(Long.MAX_VALUE));
                        if (null != field && field.getVendorVersion() == VV_AE_2)
                            entry.setRawCrc(0);
                        else
                            entry.setRawCrc(cin.getChecksum().getValue());
                        // Sync file pointer on deflated input again.
                        switch (method) {
                            case DEFLATED:
                                Inflater inf = ((ZipInflaterInputStream) in)
                                        .getInflater();
                                assert inf.finished(); // JDK6: R/W 1210/2057; JDK 7: R/W 1193/2057
                                fp += inf.getBytesRead();
                                break;
                            case BZIP2:
                                fp += din.getBytesRead();
                                break;
                            default:
                                throw new AssertionError();
                        }
                    } finally {
                        cin.close();
                    }
                    if (null != field)
                        fp += overhead(field.getKeyStrength());
                    entry.setRawCompressedSize(fp - start);

                    // We have reconstituted all meta data for the entry.
                    // Next comes the Data Descriptor.
                    // Let's parse and check it.
                    final byte[] dd = new byte[
                            entry.isZip64ExtensionsRequired()
                            ? 4 + 8 + 8
                            : 4 + 4 + 4];
                    rof.seek(fp);
                    rof.readFully(dd, 0, 4);
                    long crc = readUInt(dd, 0);
                    // Note the Data Descriptor's Signature is optional:
                    // All newer apps should write it (and so does TrueZIP),
                    // but older apps might not.
                    if (DD_SIG == crc)
                        rof.readFully(dd);
                    else
                        rof.readFully(dd, 4, dd.length - 4);
                    crc = readUInt(dd, 0);
                    final long csize;
                    final long size;
                    if (entry.isZip64ExtensionsRequired()) {
                        csize = readLong(dd, 4);
                        size = readLong(dd, 12);
                    } else {
                        csize = readUInt(dd, 4);
                        size = readUInt(dd, 8);
                    }
                    if (entry.getCrc() != crc)
                        throw new CRC32Exception(entry.getName(),
                                entry.getCrc(), crc);
                    if (entry.getCompressedSize() != csize)
                        throw new ZipException(entry.getName()
                                + " (invalid compressed size in Data Descriptor)");
                    if (entry.getSize() != size)
                        throw new ZipException(entry.getName()
                                + " (invalid uncompressed size in Data Descriptor)");
                } else {
                    // This is the easy one.
                    // The entry is not using a Data Descriptor, so we can
                    // use the properties parsed from the Local File Header.
                    fp += entry.getCompressedSize();
                    rof.seek(fp - 1);
                    if (fp > length || -1 == rof.read())
                        throw new ZipException(entry.getName()
                                + " (truncated ZIP entry)");
                }
            } catch (final IllegalArgumentException ex) {
                throw (ZipException) new ZipException(entry.getName()
                        + " (invalid meta data)").initCause(ex);
            }

            // Entry is almost recovered. Update the postamble length.
            postamble = length - rof.getFilePointer();

            // Map the entry using the name that has been determined
            // by the ZipEntryFactory.
            // Note that this name may differ from what has been found
            // in the ZIP file!
            entries.put(entry.getName(), entry);
        }
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    final Map<String, E> getRawEntries() { return entries; }

    private String decode(byte[] bytes) {
        return new String(bytes, charset);
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    @CheckForNull final byte[] getRawComment() {
        return this.comment;
    }

    /**
     * Returns the file comment.
     *
     * @return The file comment.
     */
    public @Nullable String getComment() {
        final byte[] comment = this.comment;
        return null == comment ? null : decode(comment);
    }

    /**
     * Returns {@code true} if and only if this ZIP file is busy reading
     * one or more entries.
     */
    public boolean busy() {
        return 0 < open;
    }

    /**
     * Returns the character set which is effectively used for
     * decoding entry names and the file comment.
     * Depending on the ZIP file contents, this may differ from the character
     * set provided to the constructor.
     *
     * @since TrueZIP 7.3
     */
    public Charset getRawCharset() {
        return charset;
    }

    /**
     * Returns the name of the character set which is effectively used for
     * decoding entry names and the file comment.
     * Depending on the ZIP file contents, this may differ from the character
     * set provided to the constructor.
     */
    public String getCharset() {
        return charset.name();
    }

    /**
     * Returns the number of entries in this ZIP file.
     */
    public int size() {
        return entries.size();
    }

    /**
     * Returns an iteration of all entries in this ZIP file.
     * Note that the iterated entries are shared with this instance.
     * It is illegal to change their state!
     */
    @Override
    public Iterator<E> iterator() {
        return Collections.unmodifiableCollection(entries.values()).iterator();
    }

    /**
     * Returns the entry for the given name or {@code null} if no entry with
     * this name exists.
     * Note that the returned entry is shared with this instance.
     * It is illegal to change its state!
     *
     * @param name the name of the ZIP entry.
     */
    public E getEntry(String name) {
        return entries.get(name);
    }

    /**
     * Returns the file length of this ZIP file in bytes.
     */
    public long length() {
        return length;
    }

    /**
     * Returns the size of the preamble of this ZIP file in bytes.
     *
     * @return A positive value or zero to indicate that this ZIP file does
     *         not have a preamble.
     */
    public long getPreambleLength() {
        return preamble;
    }

    /**
     * Returns an {@link InputStream} to read the preamble of this ZIP file.
     * <p>
     * Note that the returned stream is a <i>lightweight</i> stream,
     * i.e. there is no external resource such as a {@link ReadOnlyFile}
     * allocated for it. Instead, all streams returned by this method share
     * the underlying {@code ReadOnlyFile} of this {@code ZipFile}.
     * This allows to close this object (and hence the underlying
     * {@code ReadOnlyFile}) without cooperation of the returned
     * streams, which is important if the client application wants to work on
     * the underlying file again (e.g. update or delete it).
     *
     * @throws ZipException If this ZIP file has been closed.
     */
    @CreatesObligation
    public InputStream getPreambleInputStream() throws IOException {
        rof();
        return new ReadOnlyFileInputStream(
                new EntryReadOnlyFile(0, preamble));
    }

    /**
     * Returns the size of the postamble of this ZIP file in bytes.
     *
     * @return A positive value or zero to indicate that this ZIP file does
     *         not have a postamble.
     */
    public long getPostambleLength() {
        return postamble;
    }

    /**
     * Returns an {@link InputStream} to read the postamble of this ZIP file.
     * <p>
     * Note that the returned stream is a <i>lightweight</i> stream,
     * i.e. there is no external resource such as a {@link ReadOnlyFile}
     * allocated for it. Instead, all streams returned by this method share
     * the underlying {@code ReadOnlyFile} of this {@code ZipFile}.
     * This allows to close this object (and hence the underlying
     * {@code ReadOnlyFile}) without cooperation of the returned
     * streams, which is important if the client application wants to work on
     * the underlying file again (e.g. update or delete it).
     *
     * @throws ZipException If this ZIP file has been closed.
     */
    @CreatesObligation
    public InputStream getPostambleInputStream() throws IOException {
        rof();
        return new ReadOnlyFileInputStream(
                new EntryReadOnlyFile(length - postamble, postamble));
    }

    final PositionMapper getOffsetMapper() {
        return mapper;
    }

    /**
     * Returns {@code true} if and only if the offsets in this ZIP file
     * are relative to the start of the file, rather than the first Local
     * File Header.
     * <p>
     * This method is intended for very special purposes only.
     */
    public boolean offsetsConsiderPreamble() {
        assert mapper != null;
        return 0 == mapper.map(0);
    }

    /**
     * Returns the parameters for encryption or authentication of entries.
     *
     * Returns The parameters for encryption or authentication of entries.
     * @since  TrueZIP 7.3
     */
    protected abstract @CheckForNull ZipCryptoParameters getCryptoParameters();

    /**
     * Equivalent to {@link #getInputStream(String, Boolean, boolean)
     * getInputStream(name, null, true)}.
     */
    @CreatesObligation
    public final @Nullable InputStream getInputStream(String name)
    throws IOException {
        return getInputStream(name, null, true);
    }

    /**
     * Equivalent to {@link #getInputStream(String, Boolean, boolean)
     * getInputStream(entry.getName(), null, true)} instead.
     */
    @CreatesObligation
    public final @Nullable InputStream getInputStream(ZipEntry entry)
    throws IOException {
        return getInputStream(entry.getName(), null, true);
    }

    /**
     * Equivalent to {@link #getInputStream(String, Boolean, boolean)
     * getInputStream(name, true, true)}.
     */
    @CreatesObligation
    public final @Nullable InputStream getCheckedInputStream(String name)
    throws IOException {
        return getInputStream(name, true, true);
    }

    /**
     * Equivalent to {@link #getInputStream(String, Boolean, boolean)
     * getInputStream(entry.getName(), true, true)} instead.
     */
    @CreatesObligation
    public final @Nullable InputStream getCheckedInputStream(ZipEntry entry)
    throws IOException {
        return getInputStream(entry.getName(), true, true);
    }

    /**
     * Returns an {@code InputStream} for reading the contents of the given
     * entry.
     * <p>
     * If the {@link #close} method is called on this instance, all input
     * streams returned by this method are closed, too.
     *
     * @param  name The name of the entry to get the stream for.
     * @param  check Whether or not the entry content gets checked/authenticated.
     *         If the parameter {@code process} is {@code false}, then this
     *         parameter is ignored.
     *         Otherwise, if this parameter is {@code null}, then it is set to
     *         the {@link ZipEntry#isEncrypted()} property of the given entry.
     *         Finally, if this parameter is {@code true},
     *         then the following additional check is performed for the entry:
     *         <ul>
     *         <li>If the entry is encrypted, then the Message Authentication
     *             Code (MAC) value gets computed and checked.
     *             If this check fails, then a
     *             {@link ZipAuthenticationException} gets thrown from this
     *             method (pre-check).
     *         <li>If the entry is <em>not</em> encrypted, then the CRC-32
     *             value gets computed and checked.
     *             First, the local file header is checked to hold the same
     *             CRC-32 value than the central directory record.
     *             Second, the CRC-32 value is computed and checked.
     *             If this check fails, then a {@link CRC32Exception}
     *             gets thrown when {@link InputStream#close} is called on the
     *             returned entry stream (post-check).
     *         </ul>
     * @param  process Whether or not the entry contents should get processed,
     *         e.g. inflated.
     *         This should be set to {@code false} if and only if the
     *         application is going to copy entries from an input ZIP file to
     *         an output ZIP file.
     * @return A stream to read the entry data from or {@code null} if the
     *         entry does not exist.
     * @throws ZipAuthenticationException If the entry is encrypted and
     *         checking the MAC fails.
     * @throws ZipException If this file is not compatible to the ZIP File
     *         Format Specification.
     * @throws IOException If the entry cannot get read from this ZipFile.
     */
    @CreatesObligation
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("OBL_UNSATISFIED_OBLIGATION")
    protected @Nullable InputStream getInputStream(
            final String name,
            @CheckForNull Boolean check,
            final boolean process)
    throws IOException {
        final ReadOnlyFile rof = rof();
        if (name == null)
            throw new NullPointerException();
        final ZipEntry entry = entries.get(name);
        if (entry == null)
            return null;
        long fp = entry.getOffset();
        assert UNKNOWN != fp;
        fp = mapper.map(fp);
        rof.seek(fp);
        final byte[] lfh = new byte[LFH_MIN_LEN];
        rof.readFully(lfh);
        if (LFH_SIG != readUInt(lfh, 0))
            throw new ZipException(name + " (expected Local File Header)");
        fp += LFH_MIN_LEN
                + readUShort(lfh, LFH_FILE_NAME_LENGTH_OFF) // file name length
                + readUShort(lfh, LFH_FILE_NAME_LENGTH_OFF + 2); // extra field length
        ReadOnlyFile erof;
        try {
            erof = new EntryReadOnlyFile(
                    fp, entry.getCompressedSize());
        } catch (IllegalArgumentException ex) {
            throw (IOException) new ZipException(name +
                    " (invalid meta data in Local File Header or Central Directory Record)"
                    ).initCause(ex);
        }
        try {
            if (!process) {
                assert UNKNOWN != entry.getCrc();
                return new ReadOnlyFileInputStream(erof);
            }
            if (null == check)
                check = entry.isEncrypted();
            int method = entry.getMethod();
            if (entry.isEncrypted()) {
                if (WINZIP_AES != method)
                    throw new ZipException(name
                            + " (encrypted compression method "
                            + method
                            + " is not supported)");
                final WinZipAesEntryReadOnlyFile
                        eerof = new WinZipAesEntryReadOnlyFile(erof,
                                new WinZipAesEntryParameters(
                                    parameters(
                                        WinZipAesParameters.class,
                                        getCryptoParameters()),
                                    entry));
                erof = eerof;
                if (check) {
                    eerof.authenticate();
                    // Disable redundant CRC-32 check.
                    check = false;
                }
                final WinZipAesEntryExtraField field
                        = (WinZipAesEntryExtraField) entry.getExtraField(WINZIP_AES_ID);
                method = field.getMethod();
            }
            if (check) {
                // Check CRC32 in the Local File Header or Data Descriptor.
                long localCrc;
                if (entry.getGeneralPurposeBitFlag(GPBF_DATA_DESCRIPTOR)) {
                    // The CRC32 is in the Data Descriptor after the compressed
                    // size.
                    // Note the Data Descriptor's Signature is optional:
                    // All newer apps should write it (and so does TrueZIP),
                    // but older apps might not.
                    final byte[] dd = new byte[8];
                    rof.seek(fp + entry.getCompressedSize());
                    rof.readFully(dd);
                    localCrc = readUInt(dd, 0);
                    if (DD_SIG == localCrc)
                        localCrc = readUInt(dd, 4);
                } else {
                    // The CRC32 in the Local File Header.
                    localCrc = readUInt(lfh, 14);
                }
                if (entry.getCrc() != localCrc)
                    throw new CRC32Exception(name, entry.getCrc(), localCrc);
            }
            final int bufSize = getBufferSize(entry);
            InputStream in;
            switch (method) {
                case STORED:
                    in = new ReadOnlyFileInputStream(erof);
                    break;
                case DEFLATED:
                    in = new ZipInflaterInputStream(new DummyByteInputStream(erof),
                            bufSize);
                    break;
                case BZIP2:
                    in = new BZip2CompressorInputStream(
                            new ReadOnlyFileInputStream(erof));
                    break;
                default:
                    throw new ZipException(name
                            + " (compression method "
                            + method
                            + " is not supported)");
            }
            if (check)
                in = new Crc32InputStream(in, entry, bufSize);
            return in;
        } catch (final IOException ex) {
            erof.close();
            throw ex;
        }
    }

    private static int getBufferSize(final ZipEntry entry) {
        long size = entry.getSize();
        if (MAX_FLATER_BUF_LENGTH < size)
            size = MAX_FLATER_BUF_LENGTH;
        else if (size < MIN_FLATER_BUF_LENGTH)
            size = MIN_FLATER_BUF_LENGTH;
        return (int) size;
    }

    /** Checks that this ZIP file is still open for reading its entries. */
    private ReadOnlyFile rof() throws ZipException {
        final ReadOnlyFile rof = this.rof;
        if (null == rof)
            throw new ZipException("ZIP file closed!");
        return rof;
    }

    /**
     * Closes the file.
     * This closes any allocate input streams reading from this ZIP file.
     *
     * @throws IOException if an error occurs closing the file.
     */
    @Override
    public void close() throws IOException {
        final ReadOnlyFile rof = this.rof;
        if (null == rof)
            return;
        rof.close();
        this.rof = null;
    }

    /**
     * An interval read only file which accounts for itself until it gets
     * closed.
     * Note that when an object of this class gets closed, the decorated read
     * only file, i.e. the raw zip file does NOT get closed!
     */
    private final class EntryReadOnlyFile extends IntervalReadOnlyFile {
        private boolean closed;

        @CreatesObligation
        @edu.umd.cs.findbugs.annotations.SuppressWarnings("OBL_UNSATISFIED_OBLIGATION")
        EntryReadOnlyFile(final long start, final long length)
        throws IOException {
            super(rof(), start, length);
            RawZipFile.this.open++;
        }

        @Override
        public void close() throws IOException {
            if (this.closed)
                return;
            // Never close the raw ZIP file!
            //super.close();
            this.closed = true;
            RawZipFile.this.open--;
        }
    } // EntryReadOnlyFile

    /**
     * A buffered read only file which is safe for use with a concurrently
     * growing file, e.g. when another thread is appending to it.
     */
    private static final class SafeBufferedReadOnlyFile
    extends BufferedReadOnlyFile {

        final long length;

        @CreatesObligation
        @edu.umd.cs.findbugs.annotations.SuppressWarnings("OBL_UNSATISFIED_OBLIGATION")
        public SafeBufferedReadOnlyFile(
                final @WillCloseWhenClosed ReadOnlyFile rof,
                final long length)
        throws IOException {
            super(rof);
            assert length <= rof.length();
            this.length = length;
        }

        @Override
        public long length() throws IOException {
            assertOpen();
            return length;
        }
    } // SafeBufferedReadOnlyFile
}
