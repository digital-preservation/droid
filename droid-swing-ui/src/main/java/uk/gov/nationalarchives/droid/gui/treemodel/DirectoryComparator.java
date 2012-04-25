/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui.treemodel;

import java.util.Comparator;

/**
 * Marker extension of Comparator which compares DirectoryComparable classes.
 * @author rflitcroft
 * 
 * @param <T> the source type of the comparable.
 *
 */
public interface DirectoryComparator<T> extends Comparator<DirectoryComparable<T>> {

}
