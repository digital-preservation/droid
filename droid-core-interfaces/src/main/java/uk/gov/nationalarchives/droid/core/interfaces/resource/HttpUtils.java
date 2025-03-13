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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class HttpUtils {

    private final HttpClient httpClient;

    public HttpUtils(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public record HttpMetadata(Long fileSize, Long lastModified, URI uri) {}

    public HttpMetadata getHttpMetadata(URI uri) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Range", "bytes=0-1")
                .GET()
                .build();
        HttpHeaders headers;
        try {
            headers = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray())
                    .headers();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        Long lastModified = headers.firstValue("last-modified").map(lastModifiedString -> {
            try {
                ZonedDateTime parsedDate = ZonedDateTime.parse(lastModifiedString, DateTimeFormatter.RFC_1123_DATE_TIME);
                return parsedDate.toEpochSecond();
            } catch (DateTimeParseException e) {
                return Instant.now().getEpochSecond();
            }
        }).orElse(Instant.now().getEpochSecond());
        Long contentLength = Long.parseLong(
                headers
                        .firstValue("content-range")
                        .map(range -> range.split("/")[1])
                        .orElse("0")
        );
        return new HttpMetadata(contentLength, lastModified, uri);
    }
}
