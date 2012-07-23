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
package uk.gov.nationalarchives.droid.core.interfaces.filter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;


/** 
 * This class encapsulates each item of reference data. For example  
 * If PUIDs is selected in filter criteria, each PUID is represented as an object 
 * of Values and List of values are passed in to ValuesDialog to display list of 
 * available PUIDs for selection.
 * None of the fields or properties is bound to XML unless they are specifically 
 * annotated with some of the JAXB annotations. 
 */
@XmlAccessorType(XmlAccessType.NONE)
public class FilterValue {

    @XmlElement(name = "Id", required = true)
    private int id;

    @XmlElement(name = "Description", required = true)
    private String description;

    @XmlElement(name = "Value", required = true)
    private String queryParameter;

    /**
     * Constructor to creates values.
     * 
     * @param id
     *            Id of the values.
     * @param description
     *            Description of the values.
     * @param queryParameter
     *            query parameter.
     */
    public FilterValue(int id, String description, String queryParameter) {
        this.id = id;
        this.description = description;
        this.queryParameter = queryParameter;
    }

    /** Empty constructor. */
    public FilterValue() {
    }

    /**
     * @return Query parameter to generate query String.
     */
    public String getQueryParameter() {
        return queryParameter;
    }

    /**
     * @param queryParameter
     *            to set query parameter.
     */
    public void setQueryParameter(String queryParameter) {
        this.queryParameter = queryParameter;
    }

    /** Getter method for id. @return id Id of the reference data.*/
    public int getId() {
        return id;
    }

    /** Setter method for id. @param id Id of the reference data. */
    public void setId(int id) {
        this.id = id;
    }

    /** Getter method for description. @return description  */
    public String getDescription() {
        return description;
    }

    /** Setter method for description. @param description Description of the reference data.*/
    public void setDescription(String description) {
        this.description = description;
    }

    /** Overridden toString method. @return toString representation of Values (Reference Data)*/
    @Override
    public String toString() {
        return description;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(queryParameter).toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        FilterValue other = (FilterValue) obj;
        return new EqualsBuilder().append(queryParameter, other.queryParameter).isEquals();
    }
    
}
