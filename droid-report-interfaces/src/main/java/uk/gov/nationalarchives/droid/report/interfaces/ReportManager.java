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
import uk.gov.nationalarchives.droid.results.handlers.ProgressObserver;

/**
 * @author Alok Kumar Dash
 */
public interface ReportManager {
    /**
     * Generates planet xml and writes to path provided.
     * 
     * @param profileId
     *            Profile Id.
     * @param nameAndPathOfTheFile
     *            Path and name of the file.
     */
    void generatePlanetsXML(String profileId, String nameAndPathOfTheFile);

    /**
     * Generates planet xml and writes to path provided.
     * 
     * @return Map<ProfileInstance, PlanetsXMLData> map of results.
     * @param profileIds
     *            Profile Ids.
     * @param reportField
     *            reported field.
     * @param groupByField
     *            groupByField.
     * @param function
     *            Function like month/ year for last modified date field.
     * @param filter
     *            Filter to be applied to reporting.
     *
    Map<ProfileInstance, ReportData> getReportData(List<String> profileIds, Filter filter, ReportFieldEnum reportField,
            ReportFieldEnum groupByField, String function);
    */

    /**
     * Sets Observer.
     * 
     * @param observer
     *            Observer for the progress bar.
     */

    void setObserver(ProgressObserver observer);

    /**
     * FIXME: the only reason we pass in an optional filter
     * is so we can override any filters present in the profiles
     * using a different implementation of filter than FilterImpl,
     * which was made part of the profile interface, instead of
     * conforming to a general filter interface.
     * 
     * Generates a report.
     * @param request the report request
     * @param optionalFilter a filter to pass in
     * @param progressObserver a progress observer
     * @return the report
     * @throws ReportCancelledException if the report was cancelled
     */
    Report generateReport(ReportRequest request, Filter optionalFilter, 
            CancellableProgressObserver progressObserver) 
        throws ReportCancelledException;
    
    /**
     * Lists all available report definitions.
     * @return all available report definitions
     */
    List<ReportSpec> listReportSpecs();

}
