/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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
