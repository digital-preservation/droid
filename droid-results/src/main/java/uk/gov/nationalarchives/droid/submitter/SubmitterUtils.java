/**
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gsi.gov.uk>
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
package uk.gov.nationalarchives.droid.submitter;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author rflitcroft
 * 
 * Utilty class for file submission.
 *
 */
public final class SubmitterUtils {

    private static final char FORWARD_SLASH = '/';

    private SubmitterUtils() { }

    /**
     * Determines via best effort if the file system on which the file resides is available.
     * @param file a file
     * @param topLevelAbsolutePath  the top-level resource URI for the file.
     * @return true if the file system is available, false otherwise.
     */
    static boolean isFileSystemAvailable(final File file, final String topLevelAbsolutePath) {
        
        if (isEqualPath(file, topLevelAbsolutePath)) {
            return file.exists();
        } 
        
        return isFileSystemAvailable2(file, topLevelAbsolutePath);
    }

    /**
     * A method to return a file URI from a file, without creating lots of char[] garbage.
     * <p>
     * The method in File.toURI() creates a lot of char[] garbage as it appends slashes
     * and doesn't use a single stringbuilder.  This method also allows us to pass in
     * an external string builder, so we can re-use an existing one.
     *
     * @param file  The file to get a URI for.
     * @param builder A StringBuilder to help build the URI path.
     * @return A URI for the file.
     */
    //CHECKSTYLE:OFF  Too complex
    public static URI toURI(final File file, final StringBuilder builder)  {

        File absoluteFile = file.getAbsoluteFile();

        //Allow for Mockito tests where the previous assignment returns anull reference
        if (absoluteFile == null) {
            absoluteFile = new File(file.getAbsolutePath());
        }

        final String path       = absoluteFile.getPath();
        final int    length     = path.length();
        final char   separator  = File.separatorChar;

        // check how many start slashes we need.
        int numStartSlashes = 0;
        if (path.charAt(0) != separator) {
            numStartSlashes = 1;
        } else if (path.charAt(1) == separator) {
            numStartSlashes = 2;
        }

        // reset the builder to the start:
        builder.setLength(0);

        // do URI forward slashes
        for (int startSlashNum = 0; startSlashNum < numStartSlashes; startSlashNum++) {
            builder.append(FORWARD_SLASH);
        }

        // append path (transforming separators to forward slashes if necessary):
        if (separator == FORWARD_SLASH) {
            builder.append(path);
        } else {
            for (int charIndex = 0; charIndex < length; charIndex++) {
                final char theChar = path.charAt(charIndex);
                if (theChar == separator) {
                    builder.append(FORWARD_SLASH);
                } else {
                    builder.append(theChar);
                }
            }
        }

        // ensure we have a closing slash if the file is a directory:
        if (path.charAt(path.length() - 1) != separator
                && absoluteFile.isDirectory()) {
            builder.append(FORWARD_SLASH);
        }

        URI uri = null;
        try {
            uri = new URI("file", null, builder.toString(), null);
        } catch (URISyntaxException e) {
            // ignore - should never happen.
        }
        //CHECKSTYLE:ON
        return uri;
    }


    private static boolean isFileSystemAvailable2(final File file, final String topLevelAbsolutePath) {
        
        boolean available;
        
        if (file.exists()) {
            available = true;
        } else {
            if (isEqualPath(file, topLevelAbsolutePath)) {
                available = false;
            } else {
                available = isFileSystemAvailable2(file.getParentFile(), topLevelAbsolutePath);
            }
        }
        
        return available;
    }
    
    private static boolean isEqualPath(final File file1, final String topLevelAbsolutePath) {
        return topLevelAbsolutePath.equals(file1.getAbsolutePath());
    }


    
}
