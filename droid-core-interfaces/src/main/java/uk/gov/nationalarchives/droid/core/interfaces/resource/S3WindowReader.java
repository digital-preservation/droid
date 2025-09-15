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

import net.byteseek.io.reader.AbstractReader;
import net.byteseek.io.reader.cache.WindowCache;
import net.byteseek.io.reader.windows.SoftWindow;
import net.byteseek.io.reader.windows.SoftWindowRecovery;
import net.byteseek.io.reader.windows.Window;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class S3WindowReader extends AbstractReader implements SoftWindowRecovery {

    private static final int BUFFER_LENGTH = 8192;

    private static final int WINDOW_SIZE = 4 * 1024 * 1024;

    private final S3Utils.S3ObjectMetadata s3ObjectMetadata;

    private final S3Client s3Client;

    private final Long length;

    public S3WindowReader(WindowCache cache, S3Utils.S3ObjectMetadata s3ObjectMetadata, S3Client s3Client) {
        super(WINDOW_SIZE, cache);
        this.s3Client = s3Client;
        this.length = s3ObjectMetadata.contentLength();
        this.s3ObjectMetadata = s3ObjectMetadata;
    }

    @Override
    protected Window createWindow(long windowStart) throws IOException {
        if (windowStart >= 0 && windowStart < length) {
            byte[] bytes = bytesForRange(windowStart);
            int totalRead = bytes.length;
            if (totalRead > 0) {
                return new SoftWindow(bytes, windowStart, totalRead, this);
            }
        }
        return null;
    }

    private byte[] bytesForRange(long windowStart) throws IOException {
        String key = this.s3ObjectMetadata.key().orElseThrow(() -> new RuntimeException(this.s3ObjectMetadata.key() + " not found"));
        GetObjectRequest getS3ObjectRequest = GetObjectRequest.builder()
                .bucket(this.s3ObjectMetadata.bucket())
                .key(key)
                .range("bytes=" + windowStart + "-" + (windowStart + this.windowSize -1))
                .build();
        try (ResponseInputStream<GetObjectResponse> response = s3Client.getObject(getS3ObjectRequest)) {
            return toByteArray(response);
        }
    }

    private byte[] toByteArray(ResponseInputStream<GetObjectResponse> inputStream) throws IOException {
        try (InputStream in = inputStream; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[BUFFER_LENGTH];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            return out.toByteArray();
        }
    }

    @Override
    public long length() throws IOException {
        return this.length;
    }

    @Override
    public byte[] reloadWindowBytes(Window window) throws IOException {
        long windowStart = window.getWindowPosition();
        return bytesForRange(windowStart);
    }
}
