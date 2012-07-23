/**
 * Copyright (c) 2012, The National Archives <pronom@nationalarchives.gsi.gov.uk>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following
 * conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of the The National Archives nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package uk.gov.nationalarchives.droid.gui.action;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileInfo;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureType;
import uk.gov.nationalarchives.droid.gui.DroidUIContext;
import uk.gov.nationalarchives.droid.gui.ExitListener;
import uk.gov.nationalarchives.droid.gui.ProfileForm;
import uk.gov.nationalarchives.droid.profile.ProfileInstance;
import uk.gov.nationalarchives.droid.profile.ProfileManager;
import uk.gov.nationalarchives.droid.profile.ProfileManagerException;
import uk.gov.nationalarchives.droid.profile.ProfileState;

/**
 * Worker to create a new profile.
 * @author rflitcroft
 *
 */
public class NewProfileAction extends SwingWorker<Void, Void> implements ExitListener {

    /** Counter of the new profiles created. */
    private static int count = 1;

    private final Log log = LogFactory.getLog(getClass());

    private DroidUIContext droidContext;
    private ProfileManager profileManager;
    private JTabbedPane tabbedPane;
    private String profileName;
    private ProfileInstance profile;
    private ProfileForm profileForm;
    private Map<SignatureType, SignatureFileInfo> signatures;
    
    /**
     * 
     * @param context the droid ui context
     * @param profileManager the profile manager
     * @param tabbedPane the tabbed pane which has the profile forms
     */
    public NewProfileAction(DroidUIContext context,
            ProfileManager profileManager, JTabbedPane tabbedPane) {
        this.droidContext = context;
        this.profileManager = profileManager;
        this.tabbedPane = tabbedPane;
    }
    
    /**
     * Initialises this worker with the profile.
     * @param parent the profile which this worker should work with
     * @throws ProfileManagerException if the profile could not be created
     */
    public void init(ProfileForm parent) throws ProfileManagerException {
        this.profileForm = parent;
        profile = profileManager.createProfile(signatures);
        profileForm.setProfile(profile);
        profileName = "Untitled-" + count++;
        profileForm.setName(profileName);
        profile.setName(profileName);
        
        tabbedPane.add(profileForm);
        tabbedPane.setSelectedComponent(profileForm);
        tabbedPane.setTabComponentAt(tabbedPane.indexOfComponent(profileForm), profileForm.getProfileTab());
        
        JProgressBar progressBar = profileForm.getProfileProgressBar();
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        
        profileForm.getStatusLabel().setText("Initialising profile...");
        profileForm.getStatusProgressBar().setIndeterminate(true);
        profileForm.getStatusProgressBar().setVisible(true);
        
        progressBar.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("progress".equals(evt.getPropertyName())) {
                    ((JProgressBar) evt.getSource()).setValue((Integer) evt
                            .getNewValue());
                }
            }
        });

        droidContext.addProfileForm(profile.getUuid(), profileForm);
    }
    
    @Override
    protected void done() {
        try {
            if (!isCancelled()) {
                get();
            }
        } catch (InterruptedException e) {
            log.debug(e);
            throw new RuntimeException(e.getMessage(), e);
        } catch (ExecutionException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            profile.changeState(ProfileState.VIRGIN);
            profileForm.afterCreate();
            profileForm.getStatusLabel().setVisible(false);
            profileForm.getStatusProgressBar().setIndeterminate(false);
            profileForm.getStatusProgressBar().setVisible(false);
        }
    }
    
    @Override
    protected Void doInBackground() {
        
        profileManager.openProfile(profile.getUuid());
        return null;

    }
    
    /**
     * @param signatures the signatures to set
     */
    public void setSignatures(Map<SignatureType, SignatureFileInfo> signatures) {
        this.signatures = signatures;
    }
    
    /**
     * @see uk.gov.nationalarchives.droid.gui.ExitListener#onExit()
     */
    @Override
    public void onExit() {
        cancel(true);
    }

}
