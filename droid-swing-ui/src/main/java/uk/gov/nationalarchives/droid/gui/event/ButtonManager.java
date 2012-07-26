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
package uk.gov.nationalarchives.droid.gui.event;

import java.awt.EventQueue;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;

import uk.gov.nationalarchives.droid.gui.DroidUIContext;
import uk.gov.nationalarchives.droid.gui.ProfileForm;
import uk.gov.nationalarchives.droid.profile.FilterImpl;
import uk.gov.nationalarchives.droid.profile.ProfileEventListener;
import uk.gov.nationalarchives.droid.profile.ProfileInstance;
import uk.gov.nationalarchives.droid.profile.ProfileState;

/**
 * Event listener for profile events which manages UI controls. This event
 * listener MUST be called from the Event Dispatch Thread.
 * 
 * @author rflitcroft
 * 
 */
public class ButtonManager implements ProfileEventListener {

    private Set<JComponent> resourceEditing = new HashSet<JComponent>();
    private Set<JComponent> runAction = new HashSet<JComponent>();
    private Set<JComponent> stopAction = new HashSet<JComponent>();
    private Set<JComponent> saveAction = new HashSet<JComponent>();
    private Set<JComponent> loadAction = new HashSet<JComponent>();
    private Set<JComponent> createAction = new HashSet<JComponent>();
    private Set<JComponent> saveAsAction = new HashSet<JComponent>();
    private Set<JComponent> closeAction = new HashSet<JComponent>();
    private Set<JComponent> exportAction = new HashSet<JComponent>();
    private Set<JComponent> filterAction = new HashSet<JComponent>();
    private Set<JComponent> filterEnabledAction = new HashSet<JComponent>();
    private Set<JComponent> reportAction = new HashSet<JComponent>();

    private Map<ProfileState, ProfileStateMachine> states = new HashMap<ProfileState, ProfileStateMachine>();

    private DroidUIContext context;

    /**
     * @param context
     *            the droid UI context
     */
    public ButtonManager(DroidUIContext context) {
        this.context = context;
        states.put(ProfileState.VIRGIN, new VirginState());
        states.put(ProfileState.STOPPED, new StoppedState());
        states.put(ProfileState.RUNNING, new RunningState());
        states.put(ProfileState.INITIALISING, new InitialisingState());
        states.put(ProfileState.LOADING, new LoadingState());
        states.put(ProfileState.SAVING, new SavingState());
        states.put(ProfileState.FINISHED, new FinishedState());
    }

