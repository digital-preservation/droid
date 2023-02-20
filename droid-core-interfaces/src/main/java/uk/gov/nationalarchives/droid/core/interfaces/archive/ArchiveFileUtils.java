/*
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gov.uk>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following
 * conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of the The National Archives nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package uk.gov.nationalarchives.droid.core.interfaces.archive;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.apache.commons.compress.compressors.bzip2.BZip2Utils;
import org.apache.commons.compress.compressors.gzip.GzipUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import de.waldheinz.fs.FsDirectoryEntry;
import net.byteseek.io.reader.WindowReader;
import net.byteseek.io.reader.windows.Window;


/**
 * Utilities.
 * @author rflitcroft
 *
 */
public final class ArchiveFileUtils {

    private static final String TEMP_FILENAME_PREFIX = "droid-archive~";
    private static final String SSP_DELIMITER = ":/";
    private static final String ARCHIVE_DELIMITER = "!/";
    private static final String COLON = ":";
    private static final int WRITE_BUFFER_CAPACITY = 8192;
    private static final int FS_READ_BYTE_BUFFER_SIZE = 1048576;

    private ArchiveFileUtils() {
    }

    /**
     * Builds a URI for a zip file entry.
     * @param parent the parent zip file.
     * @param zipEntry the zip entry
     * @return the URI
     */
    public static URI toZipUri(URI parent, String zipEntry) {
        return toUri(parent, zipEntry, ImageType.ZIP);
    }

    /**
     * Create URI for files inside ISO image.
     * @param parent URI of parent ISO file. eg: file://home/user/isofile.iso
     * @param imageEntry Full path of entry inside iso image eg: /dir/another dir/file.txt
     * @return URI.
     */
    public static URI toIsoImageUri(URI parent, String imageEntry) {
        return toUri(parent, imageEntry, ImageType.ISO);
    }

    /**
     * Create URI for files inside RAR Archive.
     * @param parent URI of parent RAR file. eg: file://home/user/myrar.rar
     * @param rarEntry Full path of entry inside iso image eg: /dir/another dir/file.txt
     * @return URI.
     */
    public static URI toRarUri(URI parent, String rarEntry) {
        return toUri(parent, rarEntry, ImageType.RAR);
    }

    /**
     * Builds a URI for a tar file entry.
     * @param parent the parent tar file.
     * @param tarEntry the tar entry
     * @return the URI
     */
    public static URI toTarUri(URI parent, String tarEntry) {
        return toUri(parent, tarEntry, ImageType.TAR);
    }

    /**
     * Builds a URI for a 7z file entry.
     * @param parent the parent 7z file.
     * @param sevenZipEntry the 7z entry
     * @return the URI
     */
    public static URI toSevenZUri(URI parent, String sevenZipEntry) {
        return toUri(parent, sevenZipEntry, ImageType.SEVENZ);
    }

    /**
     * Builds a URI for a Fat file entry.
     * @param parent the parent Fat file.
     * @param fatEntry the Fat entry
     * @return the URI
     */
    public static URI toFatImageUri(URI parent, String fatEntry) {
        return toUri(parent, fatEntry, ImageType.FAT);
    }

