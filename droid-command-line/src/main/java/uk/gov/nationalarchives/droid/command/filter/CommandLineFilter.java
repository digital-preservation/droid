/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.command.filter;

/**
 * Filter information for the command line.
 * @author rflitcroft
 *
 */
public class CommandLineFilter {

    private String[] filters;
    private FilterType filterType;
    
    /**
     * Constructs a new command line filter.
     * @param filters the filters to use
     * @param filterType the type of filter.
     */
    public CommandLineFilter(String[] filters, FilterType filterType) {
        this.filters = filters;
        this.filterType = filterType;
    }
    
    /**
     * @return the filters
     */
    public String[] getFilters() {
        return filters;
    }
    
    /**
     * @return the filterType
     */
    public FilterType getFilterType() {
        return filterType;
    }
    
    /**
     * Filter types.
     * @author rflitcroft
     */
    public static enum FilterType {
        
        /** Logical OR of all filters. */
        ANY,
        
        /** Logical AND of all filters. */
        ALL;
    }
}
