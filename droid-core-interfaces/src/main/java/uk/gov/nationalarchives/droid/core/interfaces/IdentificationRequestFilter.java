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
package uk.gov.nationalarchives.droid.core.interfaces;

import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.filter.BasicFilter;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionOperator;
import uk.gov.nationalarchives.droid.core.interfaces.filter.Filter;
import uk.gov.nationalarchives.droid.core.interfaces.filter.FilterCriterion;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Determines if an IdentificationRequest meets filter criteria.
 * Only simple request metadata can be filtered before identification takes place:
 * Filename, filesize, lastModifiedDate and file extension.
 * The purpose of this filter is a performance optimisation: if we are writing out identifications to a CSV
 * file, and the filter would remove the result anyway, there is no point in running the actual identification
 * on it, so we can avoid submitting it for identification.
 * This should be set on any handler that processes identification requests - files, but also archive content handlers.
 */
public class IdentificationRequestFilter {

    private Filter filter;

    /**
     * Empty bean constructor that sets a null filter.
     * Use setFilter() to set a filter after construction.
     */
    public IdentificationRequestFilter() {
        this(null);
    }

    /**
     * Constructs a IdentificationRequestFilter given a Filter.
     * @param filter The filter to construct with.
     */
    public IdentificationRequestFilter(final Filter filter) {
        setFilter(filter);
    }

    /**
     * Returns whether the profile resource node meets the filter criteria.
     * @param request The request to test.
     * @return true if the node meets the filter criteria.
     */
    public boolean passesFilter(final IdentificationRequest<?> request) {
        if (filter != null) { // If no filter supplied, it will not filter anything.
            List<FilterCriterion> criteria = filter.getCriteria();
            if (criteria.size() > 0) { // a filter with no criteria will not filter anything.
                if (filter.isNarrowed()) {
                    return isFilteredNarrowed(request, criteria);
                } else {
                    return isFilteredWidened(request, criteria);
                }
            }
        }
        return false;
    }

    /**
     * Setter for the filter to use.
     * @param filter the filter to use.
     */
    public void setFilter(final Filter filter) {
        this.filter = createBeforeIdentificationFilter(filter);
    }

