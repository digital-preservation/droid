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
package uk.gov.nationalarchives.droid.profile.referencedata;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Index;

/**
 * @author rflitcroft
 *
 */
@Immutable
@Entity
@Table(name = "format")
public class Format {

    /** The NULL format. */
    public static final Format NULL = nullFormat();
    
    @Id
    @Column(name = "puid")
    private String puid;
    
    @Column(name = "mime_type")
    @Index(name = "idx_mime_type")
    private String mimeType;
    
    @Column(name = "name")
    @Index(name = "idx_format_name")
    private String name;

    @Column(name = "version")
    private String version;
    
    /**
     * @return the puid
     */
    public String getPuid() {
        return this.equals(NULL) ? null : puid;
    }

    /**
     * @param puid the puid to set
     */
    public void setPuid(String puid) {
        this.puid = puid;
    }

    /**
     * @return the mimeType
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * @param mimeType the mimeType to set
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
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
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(puid).toHashCode();
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
        Format other = (Format) obj;
        return new EqualsBuilder().append(puid, other.puid).isEquals();
    }
    
    /**
     * Null formats are an entry in the database to represent the absence of a format!
     * 
     * This is only due to performance reasons: it is quicker to do an inner join from
     * the profile resources to the format identifications, but this requires that the
     * profile resources always have something to link to.  Hence, the NULL puid.
     * 
     * It's properties are blank strings, otherwise reports are confused: e.g things don't
     * have mime types if they aren't identified (NULL puid), and also don't have mime types
     * if the identified format doesn't have a mime-type.  In both cases, the reporting on
     * mime types should be consistent - so a blank string is preferred rather than an actual
     * null value.
     * @return the null Format.
     */
    private static Format nullFormat() {
        Format fmt = new Format();
        fmt.setPuid("");
        fmt.setName("");
        fmt.setMimeType("");
        fmt.setVersion("");
        return fmt;
    }
    
}