    /**
     * Fires an event to update the buttons.
     * 
     * @param profile
     *            the active profile form.
     */
    @Override
    public void fireEvent(final ProfileInstance profile) {
        final ProfileForm activeProfile = context.getSelectedProfile();
        if (activeProfile == null || activeProfile.getProfile() == null) {
            setEnabled(saveAsAction, false); // save as - N
            setEnabled(saveAction, false); // save - N
            setEnabled(loadAction, true); // load - Y
            setEnabled(createAction, true); // new - Y
            setEnabled(runAction, false); // run - N
            setEnabled(stopAction, false); // stop - N
            setEnabled(resourceEditing, false); // add resource - N
            setEnabled(closeAction, false); // close - N
            setEnabled(exportAction, false); // export - N
            setEnabled(filterAction, false); // filter - N
            setEnabled(filterEnabledAction, false); // filter enabled - N
            setEnabled(reportAction, false); // report - N
        } else if (activeProfile.getProfile().equals(profile)) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    ProfileStateMachine state = states.get(profile.getState());
                    if (state != null) {
                        state.onEnter(activeProfile);
                    }
                }
            });
        }
    }

    private void setEnabled(Set<JComponent> components, boolean enabled) {
        for (JComponent component : components) {
            component.setEnabled(enabled);
        }
    }

    /**
     * @param c
     *            a component to do save as actions.
     */
    public void addSaveAsComponent(JComponent c) {
        saveAsAction.add(c);
    }

    /**
     * @param c
     *            a component to do save actions.
     */
    public void addSaveComponent(JComponent c) {
        saveAction.add(c);
    }

    /**
     * @param c
     *            a component to do load actions.
     */
    public void addLoadComponent(JComponent c) {
        loadAction.add(c);
    }

    /**
     * @param c
     *            a component to do create actions.
     */
    public void addCreateComponent(JComponent c) {
        createAction.add(c);
    }

    /**
     * @param c
     *            a component to do run actions.
     */
    public void addRunComponent(JComponent c) {
        runAction.add(c);
    }

    /**
     * @param c
     *            a component to do stop actions.
     */
    public void addStopComponent(JComponent c) {
        stopAction.add(c);
    }

    /**
     * @param c
     *            a component to do resource editing actions.
     */
    public void addResourceComponent(JComponent c) {
        resourceEditing.add(c);
    }

    /**
     * @param c
     *            a component to do close profile actions.
     */
    public void addCloseComponent(JComponent c) {
        closeAction.add(c);
    }

    /**
     * @param c
     *            the component to add to the export action widgets
     */
    public void addExportComponent(JComponent c) {
        exportAction.add(c);
    }

    /**
     * @param c
     *            the component to add to the filter action widgets
     */
    public void addFilterComponent(JComponent c) {
        filterAction.add(c);
    }

    
    /**
     * @param c
     *            the component to add to the filter action widgets
     */
    public void addFilterEnabledComponent(JComponent c) {
        filterEnabledAction.add(c);
    }

    /**
     * @param c
     *            the component to add to the report action widgets
     */
    public void addReportComponent(JComponent c) {
        reportAction.add(c);
    }

    private interface ProfileStateMachine {
        void onEnter(ProfileForm profileForm);
    }

    private final class StoppedState extends AbstractState {

        @Override
        public void onEnter(ProfileForm profileForm) {
            setEnabled(saveAsAction, true); // save as - Y
            setEnabled(saveAction, profileForm.getProfile().isDirty()); // save
                                                                        // dirty
            setEnabled(loadAction, true); // load - Y
            setEnabled(createAction, true); // new - Y
            setEnabled(runAction, hasResources(profileForm)); // run - only if
                                                              // profile spec
                                                              // has new
                                                              // resources
            setEnabled(stopAction, false); // stop - N
            setEnabled(resourceEditing, false); // add resource - Y
            setEnabled(closeAction, true); // close - N
            profileForm.getProfileTab().setButtonVisible(true);
            // export - Y if all profiles are exportable
            //setEnabled(exportAction, allProfilesExportable(context.allProfiles()));
            setEnabled(exportAction, atLeastOneProfileExportable(context.allProfiles()));
            setEnabled(filterAction, isFilterable(profileForm)); // filter
            setEnabled(filterEnabledAction, isFilterEnabled(profileForm)); // filter enabled
            setEnabled(reportAction, atLeastOneProfileReportable(context.allProfiles())); // report - N
            profileForm.getProgressPanel().setVisible(true);
            
        }
    }

    
    private final class VirginState extends AbstractState {

        @Override
        public void onEnter(ProfileForm profileForm) {
            setEnabled(saveAsAction, true); // save as - Y
            setEnabled(saveAction, profileForm.getProfile().isDirty()); // save
                                                                        // dirty
            setEnabled(loadAction, true); // load - Y
            setEnabled(createAction, true); // new - Y
            setEnabled(runAction, hasResources(profileForm)); // run - only if
                                                              // profile spec
                                                              // has new
                                                              // resources
            setEnabled(stopAction, false); // stop - N
            setEnabled(resourceEditing, true); // add resource - Y
            setEnabled(closeAction, true); // close - N
            profileForm.getProfileTab().setButtonVisible(true);
            // export - Y if all profiles are exportable
            //setEnabled(exportAction, allProfilesExportable(context.allProfiles()));
            setEnabled(exportAction, atLeastOneProfileExportable(context.allProfiles()));
            setEnabled(filterAction, isFilterable(profileForm)); // filter
            setEnabled(filterEnabledAction, false); // filter enabled
            setEnabled(reportAction, atLeastOneProfileReportable(context.allProfiles())); // report
                                                                                    // -
                                                                                    // N
            profileForm.getProgressPanel().setVisible(true);
        }
    }

    
    
    private final class FinishedState extends AbstractState {
        @Override
        public void onEnter(ProfileForm profileForm) {
            setEnabled(saveAsAction, true); // save as - Y
            setEnabled(saveAction, profileForm.getProfile().isDirty()); // save
                                                                        // -
                                                                        // depends
                                                                        // on
                                                                        // dirty
            setEnabled(loadAction, true); // load - Y
            setEnabled(createAction, true); // new - Y
            setEnabled(runAction, false); // run - only if profile spec has new
                                          // resources
            setEnabled(stopAction, false); // stop - N
            setEnabled(resourceEditing, false); // add resource - Y
            setEnabled(closeAction, true); // close - N
            profileForm.getProfileTab().setButtonVisible(true);
            // export - Y if all profiles are exportable
            //setEnabled(exportAction, allProfilesExportable(context.allProfiles()));
            setEnabled(exportAction, atLeastOneProfileExportable(context.allProfiles()));
            setEnabled(filterAction, isFilterable(profileForm)); // filter
            setEnabled(filterEnabledAction, isFilterEnabled(profileForm)); // filter enabled
            setEnabled(reportAction, atLeastOneProfileReportable(context.allProfiles())); // report
            profileForm.getProgressPanel().setVisible(false);
            profileForm.getThrottlePanel().setVisible(false);
        }
    }

    private final class RunningState extends AbstractState {
        @Override
        public void onEnter(ProfileForm profileForm) {
            setEnabled(saveAsAction, false); // save as - N
            setEnabled(saveAction, false); // save - depends on dirty
            setEnabled(loadAction, true); // load - Y
            setEnabled(createAction, true); // new - Y
            setEnabled(runAction, false); // run - N
            setEnabled(stopAction, true); // stop - Y
            setEnabled(resourceEditing, false); // add resource - N
            setEnabled(closeAction, false); // close - N
            profileForm.getProfileTab().setButtonVisible(false);
            setEnabled(exportAction, false); // export - N
            setEnabled(filterAction, false); // filter - N
            setEnabled(filterEnabledAction, false); // filter enabled
            setEnabled(reportAction, false); // report - N
            profileForm.getProgressPanel().setVisible(true);
        }
    }

    private final class InitialisingState extends AbstractState {
        @Override
        public void onEnter(ProfileForm profileForm) {
            setEnabled(saveAsAction, false); // save as - N
            setEnabled(saveAction, false); // save - N
            setEnabled(loadAction, true); // load - Y
            setEnabled(createAction, true); // new - N
            setEnabled(runAction, false); // run - N
            setEnabled(stopAction, false); // stop - N
            setEnabled(resourceEditing, true); // add resource - Y
            profileForm.getProfileTab().setButtonVisible(false);
            setEnabled(closeAction, false); // close - N
            setEnabled(exportAction, false); // export - N
            setEnabled(filterAction, false); // filter - N
            setEnabled(filterEnabledAction, false); // filter enabled
            setEnabled(reportAction, false); // report - N
            profileForm.getProgressPanel().setVisible(false);
            profileForm.getThrottlePanel().setVisible(false);
        }
    }

    private final class SavingState extends AbstractState {
        @Override
        public void onEnter(ProfileForm profileForm) {
            setEnabled(saveAsAction, false); // save as - N
            setEnabled(saveAction, false); // save - N
            setEnabled(loadAction, true); // load - Y
            setEnabled(createAction, true); // new - Y
            setEnabled(runAction, false); // run - N
            setEnabled(stopAction, false); // stop - N
            setEnabled(resourceEditing, false); // add resource - N
            profileForm.getProfileTab().setButtonVisible(false);
            setEnabled(closeAction, false); // close - N
            setEnabled(exportAction, false); // export - N
            setEnabled(filterAction, false); // filter - N
            setEnabled(filterEnabledAction, false); // filter enabled - N
            setEnabled(reportAction, false); // report - N
        }
    }

    private final class LoadingState extends AbstractState {
        @Override
        public void onEnter(ProfileForm profileForm) {
            setEnabled(saveAsAction, false); // save as - N
            setEnabled(saveAction, false); // save - N
            setEnabled(loadAction, true); // load - Y
            setEnabled(createAction, true); // new - N
            setEnabled(runAction, false); // run - N
            setEnabled(stopAction, false); // stop - Y
            setEnabled(resourceEditing, false); // add resource - N
            profileForm.getProfileTab().setButtonVisible(false);
            setEnabled(closeAction, false); // close - N
            setEnabled(exportAction, false); // export - N
            setEnabled(filterAction, false); // filter - N
            setEnabled(filterEnabledAction, false); // filter enabled - N
            setEnabled(reportAction, false); // report - N
            profileForm.getProgressPanel().setVisible(false);
        }
    }

    private abstract static class AbstractState implements ProfileStateMachine {
    }

    private static boolean allProfilesExportable(Collection<ProfileForm> profiles) {
        for (ProfileForm profile : profiles) {
            final ProfileState state = profile.getProfile().getState();
            if (!(state.equals(ProfileState.STOPPED) || state.equals(ProfileState.FINISHED))) {
                return false;
            }
        }
        return true;
    }
    
    private static boolean atLeastOneProfileExportable(Collection<ProfileForm> profiles) {
        boolean isExportable = false;
        for (ProfileForm profile : profiles) {
            isExportable = profile.getProfile().getState().isReportable();
            if (isExportable) { break; }
        }
        return isExportable;
    }

    private static boolean atLeastOneProfileReportable(Collection<ProfileForm> profiles) {
        boolean isReportable = false;
        for (ProfileForm profile : profiles) {
            isReportable = profile.getProfile().getState().isReportable();
            if (isReportable) { break; }
        }
        return isReportable;
    }
    
    private static boolean isFilterable(ProfileForm profile) {
        final ProfileState state = profile.getProfile().getState();
        if (state.equals(ProfileState.STOPPED) || state.equals(ProfileState.FINISHED)) {
            return true;
        }
        return false;

    }
    
    private static boolean isFilterEnabled(ProfileForm profile) {
        FilterImpl filter = profile.getProfile().getFilter();
        return filter != null && filter.hasCriteria();
    }

    /**
     * @param profileForm
     *            the profile form
     * @return true if the profile form has resources, false otherwise.
     */
    public static boolean hasResources(ProfileForm profileForm) {
        return !profileForm.getProfile().getProfileSpec().getResources().isEmpty();
    }

}
