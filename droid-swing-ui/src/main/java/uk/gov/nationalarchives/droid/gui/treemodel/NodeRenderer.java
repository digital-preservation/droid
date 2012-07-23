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

import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.tree.DefaultMutableTreeNode;

import org.netbeans.swing.outline.DefaultOutlineCellRenderer;

import uk.gov.nationalarchives.droid.core.interfaces.NodeStatus;
import uk.gov.nationalarchives.droid.core.interfaces.ResourceType;
import uk.gov.nationalarchives.droid.profile.NodeMetaData;
import uk.gov.nationalarchives.droid.profile.ProfileResourceNode;

/**
 * @author a-mpalmer
 *
 */
public class NodeRenderer extends DefaultOutlineCellRenderer {

    private static final long serialVersionUID = -574370296768932560L;

    private Icon folderResourceTypeIcon;
    private Icon folderResourceTypeNotDoneIcon;
    private Icon folderResourceTypeNotFoundIcon;
    private Icon folderResourceTypeAccessDeniedIcon;
    private Icon folderResourceTypeErrorIcon;
    private Icon containerResourceTypeIcon;
    private Icon containerResourceTypeNotDoneIcon;
    private Icon containerResourceTypeNotFoundIcon;
    private Icon containerResourceTypeAccessDeniedIcon;
    private Icon containerResourceTypeErrorIcon;
    private Icon fileResourceTypeIcon;
    private Icon fileResourceTypeNotDoneIcon;
    private Icon fileResourceTypeNotFoundIcon;
    private Icon fileResourceTypeAccessDeniedIcon;
    private Icon fileResourceTypeErrorIcon;
    private Color backColor;
    private Color darkerColor;
    
    /**
     * @param backColor the default background color for the node.
     */
    public NodeRenderer(Color backColor) {
        super();
        folderResourceTypeIcon = getIconResource("folderResourceType");
        containerResourceTypeIcon = getIconResource("containerResourceType");
        fileResourceTypeIcon = getIconResource("fileResourceType");
        
        folderResourceTypeNotDoneIcon = getIconResource("folderResourceType_NOT_DONE");
        containerResourceTypeNotDoneIcon = getIconResource("containerResourceType_NOT_DONE");
        fileResourceTypeNotDoneIcon = getIconResource("fileResourceType_NOT_DONE");
        
        folderResourceTypeNotFoundIcon = getIconResource("folderResourceType_NOTFOUND");
        containerResourceTypeNotFoundIcon = getIconResource("containerResourceType_NOTFOUND");
        fileResourceTypeNotFoundIcon = getIconResource("fileResourceType_NOTFOUND");
        
        folderResourceTypeAccessDeniedIcon = getIconResource("folderResourceType_ACCESSDENIED");
        containerResourceTypeAccessDeniedIcon = getIconResource("containerResourceType_ACCESSDENIED");
        fileResourceTypeAccessDeniedIcon = getIconResource("fileResourceType_ACCESSDENIED");

        folderResourceTypeErrorIcon = getIconResource("folderResourceType_ERROR");
        containerResourceTypeErrorIcon = getIconResource("containerResourceType_ERROR");
        fileResourceTypeErrorIcon = getIconResource("fileResourceType_ERROR");
        
        this.backColor = backColor;
        this.darkerColor = TreeUtils.getDarkerColor(backColor);
    }
    
    private Icon getIconResource(String resourceName) {
        String resourcePath = String.format("uk/gov/nationalarchives/droid/icons/%s.gif", resourceName);
        URL imgURL = getClass().getClassLoader().getResource(resourcePath);
        return imgURL == null ? null : new ImageIcon(imgURL);        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        JLabel renderer = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (isSelected) {
            renderer.setBackground(table.getSelectionBackground());
            renderer.setForeground(table.getSelectionForeground());
        } else {
            renderer.setBackground(getBackgroundColor(table, row, column));
            renderer.setForeground(getForegroundColor(value, table));
        }
        renderer.setText(getDisplayName(value));
        renderer.setIcon(getIcon(value));
        return renderer;
    }

