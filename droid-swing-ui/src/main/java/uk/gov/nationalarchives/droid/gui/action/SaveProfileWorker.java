/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui.action;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.gov.nationalarchives.droid.gui.ProfileForm;
import uk.gov.nationalarchives.droid.gui.widgetwrapper.FileChooserProxy;
import uk.gov.nationalarchives.droid.profile.ProfileInstance;
import uk.gov.nationalarchives.droid.profile.ProfileManager;
import uk.gov.nationalarchives.droid.results.handlers.ProgressObserver;

/**
 * Worker to save a profile in the background.
 * @author rflitcroft
 *
 */
public class SaveProfileWorker extends SwingWorker<ProfileInstance, Void> {

    private final Log log = LogFactory.getLog(getClass());

    private ProfileManager profileManager;
    private File destination;
    private ProfileForm profileForm;
    private ProgressObserver callback;
    private FileChooserProxy dialog;
    
    private String profileId;
    
    /**
     * 
     * @param profileManager the profile manager
     * @param profileForm the profile to save
     * @param dialog a dialog to prompt the user where to save the profile.
     */
    public SaveProfileWorker(final ProfileManager profileManager, 
            final ProfileForm profileForm, FileChooserProxy dialog) {
        
        this.dialog = dialog;
        this.profileManager = profileManager;
        this.profileForm = profileForm;
        ProfileInstance profile = profileForm.getProfile();
        
        profileId = profile.getUuid();

        callback = new ProgressObserver() {
            @Override
            public void onProgress(Integer progress) {
                setProgress(progress);
            }
        };
    }
    
    /**
     * Starts the save operation.
     * @param saveAs if true, forces a save dialog box to open.
     * @return true if the operation was not cancelled.
     */
    public SwingWorker<ProfileInstance, Void> start(boolean saveAs) {
        
        boolean cancelled = false;
        destination = profileForm.getProfile().getLoadedFrom();
        
        if (destination == null || saveAs) {
            if (dialog.getResponse() == FileChooserProxy.APPROVE) {
                destination = dialog.getSelectedFile();
                save();
            } else {
                cancelled = true;
            }
        } else if (profileForm.getProfile().isDirty()) {
            save();
        }
        
        return cancelled ? null : this;
    }
    
    private void save() {

        profileForm.beforeSave();
        final JProgressBar statusProgressBar = profileForm.getStatusProgressBar();
        
        addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if ("progress".equals(evt.getPropertyName())) {
                    statusProgressBar.setValue((Integer) evt.getNewValue());
                }
            }
        });
                
        execute();
    }
    
    @Override
    protected ProfileInstance doInBackground() throws IOException {
        ProfileInstance profile = profileManager.save(profileId, destination, callback);
        return profile;
    }
    
    @Override
    protected void done() {
        try {
            ProfileInstance profile = get();
            profileForm.setName(profile.getName());
        } catch (InterruptedException e) {
            log.debug(e);
            throw new RuntimeException(e.getMessage(), e);
        } catch (ExecutionException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            profileForm.afterSave();
        }
        
    }

}
