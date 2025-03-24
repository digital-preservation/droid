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
package uk.gov.nationalarchives.droid.profile;

import jakarta.xml.bind.annotation.XmlRootElement;

import java.net.URI;
import java.nio.file.Path;
import java.util.Date;

@XmlRootElement(name = "Http")
public class HttpProfileResource extends FileProfileResource {

    public HttpProfileResource() {}

    public HttpProfileResource(String uriString) {
        URI uri = URI.create(uriString);
        super.setUri(uri);
        setName(uri.getPath().startsWith("/") ? uri.getPath().substring(1) : uri.getPath());
        setLastModifiedDate(new Date(0));
        int dotLastIndex = uriString.lastIndexOf('.');
        setExtension(dotLastIndex > -1 ? uriString.substring(uriString.lastIndexOf('.')): "");
    }

    public static boolean isHttpUrl(String url) {
        try {
            if (url == null) {
                return false;
            }
            String scheme = URI.create(url.trim().replaceAll(" ", "%20")).getScheme();
            return scheme != null && (scheme.equals("http") || scheme.equals("https"));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public boolean isS3Object() {
        return false;
    }

    @Override
    public boolean isHttpObject() {
        return true;
    }

    @Override
    public void setSize(Path filePath) {}


    public void setUri(URI uri) {
        super.setUri(uri);
    }

}
