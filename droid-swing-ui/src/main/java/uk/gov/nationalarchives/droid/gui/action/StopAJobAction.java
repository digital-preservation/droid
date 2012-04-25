/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui.action;

import uk.gov.nationalarchives.droid.gui.DroidUIContext;
import uk.gov.nationalarchives.droid.gui.ProfileForm;
import uk.gov.nationalarchives.droid.profile.ProfileManagerImpl;

/**
 * Action to stop a job.
 * @author rflitcroft
 *
 */
public class StopAJobAction {

    private DroidUIContext droidContext;
    private ProfileManagerImpl profileManager;

    /**
     * 
     * @param context the droid ui context
     * @param profileManager the profile manager
     */
    public StopAJobAction(DroidUIContext context,
            ProfileManagerImpl profileManager) {
        this.droidContext = context;
        this.profileManager = profileManager;
    }

    /**
     * Stops the profile.
     */
    public void stop() {
        ProfileForm selectedProfile = droidContext.getSelectedProfile();
        profileManager.stop(selectedProfile.getProfile().getUuid());
    }

}
