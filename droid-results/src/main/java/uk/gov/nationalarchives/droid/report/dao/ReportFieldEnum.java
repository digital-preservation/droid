/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.report.dao;


import java.util.HashMap;
import java.util.Map;


/**
 *@author Alok Kumar Dash
 * Defines all possible reported fields. 
 */
public enum ReportFieldEnum {

    /** File name. */
    FILE_NAME("File name", new StringOrSetFieldType("profile.name")),
    /** File size. */
    FILE_SIZE("File size", new NumericFieldType("profile.file_size")),
    /** Last modified date. */
    LAST_MODIFIED_DATE("Last modified date", new DateFieldType("profile.last_modified_date")),
    /** Resource type. */
    RESOURCE_TYPE("Resource type", new StringOrSetFieldType("profile.resource_type")),
    /** Identification method. */
    IDENTIFICATION_METHOD("Identification method", new StringOrSetFieldType(
            "profile.identification_method")),
    /** Identification status. */
    IDENTIFICATION_STATUS("Identification status", new StringOrSetFieldType("profile.node_status")),
    /** File extension. */
    FILE_EXTENSION("File extension", new StringOrSetFieldType("profile.extension")),
    /** PUID. */
    PUID("PUID", new StringOrSetFieldType("format.puid")),
    /** Mime type. */
    MIME_TYPE("Mime type", new StringOrSetFieldType("format.mime_type")),
    /** File format. */
    FILE_FORMAT("Format name", new StringOrSetFieldType("format.name")),
    /** File format version. */
    FORMAT_VERSION("Format version", new StringOrSetFieldType("format.version"));

    private static Map<String, ReportFieldEnum> allFields = new HashMap<String, ReportFieldEnum>();

    static {
        for (ReportFieldEnum value : values()) {
            allFields.put(value.toString(), value);
        }
    }

    private String name;
    private ReportFieldType type;

    private ReportFieldEnum(String name, ReportFieldType type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public String toString() {
        return name;
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
    public ReportFieldType getType() {
        return type;
    }

}
