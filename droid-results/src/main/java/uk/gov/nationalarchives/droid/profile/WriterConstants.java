/*
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gov.uk>
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
package uk.gov.nationalarchives.droid.profile;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.FastDateFormat;

import java.util.Arrays;
import java.util.List;

public final class WriterConstants {
    /**
     * Default header for the corresponding column.
     */
    public static final String HEADER_NAME_ID = "ID";
    /**
     * Default header for the corresponding column.
     */
    public static final String HEADER_NAME_PARENT_ID = "PARENT_ID";
    /**
     * Default header for the corresponding column.
     */
    public static final String HEADER_NAME_URI = "URI";
    /**
     * Default header for the corresponding column.
     */
    public static final String HEADER_NAME_FILE_PATH = "FILE_PATH";
    /**
     * Default header for the corresponding column.
     */
    public static final String HEADER_NAME_NAME = "NAME";
    /**
     * Default header for the corresponding column.
     */
    public static final String HEADER_NAME_METHOD = "METHOD";
    /**
     * Default header for the corresponding column.
     */
    public static final String HEADER_NAME_STATUS = "STATUS";
    /**
     * Default header for the corresponding column.
     */
    public static final String HEADER_NAME_SIZE = "SIZE";
    /**
     * Default header for the corresponding column.
     */
    public static final String HEADER_NAME_TYPE = "TYPE";
    /**
     * Default header for the corresponding column.
     */
    public static final String HEADER_NAME_EXT = "EXT";
    /**
     * Default header for the corresponding column.
     */
    public static final String HEADER_NAME_LAST_MODIFIED = "LAST_MODIFIED";
    /**
     * Default header for the corresponding column.
     */
    public static final String HEADER_NAME_EXTENSION_MISMATCH = "EXTENSION_MISMATCH";
    /**
     * Default header for the corresponding column.
     */
    public static final String HEADER_NAME_HASH = "HASH";
    /**
     * Default header for the corresponding column.
     */
    public static final String HEADER_NAME_FORMAT_COUNT = "FORMAT_COUNT";
    /**
     * Default header for the corresponding column.
     */
    public static final String HEADER_NAME_PUID = "PUID";
    /**
     * Default header for the corresponding column.
     */
    public static final String HEADER_NAME_MIME_TYPE = "MIME_TYPE";
    /**
     * Default header for the corresponding column.
     */
    public static final String HEADER_NAME_FORMAT_NAME = "FORMAT_NAME";
    /**
     * Default header for the corresponding column.
     */
    public static final String HEADER_NAME_FORMAT_VERSION = "FORMAT_VERSION";

    /**
     * Default dateTime format to be used for writing CSVs.
     */
    public static final FastDateFormat DATE_FORMAT = DateFormatUtils.ISO_DATETIME_FORMAT;


    /**
     * Default index of the hash column, since this column header may be modified.
     * This is the only column that we may need to access using index.
     */
    public static final int HASH_ARRAY_INDEX = 12;

    /**
     * String array for all the default headers.
     */
    public static final String[] HEADERS = {
            HEADER_NAME_ID,
            HEADER_NAME_PARENT_ID,
            HEADER_NAME_URI,
            HEADER_NAME_FILE_PATH,
            HEADER_NAME_NAME,
            HEADER_NAME_METHOD,
            HEADER_NAME_STATUS,
            HEADER_NAME_SIZE,
            HEADER_NAME_TYPE,
            HEADER_NAME_EXT,
            HEADER_NAME_LAST_MODIFIED,
            HEADER_NAME_EXTENSION_MISMATCH,
            HEADER_NAME_HASH,
            HEADER_NAME_FORMAT_COUNT,
            HEADER_NAME_PUID,
            HEADER_NAME_MIME_TYPE,
            HEADER_NAME_FORMAT_NAME,
            HEADER_NAME_FORMAT_VERSION,
    };

    /**
     * List of headers that appear more than once if a file matches more than one format.
     */
    public static final List<String> PER_FORMAT_HEADERS = Arrays.asList(
            HEADER_NAME_PUID, HEADER_NAME_MIME_TYPE, HEADER_NAME_FORMAT_NAME, HEADER_NAME_FORMAT_VERSION);

    /**
     * Empty string constant.
     */
    public static final String EMPTY_STRING = "";

    /**
     * File uri scheme, used as prefix.
     */
    public static final String FILE_URI_SCHEME = "file";

    private WriterConstants() {
        //hidden constructor
    }

}
