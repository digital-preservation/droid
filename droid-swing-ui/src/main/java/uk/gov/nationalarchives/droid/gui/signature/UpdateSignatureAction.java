/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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
