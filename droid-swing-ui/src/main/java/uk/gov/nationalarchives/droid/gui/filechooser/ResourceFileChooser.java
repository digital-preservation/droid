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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

/**
 * File chooser for profile resources.
 * @author rflitcroft
 *
 */
public class ResourceFileChooser extends JFileChooser {

    private static final long serialVersionUID = 5229178885735363809L;
    private ResourceChooserButtonPanel bottomPanel;

    /**
     * Overridden to add the control buttons and sub-folders checkbox.
     * @param view the file system view
     */
    @Override
    protected void setup(FileSystemView view) {
        
        super.setup(view);

        bottomPanel = new ResourceChooserButtonPanel(this);
        setDialogType(JFileChooser.OPEN_DIALOG);
        setControlButtonsAreShown(false);
        setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        setMultiSelectionEnabled(true);
    }
    
    /**
     * @return true if the user has selected to include sub-folders in his selection.
     */
    public boolean isSelectionRecursive() {
        return bottomPanel.getSubFolders().isSelected();
    }
    
    /**
     * Overridden to add the control panel to the dialog.
     * @param parent the dialog's parent
     * @return the created dialog
     */
    @Override
    protected JDialog createDialog(Component parent) {
        JDialog dialog = super.createDialog(parent);
        
        Container contentPane = dialog.getContentPane();
        contentPane.add(bottomPanel, BorderLayout.SOUTH);
        dialog.pack();
        return dialog;
    }
    
}
