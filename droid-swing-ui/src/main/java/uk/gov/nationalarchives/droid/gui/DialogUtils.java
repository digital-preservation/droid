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
package uk.gov.nationalarchives.droid.gui;

import java.awt.Frame;
import java.util.Map;

import javax.swing.JOptionPane;

import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileInfo;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureType;

/**
 * Utility class for diaplyaing dialogs.
 * @author rflitcroft
 *
 */
public final class DialogUtils {

    /** */
    private static final String SIGNATURE_UPDATE = "Signature update";

    private DialogUtils() { }
    
    /**
     * Shows the 'update available' YES_NO dialog.
     * @param parent the parent of the dialog
     * @param sigFileInfo signature file available
     * @return the dialog response
     */
    static int showUpdateAvailableDialog(Frame parent, SignatureFileInfo sigFileInfo) {
        return JOptionPane.showConfirmDialog(
                parent,
                String.format("%s signature update v.%s is available. Do you want to download it?", 
                        sigFileInfo.getType(), sigFileInfo.getVersion()),
                SIGNATURE_UPDATE,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

    }
    
    /**
     * Shows the 'update was successful' dialog.
     * @param parent the dialog's parent
     * @param signatureFileInfo the downlaoded signature files
     */
    public static void showUpdateSuccessfulDialog(Frame parent, 
            Map<SignatureType, SignatureFileInfo> signatureFileInfo) {
        StringBuilder sb = new StringBuilder();
        
        for (SignatureFileInfo info : signatureFileInfo.values()) {
            sb.append(String.format("Signature file %s downloaded successfully.\n",
                        info));
        }
        
        JOptionPane.showMessageDialog(parent,
                sb.toString(),
                SIGNATURE_UPDATE,
                JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Shows the 'update was successful' dialog.
     * @param parent the dialog's parent
     */
    static void showNothingIsSelectedForRemoveDialog(Frame parent) {
        JOptionPane.showMessageDialog(parent,
                "Nothing is selected to remove.",
                "Nothing selected to remove.",
                JOptionPane.INFORMATION_MESSAGE);
    }
    
    
    /**
     * Shows the 'update not available' dialog.
     * @param parent the dialog's parent
     */
    static void showUpdateUnavailableDialog(Frame parent) {
        JOptionPane.showMessageDialog(parent,
                "No new signature files are available.",
                SIGNATURE_UPDATE,
                JOptionPane.INFORMATION_MESSAGE);

    }
    
    /**
     * Shows an error message dialog.
     * @param parent the parent
     * @param e the exception that caused the message
     */
    public static void showSignatureUpdateErrorDialog(Frame parent, Throwable e) {
        JOptionPane.showMessageDialog(parent, e.getLocalizedMessage(), SIGNATURE_UPDATE, JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Shows an error message dialog.
     * @param parent the parent
     * @param title the error dialog title
     * @param message the message
     */
    public static void showGeneralErrorDialog(Frame parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }
}
