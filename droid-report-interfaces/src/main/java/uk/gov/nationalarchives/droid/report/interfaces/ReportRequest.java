/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.report.interfaces;

import java.util.List;

import uk.gov.nationalarchives.droid.core.interfaces.filter.Filter;

/**
 * Encapsulates a reuest for a report.
 * @author rflitcroft
 *
 */
public class ReportRequest {

    private ReportSpec reportSpec;
    private Filter filter;
    private List<String> profileIds;
    
    /**
     * @return the filter
     */
    public Filter getFilter() {
        return filter;
    }

    /**
     * @param filter the filter to set
     */
    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    /**
     * @return the profileIds
     */
    public List<String> getProfileIds() {
        return profileIds;
    }

    /**
     * @param profileIds the profileIds to set
     */
    public void setProfileIds(List<String> profileIds) {
        this.profileIds = profileIds;
    }
    
    /**
     * @param reportSpec the reportSpec to set
     */
    public void setReportSpec(ReportSpec reportSpec) {
        this.reportSpec = reportSpec;
    }
    
    /**
     * @return the reportSpec
     */
    public ReportSpec getReportSpec() {
        return reportSpec;
    }
    
    
}
