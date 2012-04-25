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
import java.util.Date;
import java.util.List;

/**
 * @author Alok Kumar Dash
 */
public class ReportLineItem {

    //private String groupByValue;
    private List<String> groupByValues = new ArrayList<String>();
    
    /** Total count of items. */
    private Long count;

    /** Sum */
    private Long sum;

    /** Minimum value. */
    private Long minimum;

    /** Maximum value. */
    private Long maximum;

    /** Average value. */
    private Double average;
    
    /** Earliest Date. */
    private Date earliestDate;
    
    /** Latest Date. */
    private Date latestDate;

    
    /**
     * @return the count
     */
    public Long getCount() {
        return count;
    }

    /**
     * @param count
     *            the count to set
     */
    public void setCount(Long count) {
        this.count = count;
    }

    /**
     * @return the sum
     */
    public Long getSum() {
        return sum;
    }

    /**
     * @param sum
     *            the sum to set
     */
    public void setSum(Long sum) {
        this.sum = sum;
    }

    /**
     * @return the minimum
     */
    public Long getMinimum() {
        return minimum;
    }

    /**
     * @param minimum
     *            the minimum to set
     */
    public void setMinimum(Long minimum) {
        this.minimum = minimum;
    }

    /**
     * @return the maximum
     */
    public Long getMaximum() {
        return maximum;
    }

    /**
     * @param maximum
     *            the maximum to set
     */
    public void setMaximum(Long maximum) {
        this.maximum = maximum;
    }

    /**
     * @return the average
     */
    public Double getAverage() {
        return average;
    }

    /**
     * @param average
     *            the average to set
     */
    public void setAverage(Double average) {
        this.average = average;
    }

    /**
     * @return the earliestDate
     */
    public Date getEarliestDate() {
        return earliestDate;
    }

    /**
     * @param earliestDate the earliestDate to set
     */
    public void setEarliestDate(Date earliestDate) {
        this.earliestDate = earliestDate;
    }

    /**
     * @return the latestDate
     */
    public Date getLatestDate() {
        return latestDate;
    }

    /**
     * @param latestDate the latestDate to set
     */
    public void setLatestDate(Date latestDate) {
        this.latestDate = latestDate;
    }

    /**
     * @return the groupByValue
     */
//    public String getGroupByValue() {
//        return groupByValue;
//    }

    /**
     * @param groupByValue the groupByValue to set
     */
//    public void setGroupByValue(String groupByValue) {
//        this.groupByValue = groupByValue;
//    }

    /**
     * @return a list of grouping values
     */
    public List<String> getGroupByValues() {
        return groupByValues;
    }
    
    /**
     * 
     * @param values A list of values the item was grouped on.
     */
    public void setGroupByValues(List<String> values) {
        this.groupByValues.clear();
        this.groupByValues.addAll(values);
    }
    
}
