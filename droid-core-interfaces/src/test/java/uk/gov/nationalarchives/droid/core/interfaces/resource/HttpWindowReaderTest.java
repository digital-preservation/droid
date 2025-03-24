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
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.nationalarchives.droid.core.interfaces.resource.HttpTestUtils.mockHttpClient;

public class HttpWindowReaderTest {

    @Test
    public void testWindowReaderReturnsExpectedWindow() throws Exception {
        WindowCache windowCache = mock(WindowCache.class);
        HttpClient httpClient = mockHttpClient();
        HttpUtils.HttpMetadata httpMetadata = new HttpUtils.HttpMetadata(4L, 0L, URI.create("https://example.com"));
        HttpWindowReader httpWindowReader = new HttpWindowReader(windowCache, httpMetadata, httpClient);

        byte[] testResponse = "test".getBytes();
        for (int i = 0; i < testResponse.length; i++) {
            Window window = httpWindowReader.createWindow(i);
            assertEquals(window.getClass(), SoftWindow.class);
            assertEquals(window.getWindowPosition(), i);
            assertEquals(window.length(), testResponse.length);
            assertEquals(window.getByte(i), testResponse[i]);
        }
    }

    @Test
    public void testWindowReaderReturnsNullIfPositionLessThanZero() throws Exception {
        WindowCache windowCache = mock(WindowCache.class);
        HttpClient httpClient = mockHttpClient();
        HttpUtils.HttpMetadata httpMetadata = new HttpUtils.HttpMetadata(4L, 0L, URI.create("https://example.com"));
        HttpWindowReader httpWindowReader = new HttpWindowReader(windowCache, httpMetadata, httpClient);

        assertNull(httpWindowReader.createWindow(-1));
    }

    @Test
    public void testWindowReaderReturnsErrorOnHttpFailure() throws Exception {
        WindowCache windowCache = mock(WindowCache.class);
        HttpClient httpClient = mock(HttpClient.class);
        HttpUtils.HttpMetadata httpMetadata = new HttpUtils.HttpMetadata(4L, 0L, URI.create("https://example.com"));
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenThrow(new IOException("Error contacting server"));
        assertThrows(RuntimeException.class, () -> new HttpWindowReader(windowCache, httpMetadata, httpClient).getWindow(0));
    }
}
