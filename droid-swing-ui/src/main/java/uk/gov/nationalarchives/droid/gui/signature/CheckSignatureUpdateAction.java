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
package uk.gov.nationalarchives.droid.gui.signature;

import java.awt.Cursor;
import java.awt.Frame;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileInfo;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureManager;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureManagerException;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureType;
import uk.gov.nationalarchives.droid.gui.DialogUtils;

/**
 * @author rflitcroft
 *
 */
public class CheckSignatureUpdateAction extends SwingWorker<Map<SignatureType, SignatureFileInfo>, Void> {

    private final Log log = LogFactory.getLog(getClass());

    private SignatureManager signatureManager;
    private Map<SignatureType, SignatureFileInfo> signatureFileInfos;
    private SignatureUpdateProgressDialog progressDialog;
    private Frame parent;

    private boolean error;

    /**
     * 
     * Default constructor.
     */
    public CheckSignatureUpdateAction() {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<SignatureType, SignatureFileInfo> doInBackground() throws SignatureManagerException {
        
        signatureFileInfos = signatureManager.getLatestSignatureFiles();
        return signatureFileInfos;
    }
    
    /**
     * Starts the task.
     * @param parentFrame the parent of this task
     */
    public void start(Frame parentFrame) {
        this.parent = parentFrame;

        progressDialog = new SignatureUpdateProgressDialog(parent);

        parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        execute();
        progressDialog.setVisible(true);
        if (progressDialog.isCancelled()) {
            cancel(true);
        }
    }
    
    /**
     * @see javax.swing.SwingWorker#done()
     */
    @Override
    protected void done() {
        try {
            signatureFileInfos = get();
        } catch (ExecutionException e) {
            error = true;
            if (progressDialog != null) { progressDialog.setVisible(false); } 
            log.warn(e.getCause());
            DialogUtils.showSignatureUpdateErrorDialog(parent, e.getCause());
        } catch (InterruptedException e) {
            log.debug(e);
        } catch (CancellationException e) {
            log.warn(e.getMessage());
        } finally {
            if (progressDialog != null) {
                progressDialog.setVisible(false);
                progressDialog.dispose();
            }
            if (parent != null) {
                parent.setCursor(Cursor.getDefaultCursor());
            }
        }
    }
    
    /**
     * @param signatureManager the signatureManager to set
     */
    public void setSignatureManager(SignatureManager signatureManager) {
        this.signatureManager = signatureManager;
    }
    
    /**
     * @param progressDialog the progressDialog to set
     */
    public void setProgressDialog(SignatureUpdateProgressDialog progressDialog) {
        this.progressDialog = progressDialog;
    }
    
    /**
     * @return the signatureFileInfo
     */
    public Map<SignatureType, SignatureFileInfo> getSignatureFileInfos() {
        return signatureFileInfos;
    }

    /**
     * @return true if the check failed, false otherwise.
     */
    public boolean hasError() {
        return error;
    }
    
}
