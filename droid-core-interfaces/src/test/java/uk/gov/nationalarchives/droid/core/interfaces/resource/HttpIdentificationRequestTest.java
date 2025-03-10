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
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.gov.nationalarchives.droid.core.interfaces.resource.HttpTestUtils.mockHttpClient;

public class HttpIdentificationRequestTest {

    @Test
    public void testHttpIdentificationRequestGetsFirstWindowOnCreation() throws IOException, InterruptedException {
        HttpClient mockHttpClient = mockHttpClient();
        HttpIdentificationRequest request = createRequest(mockHttpClient);

        ArgumentCaptor<HttpRequest> httpRequestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockHttpClient, times(1)).send(httpRequestCaptor.capture(), any(HttpResponse.BodyHandler.class));

        HttpRequest capturedValue = httpRequestCaptor.getValue();

        Optional<String> potentialRange = capturedValue.headers().firstValue("Range");

        assertTrue(potentialRange.isPresent());
        assertEquals(potentialRange.get(), "bytes=0-1");
    }

    @Test
    public void testHttpIdentificationRequestHasCorrectAttributes() throws IOException, InterruptedException {
        HttpClient mockHttpClient = mockHttpClient();
        HttpIdentificationRequest request = createRequest(mockHttpClient);

        request.open(request.getIdentifier().getUri());

        assertEquals(request.size(), 4);
        assertNull(request.getFile());
        assertEquals(request.getFileName(), "file.txt");
        assertEquals(request.getExtension(), "txt");
    }

    @Test
    public void testGetByteReturnsExpectedValues() throws IOException {
        HttpClient mockHttpClient = mockHttpClient();
        HttpIdentificationRequest request = createRequest(mockHttpClient);

        request.open(request.getIdentifier().getUri());

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
    public void testCallsEndpointOnceForMultipleRequestsForSameRange() throws IOException, InterruptedException {
        HttpClient mockHttpClient = mockHttpClient();
        HttpIdentificationRequest request = createRequest(mockHttpClient);

        request.open(request.getIdentifier().getUri());
        request.getByte(0);
        request.getByte(0);

        verify(mockHttpClient, times(2)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    public void testErrorIfHttpCallFails() throws IOException, InterruptedException {
        HttpClient mockHttpClient = mock(HttpClient.class);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenThrow(RuntimeException.class);
        assertThrows(RuntimeException.class, () -> createRequest(mockHttpClient));
    }

    private HttpIdentificationRequest createRequest(HttpClient mockHttpClient) {
        RequestMetaData requestMetaData = new RequestMetaData(1L, 1L, "file.txt");
        URI uri = URI.create("https://example.com");
        RequestIdentifier requestIdentifier = new RequestIdentifier(uri);
        return new HttpIdentificationRequest(requestMetaData, requestIdentifier, mockHttpClient);
    }

}
