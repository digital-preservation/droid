/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.UIManager;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Ignore;
import org.junit.Test;

import uk.gov.nationalarchives.droid.gui.widgetwrapper.ProfileSelectionDialog;

public class SaveAllProfilesDialogTest {

    @Test
    @Ignore
    public void showGui() throws Exception {
        
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        

        ProfileForm profile1 = mock(ProfileForm.class);
        ProfileForm profile2 = mock(ProfileForm.class);
        
        when(profile1.getName()).thenReturn("profile1");
        when(profile2.getName()).thenReturn("profile2");
        
        List<ProfileForm> profiles = new ArrayList<ProfileForm>();
        profiles.add(profile1);
        profiles.add(profile2);
        
        ProfileSelectionDialog dialog = new SaveAllProfilesDialog(null, profiles);
            
        dialog.open();
    }
    
    @Test
    public void testSelectionWhenProfile2IsUnselected() {
        ProfileForm profile1 = mock(ProfileForm.class);
        ProfileForm profile2 = mock(ProfileForm.class);
        
        when(profile1.getName()).thenReturn("profile1");
        when(profile2.getName()).thenReturn("profile2");
        
        List<ProfileForm> profiles = new ArrayList<ProfileForm>();
        profiles.add(profile1);
        profiles.add(profile2);
        
        SaveAllProfilesDialog dialog = new SaveAllProfilesDialog(null, profiles);
        dialog.getModel().get(1).toggleSelection();
        
        List<ProfileForm> selections = dialog.getSelectedProfiles();
        assertTrue(selections.contains(profile1));
        assertFalse(selections.contains(profile2));
    }

    @Test
    public void testSelectionWhenBothProfilesAreUnselected() {
        ProfileForm profile1 = mock(ProfileForm.class);
        ProfileForm profile2 = mock(ProfileForm.class);
        
        when(profile1.getName()).thenReturn("profile1");
        when(profile2.getName()).thenReturn("profile2");
        
        List<ProfileForm> profiles = new ArrayList<ProfileForm>();
        profiles.add(profile1);
        profiles.add(profile2);
        
        SaveAllProfilesDialog dialog = new SaveAllProfilesDialog(null, profiles);
        dialog.getModel().get(0).toggleSelection();
        dialog.getModel().get(1).toggleSelection();
        
        List<ProfileForm> selections = dialog.getSelectedProfiles();
        assertFalse(selections.contains(profile2));
        assertFalse(selections.contains(profile1));
        assertTrue(selections.isEmpty());
    }
}
