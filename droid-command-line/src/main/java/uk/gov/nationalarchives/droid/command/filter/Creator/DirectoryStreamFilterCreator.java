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
package uk.gov.nationalarchives.droid.command.filter.Creator;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A class to create DirectoryStream filter(s).
 * @author sparkhi
 */
public class DirectoryStreamFilterCreator {

    private static final String STRING_DOT = ".";
    private final boolean recursive;
    private final String[] extensions;


    /**
     * Parameterized constructor to either create a recursive or non-recursive filter with extensions
     * if recursive, include directories, if non-recursive exclude directories.
     * @param recursive whether the filter would be used to recurse folders
     * @param extensions array of extensions to filter
     */
    public DirectoryStreamFilterCreator(boolean recursive, String[] extensions) {
        this.recursive = recursive;
        this.extensions = extensions;
    }

    /**
     * Default constructor recursing false, and no extension filtering.
     */
    public DirectoryStreamFilterCreator() {
        this(false, null);
    }

    /**
     * Create a filter for the directories. The filter operates on following criteria
     *      1) Accepts all directories if recursive
     *      2) Accept all files if not-recursive
     *      3) Accept all files matching extension(s) and all directories if recursive and has extension filter
     *      4) Accepts all files matching extension(s) if not recursive and has extension filter
     * @return filter
     */
    public DirectoryStream.Filter<Path> create() {
        DirectoryStream.Filter<Path> filter;
        if (this.recursive) {
            filter = new DirectoryStream.Filter<Path>() {
                @Override
                public boolean accept(final Path entry) throws IOException {
                    boolean retVal = true; //for recurse, return everything unless known otherwise
                    if (!Files.isDirectory(entry)) {
                        if (extensions == null || extensions.length == 0) {
                            retVal = true;
                        } else {
                            retVal = false; //there's a filter, we will accept only when matched
                            for (final String extension : extensions) {
                                if (entry.getFileName().toString().endsWith(STRING_DOT + extension)) {
                                    retVal = true;
                                }
                            }
                        }
                    }
                    return retVal;
                }
            };
        } else {
            filter = new DirectoryStream.Filter<Path>() {
                @Override
                public boolean accept(final Path entry) throws IOException {
                    boolean retVal = false; //non recursion, we only accept what matches the filter
                    if (!Files.isDirectory(entry)) {
                        if (extensions == null || extensions.length == 0) {
                            retVal = true;
                        } else {
                            for (final String extension : extensions) {
                                if (entry.getFileName().toString().endsWith(STRING_DOT + extension)) {
                                    retVal = true;
                                }
                            }
                        }
                    }
                    return retVal;
                }
            };
        }
        return filter;
    }
}
