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
 * This class compares sort orders already defined in the 
 * internal signatures being compared, to allow them to be
 * sorted for maximum performance.
 * 
 * <p/>To change the sort order of the signatures, change the
 * sort order calculation in the {@link InternalSignature}
 * class itself, not this comparator.
 * 
 * @author Matt Palmer
 */
public class InternalSignatureComparator implements Comparator<InternalSignature> {

    @Override
    public final int compare(InternalSignature o1, InternalSignature o2) {
        final int o1SortOrder = o1.getSortOrder();
        final int o2SortOrder = o2.getSortOrder();
        return o1SortOrder < o2SortOrder ? -1 : o1SortOrder > o2SortOrder ? 1 : 0;
    }

}
