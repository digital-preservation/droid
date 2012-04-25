/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui.widgetwrapper;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;

import org.apache.commons.io.FilenameUtils;

/**
 * Wraps a Swing filechooser.
 * @author rflitcroft
 *
 */
public class FileChooserProxyImpl implements FileChooserProxy {

    private JFileChooser fileChooser;
    private Component parent;
    
    /**
     * 
     * @param parent this component's parent
     * @param fileChooser the file chooser to wrap
     */
    public FileChooserProxyImpl(Component parent, JFileChooser fileChooser) {
        this.fileChooser = fileChooser;
        this.parent = parent;
    }
        
    @Override
    public File getSelectedFile() {
        File selectedFile = fileChooser.getSelectedFile();
        if (!selectedFile.exists() && !"droid".equals(FilenameUtils.getExtension(selectedFile.getName()))) {
            selectedFile = new File(selectedFile.getPath() + ".droid");    
        }
        return selectedFile;
    }

    @Override
    public int getResponse() {
        switch (fileChooser.showSaveDialog(parent)) {
            case JFileChooser.APPROVE_OPTION:
                return FileChooserProxy.APPROVE;
            default:
                return FileChooserProxy.CANCEL;
        }
    }
    
}
