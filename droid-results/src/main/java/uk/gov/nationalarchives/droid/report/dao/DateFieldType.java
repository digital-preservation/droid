/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.report.dao;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Alok Kumar Dash
 */
public class DateFieldType implements ReportFieldType {

    private static final int COUNT_INDEX = 0;
    private static final int EARLIEST_INDEX = 1;
    private static final int LATEST_INDEX = 2;
    private static final int GROUP_INDEX = 3;
    
    private String field;

    private boolean isGroupByExists;
    private List<GroupByField> groupingFields;
    
    /**
     * @param field
     *            The field name.
     */
    public DateFieldType(String field) {
        this.field = field;

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getSelectFieldString(List<GroupByField> groupByFields) {

        StringBuilder selectFieldQueryString = new StringBuilder();

        selectFieldQueryString.append("count(" + field + "), min(" + field + "), max(" + field + ") ");

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

        List<ReportLineItem> lineItems = new ArrayList<ReportLineItem>();

        for (Object res : results) {
            ReportLineItem reportLineItem = new ReportLineItem();

            Object[] resultsArray = (Object[]) res;
            Object count = resultsArray[COUNT_INDEX];
            Object earlyTime = resultsArray[EARLIEST_INDEX]; 
            Object latestTime = resultsArray[LATEST_INDEX];

            if (count != null) {
                reportLineItem.setCount(new Long((Integer) count));
            }

            Timestamp timestamp = (Timestamp) earlyTime;
            if (timestamp != null) {
                reportLineItem.setEarliestDate(timestamp);
            }

            timestamp = (Timestamp) latestTime;
            if (timestamp != null) {
                reportLineItem.setLatestDate(timestamp);
            }

            if (isGroupByExists) {
                List<String> values = new ArrayList<String>();
                for (int valueIndex = 0; valueIndex < groupingFields.size(); valueIndex++) {
                    values.add(getFieldValue(resultsArray[GROUP_INDEX + valueIndex]));
                }
                reportLineItem.setGroupByValues(values);
            }

            lineItems.add(reportLineItem);
        }
        return lineItems;
    }

    /**
     * @return the field
     */
    public String getField() {
        return field;
    }
    
    private String getFieldValue(Object value) {
        return (value == null) ? "" : value.toString();
    }    
    

}
