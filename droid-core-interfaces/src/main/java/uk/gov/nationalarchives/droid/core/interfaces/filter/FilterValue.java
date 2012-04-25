/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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