    /**
     * Builds a URI for a webarchive file entry modelled on the apache-commons format used for tar files.
     * @param archiveType arc or warc
     * @param parent the parent file
     * @param warcEntry the webarchive entry
     * @return the URI
     */
    public static URI toWebArchiveUri(String archiveType, URI parent, String warcEntry) {
        String parentScheme = parent.getScheme();
        String parentSsp = parent.getSchemeSpecificPart();

        final StringBuilder builder = new StringBuilder(parentSsp.length()
                + ARCHIVE_DELIMITER.length() + warcEntry.length());
        builder.append(archiveType).append(COLON).append(parentScheme);
        String newScheme = builder.toString();
        builder.setLength(0);
        builder.append(parentSsp).append(ARCHIVE_DELIMITER).append(warcEntry);
        String newSSP = builder.toString();

        try {
            return new URI(newScheme, newSSP, null);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Write contents of <code>buffer</code> to a temporary file, followed by the remaining bytes
     * in <code>channel</code>.
     *
     * <p>The bytes are read from <code>buffer</code> from the current position to its limit.</p>
     *
     * @param buffer  contains the contents of the channel read so far
     * @param channel the rest of the channel
     * @param tempDir the directory in which to create the temp file
     * @return <code>File</code> object for the temporary file.
     * @throws java.io.IOException if there is a problem writing to the file
     */
    public static Path writeEntryToTemp(final Path tempDir, final ByteBuffer buffer,
                                        final ReadableByteChannel channel) throws IOException {
        final Path tempFile = Files.createTempFile(tempDir, TEMP_FILENAME_PREFIX, null);
        // NEVER use deleteOnExit() for long running processes.
        // It can cause the JVM to track the files to delete, which 
        // is a memory leak for long running processes.  Leaving the code and comments in 
        // here as a warning to any future developers.
        // Temporary files created must be deleted by the code requesting the file
        // once they are no longer needed.
        // DO NOT USE!!!: tempFile.deleteOnExit();
        try (final ByteChannel out = Files.newByteChannel(tempFile)) {
            out.write(buffer);

            final ByteBuffer buf = ByteBuffer.allocate(WRITE_BUFFER_CAPACITY);
            buf.clear();
            while (channel.read(buf) >= 0 || buf.position() != 0) {
                buf.flip();
                out.write(buf);
                buf.compact();    // In case of partial write
            }
            return tempFile;
            //CHECKSTYLE:OFF
        } catch (RuntimeException ex) {
            //CHECKSTYLE:ON
            if (channel != null) {
                channel.close();
            }
            // don't leave temp files lying around if something went wrong.
            if (tempFile != null) {
                Files.deleteIfExists(tempFile);
            }
            throw ex;
        }
    }


    /**
     * Write FsFile data to tmp location.
     * @param fsFile Fat File directory entry.
     * @param tempDir Temporary file location for FSFile.
     * @return Path path for temporary file.
     * @throws IOException if unable to read temp file.
     */
    public static Path writeFsFileToTemp(final FsDirectoryEntry fsFile, final Path tempDir) throws IOException {

        final Path tempFilePath = tempDir.resolve(fsFile.getName());
        if (fsFile.isFile() && fsFile.getFile().getLength() > 0) {
            int sourceFileOffset = 0;
            final long fileLength = fsFile.getFile().getLength();
            final int fileLengthMod = (int) fileLength % FS_READ_BYTE_BUFFER_SIZE;

            int blockSize = fileLengthMod == 0 ? FS_READ_BYTE_BUFFER_SIZE : fileLengthMod;

            final ByteBuffer bb = ByteBuffer.allocateDirect(blockSize);
            fsFile.getFile().read(sourceFileOffset, bb);
            bb.flip();
            try (final FileChannel wChannel = FileChannel.open(tempFilePath,
                    EnumSet.of(StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING))) {
                wChannel.write(bb);
                sourceFileOffset += blockSize;

                if (sourceFileOffset < fileLength) {
                    ByteBuffer maxBuffer = ByteBuffer.allocateDirect((int) FS_READ_BYTE_BUFFER_SIZE);

                    while (sourceFileOffset < fileLength) {
                        fsFile.getFile().read(sourceFileOffset, maxBuffer);
                        maxBuffer.flip();
                        wChannel.write(maxBuffer);
                        sourceFileOffset += FS_READ_BYTE_BUFFER_SIZE;
                        maxBuffer.clear();
                    }
                }
            }
        }

        return tempFilePath;
    }


    /**
     * Write contents of <code>buffer</code> to a temporary file, followed by the remaining bytes
     * in <code>channel</code>.
     *
     * <p>The bytes are read from <code>buffer</code> from the current position to its limit.</p>
     *
     * @param tempDir the directory in which to create the temp file
     * @param buffer the initial buffer containing the first part of the file.
     * @param in the input stream containing the rest of the file to write out.
     * @return <code>File</code> object for the temporary file.
     * @throws java.io.IOException if there is a problem writing to the file
     */
    public static Path writeEntryToTemp(final Path tempDir, final byte[] buffer,
                                        final InputStream in) throws IOException {
        final Path tempFile = Files.createTempFile(tempDir, TEMP_FILENAME_PREFIX, null);
        // NEVER use deleteOnExit() for long running processes.
        // It can cause the JVM to track the files to delete, which 
        // is a memory leak for long running processes.  Leaving the code and comments in 
        // here as a warning to any future developers.
        // Temporary files created must be deleted by the code requesting the file
        // once they are no longer needed.
        // DO NOT USE!!!: tempFile.deleteOnExit();
        try (final OutputStream out = new BufferedOutputStream(Files.newOutputStream(tempFile))) {
            final byte[] buf = new byte[WRITE_BUFFER_CAPACITY];
            // write the first buffer out:
            out.write(buffer);
            int bytesRead = in.read(buf);
            while (bytesRead > 0) {
                out.write(buf, 0, bytesRead);
                bytesRead = in.read(buf);
            }
            out.flush();
            return tempFile;
            //CHECKSTYLE:OFF
        } catch (RuntimeException ex) {
            //CHECKSTYLE:ON
            if (in != null) {
                in.close();
            }
            // don't leave temp files lying around if something went wrong.
            if (tempFile != null) {
                Files.deleteIfExists(tempFile);
            }
            throw ex;
        }
    }


    /**
     * @param parent the container file
     * @return a GZIP URI
     */
    public static URI toGZipUri(URI parent) {

        String parentScheme = parent.getScheme();
        String parentSsp = parent.getSchemeSpecificPart();

        String gzEntryName = GzipUtils.getUncompressedFilename(FilenameUtils.getName(parent.getSchemeSpecificPart()));
        final StringBuilder builder = new StringBuilder(parentSsp.length()
                + ARCHIVE_DELIMITER.length() + gzEntryName.length());
        builder.append("gz:").append(parentScheme);
        String newScheme = builder.toString();
        builder.setLength(0);
        builder.append(parentSsp).append(ARCHIVE_DELIMITER).append(gzEntryName);
        String newSSP = builder.toString();

        try {
            return new URI(newScheme, newSSP, null);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    /**
     * @param parent the container file
     * @return a BZIP URI
     */
    public static URI toBZipUri(URI parent) {

        String parentScheme = parent.getScheme();
        String parentSsp = parent.getSchemeSpecificPart();

        String gzEntryName = BZip2Utils.getUncompressedFilename(FilenameUtils.getName(parent.getSchemeSpecificPart()));
        final StringBuilder builder = new StringBuilder(parentSsp.length()
                + ARCHIVE_DELIMITER.length() + gzEntryName.length());
        builder.append("bz:").append(parentScheme);
        String newScheme = builder.toString();
        builder.setLength(0);
        builder.append(parentSsp).append(ARCHIVE_DELIMITER).append(gzEntryName);
        String newSSP = builder.toString();

        try {
            return new URI(newScheme, newSSP, null);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * @param requestUri a uri
     * @return the URI needed to replay this uri.
     */
    public static URI toReplayUri(URI requestUri) {
        String originSsp = StringUtils.substringBetween(requestUri.toString(), SSP_DELIMITER, "!");
        String scheme = StringUtils.substringBefore(requestUri.toString(), SSP_DELIMITER);
        if (originSsp != null) {
            return URI.create(StringUtils.substringAfterLast(scheme, COLON) + SSP_DELIMITER + originSsp);
        }

        return requestUri;
    }

    /**
     *
     * @param path The path of a directory
     * @return String[] a string array containing the parent folders, each of
     *         which is a path in its own right (not just the names of each individual folder)
     */
    public static List<String> getAncestorPaths(String path) {
        ArrayList<String> paths = new ArrayList<String>();
        if (path != null && !path.isEmpty()) {
            String processPath = path;
            int lastSeparator = processPath.length() - 1;
            while (lastSeparator >= 0) {
                String separator = path.substring(lastSeparator, lastSeparator + 1);
                processPath = processPath.substring(0, lastSeparator);
                paths.add(processPath + separator);
                lastSeparator = FilenameUtils.indexOfLastSeparator(processPath);
            }
        }
        return paths;
    }

    /**
     * Copies bytes from a WindowReader into a byte array buffer.
     * Up to length bytes will be copied, to the available space in the byte array, from the offset specified.
     *
     * @param reader   The WindowReader to read from.
     * @param position The position in the WindowReader to read from.
     * @param buffer   The byte array to copy into.
     * @param offset   The position in the byte array to begin copying from.
     * @param length   The maximum number of bytes to copy (assuming available bytes in the reader and sufficient
     *                 space in the byte array).
     * @return         The number of bytes copied.
     * @throws IOException If there was a problem reading bytes from the WindowReader.
     */
    public static int copyToBuffer(final WindowReader reader, final long position,
                                   final byte[] buffer, final int offset, final int length) throws IOException {
        long pos = position;
        int bytesCopied = 0;
        final int bytesToRead = Math.min(length, buffer.length - offset);
        Window window = reader.getWindow(pos);
        while (bytesCopied < bytesToRead && window != null) {
            final int positionInWindow = reader.getWindowOffset(pos);
            final int availableBytes = window.length() - positionInWindow;
            final int remainingBytes = bytesToRead - bytesCopied;
            final int bytesToCopy    = remainingBytes < availableBytes ? remainingBytes : availableBytes;
            System.arraycopy(window.getArray(), positionInWindow, buffer, offset + bytesCopied, bytesToCopy);
            pos         += bytesToCopy;
            bytesCopied += bytesToCopy;
            window = reader.getWindow(pos);
        }
        return bytesCopied;
    }

    /**
     * Copies bytes from a WindowReader into a ByteBuffer.
     * It will attempt to fill the ByteBuffer, as long as there are bytes to copy in the WindowReader.
     *
     * @param reader   The WindowReader to read from.
     * @param position The position in the WindowReader to read from.
     * @param buffer   The ByteBuffer to copy bytes into.
     * @return         The number of bytes copied.
     * @throws IOException If there was a problem reading bytes from the WindowReader.
     */
    public static int copyToBuffer(final WindowReader reader, final long position,
                                   final ByteBuffer buffer) throws IOException {
        long pos = position;
        int bytesCopied = 0;
        int bufferRemaining = buffer.remaining();
        Window window = reader.getWindow(pos);
        while (bufferRemaining > 0 && window != null) {
            final int positionInWindow = reader.getWindowOffset(pos);
            final int availableBytes = window.length() - positionInWindow;
            final int bytesToCopy = availableBytes <= bufferRemaining ? availableBytes : bufferRemaining;
            buffer.put(window.getArray(), positionInWindow, bytesToCopy);
            pos         += bytesToCopy;
            bytesCopied += bytesToCopy;
            bufferRemaining = buffer.remaining();
            window = reader.getWindow(pos);
        }
        return bytesCopied;
    }



    /**
     * Create URI for files inside an image.
     * @param parent URI of parent file. eg: file://home/user/isofile.iso
     * @param entry Full path of entry inside image eg: /dir/another dir/file.txt
     * @param type image type
     * @return URI.
     */
    private static URI toUri(URI parent, String entry, ImageType type) {

        final String parentScheme = parent.getScheme();
        final String parentSsp = parent.getSchemeSpecificPart();

        final StringBuilder builder = new StringBuilder(parentSsp.length()
                + ARCHIVE_DELIMITER.length() + entry.length());
        builder.append(type).append(':').append(parentScheme);
        String newScheme = builder.toString();
        builder.setLength(0);
        builder.append(parentSsp).append(ARCHIVE_DELIMITER).append(FilenameUtils.separatorsToUnix(entry));
        String newSSP = builder.toString();

        try {
            return new URI(newScheme, newSSP, null);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private enum ImageType {
         ISO("iso"),
         RAR("rar"),
         TAR("tar"),
         ZIP("zip"),
         SEVENZ("sevenz"),
         FAT("fat");

         private String extension;

        ImageType(String extension) {
            this.extension = extension;
        }

        @Override
        public String toString() {
            return extension;
        }
    }
}
