/**
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gsi.gov.uk>
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
package uk.gov.nationalarchives.droid.core.interfaces.resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.byteseek.io.reader.ReaderInputStream;
import net.byteseek.io.reader.WindowReader;

import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;

/**
 * Created by rhubner on 3/24/17.
 */
public class RarIdentificationRequest implements IdentificationRequest<InputStream> {

    private  static final int TOP_TAIL_CAPACITY = 2 * 1024 * 1024; // hold 2Mb cache on either end of zip entry.

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final String extension;
    private final String fileName;
    private final RequestMetaData requestMetaData;
    private final RequestIdentifier identifier;
    private final Path tempDir;
    private final long size;

    private WindowReader reader;

    /**
     * Create new identification request instance.
     * @param requestMetaData a
     * @param identifier a
     * @param tempDir a
     */
    public RarIdentificationRequest(final RequestMetaData requestMetaData, final RequestIdentifier identifier, final Path tempDir) {
        this.fileName = requestMetaData.getName();
        this.extension = ResourceUtils.getExtension(fileName);
        this.requestMetaData = requestMetaData;
        this.identifier = identifier;
        this.requestMetaData.getSize();
        this.tempDir = tempDir;
        this.size = requestMetaData.getSize();

    }

    @Override
    public void open(final InputStream in) throws IOException {
        reader = ResourceUtils.getStreamReader(in, tempDir, TOP_TAIL_CAPACITY, true);
        // Force read of entire input stream to build reader and remove dependence on source input stream.
        final long readSize = reader.length(); // getting the size of a reader backed by a stream forces a stream read.
        if (size != readSize) {
            //Possible to change log level im future as we did in ZIP.
            log.warn("Rar element metadata size is not same as read size : " + readSize);
        }

    }

    @Override
    public byte getByte(final long position) throws IOException {
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
