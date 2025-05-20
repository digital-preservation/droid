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
package uk.gov.nationalarchives.droid.internal.api;

import uk.gov.nationalarchives.droid.core.interfaces.IdentificationMethod;

import java.net.URI;
import java.util.Map;

public class ApiResult {
    private final String extension;
    private final IdentificationMethod method;
    private final String puid;
    private final String name;
    private final boolean fileExtensionMismatch;
    private final URI uri;
    private final Map<HashAlgorithm, String> hashResults;

    public ApiResult(
            String extension,
            IdentificationMethod method,
            String puid,
            String name,
            boolean fileExtensionMismatch,
            URI uri,
            Map<HashAlgorithm, String> hashResults
    ) {
        this.extension = extension;
        this.method = method;
        this.puid = puid;
        this.name = name;
        this.fileExtensionMismatch = fileExtensionMismatch;
        this.uri = uri;
        this.hashResults = hashResults;
    }

    public String getName() {
        return name;
    }

    public String getPuid() {
        return puid;
    }

    public IdentificationMethod getMethod() {
        return method;
    }

    public String getExtension() {
        return extension;
    }

    public boolean isFileExtensionMismatch() {
        return fileExtensionMismatch;
    }

    public Map<HashAlgorithm, String> getHashResults() {
        return hashResults;
    }

    public URI getUri() {
        return uri;
    }
}
