/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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
