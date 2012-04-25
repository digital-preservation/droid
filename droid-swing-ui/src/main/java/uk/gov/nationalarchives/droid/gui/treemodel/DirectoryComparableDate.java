/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui.treemodel;

import java.text.DateFormat;
import java.util.Date;

import uk.gov.nationalarchives.droid.profile.ProfileResourceNode;

/**
 * @author rflitcroft
 *
 */
public class DirectoryComparableDate extends DirectoryComparableObject<Date> {

    /**
     * @param source the source Date
     * @param node the resource node for this value.
     */
    public DirectoryComparableDate(Date source, ProfileResourceNode node) {
        super(source, node);
    }
    
    /**
     * Formats the date as a String.
     * @return a formatted date.
     */
    @Override
    public String toString() {
        return getSource() == null ? "" : DateFormat.getInstance().format(getSource());
    }
    
}
