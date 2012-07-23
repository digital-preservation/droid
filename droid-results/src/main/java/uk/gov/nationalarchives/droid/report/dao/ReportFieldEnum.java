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
