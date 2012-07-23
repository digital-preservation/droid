/**
 * Copyright (c) 2012, The National Archives <pronom@nationalarchives.gsi.gov.uk>
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
package uk.gov.nationalarchives.droid.util;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;

/**
 * @author rflitcroft
 * 
 */
public final class FileUtil {

    /**
     * 
     */
    private static final int BYTES_IN_KILOBYTE = 1024;

    private FileUtil() {
    }

    /**
     * Determines if a file is a symbolic link.
     * 
     * @param file
     *            the file to determine
     * @return true ifthe file is a symbolic links; false otherwise.
     * @throws IOException
     *             if the file could not be read.
     */
    public static boolean isSymbolicLink(File file) throws IOException {
        if (file == null) {
            throw new NullPointerException("File must not be null");
        }
        File fileInCanonicalDir = null;
        if (file.getParent() == null) {
            fileInCanonicalDir = file;
        } else {
            File canonicalDir = file.getParentFile().getCanonicalFile();
            fileInCanonicalDir = new File(canonicalDir, file.getName());
        }

        return !fileInCanonicalDir.getCanonicalFile().equals(fileInCanonicalDir.getAbsoluteFile());
    }

    /**
     * Formats a file size in hmnan readable form.
     * @param fileSize the size of the file
     * @param decimalPos the number iof decimal places
     * @return a formatted file size.
     */
    public static String formatFileSize(long fileSize, int decimalPos) {
        NumberFormat fmt = NumberFormat.getNumberInstance();
        if (decimalPos >= 0) {
            fmt.setMaximumFractionDigits(decimalPos);
        }
        
        String formattedSize;
        final double size = fileSize;
        double val = size / (BYTES_IN_KILOBYTE * BYTES_IN_KILOBYTE * BYTES_IN_KILOBYTE);
        if (val > 1) {
            formattedSize = fmt.format(val).concat(" GB");
        } else {
            val = size / (BYTES_IN_KILOBYTE * BYTES_IN_KILOBYTE);
            if (val > 1) {
                formattedSize = fmt.format(val).concat(" MB");
            } else {
                val = size / BYTES_IN_KILOBYTE;
                if (val > 1) {
                    formattedSize = fmt.format(val).concat(" KB");
                } else {
                    formattedSize = fmt.format(size).concat(" bytes");
                }
            }
        }
        
        return formattedSize;
    }
}
