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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Alok Kumar Dash
 * 
 */
public class NumericFieldType implements ReportFieldType {

    private static final int COUNT_INDEX = 0;
    private static final int SUM_INDEX = 1;
    private static final int AVG_INDEX = 2;
    private static final int MIN_INDEX = 3;
    private static final int MAX_INDEX = 4;
    private static final int GROUP_INDEX = 5;
    
    private String field;

    private boolean isGroupByExists;
    private List<GroupByField> groupingFields;
    
    /**
     * @param field
     *            The field name.
     */
    public NumericFieldType(String field) {
        this.field = field;

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getSelectFieldString(List<GroupByField> groupByFields) {
        StringBuilder selectFieldQueryString = new StringBuilder();
        selectFieldQueryString.append("count(" + field + "), sum(" + field + "), avg(" + field + "), min(" + field
                + "), max(" + field + ") ");
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
            Object[] resultsArray = (Object[]) res;

            Object count   = resultsArray[COUNT_INDEX];
            Object sum     = resultsArray[SUM_INDEX];
            Object average = resultsArray[AVG_INDEX];
            Object minimum = resultsArray[MIN_INDEX];
            Object maximum = resultsArray[MAX_INDEX];
            
            if (count != null)   { reportLineItem.setCount(new Long((Integer) count)); }
            if (sum != null)     { reportLineItem.setSum(((BigInteger) sum).longValue()); }
            if (average != null) { reportLineItem.setAverage(((BigInteger) average).doubleValue()); }
            if (minimum != null) { reportLineItem.setMinimum(((BigInteger) minimum).longValue()); }
            if (maximum != null) { reportLineItem.setMaximum(((BigInteger) maximum).longValue()); }
            
            if (isGroupByExists) {
                List<String> values = new ArrayList<String>();
                for (int valueIndex = 0; valueIndex < groupingFields.size(); valueIndex++) {
                    values.add(getFieldValue(resultsArray[GROUP_INDEX + valueIndex]));
                }
                reportLineItem.setGroupByValues(values);
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
