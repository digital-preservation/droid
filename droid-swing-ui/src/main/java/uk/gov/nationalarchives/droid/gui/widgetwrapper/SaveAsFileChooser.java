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
