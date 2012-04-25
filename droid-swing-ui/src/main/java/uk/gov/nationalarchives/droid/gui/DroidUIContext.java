/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JTabbedPane;

import uk.gov.nationalarchives.droid.gui.filechooser.ProfileFileChooser;
import uk.gov.nationalarchives.droid.profile.ProfileInstance;
import uk.gov.nationalarchives.droid.profile.ProfileManager;
import uk.gov.nationalarchives.droid.profile.ProfileState;

/**
 * Context holding current profiles.
 * 
 * @author adash
 */
public class DroidUIContext {

    private Map<String, ProfileForm> profiles = new HashMap<String, ProfileForm>();
    private JTabbedPane tabbedPane;
    private ProfileManager profileManager;
    private JFileChooser profileFileChooser = new ProfileFileChooser();

    /**
     * 
     * @param tabbedPane the tabbed pane component inwhich profiles reside.
     * @param profileManager the profile manager
     */
    public DroidUIContext(JTabbedPane tabbedPane, ProfileManager profileManager) {
        this.tabbedPane = tabbedPane;
        this.profileManager = profileManager;
        
    }
    
    /**
     * 
     * @return the profile manager
     */
    public ProfileManager getProfileManager() {
        return profileManager;
    }

    /**
     * 
     * @param profileManager the profile manager to set
     */
    public void setProfileManager(ProfileManager profileManager) {
        this.profileManager = profileManager;
    }


    /**
     * 
     * @return the currently selected profile
     */
    public ProfileForm getSelectedProfile() {
        return (ProfileForm) tabbedPane.getSelectedComponent(); 
    }
    
    /**
     * Adds a new profile form to the context.
     * @param profileId the ID of the profile
     * @param profileForm the profile for to add
     */
    public void addProfileForm(String profileId, ProfileForm profileForm) {
        profiles.put(profileId, profileForm);
    }
    
    /**
     * Removes a proifile fro the context.
     * @param profileId the ID of the profile to remove.
     */
    public void remove(String profileId) {
        tabbedPane.remove(profiles.remove(profileId));
    }
    
    /**
     * @param profileId a profile ID
     * @return true if the profile given is currently selected; false otherwise.
     */
    public boolean isSelected(String profileId) {
        return getSelectedProfile().getProfile().getUuid().equals(profileId);
    }

    /**
     * Determines if a profile has been loaded using the source file given.
     * @param selectedFile a profile file.
     * @return true if a profile has been loaded from the file given; false otherwise
     */
    public boolean selectProfileWithSource(File selectedFile) {
        for (ProfileForm profileForm : profiles.values()) {
            if (selectedFile.equals(profileForm.getProfile().getLoadedFrom())) {
                tabbedPane.setSelectedComponent(profileForm);
                return true;
            }
        }
        
        return false;
    }

    /**
     * Finds all dirty profiles - i.e. those profiles which have unsaved changes.
     * @return all profiles with unsaved changes
     */
    public List<ProfileForm> allDirtyProfiles() {
        List<ProfileForm> dirtyProfiles = new ArrayList<ProfileForm>();
        
        for (ProfileForm profile : profiles.values()) {
            ProfileInstance profileInstance = profile.getProfile();
            if (profileInstance.isDirty()) {
                dirtyProfiles.add(profile);
            }
        }

        return Collections.unmodifiableList(dirtyProfiles);
    }

    /**
     * @return the profileFileChooser
     */
    public JFileChooser getProfileFileChooser() {
        return profileFileChooser;
    }

    /**
     * 
     * @return all profiles in the context.
     */
    public Collection<ProfileForm> allProfiles() {
        return Collections.unmodifiableCollection(profiles.values());
    }
    
    /**
     * Gets the for for the profile.
     * @param profileId the profile ID
     * @return the prifile's form
     */
    public ProfileForm getProfile(String profileId) {
        return profiles.get(profileId);
    }

    /**
     * @return an unmodifivbale list of running profiles.
     */
    public List<ProfileForm> allRunningProfiles() {
        List<ProfileForm> runningProfiles = new ArrayList<ProfileForm>();
        
        for (ProfileForm profile : profiles.values()) {
            ProfileInstance profileInstance = profile.getProfile();
            if (profileInstance.getState().equals(ProfileState.RUNNING)) {
                runningProfiles.add(profile);
            }
        }

        return Collections.unmodifiableList(runningProfiles);
    }

}
