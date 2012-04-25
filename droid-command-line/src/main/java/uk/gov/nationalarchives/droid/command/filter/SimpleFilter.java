/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.command.filter;

import java.util.ArrayList;
import java.util.List;

import uk.gov.nationalarchives.droid.command.filter.CommandLineFilter.FilterType;
import uk.gov.nationalarchives.droid.core.interfaces.filter.Filter;
import uk.gov.nationalarchives.droid.core.interfaces.filter.FilterCriterion;

/**
 * @author rflitcroft
 *
 */
public class SimpleFilter implements Filter {

    private FilterType type;
    private List<FilterCriterion> criteria = new ArrayList<FilterCriterion>();
    
    /**
     * 
     * @param type the type of filter
     */
    public SimpleFilter(FilterType type) {
        this.type = type;
    }
    
    /**
     * Adds a criterion.
     * @param criterion the criterion to add
     */
    public void add(FilterCriterion criterion) {
        criteria.add(criterion);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<FilterCriterion> getCriteria() {
        return criteria;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNarrowed() {
        return type.equals(FilterType.ALL);
    }
    
    
    /**
     * checks if filter is empty or filter criteria list exists. 
     * @return boolean 
     */
    public boolean hasCriteria() {
        boolean hasCriteria = false;
        if (criteria != null && !criteria.isEmpty()) {
            hasCriteria =  true;
        }
        return hasCriteria;
        
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEnabled(boolean enabled) {
    }
    
    /**
     * @return Attempts a shallow copy of the simple filter.
     * @throws CloneNotSupportedException if a problem occurs.
     */
    @Override
    public Filter clone() throws CloneNotSupportedException {
        return this.clone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FilterCriterion getFilterCriterion(int index) {
        return criteria.get(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfFilterCriterion() {
        return criteria.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNarrowed(boolean isNarrowed) {
    }
    
    
}
