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

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Uri;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.gov.nationalarchives.droid.core.interfaces.resource.S3TestUtils.mockS3Client;

public class S3IdentificationRequestTest {

    @Test
    public void testS3IdentificationRequestGetsFirstWindowOnCreation() throws IOException {
        S3Client mockS3Client = mockS3Client();
        S3IdentificationRequest request = createRequest(mockS3Client);
        S3Uri s3Uri = S3Uri.builder().uri(request.getIdentifier().getUri()).build();

        request.open(s3Uri);

        ArgumentCaptor<GetObjectRequest> getObjectRequestCaptor = ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(mockS3Client, times(1)).headObject(any(HeadObjectRequest.class));
        verify(mockS3Client, times(1)).getObject(getObjectRequestCaptor.capture());

        GetObjectRequest getRequestValue = getObjectRequestCaptor.getValue();
        assertEquals(getRequestValue.range(), "bytes=0-4095");
    }

    @Test
    public void testS3IdentificationRequestHasCorrectAttributes() throws IOException {
        S3Client mockS3Client = mockS3Client();
        S3IdentificationRequest request = createRequest(mockS3Client);
        S3Uri s3Uri = S3Uri.builder().uri(request.getIdentifier().getUri()).build();

        request.open(s3Uri);

        assertEquals(request.size(), 1);
        assertEquals(request.getFileName(), "entry.txt");
        assertEquals(request.getExtension(), "txt");
    }

    @Test
    public void testGetByteReturnsExpectedValues() throws IOException {
        S3Client mockS3Client = mockS3Client();
        S3IdentificationRequest request = createRequest(mockS3Client);
        S3Uri s3Uri = S3Uri.builder().uri(request.getIdentifier().getUri()).build();

        request.open(s3Uri);

        IOException ioException = assertThrows(IOException.class, () -> request.getByte(-1));
        assertEquals(ioException.getMessage(), "No byte at position -1");

        IOException outsideRangeException = assertThrows(IOException.class, () -> request.getByte(5));
        assertEquals(outsideRangeException.getMessage(), "No byte at position 5");

        byte[] testBytes = "test".getBytes();
        for (int i = 0; i < testBytes.length; i++) {
            assertEquals(testBytes[i], request.getByte(i));
        }
    }

    @Test
    public void testCallsS3OnceForMultipleRequestsForSameRange() throws IOException {
        S3Client mockS3Client = mockS3Client();
        S3IdentificationRequest request = createRequest(mockS3Client);
        S3Uri s3Uri = S3Uri.builder().uri(request.getIdentifier().getUri()).build();

        request.open(s3Uri);
        request.getByte(0);
        request.getByte(0);

        verify(mockS3Client, times(1)).getObject(any(GetObjectRequest.class));
    }

    @Test
    public void testErrorIfS3GetObjectCallsFail() {
        S3Client mockS3Client = mockS3Client();
        when(mockS3Client.getObject(any(GetObjectRequest.class))).thenThrow(SdkException.class);
        S3IdentificationRequest request = createRequest(mockS3Client);
        S3Uri s3Uri = S3Uri.builder().uri(request.getIdentifier().getUri()).build();

        assertThrows(SdkException.class, () -> request.open(s3Uri));
    }

    @Test
    public void testErrorIfS3HeadObjectCallsFail() {
        S3Client mockS3Client = mock(S3Client.class);
        when(mockS3Client.headObject(any(HeadObjectRequest.class))).thenThrow(SdkException.class);
        assertThrows(SdkException.class, () -> createRequest(mockS3Client));
    }

    private S3IdentificationRequest createRequest(S3Client mockS3Client) {
        RequestMetaData requestMetaData = new RequestMetaData(2L, 1L, "entry.txt");
        URI uri = URI.create("s3://bucket/test");

        RequestIdentifier requestIdentifier = new RequestIdentifier(uri);
        return new S3IdentificationRequest(requestMetaData, requestIdentifier, mockS3Client);
    }
}
