/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui.export;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author rflitcroft
 */
public class ExportFileChooser extends JFileChooser {

    private static final String CSV_EXT = "csv";
    
    private static final long serialVersionUID = -3733290468455212962L;

    private FileNameExtensionFilter csvFilter;

    @Override
    protected void setup(FileSystemView view) {
        super.setup(view);

        setAcceptAllFileFilterUsed(true);
        csvFilter = new FileNameExtensionFilter("Comma separated values (*.csv)", CSV_EXT);
        addChoosableFileFilter(csvFilter);
    }

        /**
     * Pops up a warning dialog if the selected file exists.
     */
    @Override
    public void approveSelection() {

        int confirm = JOptionPane.YES_OPTION;
        if (!getSelectedFile().exists() && getFileFilter().equals(csvFilter)) {
            final String filename = getSelectedFile().getName();
            String ext = FilenameUtils.getExtension(filename);
            if (!CSV_EXT.equals(ext)) {
                setSelectedFile(new File(getSelectedFile().getParentFile(), 
                        filename + "." + CSV_EXT));
            }
        }

        if (getSelectedFile().exists()) {
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
