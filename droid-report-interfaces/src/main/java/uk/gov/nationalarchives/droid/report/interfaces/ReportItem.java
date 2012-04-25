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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

/**
 * @author rflitcroft
 *
 */
public class ReportItem implements Aggregator {

    @XmlElement(name = "Specification")
    private ReportSpecItem reportSpecItem;
    
    @XmlElementWrapper(name = "Groups")
    @XmlElement(name = "Group")
    private List<GroupedFieldItem> groupFields = new ArrayList<GroupedFieldItem>();
    
    @XmlElement(name = "ReportItemAggregateSummary")
    private ReportData aggregateSummary = new ReportData();
    
    private Set<ReportData> aggregatedData = new HashSet<ReportData>();
    
    
    /**
     * @param reportSpecItem the reportSpec to set
     */
    public void setReportSpecItem(ReportSpecItem reportSpecItem) {
        this.reportSpecItem = reportSpecItem;
    }
    
    /**
     * Adds a grouped field item.
     * @param groupedFieldItem the grouped field item to add
     */
    public void addGroupedFieldItem(GroupedFieldItem groupedFieldItem) {
        groupFields.add(groupedFieldItem);
        groupedFieldItem.addAggregator(this);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void aggregate(ReportData reportData) {
        if (aggregatedData.add(reportData)) {
            aggregateSummary.addData(reportData);
        }
    }
    
}
