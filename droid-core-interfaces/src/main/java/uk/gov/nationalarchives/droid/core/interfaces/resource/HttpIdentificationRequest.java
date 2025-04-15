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

import net.byteseek.io.reader.ReaderInputStream;
import net.byteseek.io.reader.WindowReader;
import net.byteseek.io.reader.cache.TopAndTailFixedLengthCache;
import net.byteseek.io.reader.cache.WindowCache;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;


public class HttpIdentificationRequest implements IdentificationRequest<URI> {

    private static final int TOP_TAIL_BUFFER_CAPACITY = 30 * 1024 * 1024;
    private HttpWindowReader httpReader;
    private final RequestIdentifier identifier;
    private final RequestMetaData requestMetaData;
    private final long size;
    private final HttpClient client;
    private HttpUtils.HttpMetadata httpMetadata;
    private String extension;

    public HttpIdentificationRequest(final RequestMetaData requestMetaData, final RequestIdentifier identifier, HttpClient httpClient) {
        this.identifier = identifier;
        this.client = httpClient;
        this.requestMetaData = requestMetaData;
        this.httpMetadata = new HttpUtils(httpClient).getHttpMetadata(identifier.getUri());
        this.httpReader = buildWindowReader(identifier.getUri());
        this.size = httpMetadata.fileSize();
    }

    private HttpWindowReader buildWindowReader(final URI theFile) {
        final WindowCache cache = new TopAndTailFixedLengthCache(this.size, TOP_TAIL_BUFFER_CAPACITY);
        HttpUtils.HttpMetadata currentMetadata = this.httpMetadata == null ? new HttpUtils(client).getHttpMetadata(theFile) : this.httpMetadata;
        return new HttpWindowReader(cache, currentMetadata, this.client);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void open(final URI theFile) throws IOException {
        this.httpReader = buildWindowReader(theFile);
        httpReader.getWindow(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getExtension() {
        return extension == null ? ResourceUtils.getExtension(requestMetaData.getName()) : extension;
    }

    /**
     * Sets the file extension. This is used when it can't be determined from the file name
     * @param extension The extension to set
     */
    public final void setExtension(final String extension) {
        this.extension =  extension;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getFileName() {
        return requestMetaData.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final long size() {
        return this.size;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void close() throws IOException {}

    /**
     * {@inheritDoc}
     *
     * @throws IOException on failure to get InputStream
     */
    @Override
    public final InputStream getSourceInputStream() throws IOException {
        return new ReaderInputStream(httpReader, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final RequestMetaData getRequestMetaData() {
        return requestMetaData;
    }

    /**
     * @return the identifier
     */
    public final RequestIdentifier getIdentifier() {
        return identifier;
    }


    @Override
    public byte getByte(long position) throws IOException {
        final int result = httpReader.readByte(position);
        if (result < 0) {
            throw new IOException("No byte at position " + position);
        }
        return (byte) result;
    }

    @Override
    public WindowReader getWindowReader() {
        return this.httpReader;
    }
}
