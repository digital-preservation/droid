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
 * @param <T> the Enum type.
 *
 */
public class DirectoryComparableEnum<T extends Enum<T>> extends DirectoryComparableObject<T> {

    /**
     * @param source the source object
     * @param node the resource node for this value.
     */
    public DirectoryComparableEnum(T source, ProfileResourceNode node) {
        super(source, node);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getSource() == null ? "" : getSource().name();
    }

}
