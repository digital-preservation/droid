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
