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
import java.util.Collection;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import uk.gov.nationalarchives.droid.core.interfaces.NodeStatus;
import uk.gov.nationalarchives.droid.core.interfaces.ResourceType;
import uk.gov.nationalarchives.droid.gui.DroidUIContext;
import uk.gov.nationalarchives.droid.gui.ProfileForm;
import uk.gov.nationalarchives.droid.profile.AbstractProfileResource;
import uk.gov.nationalarchives.droid.profile.DirectoryProfileResource;
import uk.gov.nationalarchives.droid.profile.FileProfileResource;
import uk.gov.nationalarchives.droid.profile.NodeMetaData;
import uk.gov.nationalarchives.droid.profile.ProfileInstance;
import uk.gov.nationalarchives.droid.profile.ProfileManager;
import uk.gov.nationalarchives.droid.profile.ProfileResourceNode;
import uk.gov.nationalarchives.droid.profile.ProfileSpec;

/**
 * Action class for adding file s/folders to a profile spec.
 * @author rflitcroft
 *
 */
public class AddFilesAndFoldersAction {

    private DroidUIContext droidContext;
    private ProfileManager profileManager;

    /**
     * 
     * @param context the droid UI context
     * @param profileManager the profile manager API
     */
    public AddFilesAndFoldersAction(DroidUIContext context, ProfileManager profileManager) {
        this.droidContext = context;
        this.profileManager = profileManager;
    }

    /** 
     * Adds multiple files to the tree view and to the profile spec.
     * @param selectedFiles the files to add
     * @param recursive if any directories should be conisdered as recursive profile spec resources.
     */
    public void add(Collection<File> selectedFiles, boolean recursive) {
        
        ProfileForm selectedProfile = droidContext.getSelectedProfile();
        DefaultTreeModel treeModel = selectedProfile.getTreeModel();
        ProfileInstance profile = selectedProfile.getProfile();
        ProfileSpec profileSpec = profile.getProfileSpec();
        
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel.getRoot();
        for (File selectedFile : selectedFiles) {
            AbstractProfileResource newResource;
            
            // VERY basic shortcut detection:
            boolean isShortcut = !selectedFile.toURI().getPath().endsWith("/");
            if (selectedFile.isDirectory() && !isShortcut) {
                newResource = new DirectoryProfileResource(selectedFile, recursive);
            } else {
                newResource = new FileProfileResource(selectedFile);
            }

            if (profile.addResource(newResource)) {
                ProfileResourceNode primordialNode = new ProfileResourceNode(newResource.getUri());
                final NodeMetaData metaData = primordialNode.getMetaData();
                metaData.setName(newResource.getUri().getPath());
                metaData.setNodeStatus(NodeStatus.NOT_DONE);
                metaData.setResourceType(
                        newResource.isDirectory() ? ResourceType.FOLDER : ResourceType.FILE);

                DefaultMutableTreeNode node = new DefaultMutableTreeNode(primordialNode, false);
                int index = rootNode.getChildCount();
                treeModel.insertNodeInto(node, rootNode, index);
            }
        }
        profileManager.updateProfileSpec(profile.getUuid(), profileSpec);
//        if (profile.isDirty()) {
//            selectedProfile.onResourceChanged();
//        }
    }
}
