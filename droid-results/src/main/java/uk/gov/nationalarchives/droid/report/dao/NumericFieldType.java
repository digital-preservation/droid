/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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
