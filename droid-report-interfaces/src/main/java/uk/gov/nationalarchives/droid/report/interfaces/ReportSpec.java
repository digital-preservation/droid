/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.report.interfaces;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author rflitcroft
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "ReportSpec")
public class ReportSpec {

    @XmlElement(name = "Name")
    private String name;
    
    @XmlElementWrapper(name = "Items")
    @XmlElement(name = "Item")
    private List<ReportSpecItem> items = new ArrayList<ReportSpecItem>();
    
    
    @XmlTransient
    private List<File> xslTransforms;
    /**
     * @return the items
     */
    public List<ReportSpecItem> getItems() {
        return items;
    }
    
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * @param xslTransforms a list of xsl files which can transform the output of this report spec.
     */
    public void setXslTransforms(List<File> xslTransforms) {
        this.xslTransforms = xslTransforms;
    }
    
    /**
     * 
     * @return a list of xsl files which can transform the output of this report spec.
     */
    public List<File> getXslTransforms() {
        return xslTransforms;
    }
    
    
}
