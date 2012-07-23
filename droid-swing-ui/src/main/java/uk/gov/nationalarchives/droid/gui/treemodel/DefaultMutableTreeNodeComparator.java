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

import java.util.Comparator;

import javax.swing.tree.DefaultMutableTreeNode;

import org.netbeans.swing.etable.ETableColumn;

import uk.gov.nationalarchives.droid.core.interfaces.ResourceType;
import uk.gov.nationalarchives.droid.profile.ProfileResourceNode;

/**
 * Comparator for ProfileResourceNodes for tree table use.
 * INCONSISTENT WITH EQUALS!!! DO NOT USE THIS TO ORDER A SET OR HASHMAP! 
 * @author rflitcroft
 *
 */
public class DefaultMutableTreeNodeComparator implements Comparator<DefaultMutableTreeNode> {

    private ETableColumn column;
    
    /**
     * 
     * @param column the sortable column
     */
    public DefaultMutableTreeNodeComparator(ETableColumn column) {
        this.column = column;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int compare(DefaultMutableTreeNode o1, DefaultMutableTreeNode o2) {

        ProfileResourceNode f1 = (ProfileResourceNode) o1.getUserObject();
        ProfileResourceNode f2 = (ProfileResourceNode) o2.getUserObject();
        
        ResourceType resourceType1 = f1.getMetaData().getResourceType();
        ResourceType resourceType2 = f2.getMetaData().getResourceType();
        
        int typeCompare = -(resourceType1.compareTo(resourceType2));
        if (column.isAscending()) {
            typeCompare = -typeCompare;
        }
        
        return typeCompare == 0
                ? String.CASE_INSENSITIVE_ORDER.compare(f1.getMetaData().getName(), f2.getMetaData().getName())
                : typeCompare; 
    }
}
