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
package uk.gov.nationalarchives.droid.profile;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * @author rflitcroft
 * 
 */

@XmlAccessorType(XmlAccessType.NONE)
public abstract class AbstractProfileResource {

    private String location;

    @XmlElement(name = "Size")
    private long size;

    @XmlElement(name = "LastModifiedDate")
    private Date lastModifiedDate;

    @XmlElement(name = "Extension")
    private String extension;

    @XmlElement(name = "Name")
    private String name;

    @XmlElement(name = "Uri")
    private URI uri;
    
    @XmlElement(name = "Path")
    private String path;
    
    /**
     * Default Constructor.
     */
    AbstractProfileResource() {
    }

    /**
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * @param location
     *            the location to set
     */
    void setLocation(String location) {
        this.location = location;
        try {
            uri = new URI(location);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 
     * @return the URI of theis resource
     */
    public URI getUri() {
        return uri;
    }
    
    /**
     * 
     * @return the decoded file path of the resource.
     */
    public String getPath() {
        return path;
    }

    /**
     * 
     * @param path the file path to set.
     */
    public void setPath(String path) {
        this.path = path; 
    }
    
    /**
     * Sets the URI of this resource.
     * 
     * @param uri
     *            the URI to set.
     */
    protected void setUri(URI uri) {
        this.uri = uri;
        location = uri.toString();
        String decodedLocation = java.net.URLDecoder.decode(location);
        int uriPrefix = decodedLocation.indexOf(":/");
        path = decodedLocation.substring(uriPrefix + 2);
    }

    /**
     * @return true if the resource is a directory of files, false otherwise
     */
    public abstract boolean isDirectory();

    /**
     * @return true if the resource should be recursed into; false otherwise
     */
    public boolean isRecursive() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(uri).toHashCode();
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

        AbstractProfileResource other = (AbstractProfileResource) obj;
        return new EqualsBuilder().append(uri, other.uri).isEquals();

    }

    /**
     * Getter method size of the resource in bytes.
     * @return size
     */
    
    public long getSize() {
        return size;
    }
    
    /**
     * Setter method for size of resource. 
     * @param size of the resource.
     */

    public void setSize(long size) {
        this.size = size;
    }
    
    /**
     * Getter method for last modified date of resource.
     * @return lastModifiedDate
     */

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    /**
     * Setter method for last modified date for resource.
     * @param lastModifiedDate last modified data of resource.
     */
    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    
    /**
     * Getter method for extension.
     * @return  extension for resource.
     */
    public String getExtension() {
        return extension;
    }

    /**
     * Setter method for extension.
     * @param  extension for resource.
     */
    public void setExtension(String extension) {
        this.extension = extension;
    }

    
    /**
     * Getter method for name. 
     * @return name.
     */
    public String getName() {
        return name;
    }

    
    /**
     * Setter method for name.
     * @param name of the resource.
     */
    public void setName(String name) {
        this.name = name;
    }
    

}
