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
