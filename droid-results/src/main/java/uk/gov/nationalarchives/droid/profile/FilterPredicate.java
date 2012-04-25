/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.profile;

import org.apache.commons.collections.Predicate;

/**
 * @author Alok Kumar Dash.
 * 
 */
public class FilterPredicate implements Predicate {

    private FilterImpl filter;

    /**
     * COnstructor
     * 
     * @param filter
     *            Selected Filter.
     */
    FilterPredicate(FilterImpl filter) {
        this.filter = filter;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean evaluate(Object arg0) {

        /*
         * ProfileResourceNode primordialNode = (ProfileResourceNode) arg0;
         * 
         * boolean narrowed = filter.isNarrowed(); boolean enabled =
         * filter.isEnabled(); List<FilterCriteria> filterCrateriaList =
         * filter.getCriteria(); if (filterCrateriaList != null) { if
         * (filter.getCriteria().size() > 0 && enabled) { for (FilterCriteria
         * filterCriteria : filter.getCriteria()) { if
         * ("File name".equals(filterCriteria.getMetadataName())) {
         * 
         * } if ("File size".equals(filterCriteria.getMetadataName())) {
         * 
         * } if ("LastModifiedDate".equals(filterCriteria.getMetadataName())) {
         * }
         * 
         * if ("File extension".equals(filterCriteria.getMetadataName())) { }
         * 
         * } } }
         */
        return true;
    }
}
