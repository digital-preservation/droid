/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui.util;

import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * Some bespoke string utils.
 * @author rflitcroft
 *
 */
public final class DroidStringUtils {

    private static final String ELIPSES = "...";
    
    private DroidStringUtils() { }
    
   
    
    /**
     * Abbreviates a String by replacing a chunk in the middle by ellipses (...).
     * @param str the string to abbreviate.
     * @param minLeft preserves a minimum number of characters at the start
     * @param maxLength the length of the abbreviated String
     * This will be ignored if less than minLeft + minRight + 3
     * @return the abbreviated string
     */
    public static String abbreviate(String str, int minLeft, int maxLength) {
        if (str.length() <= maxLength) {
            return str;
        }
        int left = maxLength - ELIPSES.length();
        left = (left >= 0) ? left : 0;
        if (left > minLeft && minLeft >= 0) {
            left = minLeft;
        }
               
        int length = str.length();
        if (left > length) {
            left = length;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(str.substring(0, left));
        sb.append(ELIPSES);

        int removeCount = length - maxLength + sb.length();
        if (removeCount > 0 && removeCount < length) {
            sb.append(str.substring(removeCount, length));
        }
        
        return sb.toString();
        
    }
    /**
     * Returns a string abbreviated according to the length of the available space
     * in a component.
     * @param str A string which may need abbreviating.
     * @param component The component the string will be rendered in.
     * @return a string abbreviated according to the length of the available space
     */
    public static String abbreviate(String str, JComponent component) {
        String result = "";
        if (component != null) {
            Graphics g = component.getGraphics();
            FontMetrics fm = g.getFontMetrics(component.getFont());
            int stringSize = SwingUtilities.computeStringWidth(fm, str);
            final int border = 48;
            int availableWidth = component.getWidth() - border;
            if (stringSize > availableWidth) {
                final int avCharWidth = fm.charWidth('x');
                final int alwaysChop = 5;
                final int charsToChop = alwaysChop + ((stringSize - availableWidth) / avCharWidth);
                final int leftPos = (str.length() - charsToChop) / 2; 
                final int maxLength = str.length() - charsToChop;
                final int left = leftPos > 0 ? leftPos : 0;
                final int len = maxLength > left ? maxLength : left + 1;
                result = abbreviate(str, left, len);
            } else {
                result = str;
            }
        }
        return result;
    }
    
}
