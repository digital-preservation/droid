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
