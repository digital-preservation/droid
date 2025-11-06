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
package uk.gov.nationalarchives.droid.core.interfaces.http;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import uk.gov.nationalarchives.droid.core.interfaces.signature.ProxySettings;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpUtil {

    private final HttpClient httpClient;

    public HttpUtil(HttpClient httpClient) {
        this.httpClient = httpClient;
    }


    public static HttpClient createHttpClient(ProxySettings proxySettings) {
        HttpClient.Builder httpBuilder = HttpClient.newBuilder();
        HttpClient httpClient;
        if (proxySettings.isEnabled()) {
            InetSocketAddress inetSocketAddress = new InetSocketAddress(proxySettings.getProxyHost(), proxySettings.getProxyPort());
            httpClient = httpBuilder.proxy(ProxySelector.of(inetSocketAddress)).build();
        } else {
            httpClient = httpBuilder.build();
        }
        return httpClient;
    }

    public static String getBaseUrl(String url) {
        URI uri = URI.create(url);
        String portPart = uri.getPort() == -1 ? "" : ":" + uri.getPort();
        return uri.getScheme() + "://" + uri.getHost() + portPart;
    }

    public Signatures getSignaturesJson(String url) throws IOException, InterruptedException {

        ObjectMapper objectMapper = new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        HttpRequest request = HttpRequest.newBuilder(URI.create(getBaseUrl(url) + "/signatures.json")).GET()
                .header("Accept", "application/json")
                .build();

        String response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
        return objectMapper.readValue(response, Signatures.class);
    }

    public record Signatures(LatestSignature latestSignature, LatestContainerSignature latestContainerSignature) {}
    public record LatestSignature(String name, String location, int version) {}
    public record LatestContainerSignature(String name, String location, int version) {}

}
