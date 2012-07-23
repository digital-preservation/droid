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
package uk.gov.nationalarchives.droid.core.interfaces;

import java.net.URI;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;


/**
 * Class which holds the information needed to identify the source of an 
 * identification request. 
 * 
 * @author rflitcroft
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RequestIdentifier {

    @XmlAttribute(name = "NodeId")
    private Long nodeId;
    
    @XmlAttribute(name = "Prefix")
    private String prefix;
    
    @XmlAttribute(name = "ParentId")
    private Long parentId;
    
    @XmlAttribute(name = "ParentPrefix")
    private String parentPrefix;

    @XmlAttribute(name = "AncestorId")
    private Long ancestorId;

    @XmlValue
    private URI uri;
    
    /**
     * Default Constructor. 
     */
    RequestIdentifier() { }
    
    /**
     * 
     * @param uri the URI of the request's data.
     */
    public RequestIdentifier(URI uri) {
        this.uri = uri;
    }

    /**
     * @return the nodeId
     */
    public Long getNodeId() {
        return nodeId;
    }
    
    /**
     * 
     * @return The prefix.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * @param nodeId the nodeId to set
     */
    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }
    
    /**
     * 
     * @param prefix the prefix to set.
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    
    /**
     * Sets the node id and prefix from a ResourceId object.
     * 
     * @param id The resourceId to set.
     */
    public void setResourceId(ResourceId id) {
        if (id != null) {
            this.nodeId = id.getId();
            this.prefix = id.getPath();
        } else {
            this.nodeId = null;
            this.prefix = "";
        }
    }

    /**
     * @return the parentId
     */
    public Long getParentId() {
        return parentId;
    }
    
    /**
     * 
     * @return The parent prefix.
     */
    public String getParentPrefix() {
        return parentPrefix;
    }

    /**
     * @param parentId the parentId to set
     */
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
    
    /**
     * 
     * @param parentPrefix The parent prefix to set.
     */
    public void setParentPrefix(String parentPrefix) {
        this.parentPrefix = parentPrefix;
    }

    /**
     * Sets the parent id and prefix from a ResourceId object.
     * 
     * @param id The resourceId to set.
     */
    public void setParentResourceId(ResourceId id) {
        if (id != null) {
            this.parentId = id.getId();
            this.parentPrefix = id.getPath();
        } else {
            this.parentId = null;
            this.parentPrefix = "";
        }
    }
    
    /**
     * 
     * @return A resourceId for the parent, or null if there is a null parentId.
     */
    public ResourceId getParentResourceId() {
        return parentId == null ? null : new ResourceId(parentId, parentPrefix);
    }
    
    /**
     * 
     * @return A resourceId for the request, or null if there is a null nodeId.
     */
    public ResourceId getResourceId() {
        return nodeId == null ? null : new ResourceId(nodeId, prefix);
    }
    
    
    /**
     * The ancestorId is the node id of a containing archival file.
     * It is used when replaying a paused profile, to remove results
     * which need to be recreated.
     * 
     * @return the ancestorId
     */
    public Long getAncestorId() {
        return ancestorId;
    }

    /**
     * @param ancestorId the ancestorId to set
     */
    public void setAncestorId(Long ancestorId) {
        this.ancestorId = ancestorId;
    }

    /**
     * @return the uri
     */
    public URI getUri() {
        return uri;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(ancestorId)
            .append(nodeId)
            .append(prefix)
            .append(parentId)
            .append(parentPrefix)
            .append(uri)
            .toHashCode();
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
        RequestIdentifier other = (RequestIdentifier) obj;
        
        return new EqualsBuilder()
            .append(ancestorId, other.ancestorId)
            .append(nodeId, other.nodeId)
            .append(prefix, other.prefix)
            .append(parentId, other.parentId)
            .append(parentPrefix, other.parentPrefix)
            .append(uri, other.uri)
            .isEquals();
            
    }

}
