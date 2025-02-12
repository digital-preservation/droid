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

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import net.byteseek.io.reader.AbstractReader;
import net.byteseek.io.reader.cache.WindowCache;
import net.byteseek.io.reader.windows.SoftWindow;
import net.byteseek.io.reader.windows.SoftWindowRecovery;
import net.byteseek.io.reader.windows.Window;

import java.io.IOException;
import java.net.URI;

public class S3WindowReader extends AbstractReader implements SoftWindowRecovery {

    private final AmazonS3URI s3Uri;

    private final AmazonS3 s3Client;

    private final Long length;

    //private final Integer windowSize; TODO Allow override from CLI

    public S3WindowReader(WindowCache cache, URI s3Uri, AmazonS3 s3Client) {
        super(cache);
        this.s3Uri = new AmazonS3URI(s3Uri);
        this.s3Client = s3Client;
        this.length = s3Client.getObjectMetadata(this.s3Uri.getBucket(), this.s3Uri.getKey()).getContentLength();
    }

    @Override
    protected Window createWindow(long windowStart) throws IOException {
        if (windowStart >= 0) {
            GetObjectRequest getS3ObjectRequest = new GetObjectRequest(this.s3Uri.getBucket(), this.s3Uri.getKey());

            getS3ObjectRequest.setRange(windowStart, windowStart + this.windowSize - 1);

            final S3Object s3object = s3Client.getObject(getS3ObjectRequest);
            S3ObjectInputStream objectContent = s3object.getObjectContent();
            byte[] bytes  = objectContent.readAllBytes();
            int totalRead = bytes.length;
            objectContent.close();
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
