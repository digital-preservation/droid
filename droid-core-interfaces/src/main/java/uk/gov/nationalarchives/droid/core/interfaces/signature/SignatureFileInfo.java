/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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
