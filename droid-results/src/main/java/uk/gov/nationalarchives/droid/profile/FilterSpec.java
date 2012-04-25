/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.profile;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author a-mpalmer
 *
 */

@XmlRootElement(name = "FilterSpec")
@XmlAccessorType(XmlAccessType.FIELD)
public class FilterSpec {
    
    @XmlElement(name = "Filter")
    private FilterImpl filter;

    /**
     * No argument constructor for filter spec.
     */
    public FilterSpec() {
    }
    
    /**
     * 
     * @param filter The filter to construct a filter spec object with.
     */
    public FilterSpec(FilterImpl filter) {
        this.filter = filter;
    }
    
    /**
     * 
     * @return The filter wrapped in the filter spec object.
     */
    public FilterImpl getFilter() {
        return filter;
    }
    
    /**
     * 
     * @param filter The filter to wrap in the filter spec object.
     */
    public void setFilter(FilterImpl filter) {
        this.filter = filter;
    }
}
