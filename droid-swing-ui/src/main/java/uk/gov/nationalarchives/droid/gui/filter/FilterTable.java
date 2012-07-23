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
