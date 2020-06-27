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
package uk.gov.nationalarchives.droid.container;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;

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

    @XmlTransient
    private Map<String, ContainerFile> filesMap;
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
     * Returns a map of container file paths to container file objects.
     * The paths are normalised, in that any trailing forward slash is removed from them,
     * to ensure that they can be matched no matter how they are specified.
     * @return the files
     */
    public Map<String, ContainerFile> getFiles() {
        if (this.filesMap == null) {
            Map<String, ContainerFile> containerFileMap = new HashMap<String, ContainerFile>();
            for (ContainerFile file : files) {
                containerFileMap.put(file.getPath(), file);
            }
            this.filesMap = containerFileMap;
        }
        return this.filesMap;
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
