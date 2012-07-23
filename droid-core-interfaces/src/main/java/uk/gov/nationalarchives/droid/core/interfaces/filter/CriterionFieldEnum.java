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
package uk.gov.nationalarchives.droid.core.interfaces.filter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import uk.gov.nationalarchives.droid.core.interfaces.IdentificationMethod;
import uk.gov.nationalarchives.droid.core.interfaces.NodeStatus;
import uk.gov.nationalarchives.droid.core.interfaces.ResourceType;

/**
 * @author rflitcroft
 *
 */
public enum CriterionFieldEnum {
    
    /** File name. */
    FILE_NAME("File name", "metaData.name", String.class),
    /** File size. */
    FILE_SIZE("File size (bytes)", "metaData.size", Long.class),
    /** Last modified date. */
    LAST_MODIFIED_DATE("Last modified date", "metaData.lastModifiedDate", Date.class),
    /** Resource type. */
    RESOURCE_TYPE("Resource type", "metaData.resourceType", ResourceType.class),
    /** Identification method. */
    IDENTIFICATION_METHOD("Identification method", "metaData.identificationMethod", IdentificationMethod.class),
    /** Job status. */
    JOB_STATUS("Job status", "metaData.nodeStatus", NodeStatus.class),
    /** File extension. */
    FILE_EXTENSION("File extension", "metaData.extension", String.class),

    /** Identification count. */
    IDENTIFICATION_COUNT("Identification Count", "identificationCount", Integer.class),
    
    /** PUID. */
    PUID("PUID", "format.puid", String.class),
    /** Mime type. */
    MIME_TYPE("Mime type", "format.mimeType", String.class),
    /** File format. */
    FILE_FORMAT("Format name", "format.name", String.class); 
    
    private static Map<String, CriterionFieldEnum> allFields = new HashMap<String, CriterionFieldEnum>();

    static {
        for (CriterionFieldEnum value : values()) {
            allFields.put(value.toString(), value);
        }
    }
    
    private String name;
    private String propertyName;
    private Class<?> type;
    
    private CriterionFieldEnum(String name, String propertyName, Class<?> type) {
        this.name = name;
        this.propertyName = propertyName;
        this.type = type;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    /**
     * @return the propertyName
     */
    public String getPropertyName() {
        return propertyName;
    }
    
    
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * 
     * @return the type of the field's value
     */
    public Class<?> getType() {
        return type;
    }
    
}
