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

/**
 * @author matt
 *
 */
public class HyperlinkRenderer extends DefaultCellRenderer {

    /**
     * Constructor for HyperlinkRenderer.
     * @param backColor - the default background color of a cell.
     */
    public HyperlinkRenderer(Color backColor) {
        super(backColor);
    }
    
    /**
     * Constructor for Hyperlink Renderer.
     * @param backColor - the default background color of a cell.
     * @param alignment - the left, center or right alignment of the values.
     */
    public HyperlinkRenderer(Color backColor, int alignment) {
        super(backColor, alignment);
    }

    @Override
    /**
     * {@InheritDoc}
     */
    public String getDisplayValue(Object value) {
        final String display = value.toString();
        if (display.startsWith("\"")) {
            return display;
        }
        return String.format("<html><a href=\"\">%s</a></html>", value.toString());
    }
}
