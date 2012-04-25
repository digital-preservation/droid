/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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
