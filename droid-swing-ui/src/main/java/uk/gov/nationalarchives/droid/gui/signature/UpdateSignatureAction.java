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
import java.util.Collection;
import java.util.HashMap;
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
public class UpdateSignatureAction extends SwingWorker<Map<SignatureType, SignatureFileInfo>, Void> {

    private final Log log = LogFactory.getLog(getClass());
    
    private SignatureManager signatureManager;
    private SignatureFileInfo signatureFileInfo;
    
    private Frame parent;

    private SignatureUpdateProgressDialog progressDialog;
    
    private Collection<SignatureFileInfo> updates;
    
    /**
     * Default constructor.
     */
    public UpdateSignatureAction() {
    }
    
    /**
     * Starts the action.
     * @param parentFrame the parent frame
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
     * {@inheritDoc}
     */
    @Override
    protected Map<SignatureType, SignatureFileInfo> doInBackground() throws SignatureManagerException {
        Map<SignatureType, SignatureFileInfo> downloaded = new HashMap<SignatureType, SignatureFileInfo>();
        
        for (SignatureFileInfo update : updates) {
            if (!update.hasError()) {
                downloaded.put(update.getType(), signatureManager.downloadLatest(update.getType()));
            }
        }
        
        return downloaded;
    }

    /**
     * @see javax.swing.SwingWorker#done()
     */
    @Override
    protected void done() {
        try {
            Map<SignatureType, SignatureFileInfo> updatedFiles = get();
            progressDialog.setVisible(false);
            DialogUtils.showUpdateSuccessfulDialog(parent, updatedFiles);
        } catch (InterruptedException e) {
            log.debug(e);
        } catch (ExecutionException e) {
            log.error(e.getCause(), e);
            progressDialog.setVisible(false);
            DialogUtils.showSignatureUpdateErrorDialog(parent, e.getCause());
        } catch (CancellationException e) {
            log.warn(e.getMessage());
        } finally {
            progressDialog.setVisible(false);
            progressDialog.dispose();
            parent.setCursor(Cursor.getDefaultCursor());
        }
    }
    
    /**
     * @return the signatureFileInfo
     */
    public SignatureFileInfo getSignatureFileInfo() {
        return signatureFileInfo;
    }
    
    /**
     * @param signatureManager the signatureManager to set
     */
    public void setSignatureManager(SignatureManager signatureManager) {
        this.signatureManager = signatureManager;
    }

    /**
     * The sugnature types that this action will attempt to update.
     * @param updates the updates to set
     */
    public void setUpdates(Collection<SignatureFileInfo> updates) {
        this.updates = updates;
    }
    
}
