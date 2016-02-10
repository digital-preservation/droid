/**
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gsi.gov.uk>
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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import uk.gov.nationalarchives.droid.profile.referencedata.Format;

/**
* @author rflitcroft, mpalmer
*
*/

//TODO: abstract profile resource node into an interface and have JPA and JDBC versions + factory.
public class ProfileResourceNode {

    private Long id;
    private Long parentId;
    private URI uri;
    private String prefix;
    private String prefixPlusOne;
    private Integer identificationCount;
    private Date finished;
    private Boolean extensionMismatch = false;
    private List<Format> formatIdentifications = new ArrayList<Format>();
    private Integer filterStatus = 1;
    private NodeMetaData metaData = new NodeMetaData();
    
    /**
     * Default constructor.
     */
    //BNO made public for export work:  TODO: Review
    public ProfileResourceNode() { }

    /**
     * Copy constructor for ProfileResourceNode.
     * <p>
     * All simple values are copied; collections and data objects are recreated
     * with the same elements in them.
     *
     * @param toCopy The node to copy.
     */
    public ProfileResourceNode(ProfileResourceNode toCopy) {
        this.id                    = toCopy.id;
        this.parentId              = toCopy.parentId;
        this.uri                   = toCopy.uri;
        this.prefix                = toCopy.prefix;
        this.prefixPlusOne         = toCopy.prefixPlusOne;
        this.identificationCount   = toCopy.identificationCount;
        this.finished              = toCopy.finished;
        this.extensionMismatch     = toCopy.extensionMismatch;
        this.formatIdentifications = new ArrayList<Format>(toCopy.formatIdentifications);
        this.filterStatus          = toCopy.filterStatus;
        this.metaData              = new NodeMetaData(toCopy.metaData);
    }
    
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

    /**
     * @param parentId the parent to set
     */
    public void setParentId(Long parentId) {
        this.parentId = parentId;
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

    /**
     *
     * @return  The date and time that processing was completed for the resource node.
     */
    public Date getFinished() {
        return finished;
    }
    
    
}
