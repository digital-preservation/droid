/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui;

import java.awt.EventQueue;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JSlider;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;

import uk.gov.nationalarchives.droid.profile.ProfileEventListener;
import uk.gov.nationalarchives.droid.profile.ProfileInstance;
import uk.gov.nationalarchives.droid.profile.ProfileManager;

/**
 * @author rflitcroft
 *
 */
public class ProfileFormTest {

    @Test
    public void testMovingThrottleSliderUpdatesThrottle() throws Exception {
        
        final DroidUIContext context = mock(DroidUIContext.class);
        final ProfileManager profileManager = mock(ProfileManager.class);
        final DroidMainFrame mainFrame = mock(DroidMainFrame.class);
        final ProfileInstance profile = mock(ProfileInstance.class);
        
        final String profileId = "abcd";
        when(profile.getUuid()).thenReturn(profileId);
        
        final ProfileEventListener listener = mock(ProfileEventListener.class);
        
        when(context.getProfileManager()).thenReturn(profileManager);
        
        EventQueue.invokeAndWait(new Runnable() {

            public void run() {
                
                ProfileForm profileForm = new ProfileForm(mainFrame, context, listener);
                profileForm.setProfile(profile);
                
                JSlider slider = profileForm.getThrottleSlider();
                slider.setValue(123);
            }
        });
        
        verify(profileManager).setThrottleValue(profileId, 123);
    }
    
    @Test
    public void testMovingThrottleSliderUpdatesLabel() throws Exception {
        
        final String profileId = "abcd";
        
        final DroidUIContext context = mock(DroidUIContext.class);
        ProfileManager profileManager = mock(ProfileManager.class);
        final DroidMainFrame mainFrame = mock(DroidMainFrame.class);
        final ProfileInstance profile = mock(ProfileInstance.class);
        
        when(profile.getUuid()).thenReturn(profileId);
        
        final ProfileEventListener listener = mock(ProfileEventListener.class);
        
        when(context.getProfileManager()).thenReturn(profileManager);
        
        final AtomicReference<String> label = new AtomicReference<String>();
        EventQueue.invokeAndWait(new Runnable() {
            
            public void run() {
                
                ProfileForm profileForm = new ProfileForm(mainFrame, context, listener);
                profileForm.setProfile(profile);
                
                JSlider slider = profileForm.getThrottleSlider();
                slider.setValue(123);
                label.set(profileForm.getThrottleLabel().getText());
            }
        });

        assertEquals("Throttle: 123 ms", label.get());
    }
}
