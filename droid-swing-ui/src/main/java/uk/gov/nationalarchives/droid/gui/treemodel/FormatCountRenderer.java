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
import java.awt.Component;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

/**
 * @author rflitcroft
 *
 */
public class FormatCountRenderer implements TableCellRenderer {

    /** */
    private static final String ICON_URL_PATTERN = "uk/gov/nationalarchives/droid/icons/format_count_small_%s.png";

    /**
     * Cached Internal labels used to render the different types of format counts.
     */
    private JLabel nullFormatCount = new JLabel();
    private JLabel zeroFormatCount = new JLabel();
    private JLabel oneFormatCount = new JLabel();
    private JLabel manyFormatCount = new JLabel();
    private Color backColor;
    private Color darkerColor;
    /**
     * Creates a format count renderer and initialises the internal labels and icons
     * used to render the different counts.
     * @param backColor the background color to render cells in.
     */
    public FormatCountRenderer(Color backColor) {
        setLabelProperties(nullFormatCount, null);
        setLabelProperties(zeroFormatCount, 0);
        setLabelProperties(oneFormatCount, 1);
        setLabelProperties(manyFormatCount, null);
        this.backColor = backColor;
        this.darkerColor = TreeUtils.getDarkerColor(backColor);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        DirectoryComparableLong count = (DirectoryComparableLong) value;
        JLabel renderer = getRenderer(count);
        final Long sourceCount = count.getSource();
        boolean displayAsLink = sourceCount != null && sourceCount > 1;
        if (displayAsLink && count.getFilterStatus() == 1) {
            renderer.setText("<html>(<u>" + sourceCount + "</u>)</html>");
        } else {
            renderer.setText(null);
        }
        if (isSelected) {
            renderer.setBackground(table.getSelectionBackground());
            renderer.setForeground(table.getSelectionForeground());
        } else {
            renderer.setBackground(getBackgroundColor(table, row, column));
            renderer.setForeground(displayAsLink ? Color.BLUE : table.getForeground());
        }
        return renderer;
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

    
    private JLabel getRenderer(DirectoryComparableLong count) {
        JLabel result = null;
        final Long source = count.getSource();
        if (source != null && count.getFilterStatus() == 1) {
            if (source == 1) {
                result = oneFormatCount;
            } else if (source > 1) {
                result = manyFormatCount;
            } else {
                result = zeroFormatCount;
            }
        } else {
            result = nullFormatCount;
        }
        return result;
    }

    private void setLabelProperties(JLabel label, Integer count) {
        label.setOpaque(true);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setIcon(createImageIcon(count));
    }
    
    /** 
     * Returns an ImageIcon for the count given. 
     */
    private ImageIcon createImageIcon(Integer count) {
        String iconSuffix;
        if (count != null) {
            if (count == 1) {
                iconSuffix = "ONE";
            } else if (count > 1) {
                iconSuffix = "MULTIPLE";
            } else {
                iconSuffix = "ZERO";
            }
            
            URL imgURL = getClass().getClassLoader().getResource(String.format(ICON_URL_PATTERN, iconSuffix));
            return imgURL == null ? null : new ImageIcon(imgURL, iconSuffix);
        }
        return null;
    }

    
}
