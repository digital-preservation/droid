/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.core.interfaces.filter;

import java.util.List;


/**
 * @author rflitcroft
 *
 */
public interface Filter {

    /**
     * Getter method for filter criteria list.
     * @return List of filter criteria.
     */
    List<FilterCriterion> getCriteria();

    /**
     * @return true if the filter is enabled; false otherwise
     */
    boolean isEnabled();

    
    /**
     * 
     * @param enabled Whether the filter is enabled or not.
     */
    void setEnabled(boolean enabled);
    
    /**
     * @return true if ALL criteria must be satisfied, false if ANY of the criteria must be satisfied
     */
    boolean isNarrowed();

    /**
     * checks if filter is empty or filter criteria list exists. 
     * @return boolean 
     */
    boolean hasCriteria();
    
    /**
     * A narrowed filter is one where all filter criteria must be true.
     * 
     * @param isNarrowed Whether the filter is narrowed or not.
     */
    void setNarrowed(boolean isNarrowed);
    
    
    /**
     * 
     * @param index The index of the filter criterion to get.
     * @return The filter criterion at that index.
     */
    FilterCriterion getFilterCriterion(int index);
    
    
    /**
     * 
     * @return The number of filter criterion in the filter.
     */
    int getNumberOfFilterCriterion();

    
      
    /**
     * 
     * @return A clone of the filter object.
     * @throws CloneNotSupportedException if cloning is not supported.
     */
    Filter clone() throws CloneNotSupportedException;
    
}
