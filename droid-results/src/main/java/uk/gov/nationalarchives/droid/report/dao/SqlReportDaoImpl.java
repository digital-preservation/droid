/**
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gsi.gov.uk>
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
package uk.gov.nationalarchives.droid.report.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.gov.nationalarchives.droid.core.interfaces.filter.expressions.Criterion;
import uk.gov.nationalarchives.droid.core.interfaces.filter.expressions.QueryBuilder;
import uk.gov.nationalarchives.droid.profile.SqlUtils;

/**
 * JDBC implementation of JpaPlanetsXMLDaoImpl.
 * 
 * @author Alok Kumar Dash. , Brian O'Reilly
 */
public class SqlReportDaoImpl implements ReportDao {

    private static String formatfilter = "formatfilter";
    
    private final Log log = LogFactory.getLog(getClass());
    private DataSource datasource;

    /**
     * Flushes the DROID entity manager.
     */
    void flush() {
       // entityManager.flush();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ReportLineItem> getReportData(Criterion filter, ReportFieldEnum reportField) {
        return getReportData(filter, reportField, new ArrayList<GroupByField>());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ReportLineItem> getReportData(Criterion filter, ReportFieldEnum reportField,
            List<GroupByField> groupByFields) {

        PreparedStatement statement = null;
        ResultSet resultset = null;
        Connection connection = null;

        final String sqlQuery = getQueryString(reportField, groupByFields, filter);
        try {
            connection = this.datasource.getConnection();
            statement = connection.prepareStatement(sqlQuery);
            setFilterParameters(statement, filter);
            resultset = statement.executeQuery();
            List<ReportLineItem> reportData = new ArrayList<ReportLineItem>();
            reportData = reportField.getType().populateReportedData(resultset);
            return reportData;
        } catch (SQLException ex) {
            log.error("Error executing report query", ex);
        } finally {
            try {
                if (resultset != null) {
                    resultset.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                //e.printStackTrace();
                log.error("Error closing statement or results set during report generation", e);
            }

        }
        return null;

    }


    private String getQueryString(ReportFieldEnum reportField, List<GroupByField> groupByFields, Criterion filter) {
        final String selectStatement = getSelectStatement(reportField, groupByFields);
        final FilterInfo filterInfo = getFilterInfo(filter);
        final String groupingStatement = getGroupingStatement(groupByFields);
        final String queryString = selectStatement + filterInfo.getFilterSubQuery() + groupingStatement;
        return queryString;
    }
    
    // Get select statement for aggregate queries on the report field, 
    // including any grouping field in the results.
    private String getSelectStatement(ReportFieldEnum reportField, List<GroupByField> groupByFields) {
        final ReportFieldType selectType = reportField.getType();
        return "SELECT "
            + selectType.getSelectFieldString(groupByFields)
            + " FROM profile_resource_node AS profile ";
    }

    
    private FilterInfo getFilterInfo(Criterion filter) {
        final FilterInfo filterInfo = new FilterInfo();
        final QueryBuilder queryBuilder = QueryBuilder.forAlias("profileResourceNode").createAlias("format");
        queryBuilder.add(filter);
        final String ejbQl = queryBuilder.toEjbQl();
        
        // If we have a filter, get its SQL query string and parameter values:
        if (ejbQl.length() > 0) {
            filterInfo.setFilterSubQuery(buildFilterSubQuery(ejbQl));
            filterInfo.setFilterValues(queryBuilder.getValues());
        }
        return filterInfo;
    }
    
    
    private String getGroupingStatement(List<GroupByField> groupByFields) {
        StringBuilder groupByFieldQuery = new StringBuilder();
        if (groupByFields != null && groupByFields.size() > 0) {
            
            // Determine if any fields are grouping on puid or format metadata
            boolean puidGroup = false;
            boolean formatMetadataGroup = false;
            for (GroupByField group : groupByFields) {
                final ReportFieldEnum groupField = group.getGroupByField();
                puidGroup = puidGroup | groupOnPUID(groupField);
                formatMetadataGroup = formatMetadataGroup | groupOnFormatMetadata(groupField);
            }
                
            // If any grouping fields are on the format metadata group, we have to join to all tables:
            if (formatMetadataGroup) {
                groupByFieldQuery.append(" INNER JOIN identification as ident on ident.node_id = profile.node_id " 
                    + " INNER JOIN format as format on format.puid = ident.puid ");
            } else if (puidGroup) { // if grouping on puids, join only to identification table:
                groupByFieldQuery.append(" INNER JOIN identification as format on format.node_id = profile.node_id "); 
            }
            
            // add group by clause:
            groupByFieldQuery.append(getGroupByClause(groupByFields));
        }
        return groupByFieldQuery.toString();
    }
    
    private String getGroupByClause(List<GroupByField> groupByFields) {
        StringBuilder groupByFieldQuery = new StringBuilder();
        groupByFieldQuery.append("GROUP BY ");
        String separator = "";
        for (GroupByField group : groupByFields) {
            final String groupFieldName = group.getGroupByField().getType().getField();
            final String groupFunction = group.getFunction();
            groupByFieldQuery.append(separator);
            if (groupFunction != null && !groupFunction.isEmpty()) {
                groupByFieldQuery.append(groupFunction + "(");
            }
            groupByFieldQuery.append(groupFieldName);
            if (groupFunction != null && !groupFunction.isEmpty()) {
                groupByFieldQuery.append(") ");
            }
            separator = ", ";
        }
        return groupByFieldQuery.toString();
    }
    
    private String buildFilterSubQuery(final String ejbQl) {
        final String filterSQL = SqlUtils.transformEJBtoSQLFields(ejbQl, "filter", formatfilter);
        String subQuery = " INNER JOIN (SELECT DISTINCT filter.node_id "
            + " FROM profile_resource_node AS filter ";

        // Only add joins to referenced tables, to increase performance:
        if (filterOnFormats(filterSQL)) {
            if (filterOnFormatMetadata(filterSQL)) { // filtering on format metadata, so have to join to format table:
                subQuery += " INNER JOIN identification as formatident ON formatident.node_id = filter.node_id "
                    + " INNER JOIN format as formatfilter ON formatfilter.puid = formatident.puid ";
            } else { // only filtering on the PUID, so only join to identification table.
                subQuery += " INNER JOIN identification as formatfilter ON formatfilter.node_id = filter.node_id ";
            }
        }
        subQuery += " WHERE " + filterSQL + ") AS filtered "
            + " ON filtered.node_id = profile.node_id ";
        return subQuery;
    }
    
    
    private boolean filterOnFormats(final String queryString) {
        return queryString.contains(formatfilter);
    }

    /**
     *
     * @param queryString
     * @return
     */
    private boolean filterOnFormatMetadata(final String queryString) {
        return queryString.contains("formatfilter.name") || queryString.contains("formatfilter.mime_type");
    }
    
    
    private boolean groupOnPUID(ReportFieldEnum groupByField) {
        return groupByField.equals(ReportFieldEnum.PUID);
    }

    /**
     * Checks whether the group by field is either mime type or file format.
     * @param groupByField ReportFieldEnum
     * @return Boolean indicating whether or not the group field is either mime type or file format
     */
    private boolean groupOnFormatMetadata(ReportFieldEnum groupByField) {
        return groupByField.equals(ReportFieldEnum.FILE_FORMAT)
                || groupByField.equals(ReportFieldEnum.MIME_TYPE);
    }

    /**
     * Private class to model the filter information
     */
    private class FilterInfo {
        private String filterSubQuery = "";
        private Object[] filterValues = new Object[0];
        
        public String getFilterSubQuery() {
            return filterSubQuery;
        }
        
        public Object[] getFilterValues() {
            return filterValues;
        }
        
        public void setFilterSubQuery(final String subQuery) {
            filterSubQuery = subQuery;
        }
        
        public void setFilterValues(final Object[] values) {
            filterValues = values;
        }
    }

    /**
     * Sets filter parameters within a PreparedStatement containing placeholders for the values.
     * @param s The PreparedStatement in which to set filter parameters.
     * @param filter a filter containing parameters to set.
     */
    private void setFilterParameters(PreparedStatement s, Criterion filter) {

        final FilterInfo filterInfo = getFilterInfo(filter);
        Object[] filterParams = filterInfo.getFilterValues();
        int pos = 0;
        for (Object param : filterParams) {
            Object transformedValue = SqlUtils.transformParameterToSQLValue(param);

            try {
                String className = transformedValue.getClass().getSimpleName();
                //Java 6 doesn't support switch on string!!
                switch (SqlUtils.ClassName.valueOf(className)) {
                    case String:
                        s.setString(++pos, (String) transformedValue);
                        break;
                    case Date:
                        java.util.Date d = (java.util.Date) transformedValue;
                        s.setDate(++pos, new java.sql.Date(d.getTime()));
                        break;
                    case Long:
                        s.setLong(++pos, (Long) transformedValue);
                        break;
                    case Integer:
                        s.setInt(++pos, (Integer) transformedValue);
                        break;
                    default:
                        log.error("Invalid filter parameter type in SQLReportDaoImpl.java");
                        break;
                }
            } catch (SQLException e) {
                //e.printStackTrace();
                log.error(e);
            }
        }
    }

    /**
     * Sets the datasource.
     * @param datasource datasource to set for the DAO
     */
    public void setDatasource(DataSource datasource) {
        this.datasource = datasource;
    }

    /**
     * Returns the datasource.
     * @return the datasource
     */
    public DataSource getDatasource() {
        return this.datasource;
    }
}
