/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.container;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

/**
 * @author rflitcroft
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ContainerSignature {

    @XmlAttribute(name = "Id")
    private int id;
    
    @XmlAttribute(name = "ContainerType")
    private String containerType;
    
    @XmlElement(name = "Description")
    private String description;
    
    @XmlElementWrapper(name = "Files")
    @XmlElement(name = "File")
    private List<ContainerFile> files;
    
    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * @return the id
     */
    public int getId() {
        return id;
    }
    
    /**
     * @return the files
     */
    public Map<String, ContainerFile> getFiles() {
        Map<String, ContainerFile> filesMap = new HashMap<String, ContainerFile>();
        
        for (ContainerFile f : files) {
            filesMap.put(f.getPath(), f);
        }
        
        return filesMap;
    }
    
    /**
     * 
     * @return a list of all the signature files
     */
    public List<ContainerFile> listFiles() {
        return files;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }
    
    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * @param files the files to set
     */
    public void setFiles(List<ContainerFile> files) {
        this.files = files;
    }
    
    /**
     * @return the containerType
     */
    public String getContainerType() {
        return containerType;
    }
}
