/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui.action;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.netbeans.swing.outline.Outline;

import uk.gov.nationalarchives.droid.gui.DroidUIContext;
import uk.gov.nationalarchives.droid.gui.ProfileForm;
import uk.gov.nationalarchives.droid.profile.ProfileInstance;
import uk.gov.nationalarchives.droid.profile.ProfileManager;
import uk.gov.nationalarchives.droid.profile.ProfileResourceNode;

/**
 * Action to remove top level resources.
 * @author rflitcroft
 *
 */
public class RemoveFilesAndFoldersAction {

    private DroidUIContext droidContext;
    private ProfileManager profileManager;

    /**
     * 
     * @param context the droid ui context
     * @param profileManager the profile manager
     */
    public RemoveFilesAndFoldersAction(DroidUIContext context,
            ProfileManager profileManager) {
        this.droidContext = context;
        this.profileManager = profileManager;
    }

    /**
     * Removes the selected items.
     */
    public void remove() {

        ProfileForm selectedProfile = droidContext.getSelectedProfile();

        DefaultTreeModel treeMdl = selectedProfile.getTreeModel();
        Outline outline = selectedProfile.getResultsOutline();
        ProfileInstance profile = selectedProfile.getProfile();

        int[] selectedRows = outline.getSelectedRows();

        for (int i = selectedRows.length; i > 0; i--) {
            // remove node from profile spec
            int index = selectedRows[i - 1];

            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) outline
                    .getValueAt(index, 0);
            ProfileResourceNode prn = (ProfileResourceNode) treeNode
                    .getUserObject();
            if (profile.removeResource(prn.getUri())) {
                treeMdl.removeNodeFromParent(treeNode);
            }
        }

        profileManager.updateProfileSpec(
                selectedProfile.getProfile().getUuid(), profile
                        .getProfileSpec());
//        if (profile.isDirty()) {
//            selectedProfile.onResourceChanged();
//        }

    }

}
