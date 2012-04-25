/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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
