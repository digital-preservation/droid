package uk.gov.nationalarchives.droid.core.interfaces.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import net.byteseek.io.reader.ReaderInputStream;
import net.byteseek.io.reader.WindowReader;

//CHECKSTYLE:OFF - getting wrong import order - no idea why.
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//CHECKSTYLE:ON

import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;




/**
 *
 */
public class SevenZipEntryIdentificationRequest implements IdentificationRequest<InputStream> {

    private static final int TOP_TAIL_CAPACITY = 2 * 1024 * 1024; // hold 2Mb cache on either end of zip entry.

    private long size;
    private Log log = LogFactory.getLog(this.getClass());

    private WindowReader reader;
    private final RequestIdentifier identifier;
    private RequestMetaData requestMetaData;
    private final String extension;
    private final String fileName;
    private final File tempDir;


    /**
     *
     * @param metaData m
     * @param identifier i
     * @param tempDirLocation t
     */
    public SevenZipEntryIdentificationRequest(RequestMetaData metaData, RequestIdentifier identifier, File tempDirLocation) {
        this.identifier = identifier;
        this.requestMetaData = metaData;
        this.size = requestMetaData.getSize();
        this.tempDir = tempDirLocation;
        fileName = metaData.getName();
        extension = ResourceUtils.getExtension(fileName);
    }


    @Override
    public byte getByte(long position) throws IOException {
        final int result = reader.readByte(position);
        if (result < 0) {
            throw new IOException("No byte at position " + position);
        }
        return (byte) result;
    }

    @Override
    public WindowReader getWindowReader() {
        return reader;
    }

    @Override
    public final String getFileName() {
        return fileName;
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public String getExtension() {
        return extension;
    }

    @Override
    public final void close() throws IOException {
        reader.close();
    }

    @Override
    public final InputStream getSourceInputStream() throws IOException {
        return new ReaderInputStream(reader, false);
    }

    /**
     *
     * @param in i
     * @throws IOException if open unsuccessful
     */
    public final void open(final InputStream in) throws IOException {
        reader = ResourceUtils.getStreamReader(in, tempDir, TOP_TAIL_CAPACITY, true);
    }

    @Override
    public RequestMetaData getRequestMetaData() {
        return requestMetaData;
    }

    @Override
    public final RequestIdentifier getIdentifier() {
        return identifier;
    }


}
