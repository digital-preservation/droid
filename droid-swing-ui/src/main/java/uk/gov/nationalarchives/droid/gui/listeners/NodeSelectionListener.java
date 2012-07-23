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
package uk.gov.nationalarchives.droid.gui.listeners;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.netbeans.swing.outline.Outline;

import uk.gov.nationalarchives.droid.gui.DroidUIContext;
import uk.gov.nationalarchives.droid.profile.ProfileManager;

/**
 * Listener for node selections.
 * @author rflitcroft
 */
public class NodeSelectionListener implements ListSelectionListener, KeyListener {

    private static final int F5_KEY_CODE = 116;

    private DroidUIContext droidContext;
    private ProfileManager profileManager;

    /**
     * 
     * @param context the UI context
     * @param profileManager a profile manager
     */
    public NodeSelectionListener(DroidUIContext context, ProfileManager profileManager) {
        this.droidContext = context;
        this.profileManager = profileManager;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == F5_KEY_CODE) {
            Outline outline = droidContext.getSelectedProfile().getResultsOutline();
            // if any row is selected and hte node is not the root node.
            if (outline.getSelectedRow() != -1 && !getSelectedNode().isRoot()) {
                populateNode();
            }
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent event) {
        if (!event.getValueIsAdjusting()) {
            Outline outline = droidContext.getSelectedProfile().getResultsOutline();
            ListSelectionModel m = outline.getSelectionModel();
            if (outline.getSelectedRow() != -1 && m.getMinSelectionIndex() == m.getMaxSelectionIndex()) {
                if (!getSelectedNode().isRoot() && getSelectedNode().getDepth() == 0) {
                    populateNode();
                }
            }
        }
    }

    private void populateNode() {
//        // if any row is selected and multiple row is
//        // not selected.
//        DefaultMutableTreeNode node = getSelectedNode();
//        ProfileNode profileNode = (ProfileNode) node.getUserObject();
//        
//        String profileId = droidContext.getSelectedProfile().getProfile().getUuid();
//        
//        // get the filter string
//        Filter filter = droidContext.getSelectedProfile().getProfile().getFilter();
//        
//        ProfileResourceNode profileResourceNode = 
//          profileManager.findProfileResourceNodeAndImmediateChildrenWithFilter(profileId, 
//              profileNode.getUri(),filter);
//        if (profileResourceNode != null) {
//            for (ProfileResourceNode profileResourceChildNode : profileResourceNode.getChildren()) {
//                String identification = "It's a Folder";
//                if (profileResourceChildNode.getJob() != null) {
//                    List<FormatIdentification> formatIdentification = profileResourceChildNode
//                            .getJob().getFormatIdentifications();
//                    if (!formatIdentification.isEmpty()) {
//                        identification = formatIdentification.get(0).getFormat().getName();
//                    } else {
//                        String extension = FilenameUtils.getExtension(profileResourceChildNode.getUri().toString());
//                        identification = "Can not identify " + extension  + " file.";
//                    }
//                }
//    
//                // now create the nodes.
//                TreeModel treeMdl = droidContext.getSelectedProfile().getTreeModel();
//                URI uri = profileResourceChildNode.getUri();
//                
//                final NodeMetaData metaData = profileResourceChildNode.getMetaData();
//                
//                DefaultMutableTreeNode node1;
//                if (profileResourceChildNode.isContainer()) {
//                    node1 = new DefaultMutableTreeNode(new ProfileNode(
//                      metaData.getName(),    uri,   0, "", true, null, ""));
//                } else {
//                    node1 = new DefaultMutableTreeNode(new ProfileNode( 
//                      metaData.getName(), uri, metaData.getSize(), FilenameUtils.getExtension(metaData.getName()), 
//                                    false,  metaData.getLastModifiedDate(), identification));
//                }
//                node1.setAllowsChildren(profileResourceChildNode.isContainer());
//                node.setAllowsChildren(true);
//                node.add(node1);
//                ((DefaultTreeModel) treeMdl).reload(node);
//            }
//        }
    }

    private DefaultMutableTreeNode getSelectedNode() {
        Outline outline = droidContext.getSelectedProfile().getResultsOutline();
        return (DefaultMutableTreeNode) outline.getValueAt(outline.getSelectedRow(), 0);
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
        // Do nothing
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Do nothing
    }

}
