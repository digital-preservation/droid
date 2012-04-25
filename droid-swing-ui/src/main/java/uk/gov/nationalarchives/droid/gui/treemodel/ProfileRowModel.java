/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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
