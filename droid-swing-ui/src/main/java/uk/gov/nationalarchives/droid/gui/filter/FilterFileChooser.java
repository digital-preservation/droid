/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui.filter;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import org.apache.commons.io.FilenameUtils;

/**
 * @author a-mpalmer
 *
 */
public class FilterFileChooser extends JFileChooser {
    private static final String FILTER_EXT = "filter";
    
    private static final long serialVersionUID = -3733290468455212962L;

    private FileNameExtensionFilter filterFilter;

    /**
     * 
     * @param startingDir the folder to start the file chooser in.
     */
    public FilterFileChooser(File startingDir) {
        super(startingDir);
    }
    
    @Override
    protected void setup(FileSystemView view) {
        super.setup(view);

        setAcceptAllFileFilterUsed(true);
        filterFilter = new FileNameExtensionFilter("Droid filter files (*.filter)", FILTER_EXT);
        addChoosableFileFilter(filterFilter);
    }

        /**
     * Pops up a warning dialog if the selected file exists.
     */
    @Override
    public void approveSelection() {

        int confirm = JOptionPane.YES_OPTION;
        if (!getSelectedFile().exists() && getFileFilter().equals(filterFilter)) {
            final String filename = getSelectedFile().getName();
            String ext = FilenameUtils.getExtension(filename);
            if (!FILTER_EXT.equals(ext)) {
                setSelectedFile(new File(getSelectedFile().getParentFile(), 
                        filename + "." + FILTER_EXT));
            }
        }

        if (getDialogType() == SAVE_DIALOG && getSelectedFile().exists()) {
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
    }

}

