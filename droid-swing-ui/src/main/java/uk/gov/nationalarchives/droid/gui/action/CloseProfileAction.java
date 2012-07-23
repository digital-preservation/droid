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
