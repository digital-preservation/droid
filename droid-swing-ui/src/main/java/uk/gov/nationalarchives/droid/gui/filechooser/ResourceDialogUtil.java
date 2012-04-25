/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui.filechooser;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author rflitcroft
 *
 */
public final class ResourceDialogUtil {

    private ResourceDialogUtil() { }
    
    /**
     * Sorts files
     * @param files files to sort
     * @return sorted files.
     */
    static List<File> sortFiles(File[] files) {
        List<File> sortedFiles = Arrays.asList(files);
        Collections.sort(sortedFiles, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });
        
        return sortedFiles;
    }
}
