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

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Creates either a file or a directory profile resource from its file path, and whether it is a recursive profile
 * resource in the case of directories.
 *
 * @author rflitcroft
 *
 */
public class ProfileResourceFactory {

    /**
     * Resolves a location string to a profile resource.
     * @param location the resources location string
     * @param recursive if the resource should be recursed
     * @return a new profile resource
     * @throws IllegalArgumentException if the location passed in is not a file or a directory.
     */
    public AbstractProfileResource getResource(String location, boolean recursive) {
        final Path f = Paths.get(location);
        if (Files.isRegularFile(f)) {
            return new FileProfileResource(f);
        } else if (Files.isDirectory(f)) {
            return new DirectoryProfileResource(f, recursive);
        } else if (S3ProfileResource.isS3uri(location)) {
        	return new S3ProfileResource(location);
        } else if (HttpProfileResource.isHttpUrl(location)) {
            return new HttpProfileResource(location);
        } else {
            throw new IllegalArgumentException(
                    String.format("Unknown location [%s]", location));
        }
    }
}
