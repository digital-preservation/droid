/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui.config;

import java.awt.Window;
import java.io.File;

import javax.swing.JOptionPane;

import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileException;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileInfo;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureManager;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureType;

/**
 * @author rflitcroft
 *
 */
public class UploadSignatureFileAction {

    private SignatureManager signatureManager;
    
    private String fileName;
    private boolean useAsDefault;
    private SignatureType type;
    
    /**
     * Executes this action.
     * @param parent the parent window
     */
    public void execute(Window parent) {
        File f = new File(fileName);
        try {
            SignatureFileInfo info = signatureManager.upload(type, f, useAsDefault);
            String message = String.format("Signature file %s has been uploaded", info.getFile().getName());
            JOptionPane.showMessageDialog(parent, message, "Signature file uploaded", JOptionPane.INFORMATION_MESSAGE);
        } catch (SignatureFileException e) {
            JOptionPane.showMessageDialog(parent, e.getMessage(), 
                    "Error uploading signature file", JOptionPane.ERROR_MESSAGE);
        }            
    }

    /**
     * @param signatureManager the signatureManager to set
     */
    public void setSignatureManager(SignatureManager signatureManager) {
        this.signatureManager = signatureManager;
    }
    
    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    /**
     * @param useAsDefault the useAsDefault to set
     */
    public void setUseAsDefault(boolean useAsDefault) {
        this.useAsDefault = useAsDefault;
    }
    
    /**
     * @param type the type to set
     */
    public void setType(SignatureType type) {
        this.type = type;
    }
}
