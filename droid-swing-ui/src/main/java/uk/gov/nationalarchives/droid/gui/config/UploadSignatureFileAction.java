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
