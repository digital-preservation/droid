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
 * A DirectoryComparable which wraps a string and doers case-insensitive comparisons.
 * @author rflitcroft
 *
 */
public class DirectoryComparableFilename extends DirectoryComparableObject<String> {

    /**
     * 
     * @param source the string which will compared case-insensitively
     * @param node the resource node for this value.
     */
    public DirectoryComparableFilename(String source, ProfileResourceNode node) {
        super(source, node);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected int sourceCompareTo(DirectoryComparable<String> other) {
        return String.CASE_INSENSITIVE_ORDER.compare(getSource(), other.getSource());
    }
    
}
