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
