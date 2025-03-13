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

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HttpTestUtils {
    static HttpClient mockHttpClient() {
        HttpClient httpClientMock = mock(HttpClient.class);

        byte[] responseBody = "test".getBytes();
        try {
            when(httpClientMock.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenAnswer(invocation -> {
                HttpRequest argument = invocation.getArgument(0);
                HttpResponse<byte[]> httpResponseMock = mock(HttpResponse.class);
                String lastModified = DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.ofInstant(Instant.EPOCH, ZoneId.of("UTC")));
                when(httpResponseMock.headers()).thenReturn(HttpHeaders.of(
                        Map.of("content-range", List.of("bytes 0-0/4"), "last-modified", List.of(lastModified)), (a, b) -> true
                ));
                String range = argument.headers().firstValue("Range").orElseThrow(() -> new RuntimeException("Missing range"));
                int rangeStart = Integer.parseInt(range.split("=")[1].split("-")[0]);
                if (rangeStart > responseBody.length) {
                    when(httpResponseMock.body()).thenThrow(new RuntimeException("Invalid range"));
                } else {
                    when(httpResponseMock.body()).thenReturn(responseBody);
                }
                return httpResponseMock;
            });
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
        return httpClientMock;
    }
}
