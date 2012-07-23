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
