/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui.action;

import java.io.File;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import uk.gov.nationalarchives.droid.gui.DroidUIContext;
import uk.gov.nationalarchives.droid.gui.ProfileForm;
import uk.gov.nationalarchives.droid.gui.widgetwrapper.FileChooserProxy;
import uk.gov.nationalarchives.droid.gui.widgetwrapper.JOptionPaneProxy;
import uk.gov.nationalarchives.droid.profile.ProfileInstance;
import uk.gov.nationalarchives.droid.profile.ProfileManager;
import uk.gov.nationalarchives.droid.results.handlers.ProgressObserver;

public class CloseProfileActionTest {

    private CloseProfileAction action;
    
    private ProfileManager profileManager;
    private DroidUIContext context;
    private JOptionPaneProxy dialog;
    private ProfileInstance profile;
    private ProfileForm profileForm;
    private SaveProfileWorker saveAction;
    
    @Before
    public void setup() {
        profileManager = mock(ProfileManager.class);
        context = mock(DroidUIContext.class);
        profileForm = mock(ProfileForm.class);
        profile = mock(ProfileInstance.class);
        
        FileChooserProxy fileChooser = mock(FileChooserProxy.class);
        dialog = mock(JOptionPaneProxy.class);
        
        when(profileForm.getProfile()).thenReturn(profile);
        when(profile.isDirty()).thenReturn(true);
        when(profile.getUuid()).thenReturn("myProfileId");
        
        when(profileForm.getProfile()).thenReturn(profile);
        
        saveAction = new SaveProfileWorker(profileManager, profileForm, fileChooser);
        action = new CloseProfileAction(profileManager, context, profileForm);
        action.setSaveAction(saveAction);
    }
    
    @Test
    public void testExecuteWhenUserSelectsDiscard() {
        when(dialog.getResponse()).thenReturn(JOptionPaneProxy.NO);
        action.setUserOptionDialog(dialog);
        
        action.start();
        
        verify(profileManager).closeProfile("myProfileId");
        verify(context).remove("myProfileId");
        
    }

    @Test
    public void testExecuteWhenUserSelectsSaveAndProfileHasFileLocationAndIsDirty() throws Exception {
        when(dialog.getResponse()).thenReturn(JOptionPaneProxy.YES);
        when(profileManager.save(eq("myProfileId"), any(File.class), any(ProgressObserver.class))).thenReturn(profile);
        
        when(profile.getLoadedFrom()).thenReturn(new File("profile.droid"));
        when(profile.isDirty()).thenReturn(true);
        
        action.setUserOptionDialog(dialog);
        
        action.start();
        saveAction.get();
        
        
        Thread.sleep(200); // wait for listeners to fire.
        
        verify(profileManager).closeProfile("myProfileId");
        verify(context).remove("myProfileId");
        
    }

    @Test
    public void testExecuteWhenUserSelectsCancel() {
        when(dialog.getResponse()).thenReturn(JOptionPaneProxy.CANCEL);
        action.setUserOptionDialog(dialog);
        
        action.start();
        
        verify(profileManager, never()).closeProfile("myProfileId");
        verify(context, never()).remove("myProfileId");
        
    }
}
