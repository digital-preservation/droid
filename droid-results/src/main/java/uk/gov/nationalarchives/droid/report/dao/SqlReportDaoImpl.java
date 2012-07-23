/**
 * Copyright (c) 2012, The National Archives <pronom@nationalarchives.gsi.gov.uk>
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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import uk.gov.nationalarchives.droid.core.interfaces.filter.expressions.Criterion;
import uk.gov.nationalarchives.droid.core.interfaces.filter.expressions.QueryBuilder;
import uk.gov.nationalarchives.droid.profile.SqlUtils;

/**
 * JPA implementation of JpaPlanetsXMLDaoImpl.
 * 
 * @author Alok Kumar Dash.
 */
public class SqlReportDaoImpl implements ReportDao {

    private static String formatfilter = "formatfilter";
    
    private final Log log = LogFactory.getLog(getClass());

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Flushes the DROID entity manager.
     */
    void flush() {
        entityManager.flush();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public List<ReportLineItem> getReportData(Criterion filter, ReportFieldEnum reportField) {
        return getReportData(filter, reportField, new ArrayList<GroupByField>());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public List<ReportLineItem> getReportData(Criterion filter, ReportFieldEnum reportField, 
            List<GroupByField> groupByFields) {
        final Query query = getQuery(reportField, groupByFields, filter);
        final List<?> results = query.getResultList();
        List<ReportLineItem> reportData = new ArrayList<ReportLineItem>();
        if (results != null && !results.isEmpty()) {
            reportData = reportField.getType().populateReportedData(results);
        }
        return reportData;        
    }
    
    
    private Query getQuery(ReportFieldEnum reportField, List<GroupByField> groupByFields, Criterion filter) {
        final String selectStatement = getSelectStatement(reportField, groupByFields);
        final FilterInfo filterInfo = getFilterInfo(filter);
        final String groupingStatement = getGroupingStatement(groupByFields);
        final String queryString = selectStatement + filterInfo.getFilterSubQuery() + groupingStatement;
        final Query query = entityManager.createNativeQuery(queryString);
        setFilterParameters(query, filterInfo.getFilterValues());
        return query;
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
    
    
    private boolean filterOnFormatMetadata(final String queryString) {
        return queryString.contains("formatfilter.name") || queryString.contains("formatfilter.mime_type");
    }
    
    
    private boolean groupOnPUID(ReportFieldEnum groupByField) {
        return groupByField.equals(ReportFieldEnum.PUID);
    }
    
    
    private boolean groupOnFormatMetadata(ReportFieldEnum groupByField) {
        return groupByField.equals(ReportFieldEnum.FILE_FORMAT)
                || groupByField.equals(ReportFieldEnum.MIME_TYPE);
    }
    
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
    
    
    private void setFilterParameters(Query q, Object[] filterParams) {
        int pos = 1;
        for (Object param : filterParams) {
            Object transformedValue = SqlUtils.transformParameterToSQLValue(param);
            q.setParameter(pos++, transformedValue);
        }
    }       
    
}
