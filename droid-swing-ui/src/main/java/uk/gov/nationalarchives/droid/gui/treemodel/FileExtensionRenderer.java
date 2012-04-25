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

import javax.swing.Icon;
import javax.swing.SwingConstants;

/**
 * @author a-mpalmer
 *
 */
public class FileExtensionRenderer extends DefaultCellRenderer {

    private Icon warningIcon;
    
    /**
     * @param backColor the background color to render in.
     */
    public FileExtensionRenderer(Color backColor) {
        super(backColor);
        getRenderer().setHorizontalTextPosition(SwingConstants.LEFT);
        warningIcon = getIconResource("warning_extension_mismatch");
    }
    
    
    @Override
    /**
     * @param Object the object being rendered.
     * @return Icon the icon for a file extension mismatch.
     * @see uk.gov.nationalarchives.droid.gui.treemodel.DefaultCellRenderer#getIcon(java.lang.Object)
     */
    public Icon getIcon(Object value) {
        DirectoryComparableObject o = (DirectoryComparableObject) value;
        if (o.getExtensionMismatch()) {
            return warningIcon;
        }
        return null;
    }

}
