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
package uk.gov.nationalarchives.droid.profile;

import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionOperator;
import uk.gov.nationalarchives.droid.core.interfaces.filter.Filter;
import uk.gov.nationalarchives.droid.core.interfaces.filter.FilterCriterion;
import uk.gov.nationalarchives.droid.profile.referencedata.Format;

import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Determines if a ProfileResourceNode meets filter criteria.
 */
public class ProfileResourceNodeFilter {

    private Filter filter;

    /**
     * Empty bean constructor that sets a null filter.
     * Use setFilter() to set a filter after construction.
     */
    public ProfileResourceNodeFilter() {
        this(null);
    }

    /**
     * Constructs a ProfileResourceNode given a Filter.
     * @param filter The filter to construct with.
     */
    public ProfileResourceNodeFilter(final Filter filter) {
        this.filter = filter;
    }

    /**
     * Returns whether the profile resource node meets the filter criteria.
     * @param node The node to test.
     * @return true if the node meets the filter criteria.
     */
    public boolean isFiltered(final ProfileResourceNode node) {
        if (filter != null) { // If no filter supplied, it will not filter anything.
            List<FilterCriterion> criteria = filter.getCriteria();
            if (criteria.size() > 0) { // a filter with no criteria will not filter anything.
                if (filter.isNarrowed()) {
                    return isFilteredNarrowed(node, criteria);
                } else {
                    return isFilteredWidened(node, criteria);
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
        this.filter = filter;
    }

    /**
     * Is filtered if the node meets ANY of the filter criteria.
     * @param node The node to filter
     * @param criteria The list of filter criterions.
     * @return true if the node meets ANY of the filter criteria.
     */
    private boolean isFilteredWidened(final ProfileResourceNode node, final List<FilterCriterion> criteria) {
        for (FilterCriterion criterion : criteria) {
            if (meetsCriterion(node, criterion)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Is filtered if the node meets ALL of the filter criteria.
     * @param node The node to filter.
     * @param criteria The list of filter criteria.
     * @return true if the node meets ALL of the filter criteria.
     */
    private boolean isFilteredNarrowed(final ProfileResourceNode node, final List<FilterCriterion> criteria) {
       for (FilterCriterion criterion : criteria) {
           if (!meetsCriterion(node, criterion)) {
               return false;
           }
       }
       return true;
    }

    private boolean meetsCriterion(final ProfileResourceNode node, final FilterCriterion criterion) {
        boolean result = false;
        CriterionOperator operator = criterion.getOperator();
        Object criterionValue = criterion.getValue();
        switch (criterion.getField()) {
            case FILE_NAME: {
                result = compareStrings(node.getMetaData().getName(), operator, criterionValue);
                break;
            }
            case FILE_SIZE: {
                result = compareLongs(node.getMetaData().getSize(), operator, criterionValue);
                break;
            }
            case LAST_MODIFIED_DATE: {
                result = compareDates(node.getMetaData().getLastModifiedDate(), operator, criterionValue);
                break;
            }
            case RESOURCE_TYPE: {
                result = compareEnums(node.getMetaData().getResourceType(), operator, criterionValue);
                break;
            }
            case IDENTIFICATION_METHOD: {
                result = compareEnums(node.getMetaData().getIdentificationMethod(), operator, criterionValue);
                break;
            }
            case JOB_STATUS: {
                result = compareEnums(node.getMetaData().getNodeStatus(), operator, criterionValue);
                break;
            }
            case FILE_EXTENSION: {
                result = compareCaseInsensitiveStrings(node.getMetaData().getExtension(), operator, criterionValue);
                break;
            }
            case IDENTIFICATION_COUNT: {
                result = compareIntegers(node.getIdentificationCount(), operator, criterionValue);
                break;
            }
            case EXTENSION_MISMATCH: {
                result = compareBooleans(node.getExtensionMismatch(), operator, criterionValue);
                break;
            }
            case PUID: {
                result = comparePuids(node.getFormatIdentifications(), operator, criterionValue);
                break;
            }
            case FILE_FORMAT: {
                result = compareFormats(node.getFormatIdentifications(), operator, criterionValue);
                break;
            }
            case MIME_TYPE: {
                result = compareMimeTypes(node.getFormatIdentifications(), operator, criterionValue);
                break;
            }
        }
        return result;
    }

    private boolean compareMimeTypes(List<Format> formatIdentifications, CriterionOperator operator, Object criterionValue) {
        boolean result = false;
        if (operator == CriterionOperator.ANY_OF) {
            result = compareAnyOfMimeType(formatIdentifications, criterionValue);
        } else if (operator == CriterionOperator.NONE_OF) {
            result = compareNoneOfMimeType(formatIdentifications, criterionValue);
        }
        return result;
    }

    private boolean compareNoneOfMimeType(List<Format> formatIdentifications, Object criterionValue) {
        boolean result = false;
        Object[] values = (Object[]) criterionValue;
        boolean foundValue = false;
        SEARCH: for (Format format : formatIdentifications) {
            String mimeType = format.getMimeType();
            for (Object value : values) {
                if (mimeType.equals(value)) {
                    foundValue = true;
                    break SEARCH;
                }
            }
        }
        if (!foundValue) {
            result = true;
        }
        return result;
    }

    private boolean compareAnyOfMimeType(List<Format> formatIdentifications, Object criterionValue) {
        boolean result = false;
        Object[] values = (Object[]) criterionValue;
        SEARCH: for (Format format : formatIdentifications) {
            String mimeType = format.getMimeType();
            for (Object value : values) {
                if (mimeType.equals(value)) {
                    result = true;
                    break SEARCH;
                }
            }
        }
        return result;
    }

    private boolean compareFormats(List<Format> formatIdentifications, CriterionOperator operator, Object criterionValue) {
        boolean result = false;
        String value = ((String) criterionValue).toLowerCase(Locale.ROOT); //TODO: bug - inverted operators have different criteria for lists.
        if (isOperatorInverted(operator)) {
            int foundCount = 0;
            for (Format format : formatIdentifications) {
                String formatName = format.getName().toLowerCase(Locale.ROOT);
                if (compareStrings(formatName, operator, value)) {
                    foundCount++;
                }
            }
            result = foundCount == formatIdentifications.size();
        } else {
            for (Format format : formatIdentifications) {
                String formatName = format.getName().toLowerCase(Locale.ROOT);
                if (compareStrings(formatName, operator, value)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    private boolean comparePuids(List<Format> formatIdentifications, CriterionOperator operator, Object criterionValue) {
        boolean result = false;
        if (operator == CriterionOperator.ANY_OF) {
            result = compareAnyOfPuids(formatIdentifications, criterionValue);
        } else if (operator == CriterionOperator.NONE_OF) {
            result = compareNoneOfPuids(formatIdentifications, criterionValue);
        }
        return result;
    }

    //TODO: should PUID values be case sensitive?
    private boolean compareNoneOfPuids(List<Format> formatIdentifications, Object criterionValue) {
        boolean result = false;
        Object[] values = (Object[]) criterionValue;
        boolean foundValue = false;
        SEARCH: for (Format format : formatIdentifications) {
            String puid = format.getPuid();
            for (Object value : values) {
                if (puid.equals(value)) {
                    foundValue = true;
                    break SEARCH;
                }
            }
        }
        if (!foundValue) {
            result = true;
        }
        return result;
    }

    //TODO: should PUID values be case sensitive?
    private boolean compareAnyOfPuids(List<Format> formatIdentifications, Object criterionValue) {
        boolean result = false;
        Object[] values = (Object[]) criterionValue;
        SEARCH: for (Format format : formatIdentifications) {
            String puid = format.getPuid();
            for (Object value : values) {
                if (puid.equals(value)) {
                    result = true;
                    break SEARCH;
                }
            }
        }
        return result;
    }

    private boolean compareBooleans(Boolean nodeValue, CriterionOperator operator, Object criterionValue) {
        boolean result = false;
        if (operator == CriterionOperator.EQ) {
            result = nodeValue.equals(criterionValue);
        } else if (operator == CriterionOperator.NE) {
            result = !nodeValue.equals(criterionValue);
        }
        return result;
    }

    private boolean compareIntegers(int nodeValue, CriterionOperator operator, Object criterionValue) {
        boolean result = false;
        int compareValue = (Integer) criterionValue;
        switch (operator) {
            case LT: {
                result = nodeValue < compareValue;
                break;
            }
            case LTE: {
                result = nodeValue <= compareValue;
                break;
            }
            case EQ: {
                result = nodeValue == compareValue;
                break;
            }
            case NE: {
                result = nodeValue != compareValue;
                break;
            }
            case GT: {
                result = nodeValue > compareValue;
                break;
            }
            case GTE: {
                result = nodeValue >= compareValue;
                break;
            }
        }
        return result;
    }

    private boolean compareEnums(Enum<?> nodeValue, CriterionOperator operator, Object criterionValue) {
        boolean result = false;
        Object[] compareValues = (Object[]) criterionValue;
        if (operator == CriterionOperator.ANY_OF) {
            for (Object compareValue : compareValues) {
                if (nodeValue.equals(compareValue)) {
                    result = true;
                    break;
                }
            }
        } else if (operator == CriterionOperator.NONE_OF) {
            boolean foundValue = false;
            for (Object compareValue : compareValues) {
                if (nodeValue.equals(compareValue)) {
                    foundValue = true;
                    break;
                }
            }
            if (!foundValue) {
                result = true;
            }
        }
        return result;
    }

    private boolean compareDates(Date nodeValue, CriterionOperator operator, Object criterionValue) {
        boolean result = false;
        Date compareValue = (Date) criterionValue;
        switch (operator) {
            case LT: {
                result = nodeValue.before(compareValue);
                break;
            }
            case LTE: {
                result = nodeValue.before(compareValue) || nodeValue.equals(compareValue);
                break;
            }
            case EQ: {
                result = nodeValue.equals(compareValue);
                break;
            }
            case NE: {
                result = !nodeValue.equals(compareValue);
                break;
            }
            case GT: {
                result = nodeValue.after(compareValue);
                break;
            }
            case GTE: {
                result = nodeValue.after(compareValue) || nodeValue.equals(compareValue);
                break;
            }
        }
        return result;
    }

    private boolean compareLongs(long nodeValue, CriterionOperator operator, Object criterionValue) {
        boolean result = false;
        long compareValue = (Long) criterionValue;
        switch (operator) {
            case LT: {
                result = nodeValue < compareValue;
                break;
            }
            case LTE: {
                result = nodeValue <= compareValue;
                break;
            }
            case EQ: {
                result = nodeValue == compareValue;
                break;
            }
            case NE: {
                result = nodeValue != compareValue;
                break;
            }
            case GT: {
                result = nodeValue > compareValue;
                break;
            }
            case GTE: {
                result = nodeValue >= compareValue;
                break;
            }
        }
        return result;
    }

    private boolean compareStrings(String nodeValue, CriterionOperator operator, Object criterionValue) {
        boolean result = false;
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
            result = isOperatorInverted(operator)? foundCount == values.length : foundCount > 0;
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
            result = isOperatorInverted(operator)? foundCount == values.length : foundCount > 0;
        }
        return result;
    }

    private boolean compareString(String nodeValue, CriterionOperator operator, String compareValue) {
        boolean result = false;
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
        return result;
    }

}
