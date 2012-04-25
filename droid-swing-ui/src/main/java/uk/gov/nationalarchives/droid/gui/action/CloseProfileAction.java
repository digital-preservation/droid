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

import javax.swing.SwingWorker;

import uk.gov.nationalarchives.droid.gui.DroidUIContext;
import uk.gov.nationalarchives.droid.gui.ProfileForm;
import uk.gov.nationalarchives.droid.gui.widgetwrapper.JOptionPaneProxy;
import uk.gov.nationalarchives.droid.profile.ProfileManager;

/**
 * Action to close a profile.
 * @author rflitcroft
 *
 */
public class CloseProfileAction {

    private ProfileManager profileManager;
    private DroidUIContext context;
    private ProfileForm parent;
    
    private JOptionPaneProxy userOptionDialog;
    private SaveProfileWorker saveAction;
    
    /**
     * 
     * @param profileManager the profile manager
     * @param context the droid ui context
     * @param parent the profile to close
     */
    public CloseProfileAction(ProfileManager profileManager, DroidUIContext context, ProfileForm parent) {
        this.profileManager = profileManager;
        this.context = context;
        this.parent = parent;
    }
    
    /**
     * Starts the action.
     */
    public void start() {
        if (!parent.getProfile().isDirty()) {
            closeAndRemove(parent.getProfile().getUuid());
        } else {
            int response = userOptionDialog.getResponse();
            if (response == JOptionPaneProxy.NO) {
                String profileId = parent.getProfile().getUuid();
                profileManager.closeProfile(profileId);
                context.remove(profileId);
            } else if (response == JOptionPaneProxy.YES) {
                
                saveAction.addPropertyChangeListener(new PropertyChangeListener() {
                    
                    @Override
                    public void propertyChange(PropertyChangeEvent event) {
                        if ("state".equals(event.getPropertyName())
                                && event.getNewValue() == SwingWorker.StateValue.DONE) {
                            if (!saveAction.isCancelled()) {
                                closeAndRemove(parent.getProfile().getUuid());
                            }
                        }
                    }
                });
                
                saveAction.start(false);
            }
        }
    }
    
    private void closeAndRemove(String profileId) {
        context.remove(parent.getProfile().getUuid());
        profileManager.closeProfile(parent.getProfile().getUuid());
    }

    /**
     * Sets the dialog to prompt the user to save.
     * @param dialog the dialog to set
     */
    public void setUserOptionDialog(JOptionPaneProxy dialog) {
        this.userOptionDialog = dialog;
    }

    /**
     * @param saveAction the saveAction to set
     */
    public void setSaveAction(SaveProfileWorker saveAction) {
        this.saveAction = saveAction;
    }
}
