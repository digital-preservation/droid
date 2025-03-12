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
package uk.gov.nationalarchives.droid.container;

import org.apache.commons.io.FilenameUtils;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;

public class FileMatcher {

    private static final String CONTAINER_NAME_PLACEHOLDER = "{containerFileName}";

    private final FileSystem fileSystem;

    public FileMatcher() {
        this.fileSystem = FileSystems.getFileSystem(URI.create("file:///"));
    }

    public boolean fileMatches(String fileName, String toMatch, String containerFileName) {
        return fileName.equals(toMatch) || globMatches(fileName, toMatch) || matchesName(fileName, toMatch, containerFileName);
    }

    private boolean matchesName(String fileName, String toMatch, String containerFileName) {
        if (fileName != null && fileName.startsWith(CONTAINER_NAME_PLACEHOLDER)) {
            return toMatch.equals(fileName.replace(CONTAINER_NAME_PLACEHOLDER, FilenameUtils.removeExtension(containerFileName)));
        }
        return false;
    }

    private boolean globMatches(String pattern, String toMatch) {
        PathMatcher pathMatcher = fileSystem.getPathMatcher("glob:" + pattern);
        return pathMatcher.matches(fileSystem.getPath(toMatch));
    }


}
