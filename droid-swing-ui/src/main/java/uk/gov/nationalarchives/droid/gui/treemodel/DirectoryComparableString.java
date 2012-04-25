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
public class DirectoryComparableString extends DirectoryComparableObject<String> {

    /**
     * 
     * @param source the source string
     * @param node the resource node for this value.
     */
    public DirectoryComparableString(String source, ProfileResourceNode node) {
        super(source, node);
    }
    
    /**
     * A case insensitive comparison for strings.
     * @param o The string to be compared with.
     * @return A case insensitive string comparison.
     */
    @Override
    protected int sourceCompareTo(DirectoryComparable<String> o)  {
        return String.CASE_INSENSITIVE_ORDER.compare(getSource(), o.getSource());
    }
}
