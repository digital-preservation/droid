/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui.action;

import java.awt.Component;
import java.util.List;

import javax.swing.JOptionPane;

import uk.gov.nationalarchives.droid.gui.DroidUIContext;
import uk.gov.nationalarchives.droid.gui.ProfileForm;
import uk.gov.nationalarchives.droid.profile.ProfileManager;

/**
 * Action to stop running profiles.
 * @author rflitcroft
 *
 */
public class StopRunningProfilesAction {

    private DroidUIContext context;
    private ProfileManager profileManager;
    private Component parent;

    /**
     * 
     * @param profileManager the profile manager
     * @param context the droid UI context
     * @param parent the prant component
     */
    public StopRunningProfilesAction(ProfileManager profileManager, DroidUIContext context,
        Component parent) {
    
        this.profileManager = profileManager;
        this.context = context;
        this.parent = parent;
    }
    
    /**
     * Returns true if all profiles were stopped; false otherwise.
     * @return true if the profiles were stopped, false otherwise.
     */
    public boolean execute() {
        
        boolean profilesStopped;
        
        List<ProfileForm> runningProfiles = context.allRunningProfiles();
        if (runningProfiles.isEmpty()) {
            profilesStopped = true;
        } else {
            int response = JOptionPane.showConfirmDialog(parent, 
                    "Running profiles must be stopped before DROID closes. Do you want to stop running profiles?",
                    "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (response == JOptionPane.YES_OPTION) {
                for (ProfileForm profile : runningProfiles) {
                    profileManager.stop(profile.getProfile().getUuid());
                }
                profilesStopped = true;
            } else {
                profilesStopped = false;
            }
        }
        
        return profilesStopped;
    }
    
}
