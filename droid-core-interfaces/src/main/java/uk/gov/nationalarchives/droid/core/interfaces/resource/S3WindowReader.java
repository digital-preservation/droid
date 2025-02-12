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
import software.amazon.awssdk.services.s3.S3Uri;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;

import java.io.IOException;

public class S3WindowReader extends AbstractReader implements SoftWindowRecovery {

    private final S3Object s3Object;

    private final S3Client s3Client;

    private final Long length;

    //private final Integer windowSize; TODO Allow override from CLI

    private record S3Object(String bucket, String key) {}

    public S3WindowReader(WindowCache cache, S3Uri uri, S3Client s3Client) {
        super(cache);
        this.s3Client = s3Client;
        String bucket = uri.bucket().orElseThrow(() -> new RuntimeException("Bucket not found in uri " + uri));
        String key = uri.key().orElseThrow(() -> new RuntimeException("Key not found in uri " + uri));
        HeadObjectRequest request = HeadObjectRequest.builder().bucket(bucket).key(key).build();
        this.length = s3Client.headObject(request).contentLength();
        this.s3Object = new S3Object(bucket, key);
    }

    @Override
    protected Window createWindow(long windowStart) throws IOException {
        if (windowStart >= 0) {
            GetObjectRequest getS3ObjectRequest = GetObjectRequest.builder()
                    .bucket(this.s3Object.bucket)
                    .key(this.s3Object.key)
                    .range("bytes=" + windowStart + "-" + (windowStart + this.windowSize -1))
                    .build();


            ResponseInputStream<GetObjectResponse> response = s3Client.getObject(getS3ObjectRequest);
            byte[] bytes  = response.readAllBytes();
            int totalRead = bytes.length;
            response.close();
            if (totalRead > 0) {
                return new SoftWindow(bytes, windowStart, totalRead, this);
            }
        }
        return null;
    }

    @Override
    public long length() throws IOException {
        return this.length;
    }

    @Override
    public byte[] reloadWindowBytes(Window window) throws IOException {
        return new byte[0];
    }
}
