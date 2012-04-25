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

/**
 * @author Alok Kumar Dash Interface mandates required methods for reported
 *         field type
 */
public interface ReportFieldType {

    /**
     * Depending upon the type of the reported field i.e. Numeric, Date or
     * String/Set the aggregate function is applied to the select part of the
     * query string.
     * 
    * @param groupByFields
     *            A list of fields and functions to group by.
     * @return String The select query string (only the selecting fields part).
     */
    String getSelectFieldString(List<GroupByField> groupByFields);


    /**
     * This method populates report data.
     * 
     * @param results
     *            result set from database.
     * @return ReportData
     */

    List<ReportLineItem> populateReportedData(List<?> results);

    /**
     * Returns field string to be used in the query. 
     * @return String the persistence field name 
     */
    String getField();

}
