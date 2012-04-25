/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.report.interfaces;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author rflitcroft
 *
 */
public class ReportData {

    @XmlElement(name = "Count")
    private long count;

    @XmlElement(name = "Sum")
    private Long sum;
    
    @XmlElement(name = "Min")
    private Long min;
    
    @XmlElement(name = "Max")
    private Long max;
    
    @XmlElement(name = "Average")
    private Long roundedAverage;
    
    @XmlTransient
    private Double average;
    
    /**
     * @param average the average to set
     */
    public void setAverage(Double average) {
        this.average = average;
        updateRoundedAverage();
    }
    
    /**
     * @param count the count to set
     */
    public void setCount(Long count) {
        this.count = count;
    }
    
    /**
     * @param max the max to set
     */
    public void setMax(Long max) {
        this.max = max;
    }
    
    /**
     * @param min the min to set
     */
    public void setMin(Long min) {
        this.min = min;
    }
    
    /**
     * @param sum the sum to set
     */
    public void setSum(Long sum) {
        this.sum = sum;
    }
    
    private void updateMinAndMax(ReportData reportData) {
        if (reportData.min != null) {
            if (min == null || reportData.min < min) {
                min = reportData.min;
            }
        }
        
        if (reportData.max != null) {
            if (max == null || reportData.max > max) {
                max = reportData.max;
            }
        }
    }
    
    private void updateRoundedAverage() {
        roundedAverage = average == null ? null : Math.round(average);
    }
    
    /**
     * Adds/aggregates report data.
     * @param reportData the report data to aggregate.
     */
    public void addData(ReportData reportData) {
        
        updateMinAndMax(reportData);
        double thisAverage = average == null ? 0 : average;
        
        if (reportData.average != null) {
            double temp = (thisAverage * count) + (reportData.average * reportData.count);
            count += reportData.count;
            average = temp / count;
            updateRoundedAverage();
        } else {
            count += reportData.count;
        }

        if (reportData.sum != null) {
            if (sum == null) {
                sum = reportData.sum;
            } else {
                sum += reportData.sum;
            }
        }
    }

}
