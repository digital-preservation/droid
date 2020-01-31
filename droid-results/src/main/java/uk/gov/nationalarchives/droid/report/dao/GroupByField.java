/**
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gov.uk>
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
     * Empty bean constructor.
     */
    public GroupByField() {
    }

    /**
     * Parameterized constructor.
     * @param field The field
     * @param function The function.
     */
    public GroupByField(ReportFieldEnum field, String function) {
        setGroupByField(field);
        setFunction(function);
    }

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
