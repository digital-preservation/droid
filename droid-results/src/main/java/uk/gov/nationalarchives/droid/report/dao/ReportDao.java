/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.report.dao;

import java.util.List;

import uk.gov.nationalarchives.droid.core.interfaces.filter.expressions.Criterion;

/**
 * Report DAO Interface.
 * @author Alok Kumar Dash
 */
public interface ReportDao {

    /**
     * Returns Data for reporting XML.
     * @param filter Filter to be applied.
     * @param reportField Reported field.
     * @param groupByFields
     *            A list of groupbyField objects having fields to group by and any associated grouping functions.
     * @return Data required for reports
     */
    List<ReportLineItem> getReportData(Criterion filter, ReportFieldEnum reportField, List<GroupByField> groupByFields);

    
    /**
     * Returns Data for reporting XML.
     * @param filter Filter to be applied.
     * @param reportField Reported field.
     * @return Data required for reports
     */
    List<ReportLineItem> getReportData(Criterion filter, ReportFieldEnum reportField);

    
}
