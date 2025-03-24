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

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpWindowReader extends AbstractReader implements SoftWindowRecovery {

    private final HttpClient httpClient;

    private final Long length;

    private final URI uri;

    public HttpWindowReader(WindowCache cache, HttpUtils.HttpMetadata httpMetadata, HttpClient httpClient) {
        super(cache);
        this.uri = httpMetadata.uri();
        this.httpClient = httpClient;
        this.length = httpMetadata.fileSize();
    }

    private HttpResponse<byte[]> responseWithRange(long rangeStart, long rangeEnd) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(this.uri)
                .header("Range", "bytes=" + rangeStart + "-" + (rangeEnd + this.windowSize - 1))
                .GET()
                .build();

        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Window createWindow(long windowStart) throws IOException {
        if (windowStart >= 0) {
            byte[] bytes  = responseWithRange(windowStart, (windowStart + this.windowSize -1)).body();
            int totalRead = bytes.length;
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
