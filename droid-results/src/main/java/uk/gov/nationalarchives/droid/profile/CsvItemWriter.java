/**
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

import java.io.Writer;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.univocity.parsers.csv.CsvFormat;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import com.univocity.parsers.common.TextWritingException;

import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalConfig;
import uk.gov.nationalarchives.droid.core.interfaces.util.DroidUrlFormat;
import uk.gov.nationalarchives.droid.export.interfaces.ExportOptions;
import uk.gov.nationalarchives.droid.export.interfaces.ItemWriter;
import uk.gov.nationalarchives.droid.profile.referencedata.Format;

/**
 * @author rflitcroft
 *
 */
public class CsvItemWriter implements ItemWriter<ProfileResourceNode> {

    /**
     * Headers used in the CSV output
     */
    static final String[] HEADERS = {
            "ID",
            "PARENT_ID",
            "URI",
            "FILE_PATH",
            "NAME",
            "METHOD",
            "STATUS",
            "SIZE",
            "TYPE",
            "EXT",
            "LAST_MODIFIED",
            "EXTENSION_MISMATCH",
            "HASH",
            "FORMAT_COUNT",
            "PUID",
            "MIME_TYPE",
            "FORMAT_NAME",
            "FORMAT_VERSION",
    };

    private static final String FILE_URI_SCHEME = "file";

    /*
     * Indexes of the headers used in the CSV output.
     */
    private static final int ID_ARRAY_INDEX                 = 0;
    private static final int PARENT_ID_ARRAY_INDEX          = 1;
    private static final int URI_ARRAY_INDEX                = 2;
    private static final int FILE_PATH_ARRAY_INDEX          = 3;
    private static final int FILE_NAME_ARRAY_INDEX          = 4;
    private static final int ID_METHOD_ARRAY_INDEX          = 5;
    private static final int STATUS_ARRAY_INDEX             = 6;
    private static final int SIZE_ARRAY_INDEX               = 7;
    private static final int RESOURCE_ARRAY_INDEX           = 8;
    private static final int EXTENSION_ARRAY_INDEX          = 9;
    private static final int LAST_MODIFIED_ARRAY_INDEX      = 10;
    private static final int EXTENSION_MISMATCH_ARRAY_INDEX = 11;
    private static final int HASH_ARRAY_INDEX               = 12;
    private static final int ID_COUNT_ARRAY_INDEX           = 13;
    private static final int PUID_ARRAY_INDEX               = 14;
    private static final int MIME_TYPE_ARRAY_INDEX          = 15;
    private static final int FORMAT_NAME_ARRAY_INDEX        = 16;
    private static final int FORMAT_VERSION_ARRAY_INDEX     = 17;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private CsvWriter csvWriter;
    private FastDateFormat dateFormat = DateFormatUtils.ISO_DATETIME_FORMAT;
    private ExportOptions options = ExportOptions.ONE_ROW_PER_FILE;
    
    private String[] headers;
    private boolean quoteAllFields;
    private boolean[] columnsToWrite;
    private int numColumnsToWrite;

    /**
     * Empty bean constructor.
     */
    public CsvItemWriter() {
       this(null);
    }

    /**
     * Constructor taking parameters.
     * @param writer The CsvWriter to use.
     */
    public CsvItemWriter(CsvWriter writer) {
        this.csvWriter = writer;
        this.quoteAllFields = true;
        numColumnsToWrite = HEADERS.length;
        columnsToWrite = new boolean[HEADERS.length];
        Arrays.fill(columnsToWrite, true);
    }

    @Override
    public void write(List<? extends ProfileResourceNode> nodes) {
        switch (options) {
            case ONE_ROW_PER_FILE: {
                writeOneRowPerFile(nodes);
                break;
            }
            case ONE_ROW_PER_FORMAT: {
                writeOneRowPerFormat(nodes);
                break;
            }
            default: {
                writeOneRowPerFile(nodes);
            }
        }
    }
    
