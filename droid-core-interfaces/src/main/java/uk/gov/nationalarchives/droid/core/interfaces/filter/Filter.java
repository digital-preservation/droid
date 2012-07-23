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
