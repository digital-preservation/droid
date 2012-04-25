/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui.widgetwrapper;

import java.io.File;

/**
 * Interface for file chooser.
 * @author rflitcroft
 *
 */
public interface FileChooserProxy {

    /** the Approved option. */
    int APPROVE = 1;

    /** the Cancelled option. */
    int CANCEL = 2;
    
    /**
     * 
     * @return te selected file
     */
    File getSelectedFile();
    
    /**
     * 
     * @return The response i.e. APPROVE or CANCEL
     */
    int getResponse();
}
