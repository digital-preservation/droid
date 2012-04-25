/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui.filter;

import java.awt.Component;
import java.io.Serializable;
import java.util.EventObject;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.EventListenerList;
import javax.swing.table.TableCellEditor;

/**
 * @author Alok Kumar Dash
 */
public class JComponentCellEditor implements TableCellEditor, Serializable {

    private static final long serialVersionUID = 17599959609189470L;

    private EventListenerList listenerList = new EventListenerList();

    private JComponent editorComponent;

    /**
     * @return the component.
     */
    public Component getComponent() {
        return editorComponent;
    }

    @Override
    public Object getCellEditorValue() {
        return editorComponent;
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        return true;
    }

    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }

    @Override
    public boolean stopCellEditing() {

        return true;
    }

    @Override
    public void cancelCellEditing() {

    }

    @Override
    public void addCellEditorListener(CellEditorListener l) {
        listenerList.add(CellEditorListener.class, l);
    }

    @Override
    public void removeCellEditorListener(CellEditorListener l) {
        listenerList.remove(CellEditorListener.class, l);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {

        editorComponent = (JComponent) value;
        return editorComponent;
    }

}
