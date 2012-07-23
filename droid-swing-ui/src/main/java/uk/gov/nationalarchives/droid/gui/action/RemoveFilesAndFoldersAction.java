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
