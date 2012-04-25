/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.submitter;

import java.io.File;
import java.net.URI;

/**
 * @author rflitcroft
 * 
 * Utilty class for file submission.
 *
 */
public final class SubmitterUtils {

    private SubmitterUtils() { }
    
    /**
     * Determines via best effort if the file system on which the file resides is available.
     * @param file a file
     * @param topLevelResource the top-level resource URI for the file.
     * @return true if the file system is available, false otherwise.
     */
    static boolean isFileSystemAvailable(File file, URI topLevelResource) {
        
        File topLevel = new File(topLevelResource);
        
        if (isEqualPath(file, topLevel)) {
            return file.exists();
        } 
        
        return isFileSystemAvailable(file, topLevel);
    }
    
    private static boolean isFileSystemAvailable(File file, File topLevelResource) { 
        
        boolean available;
        
        if (file.exists()) {
            available = true;
        } else {
            if (isEqualPath(file, topLevelResource)) {
                available = false;
            } else {
                available = isFileSystemAvailable(file.getParentFile(), topLevelResource);
            }
        }
        
        return available;
    }
    
    private static boolean isEqualPath(File file1, File file2) {
        return file1.getAbsolutePath().equals(file2.getAbsolutePath());
    }
    
}