    /**
     * Is filtered if the node meets ANY of the filter criteria.
     * @param request The node to filter
     * @param criteria The list of filter criterions.
     * @return true if the node meets ANY of the filter criteria.
     */
    private boolean isFilteredWidened(final IdentificationRequest<?> request, final List<FilterCriterion> criteria) {
        for (FilterCriterion criterion : criteria) {
            if (meetsCriterion(request, criterion)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Is filtered if the node meets ALL of the filter criteria.
     * @param request The node to filter.
     * @param criteria The list of filter criteria.
     * @return true if the node meets ALL of the filter criteria.
     */
    private boolean isFilteredNarrowed(final IdentificationRequest<?> request, final List<FilterCriterion> criteria) {
        for (FilterCriterion criterion : criteria) {
            if (!meetsCriterion(request, criterion)) {
                return false;
            }
        }
        return true;
    }

    private boolean meetsCriterion(final IdentificationRequest<?> request, final FilterCriterion criterion) {
        boolean result;
        CriterionOperator operator = criterion.getOperator();
        Object criterionValue = criterion.getValue();
        switch (criterion.getField()) {
            case FILE_NAME: { //TODO: should be case insensitive?
                result = compareStrings(request.getRequestMetaData().getName(), operator, criterionValue);
                break;
            }
            case FILE_SIZE: {
                result = compareLongs(request.getRequestMetaData().getSize(), operator, criterionValue);
                break;
            }
            case LAST_MODIFIED_DATE: {
                result = compareDates(request.getRequestMetaData().getTime(), operator, criterionValue);
                break;
            }
            case FILE_EXTENSION: {
                result = compareCaseInsensitiveStrings(request.getExtension(), operator, criterionValue);
                break;
            }
            default : {
                // any criterio other than basic metadata always passes - although these should already be removed.
                result = true;
                break;
            }
        }
        return result;
    }

    private boolean compareDates(Long requestTime, CriterionOperator operator, Object criterionValue) {
        boolean result = false;
        if (requestTime != null) {
            long compareValue = ((Date) criterionValue).getTime();
            switch (operator) {
                case LT: {
                    result = requestTime < compareValue;
                    break;
                }
                case LTE: {
                    result = requestTime <= compareValue;
                    break;
                }
                case EQ: {
                    result = requestTime == compareValue;
                    break;
                }
                case NE: {
                    result = requestTime != compareValue;
                    break;
                }
                case GT: {
                    result = requestTime > compareValue;
                    break;
                }
                case GTE: {
                    result = requestTime >= compareValue;
                    break;
                }
            }
        }
        return result;
    }

    private boolean compareLongs(Long nodeValue, CriterionOperator operator, Object criterionValue) {
        boolean result = false;
        if (nodeValue != null) {
            long compareValue = (Long) criterionValue;
            switch (operator) {
                case LT: {
                    result = nodeValue.compareTo(compareValue) < 0;
                    break;
                }
                case LTE: {
                    result = nodeValue.compareTo(compareValue) <= 0;
                    break;
                }
                case EQ: {
                    result = nodeValue.compareTo(compareValue) == 0;
                    break;
                }
                case NE: {
                    result = nodeValue.compareTo(compareValue) != 0;
                    break;
                }
                case GT: {
                    result = nodeValue.compareTo(compareValue) > 0;
                    break;
                }
                case GTE: {
                    result = nodeValue.compareTo(compareValue) >= 0;
                    break;
                }
            }
        }
        return result;
    }

    private boolean compareStrings(String nodeValue, CriterionOperator operator, Object criterionValue) {
        boolean result = false;
        if (nodeValue != null) {
            if (criterionValue instanceof String) {
                result = compareString(nodeValue, operator, (String) criterionValue);
            } else if (criterionValue instanceof Object[]) {
                Object[] values = (Object[]) criterionValue;
                int foundCount = 0;
                for (Object value : values) {
                    if (compareString(nodeValue, operator, (String) value)) {
                        foundCount++;
                    }
                }
                result = isOperatorInverted(operator) ? foundCount == values.length : foundCount > 0;
            }
        }
        return result;
    }

    private boolean isOperatorInverted(CriterionOperator operator) {
        return (operator == CriterionOperator.NE ||
                operator == CriterionOperator.NONE_OF ||
                operator == CriterionOperator.NOT_STARTS_WITH ||
                operator == CriterionOperator.NOT_ENDS_WITH ||
                operator == CriterionOperator.NOT_CONTAINS);
    }

    private boolean compareCaseInsensitiveStrings(String nodeValue, CriterionOperator operator, Object criterionValue) {
        boolean result = false;
        if (nodeValue != null) {
            String nodeValueLower = nodeValue.toLowerCase(Locale.ROOT);
            if (criterionValue instanceof String) {
                String criterionValueLower = ((String) criterionValue).toLowerCase(Locale.ROOT);
                result = compareString(nodeValueLower, operator, criterionValueLower);
            } else if (criterionValue instanceof Object[]) {
                Object[] values = (Object[]) criterionValue;
                int foundCount = 0;
                for (Object value : values) {
                    String criterionValueLower = ((String) value).toLowerCase(Locale.ROOT);
                    if (compareString(nodeValueLower, operator, (String) criterionValueLower)) {
                        foundCount++;
                    }
                }
                result = isOperatorInverted(operator) ? foundCount == values.length : foundCount > 0;
            }
        }
        return result;
    }

    private boolean compareString(String nodeValue, CriterionOperator operator, String compareValue) {
        boolean result = false;
        if (nodeValue != null) {
            switch (operator) {
                case LT: {
                    result = nodeValue.compareTo(compareValue) < 0;
                    break;
                }
                case LTE: {
                    result = nodeValue.compareTo(compareValue) <= 0;
                    break;
                }
                case EQ: {
                    result = nodeValue.compareTo(compareValue) == 0;
                    break;
                }
                case NE: {
                    result = !(nodeValue.compareTo(compareValue) == 0);
                    break;
                }
                case GT: {
                    result = nodeValue.compareTo(compareValue) > 0;
                    break;
                }
                case GTE: {
                    result = nodeValue.compareTo(compareValue) >= 0;
                    break;
                }
                case STARTS_WITH: {
                    result = nodeValue.startsWith(compareValue);
                    break;
                }
                case NOT_STARTS_WITH: {
                    result = !nodeValue.startsWith(compareValue);
                    break;
                }
                case ENDS_WITH: {
                    result = nodeValue.endsWith(compareValue);
                    break;
                }
                case NOT_ENDS_WITH: {
                    result = !nodeValue.endsWith(compareValue);
                    break;
                }
                case CONTAINS: {
                    result = nodeValue.contains(compareValue);
                    break;
                }
                case NOT_CONTAINS: {
                    result = !nodeValue.contains(compareValue);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Removes any criteria that relate to subsequent identification activity.
     * Only criteria that can be tested BEFORE any identification occurs can be applied.
     * @param filter The filter to analyze
     * @return A filter which only contains criteria which can be tested before identification.
     */
    private Filter createBeforeIdentificationFilter(Filter filter) {
        Filter result = null;
        if (filter != null) {
            List<FilterCriterion> criteria = filter.getCriteria();
            List<FilterCriterion> newCriteria = new ArrayList<>();
            for (FilterCriterion criterion : criteria) {
                switch (criterion.getField()) {
                    case FILE_NAME:
                    case FILE_SIZE:
                    case FILE_EXTENSION:
                    case LAST_MODIFIED_DATE: {
                        newCriteria.add(criterion);
                        break;
                    }
                    default : break; // don't add.
                }
            }
            if (newCriteria.size() > 0) {
                result = new BasicFilter(newCriteria, filter.isNarrowed());
            }
        }
        return result;
    }

}
