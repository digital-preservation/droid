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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

/**
 * @author rflitcroft
 *
 */
public class GroupedFieldItem {

    
    //@XmlElement(name = "Value")
    //private String value;

    @XmlElementWrapper(name = "Values")
    @XmlElement(name = "Value")
    private List<String> values = new ArrayList<String>();

    
    @XmlElementWrapper(name = "ProfileSummaries")
    @XmlElement(name = "ProfileSummary")
    private List<ProfileReportData> profileSummaries = new ArrayList<ProfileReportData>();
    
    @XmlElement(name = "GroupAggregateSummary")
    private ReportData groupAggregateData = new ReportData();
    
    private Aggregator aggregator;
    
    /**
     * @param value the value to set
     */
//    public void setValue(String value) {
//        this.value = value;
//    }
    
    /**
     * @param values list of values to set.
     */
    public void setValues(List<String> values) {
        this.values.clear();
        this.values.addAll(values);
    }
    
    /**
     * Adds profile data to a grouped field item.
     * @param profileData the profile data to add
     */
    public void addProfileData(ProfileReportData profileData) {
        profileSummaries.add(profileData);
        groupAggregateData.addData(profileData);

        if (aggregator != null) {
            for (ReportData reportData : profileSummaries) {
                aggregator.aggregate(reportData);
            }
        }
    }
    
    /**
     * @param newAggregator the aggregator to add.
     */
    public void addAggregator(Aggregator newAggregator) {
        this.aggregator = newAggregator;
        
        for (ReportData reportData : profileSummaries) {
            aggregator.aggregate(reportData);
        }
    }
}
