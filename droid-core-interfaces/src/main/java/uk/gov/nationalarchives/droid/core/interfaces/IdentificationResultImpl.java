/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.core.interfaces;


import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;


/**
 * @author rflitcroft
 *
 */
public class IdentificationResultImpl implements IdentificationResult {
    
    private String puid;
    private String name;
    private String mimeType;
    private String version;
    private String extId;
    private IdentificationMethod method;
    private RequestIdentifier identifier;
    private RequestMetaData requestMetaData;
    
    /**
     * @return the puid
     */
    public String getPuid() {
        return puid;
    }
    
    /**
     * @param puid the puid to set
     */
    public void setPuid(String puid) {
        this.puid = puid;
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
     * @return the extId
     */
    public String getExtId() {
        return extId;
    }

    /**
     * @param extId the extId to set
     */
    public void setExtId(String extId) {
        this.extId = extId;
    }

    /**
     * 
     * @return The version of the file format.
     */
    public String getVersion() {
        return version;
    }
    
    /**
     * 
     * @param version The file format version.
     */
    public void setVersion(String version) {
        this.version = version;
    }
    
    /**
     * @return the method
     */
    public IdentificationMethod getMethod() {
        return method;
    }

    /**
     * @param method the method to set
     */
    public void setMethod(IdentificationMethod method) {
        this.method = method;
    }

    /**
     * @param requestMetaData the requestMetaData to set
     */
    public void setRequestMetaData(RequestMetaData requestMetaData) {
        this.requestMetaData = requestMetaData;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public RequestMetaData getMetaData() {
        return requestMetaData;
    }
    
    /**
     * @param identifier the identifier to set
     */
    public void setIdentifier(RequestIdentifier identifier) {
        this.identifier = identifier;
    }
    
    /**
     * @return the identifier
     */
    public RequestIdentifier getIdentifier() {
        return identifier;
    }

}
