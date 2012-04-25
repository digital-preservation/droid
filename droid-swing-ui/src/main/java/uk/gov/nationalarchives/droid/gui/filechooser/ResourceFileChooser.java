/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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
