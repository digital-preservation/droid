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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
//import javax.persistence.ManyToOne;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import uk.gov.nationalarchives.droid.core.interfaces.TextEncoding;
import uk.gov.nationalarchives.droid.profile.referencedata.Format;
import uk.gov.nationalarchives.droid.profile.types.UriType;

/**
 * @author rflitcroft, mpalmer
 *
 */
@TypeDef(name = "uri", typeClass = UriType.class)
@Entity
@Table(name = "profile_resource_node")
@SqlResultSetMappings({ 
  @SqlResultSetMapping(
      name = "ResourceNodeWithFilterStatus",
      entities = { @EntityResult(entityClass = ProfileResourceNode.class) },
      columns = { @ColumnResult(name = "FilterStatus") }
  )
})
public class ProfileResourceNode {

    private static final int URI_LENGTH = 4000;
    
    @Id
    // If using the BatchResultHandler, we assign our own ids, so do not use the generatedvalue.
    // If using the ResultHandlerImpl, ids are assigned by the database, so we need the line below.
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "node_id")
    private Long id;
    
    /*
    @ManyToOne(optional = true)
    @JoinColumn(name = "parent_id")
    private ProfileResourceNode parent;
    */
    
    @Column(name = "parent_id", unique = false)
    @Index(name = "idx_parent_id")
    private Long parentId;
    
    @Column(nullable = false, name = "uri", unique = false, length = URI_LENGTH)
    @Type(type = "uri")
    @Index(name = "idx_uri")
    private URI uri;
    
    @Column(name = "prefix")
    @Index(name = "idx_prefix")
    private String prefix;
    
    @Index(name = "idx_prefix_plus_one")
    @Column(name = "prefix_plus_one")
    private String prefixPlusOne;
    
    @Index(name = "idx_id_count")
    @Column(name = "identification_count")
    private Integer identificationCount;
    
    @Column(name = "finished_timestamp")
    @Temporal(TemporalType.TIMESTAMP)
    private Date finished;    
    
    // Message status boolean flags:
    @Index(name = "idx_extension_mismatch")
    @Column(nullable = false, name = "extension_mismatch")
    //@Access(AccessType.PROPERTY)
    private Boolean extensionMismatch = false;
    
    @Index(name = "idx_text_encoding")
    @Column(name = "text_encoding")
    @Enumerated(EnumType.ORDINAL)
    private TextEncoding textEncoding;
    
   
    @org.hibernate.annotations.Fetch(FetchMode.SELECT)
    //@OneToMany(mappedBy = "node", fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "identification", 
            joinColumns = { @JoinColumn(name = "node_id") },
            inverseJoinColumns = { @JoinColumn(name = "puid") })
    private List<Format> formatIdentifications = new ArrayList<Format>();
    
    @Transient
    private Integer filterStatus = 1;    
    
    @Embedded
    private NodeMetaData metaData = new NodeMetaData();
    
    /**
     * Default constructor.
     */
    ProfileResourceNode() { }
    
    /**
     * @param uri the URI of the resource node
     */
    public ProfileResourceNode(URI uri) {
        this.uri = uri;
    }
    
    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }
    
    /**
     * 
     * @param id Sets the id.
     */
    public void setId(Long id) {
        this.id = id;
    }


    /**
     * @return the uri
     */
    public URI getUri() {
        return uri;
    }

    
    /**
     * @return the parent
     */
    public boolean allowsChildren() {
        return metaData.getResourceType().allowsChildren();
    }

    /**
     * @return the metaData
     */
    public NodeMetaData getMetaData() {
        return metaData;
    }

    /**
     * @param metaData the metaData to set
     */
    public void setMetaData(NodeMetaData metaData) {
        this.metaData = metaData;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(uri).toHashCode();
    }
    
    /**
     * @return the parent
     */
    public Long getParentId() {
        return parentId;
    }
    /*
    public ProfileResourceNode getParent() {
        return parent;
    }
    */
    /**
     * @param parentId the parent to set
     */
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
    /*
    public void setParent(ProfileResourceNode parent) {
        this.parent = parent;
    }
    */    
    
    /**
     * Adds metadata about number and size of children resources.
     * FIXME: Causes database locks as each node tries to update...
     * @param cSize the size of the child resource.
     */
    /*
    public void addChildMetadata(Long cSize) {
        this.childCount += 1;
        if (cSize != null) {
            this.childSize += cSize;
        }
        if (parent != null) {
            parent.addChildMetadata(cSize);
        }
    }
    */
    
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
        
        ProfileResourceNode other = (ProfileResourceNode) obj;
        
        return new EqualsBuilder().append(uri, other.uri).isEquals();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        //return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append(uri).toString();
        return java.net.URLDecoder.decode(uri.toString());
    }

    /**
     * @return prefix Getter method for prefix.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * @param prefix Setter method for setting prefix.
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * @return prefixPlusOne Getter method for getting prefixPlusOne
     */
    public String getPrefixPlusOne() {
        return prefixPlusOne;
    }

    /**
     * @param prefixPlusOne Setter method for prefixPlusOne.
     */
    public void setPrefixPlusOne(String prefixPlusOne) {
        this.prefixPlusOne = prefixPlusOne;
    }

    /**
     * Adds an identification to the identification job.
     * @param formatIdentification the identification to add
     */
    public void addFormatIdentification(Format formatIdentification) {
        formatIdentifications.add(formatIdentification);
        if (!formatIdentification.equals(Format.NULL)) {
            if (identificationCount == null) {
                setZeroIdentifications();
            }
            identificationCount++;
        }
    }
    
    /**
     * Initialises the identification count to zero.
     */
    public void setZeroIdentifications() {
        identificationCount = 0;
    }

    /**
     * @return List of format identifications
     */
    public List<Format> getFormatIdentifications() {
        return Collections.unmodifiableList(formatIdentifications);
    }

    /**
     * @return the identificationCount
     */
    public Integer getIdentificationCount() {
        return identificationCount;
    }
    
    /**
     * @param status the filter status
     *
     */
    public void setFilterStatus(Integer status) {
        //FIXME: filter status should be enumeration, not an integer value.
        filterStatus = status;
    }
    
    /**
     * 
     * @return the filter status
     */
    public int getFilterStatus() {
        return filterStatus;
    }    
 
    /**
     * 
     */
    public void setNoFormatsIdentified() {
        identificationCount = null;
    }
    
    
    /**
     * 
     * @return Whether the resource has a file extension mismatch.
     */
    public Boolean getExtensionMismatch() {
        return extensionMismatch;
    }
    

    /**
     * 
     * @param extensionMismatch Whether the resource has a file extension mismatch.
     */
    public void setExtensionMismatch(Boolean extensionMismatch) {
        this.extensionMismatch = extensionMismatch;
    }

    /**
     * @param date the date the node was finished profiling.
     */
    public void setFinished(Date date) {
        this.finished = date;
    }
    
    
}
