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

import javax.swing.SwingConstants;

import uk.gov.nationalarchives.droid.util.FileUtil;

/**
 * @author a-mpalmer
 *
 */
public class FileSizeRenderer extends DefaultCellRenderer {

    /**
     * Constructs a file size renderer and sets the generic properties of its 
     * private cached JLabel object used to actually render.
     * @param backColor - the background color of the cell.
     */
    public FileSizeRenderer(Color backColor) {
        super(backColor);
        getRenderer().setHorizontalAlignment(SwingConstants.RIGHT);
    }
        
    @Override
    public String getDisplayValue(Object value) {
        if (getFilterStatus(value) != 1) {
            return "";
        }
        DirectoryComparableLong val = (DirectoryComparableLong) value;
        final Long sizeValue = val.getSource(); 
        return sizeValue == null ? "" : FileUtil.formatFileSize(sizeValue, 1) + "  ";
    }
    
}
