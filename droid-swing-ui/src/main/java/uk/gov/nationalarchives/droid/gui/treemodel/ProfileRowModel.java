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

import javax.swing.tree.DefaultMutableTreeNode;

import org.netbeans.swing.outline.RowModel;

import uk.gov.nationalarchives.droid.profile.ProfileResourceNode;

/**
 * @author rflitcroft
 *
 */
public class ProfileRowModel implements RowModel {

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getColumnClass(int index) {
        return OutlineColumn.values()[index].getColumnClass();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getColumnCount() {
        return OutlineColumn.values().length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getColumnName(int index) {
        return OutlineColumn.values()[index].getName();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValueFor(Object node, int index) {
        if (node != null) {
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
            return OutlineColumn.values()[index].getValue((ProfileResourceNode) treeNode.getUserObject());
        }
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCellEditable(Object arg0, int arg1) {
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setValueFor(Object arg0, int arg1, Object arg2) {
        // TODO Auto-generated method stub
    }
}
