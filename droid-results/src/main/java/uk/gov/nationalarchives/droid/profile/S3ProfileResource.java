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

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.Date;
import java.util.regex.Pattern;

import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "S3")
public class S3ProfileResource extends FileProfileResource {

    private boolean isDirectory;

    public S3ProfileResource() {}

    public S3ProfileResource(final String uri) {
        setUri(URI.create(uri));
        setName(uri.substring(uri.lastIndexOf('/') + 1));

        setLastModifiedDate(new Date(0));

        int dotLastIndex = uri.lastIndexOf('.');

        setExtension(dotLastIndex > -1 ? uri.substring(uri.lastIndexOf('.')): "");
    }

    public S3ProfileResource(final File file) {
        this.isDirectory = file.isDirectory();
        setUri(file.toURI());
        String s3uriString = getUri().toString();

        // TODO Find the filename
        setName(s3uriString.substring(s3uriString.lastIndexOf('/') + 1));

        setLastModifiedDate(new Date(0));

        int dotLastIndex = s3uriString.lastIndexOf('.');

        setExtension(dotLastIndex > -1 ? s3uriString.substring(s3uriString.lastIndexOf('.')): "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDirectory() {
        return isDirectory;
    }

    @Override
    public boolean isS3Object() {
        return true;
    }

    @Override
    public boolean isHttpObject() {
        return false;
    }

    @Override
    public void setSize(Path s3Path) {
        System.out.println("S3ProfileResource setSize called");
    }

    public static boolean isS3uri(String candidateS3uri) {
        return Pattern.matches("^s3://([^/]+)/(.*?([^/]+))$", candidateS3uri);
    }
}