    /**
     * Returns the fully-qualified path of a node which is a root node, and the
     * name of any node which is not.
     * @param node the node
     * @return the display name
     */
    public String getDisplayName(Object node) {

        ProfileResourceNode profileNode = (ProfileResourceNode) ((DefaultMutableTreeNode) node)
            .getUserObject();
        return profileNode.getParentId() == null ? new File(profileNode.getUri()).getPath() 
                : profileNode.getMetaData().getName();
        //return profileNode.getParent() == null ? new File(profileNode.getUri()).getPath() 
        //        : profileNode.getMetaData().getName();
    }
    
    /**
     * Gets the correct icon for the node.
     * @param node the node
     * @return the icon for the node
     */
    // CHECKSTYLE:OFF cyclomatic complexity caused by switch statement.
    public Icon getIcon(Object node) {
    // CHECKSTYLE:ON
        
        ProfileResourceNode profileNode = getNode(node);
        NodeMetaData metadata = profileNode.getMetaData();
        ResourceType nodeType = metadata.getResourceType();
        NodeStatus status = metadata.getNodeStatus();
        Icon icon = null;        
        switch (nodeType) {
            case FOLDER:
                if (profileNode.getFilterStatus() != 1) {
                    icon = folderResourceTypeNotDoneIcon;
                } else {
                    switch (status) {
                        case NOT_DONE:
                            icon = folderResourceTypeNotDoneIcon;
                            break;
                        case ACCESS_DENIED:
                            icon = folderResourceTypeAccessDeniedIcon;
                            break;
                        case NOT_FOUND:
                            icon = folderResourceTypeNotFoundIcon;
                            break;
                        case ERROR:
                            icon = folderResourceTypeErrorIcon;
                            break;
                        default:
                            icon = folderResourceTypeIcon;
                            break;
                    }
                }
                break;
            case FILE:
                if (profileNode.getFilterStatus() != 1) {
                    icon = fileResourceTypeNotDoneIcon;
                } else {
                    switch (status) {
                        case NOT_DONE:
                            icon = fileResourceTypeNotDoneIcon;
                            break;
                        case ACCESS_DENIED:
                            icon = fileResourceTypeAccessDeniedIcon;
                            break;
                        case NOT_FOUND:
                            icon = fileResourceTypeNotFoundIcon;
                            break;
                        case ERROR:
                            icon = fileResourceTypeErrorIcon;
                            break;
                        default:
                            icon = fileResourceTypeIcon;
                    }
                }
                break;
            case CONTAINER:
                if (profileNode.getFilterStatus() != 1) {
                    icon = containerResourceTypeNotDoneIcon;
                } else {
                    switch (status) {
                        case NOT_DONE: // should be impossible, but implemented anyway.
                            icon = containerResourceTypeNotDoneIcon;
                            break;
                        case ACCESS_DENIED:
                            icon = containerResourceTypeAccessDeniedIcon;
                            break;
                        case NOT_FOUND:
                            icon = containerResourceTypeNotFoundIcon;
                            break;
                        case ERROR:
                            icon = containerResourceTypeErrorIcon;
                            break;
                        default: 
                            icon = containerResourceTypeIcon;
                    }
                }
                break;
            default:
        }
        return icon;
    }
    
    
    private Color getBackgroundColor(JTable table, int row, int column) {
        Color theColor;
        if (row % 2 == 0) {
            theColor = this.backColor;
        } else {
            theColor = this.darkerColor;
        } 
        return theColor;
    }    
    
    private Color getForegroundColor(Object obj, JTable table) {
        return table.getForeground();
    }
    
    private ProfileResourceNode getNode(Object node) {
        return (ProfileResourceNode) ((DefaultMutableTreeNode) node).getUserObject();
    }
}
