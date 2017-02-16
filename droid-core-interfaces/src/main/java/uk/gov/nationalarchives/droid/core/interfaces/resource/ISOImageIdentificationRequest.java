package uk.gov.nationalarchives.droid.core.interfaces.resource;

import net.byteseek.io.reader.ReaderInputStream;
import net.byteseek.io.reader.WindowReader;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by rhubner on 2/13/17.
 */
public class ISOImageIdentificationRequest implements IdentificationRequest<InputStream> {

    private  static final int TOP_TAIL_CAPACITY = 2 * 1024 * 1024; // hold 2Mb cache on either end of zip entry.

    private final String extension;
    private final String fileName;
    private final RequestMetaData requestMetaData;
    private final RequestIdentifier identifier;
    private final File tempDir;
    private final long size;

    private WindowReader reader;


    public ISOImageIdentificationRequest(RequestMetaData requestMetaData, RequestIdentifier identifier, File tempDir) {
        this.fileName = requestMetaData.getName();
        this.extension = ResourceUtils.getExtension(fileName);
        this.requestMetaData = requestMetaData;
        this.identifier = identifier;
        this.requestMetaData.getSize();
        this.tempDir = tempDir;
        this.size = requestMetaData.getSize();

        if(size < 0) {
            throw new IllegalArgumentException("Size can't be bellow zero.");
        }

    }

    @Override
    public void open(InputStream in) throws IOException {
        reader = ResourceUtils.getStreamReader(in, tempDir, TOP_TAIL_CAPACITY, true);
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
    public String getFileName() {
        return this.fileName;
    }

    @Override
    public long size() {
        return this.size;
    }

    @Override
    public String getExtension() {
        return this.extension;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    @Override
    public InputStream getSourceInputStream() throws IOException {
        return new ReaderInputStream(reader, false);
    }

    @Override
    public RequestMetaData getRequestMetaData() {
        return this.requestMetaData;
    }

    @Override
    public RequestIdentifier getIdentifier() {
        return identifier;
    }
}
