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
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.io.FilenameUtils;

/**
 * @author rflitcroft
 *
 */
public class SaveAsFileChooser extends JFileChooser {
    
    private static final long serialVersionUID = -6568564046303804534L;

    private String warningDialogTitle;
    private String warningMessage;
    private String wrongExtensionMessage;
    
    private Map<FileFilter, String> filters = new HashMap<FileFilter, String>();
    
    /**
     * 
     */
    public SaveAsFileChooser() {
    }
    
    /**
     * @see javax.swing.JFileChooser#approveSelection()
     */
    @Override
    public void approveSelection() {
        // Check if the extension is correct a file which doesn't already exist.
        // This can change file extension to correct extension for export type,
        //...which can result in the file now existing.
        checkExtensionForNewFile();      
        
        // Check if the extension is correct for an existing file.  
        // This can change the file extension to a correct extension for export type,
        // ... which can result in the file not already existing!  Or to another existing file.
        checkExtensionForExistingFile(); // 

        // Make sure we can proceed to write to this file
        // (checks if user is happy to overwrite the file, if it already exists)
        if (overwriteConfirmedIfExists()) {
            super.approveSelection();
        }
    }

    /*
     * Ensure that if the selected file doesn't already exist, it gets the correct file extension.
     */
    private void checkExtensionForNewFile() {

        File f = getSelectedFile();
        if (f != null && !f.exists()) {
            String defaultExtension = filters.get(getFileFilter());
            if (defaultExtension != null) {
                setSelectedFile(appendExtension(getSelectedFile(), defaultExtension));
            }
        }
    }

    /**
     * Ensure that if the file selected already exists, but has the wrong file extension, we offer
     * the option to save with the correct file extension appended
     */
    private void checkExtensionForExistingFile() {
        File f = getSelectedFile();
        if (f != null && f.exists()) {
            String defaultExtension = filters.get(getFileFilter());
            if (!FilenameUtils.isExtension(f.getName(), defaultExtension)) {
                String message = String.format(wrongExtensionMessage, f.getPath(), defaultExtension);
                int confirm = JOptionPane.showConfirmDialog(this, message, warningDialogTitle, 
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    setSelectedFile(appendExtension(getSelectedFile(), defaultExtension));
                }
            }
        }
    }
    

    private boolean overwriteConfirmedIfExists() {
        boolean proceed = true;
        File f = getSelectedFile();
        if (f != null && f.exists()) {
            String message = String.format(warningMessage, f.getPath());
            int confirm = JOptionPane.showConfirmDialog(this, message, warningDialogTitle, 
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            proceed = confirm == JOptionPane.YES_OPTION;
        }
        return proceed; 
    }
    
    /**
     * 
     * @return the file extension associated with the currently selected filter.
     */
    public String getSelectedFilterExtension() {
        return getFileFilter() == null ? null : filters.get(getFileFilter());
    }
    
    private static File appendExtension(File file, String expectedExtension) {
        String fileName = file.getName();
        File f;
        if (!FilenameUtils.isExtension(fileName, expectedExtension)) {
            f = new File(file.getAbsolutePath() + "." + expectedExtension);
        } else {
            f = file;
        }
        return f;
    }
    
    /**
     * Adds a chooseable file filter and associates it with a default extension.
     * @param filter the filter to add
     * @param ext the default extension for files of this type
     */
    public void addChoosableFileFilterWithDefaultExtension(FileFilter filter, String ext) {
        filters.put(filter, ext);
        addChoosableFileFilter(filter);
    }
    
    
    /**
     * @param warningDialogTitle the warningDialogTitle to set
     */
    public void setWarningDialogTitle(String warningDialogTitle) {
        this.warningDialogTitle = warningDialogTitle;
    }
    
    /**
     * @param warningMessage the warningMessage to set
     */
    public void setWarningMessage(String warningMessage) {
        this.warningMessage = warningMessage;
    }
    
    /**
     * @param wrongExtensionMessage the warning if a file extension is wrong.
     */
    public void setWrongExtensionMessage(String wrongExtensionMessage) {
        this.wrongExtensionMessage = wrongExtensionMessage;
    }

}
