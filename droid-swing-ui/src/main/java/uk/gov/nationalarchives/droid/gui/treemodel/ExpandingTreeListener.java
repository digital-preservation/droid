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
package uk.gov.nationalarchives.droid.gui.treemodel;

import java.awt.Cursor;
import java.util.Enumeration;
import java.util.List;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;

import uk.gov.nationalarchives.droid.gui.ProfileForm;
import uk.gov.nationalarchives.droid.profile.ProfileManager;
import uk.gov.nationalarchives.droid.profile.ProfileResourceNode;

/**
 * @author rflitcroft
 *
 */
public class ExpandingTreeListener implements TreeWillExpandListener {

    private ProfileManager profileManager;
    private ProfileForm profileForm;

    /**
     * @param profileManager a profile manager. 
     * @param profileForm the parent profile form 
     */
    public ExpandingTreeListener(ProfileManager profileManager, ProfileForm profileForm) {
        this.profileManager = profileManager;
        this.profileForm = profileForm;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void treeWillExpand(TreeExpansionEvent event) {
        try {
            profileForm.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            DefaultMutableTreeNode expandingNode = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
            ProfileResourceNode prn = (ProfileResourceNode) expandingNode.getUserObject();
            profileForm.getInMemoryNodes().put(prn.getId(), expandingNode);
            expandingNode.removeAllChildren();
            
            final List<ProfileResourceNode> childNodes = 
                profileManager.findProfileResourceNodeAndImmediateChildren(
                        profileForm.getProfile().getUuid(), prn.getId());
            if (!childNodes.isEmpty()) {
                expandingNode.setAllowsChildren(true);
                for (ProfileResourceNode node : childNodes) {
                    DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(node, node.allowsChildren());
                    expandingNode.add(newNode);
                    profileForm.getInMemoryNodes().put(node.getId(), newNode);
                }
            }
            
            if (expandingNode.getChildCount() == 0) {
                expandingNode.setAllowsChildren(false);
            }
            
            profileForm.getTreeModel().nodeStructureChanged(expandingNode);
        } finally {
            profileForm.setCursor(Cursor.getDefaultCursor());
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public void treeWillCollapse(TreeExpansionEvent event) {
        DefaultMutableTreeNode collapsingNode = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
        ProfileResourceNode prn = (ProfileResourceNode) collapsingNode.getUserObject();
        profileForm.getInMemoryNodes().remove(prn.getId());
        
        for (Enumeration<DefaultMutableTreeNode> e = collapsingNode.children(); e.hasMoreElements();) {
            DefaultMutableTreeNode nodeToRemove = e.nextElement();
            final ProfileResourceNode node = (ProfileResourceNode) nodeToRemove.getUserObject();
            profileForm.getInMemoryNodes().remove(node.getId());
        }
        collapsingNode.removeAllChildren();
        
        profileForm.getTreeModel().nodeStructureChanged(collapsingNode);
    }
    
}
