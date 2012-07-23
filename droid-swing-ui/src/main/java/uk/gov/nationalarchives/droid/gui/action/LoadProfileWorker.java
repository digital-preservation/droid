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
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.gov.nationalarchives.droid.core.interfaces.NodeStatus;
import uk.gov.nationalarchives.droid.gui.DroidUIContext;
import uk.gov.nationalarchives.droid.gui.ProfileForm;
import uk.gov.nationalarchives.droid.profile.ProfileInstance;
import uk.gov.nationalarchives.droid.profile.ProfileManager;
import uk.gov.nationalarchives.droid.profile.ProfileResourceNode;
import uk.gov.nationalarchives.droid.profile.ProgressState;
import uk.gov.nationalarchives.droid.results.handlers.ProgressObserver;

/**
 *
 * @author rflitcroft
 */
public class LoadProfileWorker extends SwingWorker<ProfileInstance, Void> {

    private static final int UNITY_PERCENT = 100;

    private final Log log = LogFactory.getLog(getClass());

    private ProfileManager profileManager;
    private DroidUIContext context;
    private JTabbedPane tabbedPane;
    private ProgressObserver observer;
    private File profileFile;
    private ProfileForm profilePanel;

    /**
     * 
     * @param profileManager the profile manager
     * @param context the droid UI context
     * @param tabbedPane the tabbed pane for profiles
     */
    public LoadProfileWorker(ProfileManager profileManager, DroidUIContext context, JTabbedPane tabbedPane) {
        this.profileManager = profileManager;
        this.context = context;
        this.tabbedPane = tabbedPane;

    }
    
    /**
     * Initialises the worler with a profile.
     * @param parent the parent profile for this worker
     */
    public void init(ProfileForm parent) {
        
        this.profilePanel = parent;
        
        profilePanel.setName(FilenameUtils.getBaseName(profileFile.getName()));

        final JProgressBar statusProgressBar = profilePanel.getStatusProgressBar();
        final JLabel statusLabel = profilePanel.getStatusLabel();

        statusProgressBar.setVisible(true);
        statusProgressBar.setIndeterminate(false);
        statusProgressBar.setIndeterminate(false);
        statusLabel.setText("Loading profile...");

        observer = new ProgressObserver() {
            @Override
            public void onProgress(Integer progress) {
                setProgress(progress);
            }
        };

        addPropertyChangeListener(new PropertyChangeListener() {
            public  void propertyChange(PropertyChangeEvent evt) {
                if ("progress".equals(evt.getPropertyName())) {
                    statusProgressBar.setValue((Integer) evt.getNewValue());
                    if (getProgress() == UNITY_PERCENT) {
                        statusProgressBar.setIndeterminate(true);
                        statusLabel.setText("Initialising profile...");
                    }
                }
            }
        });

        // pre-initailse the tabbed pane
        tabbedPane.add(profilePanel);
        tabbedPane.setSelectedComponent(profilePanel);
        tabbedPane.setTabComponentAt(tabbedPane.indexOfComponent(profilePanel), profilePanel.getProfileTab());

        JProgressBar progressBar = profilePanel.getProfileProgressBar();
        progressBar.setValue(0);
        profilePanel.getProgressPanel().setVisible(false);
    }

    @Override
    protected void done() {
        try {
            ProfileInstance profile = get();
            
            // initialise the progress
            ProgressState prog = profile.getProgress();
            int progress;
            if (prog == null) {
                progress = 0;
            } else {
                progress = (int) (UNITY_PERCENT * prog.getCount() / prog.getTarget());
            }
            profilePanel.getProfileProgressBar().setValue(progress);

            profilePanel.setProfile(profile);
            profilePanel.getStatusLabel().setText("Profile Loaded OK.");

            profilePanel.setName(profile.getName());
            context.addProfileForm(profile.getUuid(), profilePanel);

            // populate the outline with the first-level nodes
            DefaultTreeModel treeModel = profilePanel.getTreeModel();
            DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel.getRoot();
            
            for (ProfileResourceNode profileNode : profileManager.findRootNodes(profile.getUuid())) {
                boolean allowsChildren = profileNode.allowsChildren() 
                    && !NodeStatus.NOT_DONE.equals(profileNode.getMetaData().getNodeStatus());
                DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(profileNode, allowsChildren);
                rootNode.add(treeNode);
            }

            treeModel.reload();
            profilePanel.afterLoad();
        } catch (InterruptedException e) {
            log.debug(e);
            throw new RuntimeException(e.getMessage(), e);
        } catch (ExecutionException e) {
            log.error(e.getCause(), e);
            JOptionPane.showMessageDialog(profilePanel, String.format("Error opening profile %s", profileFile),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            tabbedPane.remove(profilePanel);
        }
    }

    @Override
    protected ProfileInstance doInBackground() throws IOException {
        return profileManager.open(profileFile, observer);
    }

    /**
     * Sets the file that was the source of this profile.
     * @param profileFile the source of the profile
     */
    public void setProfileFile(File profileFile) {
        this.profileFile = profileFile;
    }

}
