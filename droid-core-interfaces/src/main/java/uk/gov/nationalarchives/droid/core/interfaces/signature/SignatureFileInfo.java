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
package uk.gov.nationalarchives.droid.core.interfaces.signature;

import java.io.File;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * @author rflitcroft
 *
 */
public class SignatureFileInfo {

    private int version;
    private boolean deprecated;
    private File file;
    private SignatureServiceException error;
    
    private SignatureType type;
    
    /**
     * @param version the version
     * @param deprecated whether the version id deprecated 
     * @param type the signature type
     */
    public SignatureFileInfo(int version, boolean deprecated, SignatureType type) {
        this.version = version;
        this.deprecated = deprecated;
        this.type = type;
    }

    /**
     * Constructor for describing an error condition withe signature service.
     * @param e the error
     */
    public SignatureFileInfo(SignatureServiceException e) {
        error = e;
    }

    /**
     * @return the version
     */
    public int getVersion() {
        return version;
    }

    /**
     * @return the deprecated
     */
    public boolean isDeprecated() {
        return deprecated;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(version)
            .append(type)
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
        SignatureFileInfo other = (SignatureFileInfo) obj;
        return new EqualsBuilder()
            .append(version, other.version)
            .append(type, other.type)
            .isEquals();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Version " + version;
    }

    /**
     * @return the uri
     */
    public File getFile() {
        return file;
    }
    
    /**
     * @param file the file to set
     */
    public void setFile(File file) {
        this.file = file;
    }
    
    /**
     * @return the type
     */
    public SignatureType getType() {
        return type;
    }
    
    /**
     * @return the error
     */
    public SignatureServiceException getError() {
        return error;
    }
    
    /**
     * 
     * @return true if the info represents an error, false otherwise
     */
    public boolean hasError() {
        return error != null;
    }
    
}
