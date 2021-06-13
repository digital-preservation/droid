/*
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gov.uk>
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A basic implementation of Filter.
 */
public final class BasicFilter implements Filter {

    private final List<FilterCriterion> criteria;
    private boolean enabled;
    private boolean narrowed;

    /**
     * Constructs a null filter that never filters anything.
     */
    public BasicFilter() {
        this(null, true);
    }

    /**
     * Constructs a filter with a single filter criterion.
     *
     * @param criterion The filter criterion to use.
     */
    public BasicFilter(FilterCriterion criterion) {
        this(Arrays.asList(criterion), true);
    }

    /**
     * Constructs a filter given a list of criterion, and whether it is narrowed.  Enabled by default.
     * @param criteria the filter criteria to use.
     * @param isNarrowed whether the filter is narrowed (true) or widened (false).
     */
    public BasicFilter(List<FilterCriterion> criteria, boolean isNarrowed) {
        this(criteria, isNarrowed, true);
    }

    /**
     * Constructs a filter given a list of criterion, whether it is narrowed, whether it is enabled.
     * @param criteria the filter criteria to use.
     * @param isNarrowed whether the filter is narrowed (true) or widened (false).
     * @param isEnabled whether the filter is enabled.
     */
    public BasicFilter(List<FilterCriterion> criteria, boolean isNarrowed, boolean isEnabled) {
        this.criteria = criteria == null ? Collections.EMPTY_LIST : criteria;
        this.narrowed = isNarrowed;
        this.enabled = isEnabled;
    }

    @Override
    public List<FilterCriterion> getCriteria() {
        return criteria;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isNarrowed() {
        return narrowed;
    }

    @Override
    public boolean hasCriteria() {
        return criteria.size() > 0;
    }

    @Override
    public void setNarrowed(boolean isNarrowed) {
        this.narrowed = isNarrowed;
    }

    @Override
    public FilterCriterion getFilterCriterion(int index) {
        return criteria.get(index);
    }

    @Override
    public int getNumberOfFilterCriterion() {
        return criteria.size();
    }

    /**
     * <b>Shallow Copy:</b>
     * BasicFilter clone only returns a shallow copy.  The FilterCriterion will be the same
     * objects as those in this object, although it will have its own list of them.
     * {@inheritDoc}
     */
    @Override
    public Filter clone() {
        return new BasicFilter(new ArrayList<>(criteria), narrowed, enabled);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(narrowed: " + narrowed + " enabled: " + enabled + "criteria: " + criteria + ')';
    }

}
