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
package uk.gov.nationalarchives.droid.core.interfaces.resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import org.apache.commons.lang.NotImplementedException;

import net.byteseek.io.reader.WindowReader;

import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;


/**
 * FatFileIdentificationRequest.
 */
public class FatFileIdentificationRequest implements IdentificationRequest<InputStream> {

    private  static final int TOP_TAIL_CAPACITY = 2 * 1024 * 1024; // hold 2Mb cache on either end of zip entry.

    private final String fileName;
    private final String extension;

    private final RequestMetaData requestMetaData;
    private final RequestIdentifier identifier;
    private final Path tempDir;

    private long size;

    private WindowReader reader;

    /**
     * @param requestMetaData requestMetaData.
     * @param identifier RequestIdentifier.
     * @param tempDir Fat files are read into buffer and written to temp file.
     */
    public FatFileIdentificationRequest(RequestMetaData requestMetaData, RequestIdentifier identifier, final Path tempDir) {
        this.requestMetaData = requestMetaData;
        this.identifier = identifier;
        size = requestMetaData.getSize();
        fileName = requestMetaData.getName();
        this.tempDir = tempDir;
        extension = ResourceUtils.getExtension(fileName);
    }


    @Override
    public void open(InputStream in) throws IOException {
        reader = ResourceUtils.getStreamReader(in, tempDir, TOP_TAIL_CAPACITY);
        // Force read of entire input stream to build reader and remove dependence on source input stream.
        size = reader.length(); // getting the size of a reader backed by a stream forces a stream read.
    }


    @Override
    public byte getByte(long position) throws IOException {
        return (byte) reader.readByte(position);
    }

    @Override
    public WindowReader getWindowReader() {
        return reader;
    }

    @Override
    public String getFileName() {
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
    public InputStream getSourceInputStream() throws IOException {
        throw new NotImplementedException("not implemented");
    }

    @Override
    public RequestMetaData getRequestMetaData() {
        return requestMetaData;
    }

    @Override
    public RequestIdentifier getIdentifier() {
        return identifier;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    /**
     * @return temp directory for files.
     */
    public Path getTempDir() {
        return this.tempDir;
    }
}
