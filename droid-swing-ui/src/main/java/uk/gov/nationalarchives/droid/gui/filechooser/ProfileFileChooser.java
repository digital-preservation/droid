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
        setFileFilter(filter);
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
