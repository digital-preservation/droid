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

import net.byteseek.io.reader.cache.WindowCache;
import net.byteseek.io.reader.windows.SoftWindow;
import net.byteseek.io.reader.windows.Window;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.Invocation;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Uri;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.net.URI;
import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.nationalarchives.droid.core.interfaces.resource.S3TestUtils.mockS3Client;


public class S3WindowReaderTest {

    @Test
    public void testWindowReaderReturnsExpectedWindow() throws Exception {
        WindowCache windowCache = mock(WindowCache.class);
        S3Client s3Client = mockS3Client();
        S3Uri s3Uri = S3Uri.builder().uri(URI.create("s3://bucket/key")).build();
        S3Utils.S3ObjectMetadata s3ObjectMetadata = new S3Utils.S3ObjectMetadata("bucket", Optional.of("key"), s3Uri, 4L, 1L);
        S3WindowReader s3WindowReader = new S3WindowReader(windowCache, s3ObjectMetadata, s3Client);

        byte[] testResponse = "test".getBytes();
        for (int i = 0; i < testResponse.length; i++) {
            Window window = s3WindowReader.createWindow(i);
            assertEquals(window.getClass(), SoftWindow.class);
            assertEquals(window.getWindowPosition(), i);
            assertEquals(window.length(), testResponse.length);
            assertEquals(window.getByte(i), testResponse[i]);
        }
    }

    @Test
    public void testWindowReaderReturnsExpectedWindowForLargeFile() throws Exception {
        WindowCache windowCache = mock(WindowCache.class);
        String response = StringUtils.repeat("*", 5 * 1024 * 1024);
        S3Client s3Client = mockS3Client(response);
        S3Uri s3Uri = S3Uri.builder().uri(URI.create("s3://bucket/key")).build();
        S3Utils.S3ObjectMetadata s3ObjectMetadata = new S3Utils.S3ObjectMetadata("bucket", Optional.of("key"), s3Uri, 4L, 1L);
        S3WindowReader s3WindowReader = new S3WindowReader(windowCache, s3ObjectMetadata, s3Client);

        s3WindowReader.createWindow(0);
        Collection<Invocation> invocations = Mockito.mockingDetails(s3Client).getInvocations();
        GetObjectRequest getObjectRequest = (GetObjectRequest)invocations.stream().toList().getFirst().getArguments()[0];
        assertEquals(getObjectRequest.range(), "bytes=0-" + ((4 * 1024 * 1024) - 1));
    }

    @Test
    public void testWindowReaderReturnsNullIfPositionLessThanZero() throws Exception {
        WindowCache windowCache = mock(WindowCache.class);
        S3Client s3Client = mock(S3Client.class);
        S3Uri s3Uri = S3Uri.builder().uri(URI.create("s3://bucket/key")).build();
        S3Utils.S3ObjectMetadata s3ObjectMetadata = new S3Utils.S3ObjectMetadata("bucket", Optional.of("key"), s3Uri, 1L, 1L);
        S3WindowReader s3WindowReader = new S3WindowReader(windowCache, s3ObjectMetadata, s3Client);

        assertNull(s3WindowReader.createWindow(-1));
    }

    @Test
    public void testWindowReaderReturnsNullIfPositionGreaterOrEqualToLength() throws Exception {
        WindowCache windowCache = mock(WindowCache.class);
        S3Client s3Client = mockS3Client();
        S3Uri s3Uri = S3Uri.builder().uri(URI.create("s3://bucket/key")).build();
        S3Utils.S3ObjectMetadata s3ObjectMetadata = new S3Utils.S3ObjectMetadata("bucket", Optional.of("key"), s3Uri, 4L, 1L);
        S3WindowReader s3WindowReader = new S3WindowReader(windowCache, s3ObjectMetadata, s3Client);

        assertNotNull(s3WindowReader.createWindow(3));
        assertNull(s3WindowReader.createWindow(4));
        assertNull(s3WindowReader.createWindow(5));
    }


    @Test
    public void testWindowReaderReturnsErrorOnS3Failure() throws Exception {
        WindowCache windowCache = mock(WindowCache.class);
        S3Client s3Client = mock(S3Client.class);
        when(s3Client.getObject(any(GetObjectRequest.class))).thenThrow(S3Exception.builder().message("Error contacting s3").build());
        S3Uri s3Uri = S3Uri.builder().uri(URI.create("s3://bucket/key")).build();
        S3Utils.S3ObjectMetadata s3ObjectMetadata = new S3Utils.S3ObjectMetadata("bucket", Optional.of("key"), s3Uri, 1L, 1L);
        S3WindowReader s3WindowReader = new S3WindowReader(windowCache, s3ObjectMetadata, s3Client);

        assertThrows(S3Exception.class, () -> s3WindowReader.createWindow(0));
    }
}
