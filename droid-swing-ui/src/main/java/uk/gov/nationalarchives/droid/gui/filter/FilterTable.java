/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui.filter;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * @author Alok Kumar Dash
 */
public class FilterTable extends JTable {

    private static final long serialVersionUID = 8895696052738089249L;

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        TableColumn tableColumn = getColumnModel().getColumn(column);
        TableCellRenderer renderer = tableColumn.getCellRenderer();
        if (renderer == null) {
            Class<?> c = getColumnClass(column);
            if (c.equals(Object.class)) {
                Object o = getValueAt(row, column);
                if (o != null) {
                    c = getValueAt(row, column).getClass();
                }
            }
            renderer = getDefaultRenderer(c);
        }
        return renderer;
    }

    @Override
    public TableCellEditor getCellEditor(int row, int column) {
        TableColumn tableColumn = getColumnModel().getColumn(column);
        TableCellEditor editor = tableColumn.getCellEditor();
        if (editor == null) {
            Class<?> c = getColumnClass(column);
            if (c.equals(Object.class)) {
                Object o = getValueAt(row, column);
                if (o != null) {
                    c = getValueAt(row, column).getClass();
                }
            }
            editor = getDefaultEditor(c);
        }
        return editor;
    }

}
