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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;

/**
 * A collection of identification results.
 * @author rflitcroft
 *
 */
public class IdentificationResultCollection {

    private Map<String, IdentificationResult> puidMap = new HashMap<String, IdentificationResult>();
    private List<IdentificationResult> results = new ArrayList<IdentificationResult>();
    private URI resourceUri;
    private Long fileLength;
    private ResourceId correlationId;
    private boolean archive;
    private RequestMetaData requestMetaData;
    private Boolean fileExtensionMismatch = false;
    
    /**
     * 
     * @param request the original request.
     */
    public IdentificationResultCollection(IdentificationRequest request) {
        correlationId = request.getIdentifier().getParentResourceId();
        resourceUri = request.getIdentifier().getUri();
    }

    /**
     * Adds a result.
     * @param result the result to add
     */
    public void addResult(IdentificationResult result) {
        // Don't add the same puid more than once to a result collection.
        final String puid = result.getPuid();
        if (!puidMap.containsKey(puid)) {
            puidMap.put(puid, result);
            results.add(result);
        }
    }
    
    /**
     * Removes a result.
     * 
     * @param result The result to remove.
     */
    public void removeResult(IdentificationResult result) {
        if (results.remove(result)) {
            final String puid = result.getPuid();
            puidMap.remove(puid);
        }
    }    
    
    /**
     * 
     * @return a Collection of all results added.
     */
    public List<IdentificationResult> getResults() {
        return results;
    }
    
    /**
     * @return the jobCorrelationId
     */
    public ResourceId getCorrelationId() {
        return correlationId;
    }

    /**
     * The URI of the request.
     * @param uri the uri of the request
     */
    public void setUri(URI uri) {
        resourceUri = uri;
    }

    /**
     * 
     * @return the URI of the request
     */
    public URI getUri() {
        return resourceUri;
    }
    
    /**
     * The file length of the resource.
     * @param fileLength the length of the file
     */
    public void setFileLength(Long fileLength) {
        this.fileLength = fileLength;
    }
    
    /**
     * @return The file lenghth of the resource
     */
    public Long getFileLength() {
        return fileLength;
    }
    
    /**
     * @param archive true if the identification idicated an archive format; false otherwise
     */
    public void setArchive(boolean archive) {
        this.archive = archive;
    }
    
    /**
     * @return the archive
     */
    public boolean isArchive() {
        return archive;
    }
    
    /**
     * @param value Whether there is a file extension mismatch
     */
    public void setExtensionMismatch(Boolean value) {
        fileExtensionMismatch = value;
    }
    
    /**
     * 
     * @return whether there is a file extension mismatch.
     */
    public Boolean getExtensionMismatch() {
        return fileExtensionMismatch;
    }
    
    /**
     * @param requestMetaData the requestMetaData to set
     */
    public void setRequestMetaData(RequestMetaData requestMetaData) {
        this.requestMetaData = requestMetaData;
    }
    
    /**
     * @return the requestMetaData
     */
    public RequestMetaData getRequestMetaData() {
        return requestMetaData;
    }


    
}
