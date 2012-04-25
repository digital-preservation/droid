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