    private void writeOneRowPerFile(List<? extends ProfileResourceNode> nodes) {
        try {
            for (ProfileResourceNode node : nodes) {
                List<String> nodeEntries = new ArrayList<String>();
                addNodeColumns(nodeEntries, node);
                for (Format format : node.getFormatIdentifications()) {
                    addColumn(nodeEntries, PUID_ARRAY_INDEX, format.getPuid());
                    addColumn(nodeEntries, MIME_TYPE_ARRAY_INDEX, format.getMimeType());
                    addColumn(nodeEntries, FORMAT_NAME_ARRAY_INDEX, format.getName());
                    addColumn(nodeEntries, FORMAT_VERSION_ARRAY_INDEX, format.getVersion());
                }
                csvWriter.writeRow(nodeEntries);
            }
            csvWriter.flush();
        } catch (final TextWritingException e) {
            log.error(e.getRecordCharacters(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    private void writeOneRowPerFormat(List<? extends ProfileResourceNode> nodes) {
        try {
            for (ProfileResourceNode node : nodes) {
                for (Format format : node.getFormatIdentifications()) {
                    List<String> nodeEntries = new ArrayList<>();
                    addNodeColumns(nodeEntries, node);
                    addColumn(nodeEntries, PUID_ARRAY_INDEX, format.getPuid());
                    addColumn(nodeEntries, MIME_TYPE_ARRAY_INDEX, format.getMimeType());
                    addColumn(nodeEntries, FORMAT_NAME_ARRAY_INDEX, format.getName());
                    addColumn(nodeEntries, FORMAT_VERSION_ARRAY_INDEX, format.getVersion());
                    csvWriter.writeRow(nodeEntries);
                }
            }
            csvWriter.flush();
        } catch (final TextWritingException e) {
            log.error(e.getRecordCharacters(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * @param csvWriter the csvWriter to write to.
     */
    void setCsvWriter(CsvWriter csvWriter) {
        this.csvWriter = csvWriter;
    }

    @Override
    public void open(final Writer writer) {
        final CsvWriterSettings csvWriterSettings = new CsvWriterSettings();
        csvWriterSettings.setQuoteAllFields(quoteAllFields);
        CsvFormat format = new CsvFormat();
        // following Unix convention on line separators as previously
        format.setLineSeparator("\n");
        csvWriterSettings.setFormat(format);
        csvWriter = new CsvWriter(writer, csvWriterSettings);
        if (headers == null) {
            headers = HEADERS;
        }
        String[] headersToWrite = getHeadersToWrite(headers);
        csvWriter.writeHeaders(headersToWrite);
    }

    @Override
    public void setOptions(ExportOptions options) {
        this.options = options;
    }
    
    /**
     * Closes the CSV writer.
     */
    @Override
    public void close() {
        csvWriter.close();
    }
    
    private static String nullSafeName(Enum<?> value) {
        return value == null ? "" : value.toString();
    }
    
    private static String nullSafeNumber(Number number) {
        return number == null ? "" : number.toString();
    }
    
    private static String nullSafeDate(Date date, FastDateFormat format) {
        return date == null ? "" : format.format(date);
    }
    
    private static String toFilePath(URI uri) {
        if (FILE_URI_SCHEME.equals(uri.getScheme())) {
            return Paths.get(uri).toAbsolutePath().toString();
        }
        
        return null;
    }

    private static String toFileName(String name) {
        return FilenameUtils.getName(name);
    }
    
    /**
     * No config is needed by this class, but it's retained temporarily for backwards compatibility purposes.
     * @param config the config to set
     */
    public void setConfig(DroidGlobalConfig config) {
    }

    /**
     * @param headersToSet the headers to set
     */
    @Override
    public void setHeaders(Map<String, String> headersToSet) {

        if (this.headers == null) {
            this.headers = HEADERS;
        }

        String hashHeader = headersToSet.get("hash");
        if (hashHeader != null) {
            this.headers[HASH_ARRAY_INDEX] = hashHeader;
        }
    }

    @Override
    public void setQuoteAllFields(boolean quoteAll) {
        this.quoteAllFields = quoteAll;
    }

    @Override
    public void setColumnsToWrite(String columnNames) {
        Set<String> headersToWrite = getColumnsToWrite(columnNames);
        if (headersToWrite == null) {
            Arrays.fill(columnsToWrite, true);
            numColumnsToWrite = columnsToWrite.length;
        } else {
            int numberToWrite = 0;
            for (int i = 0; i < HEADERS.length; i++) {
                if (headersToWrite.contains(HEADERS[i])) {
                    columnsToWrite[i] = true;
                    numberToWrite++;
                    headersToWrite.remove(HEADERS[i]);
                } else {
                    columnsToWrite[i] = false;
                }
            }
            // Defence: if no valid column names were specified, log a warning then write them all out:
            if (numberToWrite == 0) {
                numColumnsToWrite = columnsToWrite.length;
                Arrays.fill(columnsToWrite, true);
                log.warn("-co option: no CSV columns specified are valid, writing all columns: " + columnNames);
            } else {
                numColumnsToWrite = numberToWrite;
                // If there are some columns specified left over, they aren't valid columns - log a warning:
                if (headersToWrite.size() > 0) {
                    String invalidHeaders = "";
                    for (String invalidColumn : headersToWrite) {
                        invalidHeaders = invalidHeaders + invalidColumn + ' ';
                    }
                    log.warn("-co option - some CSV columns specified were invalid: " + invalidHeaders);
                }
            }
        }
    }

    private Set<String> getColumnsToWrite(String columnNames) {
        if (columnNames != null && !columnNames.isEmpty()) {
            String[] columns = columnNames.split(" ");
            if (columns.length > 0) {
                Set<String> set = new HashSet<>();
                for (String column : columns) {
                    set.add(column.toUpperCase(Locale.ROOT));
                }
                return set;
            }
        }
        return null;
    }

    private String[] getHeadersToWrite(String[] headersToWrite) {
        if (numColumnsToWrite < columnsToWrite.length) {
            String[] newHeaders = new String[numColumnsToWrite];
            int newHeaderIndex = 0;
            for (int i = 0; i < columnsToWrite.length; i++) {
                if (columnsToWrite[i]) {
                    newHeaders[newHeaderIndex++] = headers[i];
                }
            }
            return newHeaders;
        }
        return headersToWrite;
    }

    private void addNodeColumns(List<String> row, ProfileResourceNode node) {
        NodeMetaData metaData = node.getMetaData();
        addColumn(row, ID_ARRAY_INDEX, nullSafeNumber(node.getId()));
        addColumn(row, PARENT_ID_ARRAY_INDEX, nullSafeNumber(node.getParentId()));
        addColumn(row, URI_ARRAY_INDEX, DroidUrlFormat.format(node.getUri()));
        addColumn(row, FILE_PATH_ARRAY_INDEX, toFilePath(node.getUri()));
        addColumn(row, FILE_NAME_ARRAY_INDEX, toFileName(metaData.getName()));
        addColumn(row, ID_METHOD_ARRAY_INDEX, nullSafeName(metaData.getIdentificationMethod()));
        addColumn(row, STATUS_ARRAY_INDEX, metaData.getNodeStatus().getStatus());
        addColumn(row, SIZE_ARRAY_INDEX, nullSafeNumber(metaData.getSize()));
        addColumn(row, RESOURCE_ARRAY_INDEX, metaData.getResourceType().getResourceType());
        addColumn(row, EXTENSION_ARRAY_INDEX, metaData.getExtension());
        addColumn(row, LAST_MODIFIED_ARRAY_INDEX, nullSafeDate(metaData.getLastModifiedDate(), dateFormat));
        addColumn(row, EXTENSION_MISMATCH_ARRAY_INDEX, node.getExtensionMismatch().toString());
        addColumn(row, HASH_ARRAY_INDEX, metaData.getHash());
        addColumn(row, ID_COUNT_ARRAY_INDEX, nullSafeNumber(node.getIdentificationCount()));
    }

    private void addColumn(List<String> row, int columnIndex, String value) {
        if (columnsToWrite[columnIndex]) {
            row.add(value);
        }
    }



}
