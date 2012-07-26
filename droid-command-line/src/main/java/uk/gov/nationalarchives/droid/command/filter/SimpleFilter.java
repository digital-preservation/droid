/**
 * Copyright (c) 2012, The National Archives <pronom@nationalarchives.gsi.gov.uk>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following
 * conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of the The National Archives nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
