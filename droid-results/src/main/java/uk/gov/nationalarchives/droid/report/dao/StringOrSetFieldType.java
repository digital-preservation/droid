/**
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
package uk.gov.nationalarchives.droid.report.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Alok Kumar Dash
 * 
 */
public class StringOrSetFieldType implements ReportFieldType {

    private static final int COUNT_INDEX = 0;
    private static final int GROUP_INDEX = 1;
     
    private String field;

    private boolean isGroupByExists;
    private List<GroupByField> groupingFields;

    /**
     * @param field
     *            The field name.
     */
    public StringOrSetFieldType(String field) {
        this.field = field;

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getSelectFieldString(List<GroupByField> groupByFields) {
        StringBuilder selectFieldQueryString = new StringBuilder();
        selectFieldQueryString.append("count(" + field + ") ");
        groupingFields = groupByFields;
        isGroupByExists = groupByFields != null && groupByFields.size() > 0;
        if (isGroupByExists) {
            for (GroupByField group : groupingFields) {
                selectFieldQueryString.append(" , ");
                String function = group.getFunction();
                if (function != null && !function.isEmpty()) {
                    selectFieldQueryString.append(function + "(");
                }
                String groupByFieldName = group.getGroupByField().getType().getField();
                selectFieldQueryString.append(groupByFieldName);
                
                if (function != null && !function.isEmpty()) {
                    selectFieldQueryString.append(")");
                }
            }
        }
        return selectFieldQueryString.toString();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<ReportLineItem> populateReportedData(List<?> results) {

        List<ReportLineItem> reportData = new ArrayList<ReportLineItem>();

        ReportLineItem reportLineItem = null;

        for (Object res : results) {
            reportLineItem = new ReportLineItem();
            if (isGroupByExists) {
                Object[] resultsArray = (Object[]) res;
                Object count = resultsArray[COUNT_INDEX];
                if (count != null) {
                    Long value = new Long((Integer) count);
                    reportLineItem.setCount(value);
                }
               
                List<String> values = new ArrayList<String>();
                for (int valueIndex = 0; valueIndex < groupingFields.size(); valueIndex++) {
                    values.add(getFieldValue(resultsArray[GROUP_INDEX + valueIndex]));
                }
                reportLineItem.setGroupByValues(values);
 
            } else {
                if (res != null) {
                    Long value = new Long((Integer) res);
                    reportLineItem.setCount(value);
                }
            }

            reportData.add(reportLineItem);
        }
        return reportData;
    }


    /**
     * Returns a list of ReportLineItems from a ResultSet.
     * @param results ResultSet The result set from which to extract ReportLineItems
     * @return A list of ReportLineItems extracted from the ResultSet
     * @throws SQLException SQL Exception
     */
    public List<ReportLineItem> populateReportedData(ResultSet results) throws SQLException {

        List<ReportLineItem> reportData = new ArrayList<ReportLineItem>();

        ReportLineItem reportLineItem = null;

        while (results.next()) {
            reportLineItem = new ReportLineItem();
            if (isGroupByExists) {

                int numberOfColumns = results.getMetaData().getColumnCount();
                Object[] resultsArray =  new Object[numberOfColumns];

                for (int i = 0; i < numberOfColumns; i++) {
                    resultsArray[i] = results.getObject(i + 1);
                }

                //Object[] resultsArray = (Object[]) res;
                Object count = resultsArray[COUNT_INDEX];
                if (count != null) {
                    Long value = new Long((Integer) count);
                    reportLineItem.setCount(value);
                }

                List<String> values = new ArrayList<String>();
                for (int valueIndex = 0; valueIndex < groupingFields.size(); valueIndex++) {
                    values.add(getFieldValue(resultsArray[GROUP_INDEX + valueIndex]));
                }
                reportLineItem.setGroupByValues(values);

            } else {
                //BNO:  any reason we can't just retrieve directly as long - if the cast will always work?
                Object res = results.getObject(1);
                if (res != null) {
                    Long value = new Long((Integer) res);
                    reportLineItem.setCount(value);
                }
            }

            reportData.add(reportLineItem);
        }
        return reportData;
    }


    private String getFieldValue(Object value) {
        return (value == null) ? "" : value.toString();
    }
    
    /**
     * @return the field
     */
    public String getField() {
        return field;
    }

}
