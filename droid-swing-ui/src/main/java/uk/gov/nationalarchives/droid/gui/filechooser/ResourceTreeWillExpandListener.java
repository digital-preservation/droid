/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui.filechooser;

import java.io.File;
import java.util.List;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author rflitcroft
 *
 */
public class ResourceTreeWillExpandListener implements TreeWillExpandListener {

    @Override
    public void treeWillExpand(TreeExpansionEvent event) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
        node.removeAllChildren();
        File file = (File) node.getUserObject();
        File[] children = file.listFiles();
        List<File> sortedChildren = ResourceDialogUtil.sortFiles(children);
        for (File f : sortedChildren) {
            if (f.isDirectory()) {
                //boolean allowsChildren = f.listFiles() != null && f.listFiles().length > 0;
                // don't check children when expanding node - assume it has them as it is a directory.
                final boolean allowsChildren = true; 
                DefaultMutableTreeNode child = new DefaultMutableTreeNode(f, allowsChildren);
                node.add(child);
            }
        }
    }
    
    @Override
    public void treeWillCollapse(TreeExpansionEvent event) {
        //DefaultMutableTreeNode node = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
        //File f = (File) node.getUserObject();
    }
}
