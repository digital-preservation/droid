/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.report.dao;

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
