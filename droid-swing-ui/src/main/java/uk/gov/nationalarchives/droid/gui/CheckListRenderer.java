/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 *
 * @author rflitcroft
 */
public class CheckListRenderer extends JCheckBox implements ListCellRenderer {

    private static final long serialVersionUID = -7661637810084010790L;

    /** Creates new CheckListRenderer. */
    public CheckListRenderer() {
    }

    /**
     * @Override
     * {@inheritDoc}
     * 
     */
    @Override
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        CheckListCellModel row = (CheckListCellModel) value;
        setSelected(row.isSelected());
        setText(row.getLabel());
        setFont(list.getFont());
        setBackground(list.getBackground());
        return this;
    }

}
