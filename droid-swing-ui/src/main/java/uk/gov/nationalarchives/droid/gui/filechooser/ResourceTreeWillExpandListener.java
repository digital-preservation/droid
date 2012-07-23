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
