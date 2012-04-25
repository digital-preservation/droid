/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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
