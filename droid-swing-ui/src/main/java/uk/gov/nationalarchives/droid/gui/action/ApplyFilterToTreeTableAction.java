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

import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.netbeans.swing.outline.Outline;

import uk.gov.nationalarchives.droid.gui.ProfileForm;
import uk.gov.nationalarchives.droid.profile.ProfileManager;
import uk.gov.nationalarchives.droid.profile.ProfileResourceNode;

/**
 * Worker to apply filter.
 * @author rflitcroft
 *
 */
public class ApplyFilterToTreeTableAction {

    //private DroidUIContext droidContext;
    private ProfileManager profileManager;
    private ProfileForm profileToFilter;

    /**
     * 
     * @param profile the profile form to filter.
     * @param profileManager the profile manager
     */
    public ApplyFilterToTreeTableAction(ProfileForm profile,
            ProfileManager profileManager) {
        this.profileToFilter = profile;
            //DroidUIContext context,
        //this.droidContext = context;
        this.profileManager = profileManager;
    }

    /**
     * Applies the filter.
     */
    public void applyFilter() {

        //ProfileForm selectedProfile = droidContext.getSelectedProfile();
        //DefaultTreeModel treeMdl = (DefaultTreeModel) selectedProfile
        DefaultTreeModel treeMdl = (DefaultTreeModel) profileToFilter
                .getTreeModel();

        Outline outline = profileToFilter.getResultsOutline();
        for (int i = outline.getRowCount(); i > 0; i--) {
            // remove node from profile spec
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) outline.getValueAt(i - 1, 0);
            if (!node.isRoot()) {
                treeMdl.removeNodeFromParent(node);
            }
        }
        treeMdl.reload();
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeMdl.getRoot();

        //String profileId = droidContext.getSelectedProfile().getProfile().getUuid();
        String profileId = profileToFilter.getProfile().getUuid();

        List<ProfileResourceNode> childNodes = profileManager.findRootNodes(profileId);

        for (ProfileResourceNode profileNode : childNodes) {
            DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(profileNode);
            treeNode.setAllowsChildren(profileNode.allowsChildren());
            rootNode.add(treeNode);
        }
        treeMdl.reload();

    }

}
