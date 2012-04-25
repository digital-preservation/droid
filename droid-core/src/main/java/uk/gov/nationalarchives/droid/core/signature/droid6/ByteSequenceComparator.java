/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */

package uk.gov.nationalarchives.droid.core.signature.droid6;

import java.util.Comparator;

/**
 * Compares two byte sequences on their pre-calculated sort order.
 *
 * @author Matt Palmer
 */
public class ByteSequenceComparator implements Comparator<ByteSequence> {

    @Override
    public final int compare(ByteSequence o1, ByteSequence o2) {
        final int o1SortOrder = o1.getSortOrder();
        final int o2SortOrder = o2.getSortOrder();
        // use safe method of comparing sort orders (no possibility of number overflow
        // which may be caused if sortOrder numbers are large)
        return o1SortOrder < o2SortOrder ? -1 : o1SortOrder > o2SortOrder ? 1 : 0;
    }

}
