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

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Uri;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;

import java.net.URI;
import java.util.Optional;

public class S3Utils {

    private static final String BUCKET_NOT_FOUND = "Bucket not found in uri ";

    private final S3Client s3Client;
    private final Region region;

    public S3Utils(S3Client s3Client) {
        this.s3Client = s3Client;
        this.region = DefaultAwsRegionProviderChain.builder().build().getRegion();
    }

    public S3Utils(S3Client s3Client, Region region) {
        this.s3Client = s3Client;
        this.region = region;
    }

    public record S3ObjectMetadata(String bucket, Optional<String> key, S3Uri uri, Long contentLength, Long lastModified) {}

    public record S3ObjectList(String bucket, Iterable<S3Object> contents) {}

    public S3ObjectMetadata getS3ObjectMetadata(final URI uri) {

        S3Uri s3Uri = S3Utilities.builder().region(region).build().parseUri(uri);
        return getS3ObjectMetadata(s3Uri);
    }

    public S3ObjectMetadata getS3ObjectMetadata(final S3Uri s3Uri) {
        String bucket = s3Uri.bucket().orElseThrow(() -> new RuntimeException(BUCKET_NOT_FOUND + s3Uri));
        Optional<String> key = s3Uri.key();
        long contentLength = 0L;
        long lastModified = 0L;
        if (key.isPresent()) {
            try {
                HeadObjectRequest headObjectRequest = HeadObjectRequest.builder().bucket(bucket).key(key.get()).build();
                HeadObjectResponse headObjectResponse = this.s3Client.headObject(headObjectRequest);
                contentLength = headObjectResponse.contentLength();
                lastModified = headObjectResponse.lastModified().getEpochSecond();
            } catch (NoSuchKeyException ignored) {}
        }
        return new S3ObjectMetadata(bucket, key, s3Uri, contentLength, lastModified);
    }

    public S3ObjectList listObjects(final URI uri) {
        S3Uri s3Uri = S3Utilities.builder().region(region).build().parseUri(uri);
        String bucket = s3Uri.bucket().orElseThrow(() -> new RuntimeException(BUCKET_NOT_FOUND + uri));
        Optional<String> prefix = s3Uri.key();

        ListObjectsV2Request.Builder builder = ListObjectsV2Request.builder().bucket(bucket);
        ListObjectsV2Request request;
        if (prefix.isPresent()) {
            request = builder.prefix(prefix.get()).build();
        } else {
            request = builder.build();
        }

        ListObjectsV2Iterable responseIterable = s3Client.listObjectsV2Paginator(request);
        return new S3ObjectList(bucket, responseIterable.contents());
    }
}
