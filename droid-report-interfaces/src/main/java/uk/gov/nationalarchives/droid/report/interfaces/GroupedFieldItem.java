/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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
