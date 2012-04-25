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

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

/**
 * File Chooser for profile files.
 * @author rflitcroft
 *
 */
public class ProfileFileChooser extends JFileChooser {

    private static final long serialVersionUID = -3827306804452137660L;

    /**
     * Overridden to initialise the file chooser for droid profiles.
     * @param view the file system view
     */
    @Override
    protected void setup(FileSystemView view) {
        super.setup(view);
        
        setAcceptAllFileFilterUsed(true);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("DROID 6 profile", "droid");
        addChoosableFileFilter(filter); 
    }
    
    /**
     * Pops up a warning dialog if the selected file exists.
     */
    @Override
    public void approveSelection() {
     
        if (getDialogType() == SAVE_DIALOG) {
            
            int confirm = JOptionPane.YES_OPTION;
            File selectedFile = getSelectedFile();
            if (selectedFile != null && selectedFile.exists()) {
                confirm = JOptionPane.showConfirmDialog(
                        this, 
                        "The file you have selected already exists. Do you wish to overwrite it?",
                        "File exists warning",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );
            }
            
            if (confirm == JOptionPane.YES_OPTION) {
                super.approveSelection();
            } else if (confirm == JOptionPane.CANCEL_OPTION) {
                super.cancelSelection();
            }
            
        } else {
            super.approveSelection();
        }
    }

}
