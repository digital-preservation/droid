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
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Uri;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;


public class S3IdentificationRequest implements IdentificationRequest<S3Uri> {

    private static final int TOP_TAIL_BUFFER_CAPACITY = 30 * 1024 * 1024;
    private WindowReader s3Reader;
    private final RequestIdentifier identifier;
    private final RequestMetaData requestMetaData;

    private final S3Client s3client;
    private final S3Utils.S3ObjectMetadata s3ObjectMetadata;

    public S3IdentificationRequest(final RequestMetaData requestMetaData, final RequestIdentifier identifier, final S3Client s3Client) {
        this.identifier = identifier;
        this.s3client = s3Client;
        this.requestMetaData = requestMetaData;
        S3Utils s3Utils = new S3Utils(s3Client);

        this.s3ObjectMetadata = s3Utils.getS3ObjectMetadata(identifier.getUri());
        this.s3Reader = buildWindowReader();

    }

    private WindowReader buildWindowReader() {
        final WindowCache cache = new TopAndTailFixedLengthCache(this.s3ObjectMetadata.contentLength(), TOP_TAIL_BUFFER_CAPACITY);
        return new S3WindowReader(cache, s3ObjectMetadata, s3client);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void open(final S3Uri theFile) throws IOException {
        this.s3Reader = buildWindowReader();
        s3Reader.getWindow(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getExtension() {
        return ResourceUtils.getExtension(requestMetaData.getName());
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
        return this.s3ObjectMetadata.contentLength();
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
        return new ReaderInputStream(s3Reader, false);
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
        final int result = s3Reader.readByte(position);
        if (result < 0) {
            throw new IOException("No byte at position " + position);
        }
        return (byte) result;
    }

    @Override
    public WindowReader getWindowReader() {
        return this.s3Reader;
    }

    /**
     * Return file associate with identification request.
     *
     * @return File
     */
    public Path getFile() {
        return null;
    }
}
