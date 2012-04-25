/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui.treemodel;

import uk.gov.nationalarchives.droid.profile.ProfileResourceNode;

/**
 * @author rflitcroft
 *
 */
public class DirectoryComparableLong extends DirectoryComparableObject<Long> {

    /**
     * @param source the source long
     * @param node the resource node for this value.
     */
    public DirectoryComparableLong(Long source, ProfileResourceNode node) {
        super(source, node);
    }
    
    /**
     * @param source the source long
     * @param node the resource node for this value.
     */
    public DirectoryComparableLong(Integer source, ProfileResourceNode node) {
        super(source == null ? null : (long) source, node);
    }

    /**
     * Formats the Long as a string.
     * @return a number formatted String.
     */
    //@Override
    //public String toString() {
    //    return getSource() == null ? "" : NumberFormat.getInstance().format(getSource());
    //}
}
