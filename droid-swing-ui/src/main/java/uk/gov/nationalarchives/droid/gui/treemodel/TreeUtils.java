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
import java.util.List;

import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;

/**
 * @author a-mpalmer
 *
 */
public final class TreeUtils {

    private static final double LIGHTER_SCALE = 1.05;
    private static final double DARKER_SCALE = 0.95;
    private static final int MINVALUE = 0;
    private static final int MAXVALUE = 255;
    
    private TreeUtils() {
    }
  
    
    private static int getScaledColorComponent(int colorComponent, double scaleFactor) {
        int scaledValue = (int) (colorComponent * scaleFactor);
        if (scaledValue < MINVALUE) {
            scaledValue = MINVALUE;
        } else if (scaledValue > MAXVALUE) {
            scaledValue = MAXVALUE;
        }
        return scaledValue;
    }
    
    /**
     * 
     * @param color The color to adjust
     * @param scaleFactor the amount to scale the colors by
     * @return The adjusted color
     */
    public static Color getScaledColor(Color color, double scaleFactor) {
        return new Color(
                getScaledColorComponent(color.getRed(), scaleFactor),
                getScaledColorComponent(color.getGreen(), scaleFactor),
                getScaledColorComponent(color.getBlue(), scaleFactor));
    }

    /**
     * 
     * @param color The color to make darker
     * @return The darker color
     */
    public static Color getDarkerColor(Color color) {
        return getScaledColor(color, DARKER_SCALE);
    }
    
    /**
     * 
     * @param color The color to make lighter
     * @return The lighter color
     */
    public static Color getLighterColor(Color color) {
        return getScaledColor(color, LIGHTER_SCALE);
    }

    
    /**
     * Returns whether a table column is sorted.
     * @param table - the table to check
     * @param column - the column in the table to check.
     * @return boolean - whether the column is sorted or not.
     */
    public static boolean isColumnSorted(JTable table, int column) {
        boolean isSorted = false;
        final int modelColumn = table.convertColumnIndexToModel(column);
        RowSorter sorter = table.getRowSorter();
        if (sorter != null) {
            List<? extends SortKey> sortKeys = sorter.getSortKeys();
            for (SortKey sortKey : sortKeys) {
                if (sortKey.getColumn() == modelColumn) {
                    isSorted = true;
                    break;
                }
            }
        }
        return isSorted;
    }
}
