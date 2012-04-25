/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.report.dao;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;


/**
 * @author a-mpalmer
 *
 */
@XmlAccessorType(XmlAccessType.NONE)
public class GroupByField {

    @XmlElement(name = "Field")
    private ReportFieldEnum field;
    
    @XmlElement(name = "Function")
    private String function;
    
    /**
     * 
     * @return The grouping field
     */
    public ReportFieldEnum getGroupByField() {
        return field;
    }
    
    /**
     * 
     * @return The grouping function
     */
    public String getFunction() {
        return function;
    }
    
    /**
     * 
     * @param groupByField The field to group by
     */
    public void setGroupByField(ReportFieldEnum groupByField) {
        this.field = groupByField;
    }
    
    /**
     * 
     * @param function The function to apply to the grouping field
     */
    public void setFunction(String function) {
        this.function = function;
    }
}
