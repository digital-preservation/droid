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
