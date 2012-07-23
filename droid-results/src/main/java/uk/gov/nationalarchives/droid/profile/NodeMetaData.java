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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.hibernate.annotations.Index;

import uk.gov.nationalarchives.droid.core.interfaces.IdentificationMethod;
import uk.gov.nationalarchives.droid.core.interfaces.NodeStatus;
import uk.gov.nationalarchives.droid.core.interfaces.ResourceType;

/**
 * @author rflitcroft
 * 
 */
@Embeddable
public class NodeMetaData {
    
    private static final int MD5_LENGTH = 32;

    @Index(name = "idx_prn_file_size")
    @Column(name = "file_size")
    private Long size;
    
    @Index(name = "idx_prn_last_modified")
    @Column(name = "last_modified_date")
    private Date lastModifiedDate;

    @Index(name = "idx_prn_name")
    @Column(name = "name", nullable = false)
    private String name;

    @Index(name = "idx_prn_extension")
    @Column(name = "extension")
    private String extension;

    @Index(name = "idx_prn_node_status")
    @Column(name = "node_status")
    @Enumerated(EnumType.ORDINAL)
    private NodeStatus nodeStatus;

    @Index(name = "idx_prn_id_method")
    @Column(name = "identification_method")
    @Enumerated(EnumType.ORDINAL)
    private IdentificationMethod identificationMethod;

    
    @Index(name = "idx_prn_id_resourceType")
    @Column(name = "resource_type", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private ResourceType  resourceType;
    
    @Index(name = "idx_prn_hash")
    @Column(name = "hash", length = MD5_LENGTH)
    private String hash;
  
    /**
     * @return the size
     */
    public Long getSize() {
        return size;
    }

    /**
     * @param size
     *            the size to set
     */
    public void setSize(Long size) {
        this.size = size;
    }

    /**
     * @return the lastModifiedDate
     */
    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    /**
     * @param lastModified
     *            the last modified time to set.
     */
    public void setLastModified(Long lastModified) {
        if (lastModified != null) {
            lastModifiedDate = new Date(lastModified);
        }
    }

    /**
     * @return the name of metadata.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the extension
     */
    public String getExtension() {
        return extension;
    }

    /**
     * Always converts the file extension to lower case.
     * 
     * @param extension
     *            the extension to set
     */
    public void setExtension(String extension) {
        this.extension = extension.toLowerCase();
    }

    /**
     * @param lastModifiedDate
     *            the lastModifiedDate to set
     */
    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    /**
     * @return the nodeStatus
     */
    public NodeStatus getNodeStatus() {
        return nodeStatus;
    }

    /**
     * @param nodeStatus
     *            the nodeStatus to set
     */
    public void setNodeStatus(NodeStatus nodeStatus) {
        this.nodeStatus = nodeStatus;
    }

    /**
     * @return the identificationMethod
     */
    public IdentificationMethod getIdentificationMethod() {
        return identificationMethod;
    }

    /**
     * @param identificationMethod
     *            the identificationMethod to set
     */
    public void setIdentificationMethod(
            IdentificationMethod identificationMethod) {
        this.identificationMethod = identificationMethod;
    }

    /**
     * @return the resourceType
     */
    public ResourceType getResourceType() {
        return resourceType;
    }

    /**
     * @param resourceType the resourceType to set
     */
    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }
    
    /**
     * @param hash the hash to set
     */
    public void setHash(String hash) {
        this.hash = hash;
    }
    
    /**
     * @return the hash
     */
    public String getHash() {
        return hash;
    }

}
