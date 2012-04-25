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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import uk.gov.nationalarchives.droid.profile.FilterImpl;
import uk.gov.nationalarchives.droid.report.dao.GroupByField;
import uk.gov.nationalarchives.droid.report.dao.ReportFieldEnum;

/**
 * @author rflitcroft
 *
 */
@XmlAccessorType(XmlAccessType.NONE)
public class ReportSpecItem {

    @XmlElement(name = "Description", required = true)
    private String description;
    
    @XmlElement(name = "Field", required = true)
    private ReportFieldEnum field;
    
    /*
    @XmlElement(name = "GroupByField")
    private ReportFieldEnum groupByField;
    
    @XmlElement(name = "Function")
    private String function;
    */
    
    @XmlElementWrapper(name = "GroupByFields")
    @XmlElement(name = "GroupByField")
    private List<GroupByField> groupByFields = new ArrayList<GroupByField>();
    
    
    @XmlElement(name = "Filter")
    private FilterImpl filter;
    
    /**
     * @param field the fieldName to set
     */
    public void setField(ReportFieldEnum field) {
        this.field = field;
    }
    
    /**
     * @param groupByField the groupByField to set
     */
//    public void setGroupByField(ReportFieldEnum groupByField) {
//        this.groupByField = groupByField;
//    }
    
    /**
     * @param function the function to set
     */
//    public void setFunction(String function) {
//        this.function = function;
//    }

    
    /**
     * @return the fieldName
     */
    public ReportFieldEnum getField() {
        return field;
    }
    
    /**
     * @return the function
     */
//    public String getFunction() {
//        return function;
//    }
    
    /**
     * @return the groupByFieldName
     */
//    public ReportFieldEnum getGroupByField() {
//        return groupByField;
//    }
  
    /**
     * @return the list of groupByFields
     */
    public List<GroupByField> getGroupByFields() {
        return groupByFields;
    }
    
    /**
     * 
     * @param groupByFields A list of grouping field definitions.
     */
    public void setGroupByFields(List<GroupByField> groupByFields) {
        this.groupByFields = groupByFields;
    }
    
    /**
     * @return the filter
     */
    public FilterImpl getFilter() {
        return filter;
    }
    
    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
}
