/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui;

import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Ignore;
import org.junit.Test;

import uk.gov.nationalarchives.droid.gui.action.LoadProfileWorker;
import uk.gov.nationalarchives.droid.profile.ProfileManager;

/**
 *
 * @author rflitcroft
 */
public class LoadProfileWorkerTest {

    @Test
    @Ignore
    public void testInit() {

        ProfileManager profileManager = mock(ProfileManager.class);
        DroidUIContext context = mock(DroidUIContext.class);
        JTabbedPane tabbedPane = mock(JTabbedPane.class);
        ProfileForm profileForm = mock(ProfileForm.class);

        JProgressBar progressBar = new JProgressBar();
        JProgressBar profileProgressBar = new JProgressBar();
        JLabel label = new JLabel();

        when(profileForm.getStatusProgressBar()).thenReturn(progressBar);
        when(profileForm.getStatusLabel()).thenReturn(label);
        when(profileForm.getProfileProgressBar()).thenReturn(profileProgressBar);

        LoadProfileWorker worker = new LoadProfileWorker(profileManager, context, tabbedPane);
        worker.init(profileForm);

        verify(profileForm).setName("Loading...");

        assertEquals(0.0D, progressBar.getPercentComplete());

    }
}
