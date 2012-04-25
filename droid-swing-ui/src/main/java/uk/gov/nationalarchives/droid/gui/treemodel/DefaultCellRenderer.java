/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui.treemodel;

import java.awt.Color;
import java.awt.Component;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;



/**
 * @author a-mpalmer
 *
 */
public class DefaultCellRenderer implements TableCellRenderer {

    private JLabel renderer = new JLabel();
    private Color backColor;
    private Color darkerColor;
   
    /**
     * 
     * @param backColor The default background color to render in
     */
    public DefaultCellRenderer(Color backColor) {
        renderer.setOpaque(true);
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        this.backColor = backColor;
        this.darkerColor = TreeUtils.getDarkerColor(backColor);
    }
    
    /**
     * Overloaded constructor for the default cell renderer taking an alignment.
     * @param backColor - the default background color for the cell
     * @param alignment - the alignment (SwingConstants) of the cell contents.
     */
    public DefaultCellRenderer(Color backColor, int alignment) {
        this(backColor);
        renderer.setHorizontalAlignment(alignment);
    }
    
    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        if (isSelected) {
            renderer.setBackground(table.getSelectionBackground());
            renderer.setForeground(table.getSelectionForeground());
        } else {
            renderer.setBackground(getBackgroundColor(table, row, column));
            renderer.setForeground(table.getForeground());
        }
        if (getFilterStatus(value) == 1) {
            renderer.setText(getDisplayValue(value));
            renderer.setIcon(getIcon(value));
        } else {
            renderer.setText("");
            renderer.setIcon(null);
        }
        return renderer;
    }
    
    /**
     * getDisplayValue returns the display value for an object in the
     * renderer.  This can be overridden by subclasses to provide for
     * more specialised display values (e.g. the HyperLinkRenderer).
     * @param value The object to get a display value for.
     * @return String the display value of the object.
     */
    public String getDisplayValue(Object value) {
        return value.toString();
    }
    
    /**
     * 
     * @param value the value being rendererd
     * @return An icon to render.
     */
    public Icon getIcon(Object value) {
        return null;
    }
    
    /**
     * 
     * @return The renderer component.
     */
    protected JLabel getRenderer() {
        return renderer;
    }
    
    
    /**
     * 
     * @param resourceName the name of the icon resource to load.
     * @return An icon containing the loaded icon resource.
     */
    protected Icon getIconResource(String resourceName) {
        String resourcePath = String.format("uk/gov/nationalarchives/droid/icons/%s.gif", resourceName);
        URL imgURL = getClass().getClassLoader().getResource(resourcePath);
        return imgURL == null ? null : new ImageIcon(imgURL);        
    }
    
    
    /**
     * 
     * @param value The value being displayed
     * @return A filter status - 0 - does not meet filter, 1 meets filter, 2 children meets filter
     */
    public int getFilterStatus(Object value) {
        DirectoryComparableObject o = (DirectoryComparableObject) value;
        return o.getFilterStatus();
    }    
    
    private Color getBackgroundColor(JTable table, int row, int column) {
        Color theColor;
        if (row % 2 == 0) {
            theColor = this.backColor;
        } else {
            theColor = this.darkerColor;
        }
        return theColor;
    }    
   
}
