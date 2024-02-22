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

import com.univocity.parsers.common.TextWritingException;
import com.univocity.parsers.csv.CsvFormat;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalConfig;
import uk.gov.nationalarchives.droid.core.interfaces.util.DroidUrlFormat;
import uk.gov.nationalarchives.droid.export.interfaces.ExportOptions;
import uk.gov.nationalarchives.droid.export.interfaces.ExportTemplate;
import uk.gov.nationalarchives.droid.export.interfaces.ExportTemplateColumnDef;
import uk.gov.nationalarchives.droid.export.interfaces.ItemWriter;
import uk.gov.nationalarchives.droid.profile.referencedata.Format;

import java.io.File;
import java.io.Writer;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author rflitcroft
 *
 */
public class CsvItemWriter implements ItemWriter<ProfileResourceNode> {

    private static final String HEADER_NAME_ID = "ID";
    private static final String HEADER_NAME_PARENT_ID = "PARENT_ID";
    private static final String HEADER_NAME_URI = "URI";
    private static final String HEADER_NAME_FILE_PATH = "FILE_PATH";
    private static final String HEADER_NAME_NAME = "NAME";
    private static final String HEADER_NAME_METHOD = "METHOD";
    private static final String HEADER_NAME_STATUS = "STATUS";
    private static final String HEADER_NAME_SIZE = "SIZE";
    private static final String HEADER_NAME_TYPE = "TYPE";
    private static final String HEADER_NAME_EXT = "EXT";
    private static final String HEADER_NAME_LAST_MODIFIED = "LAST_MODIFIED";
    private static final String HEADER_NAME_EXTENSION_MISMATCH = "EXTENSION_MISMATCH";
    private static final String HEADER_NAME_HASH = "HASH";
    private static final String HEADER_NAME_FORMAT_COUNT = "FORMAT_COUNT";
    private static final String HEADER_NAME_PUID = "PUID";
    private static final String HEADER_NAME_MIME_TYPE = "MIME_TYPE";
    private static final String HEADER_NAME_FORMAT_NAME = "FORMAT_NAME";
    private static final String HEADER_NAME_FORMAT_VERSION = "FORMAT_VERSION";

    /**
     * Headers used in the CSV output
     */
    static final String[] HEADERS = {
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
     * List of headers that appear more than once if a file matches more than one format
     */
    static final List<String> PER_FORMAT_HEADERS = Arrays.asList(
            HEADER_NAME_PUID, HEADER_NAME_MIME_TYPE, HEADER_NAME_FORMAT_NAME, HEADER_NAME_FORMAT_VERSION);

    private static final String FILE_URI_SCHEME = "file";

    //default index of the hash column, since this column header may be modified, this is the only
    // column that we may need to access using index.
    private static final int HASH_ARRAY_INDEX               = 12;
    private static final String BLANK_SPACE_DELIMITER       = " ";
    private static final String EMPTY_STRING       = "";

    private Map<String, Boolean> columnsToWriteMap = new HashMap<>();

    private final Logger log = LoggerFactory.getLogger(getClass());

    private CsvWriter csvWriter;
    private final FastDateFormat dateFormat = DateFormatUtils.ISO_DATETIME_FORMAT;
    private ExportOptions options = ExportOptions.ONE_ROW_PER_FILE;
    
    private String[] allHeaders;
    private boolean quoteAllFields;
    private int numColumnsToWrite;
    private ExportTemplate exportTemplate;

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
        populateDefaultColumnsToWrite();
    }

    private void populateDefaultColumnsToWrite() {
        columnsToWriteMap.put(HEADER_NAME_ID, true);
        columnsToWriteMap.put(HEADER_NAME_PARENT_ID, true);
        columnsToWriteMap.put(HEADER_NAME_URI, true);
        columnsToWriteMap.put(HEADER_NAME_FILE_PATH, true);
        columnsToWriteMap.put(HEADER_NAME_NAME, true);
        columnsToWriteMap.put(HEADER_NAME_METHOD, true);
        columnsToWriteMap.put(HEADER_NAME_STATUS, true);
        columnsToWriteMap.put(HEADER_NAME_SIZE, true);
        columnsToWriteMap.put(HEADER_NAME_TYPE, true);
        columnsToWriteMap.put(HEADER_NAME_EXT, true);
        columnsToWriteMap.put(HEADER_NAME_LAST_MODIFIED, true);
        columnsToWriteMap.put(HEADER_NAME_EXTENSION_MISMATCH, true);
        columnsToWriteMap.put(HEADER_NAME_HASH, true);
        columnsToWriteMap.put(HEADER_NAME_FORMAT_COUNT, true);
        columnsToWriteMap.put(HEADER_NAME_PUID, true);
        columnsToWriteMap.put(HEADER_NAME_MIME_TYPE, true);
        columnsToWriteMap.put(HEADER_NAME_FORMAT_NAME, true);
        columnsToWriteMap.put(HEADER_NAME_FORMAT_VERSION, true);
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
                //what? unknown option
                log.warn("Unable to handle ExportOptions = " + options + ", was there a new option created?");
                options = ExportOptions.ONE_ROW_PER_FILE;
                writeOneRowPerFile(nodes);
            }
        }
    }
    
    private void writeOneRowPerFile(List<? extends ProfileResourceNode> nodes) {
        int maxIdCount = getMaxIdentificationCount(nodes);
        if (csvWriter.getRecordCount() == 0) {
            writeHeadersForOneRowPerFileExport(maxIdCount);
        }
        try {
            writeDataRowsForOneRowPerFileExport(nodes, maxIdCount);
        } catch (final TextWritingException e) {
            log.error(e.getRecordCharacters(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    //CHECKSTYLE:OFF - cyclomatic complexity is too high but we have to go through so many column names!!
    private void writeDataRowsForOneRowPerFileExport(List<? extends ProfileResourceNode> nodes, int maxIdCount) {
        if (exportTemplate != null) {
            Map<Integer, ExportTemplateColumnDef> columnPositions = exportTemplate.getColumnOrderMap();
            int maxCols = columnPositions.keySet().stream().max(Integer::compare).get();
            for (ProfileResourceNode node : nodes) {
                List<String> nodeEntries = new ArrayList<>();
                for (int i = 0; i <= maxCols; i++) {
                    ExportTemplateColumnDef def = columnPositions.get(i);
                    if (def.getColumnType() == ExportTemplateColumnDef.ColumnType.ConstantString) {
                        nodeEntries.add(def.getDataValue());
                    } else {
                        String columnName = def.getOriginalColumnName();
                        if (PER_FORMAT_HEADERS.contains(columnName)) {
                            addFormatColumnTemplate(nodeEntries, node, def, maxIdCount);
                        } else {
                            addNodeColumn(nodeEntries, node, def);
                        }
                    }
                }
                csvWriter.writeRow(nodeEntries);
            }
        } else {
            for (ProfileResourceNode node : nodes) {
                List<String> nodeEntries = new ArrayList<>();
                addNodeColumnsInDefaultOrder(nodeEntries, node);
                List<Format> formatIdentifications = node.getFormatIdentifications();
                for (int i = 0; i < maxIdCount; i++) {
                    addColumn(nodeEntries, HEADER_NAME_PUID,  (i < formatIdentifications.size()) ?  formatIdentifications.get(i).getPuid() : EMPTY_STRING);
                    addColumn(nodeEntries, HEADER_NAME_MIME_TYPE, (i < formatIdentifications.size()) ?  formatIdentifications.get(i).getMimeType() : EMPTY_STRING);
                    addColumn(nodeEntries, HEADER_NAME_FORMAT_NAME, (i < formatIdentifications.size()) ?  formatIdentifications.get(i).getName() : EMPTY_STRING);
                    addColumn(nodeEntries, HEADER_NAME_FORMAT_VERSION, (i < formatIdentifications.size()) ?  formatIdentifications.get(i).getVersion(): EMPTY_STRING);
                }
                csvWriter.writeRow(nodeEntries);
            }
        }
        csvWriter.flush();
    }

    private void addNodeColumn(List<String> nodeEntries, ProfileResourceNode node, ExportTemplateColumnDef def) {
        NodeMetaData metaData = node.getMetaData();
        if (!columnsToWriteMap.get(def.getOriginalColumnName())) {
            return;
        }
        String columnValue = "";
        switch (def.getOriginalColumnName()) {
            case HEADER_NAME_ID:
                columnValue = nullSafeNumber(node.getId());
                break;
            case HEADER_NAME_PARENT_ID:
                columnValue = nullSafeNumber(node.getParentId());
                break;
            case HEADER_NAME_URI:
                columnValue = DroidUrlFormat.format(node.getUri());
                break;
            case HEADER_NAME_FILE_PATH:
                columnValue = toFilePath(node.getUri());
                break;
            case HEADER_NAME_NAME:
                columnValue = toFileName(metaData.getName());
                break;
            case HEADER_NAME_METHOD:
                columnValue = nullSafeName(metaData.getIdentificationMethod());
                break;
            case HEADER_NAME_STATUS:
                columnValue = metaData.getNodeStatus().getStatus();
                break;
            case HEADER_NAME_SIZE:
                columnValue = nullSafeNumber(metaData.getSize());
                break;
            case HEADER_NAME_TYPE:
                columnValue = metaData.getResourceType().getResourceType();
                break;
            case HEADER_NAME_EXT:
                columnValue = metaData.getExtension();
                break;
            case HEADER_NAME_LAST_MODIFIED:
                columnValue = nullSafeDate(metaData.getLastModifiedDate(), dateFormat);
                break;
            case HEADER_NAME_EXTENSION_MISMATCH:
                columnValue = node.getExtensionMismatch().toString();
                break;
            case HEADER_NAME_HASH:
                columnValue = metaData.getHash();
                break;
            case HEADER_NAME_FORMAT_COUNT:
                columnValue = nullSafeNumber(node.getIdentificationCount());
                break;
        }
        nodeEntries.add(def.getOperatedValue(columnValue));
    }

    private void addFormatColumnTemplate(List<String> nodeEntries, ProfileResourceNode node, ExportTemplateColumnDef def, int maxIdCount) {
        List<Format> formats = node.getFormatIdentifications();
        if (!columnsToWriteMap.get(def.getOriginalColumnName())) {
            return;
        }
        for (int i = 0; i < maxIdCount; i++) {
            Format format = i < formats.size() ? formats.get(i) : null;
            nodeEntries.add(def.getOperatedValue(getFormatValue(def.getOriginalColumnName(), format)));
        }
    }

    private String getFormatValue(String columnName, Format format) {
        String retVal = EMPTY_STRING;
        switch (columnName) {
            case HEADER_NAME_PUID:
                retVal = format == null ? EMPTY_STRING : format.getPuid();
                break;
            case HEADER_NAME_MIME_TYPE:
                retVal = format == null ? EMPTY_STRING : format.getMimeType();
                break;
            case HEADER_NAME_FORMAT_NAME:
                retVal = format == null ? EMPTY_STRING : format.getName();
                break;
            case HEADER_NAME_FORMAT_VERSION:
                retVal = format == null ? EMPTY_STRING : format.getVersion();
        }
        return retVal;
    }

    //CHECKSTYLE:ON

    private void writeOneRowPerFormat(List<? extends ProfileResourceNode> nodes) {
        if (csvWriter.getRecordCount() == 0) {
            writeHeadersForOneRowPerFormatExport();
        }
        try {
            writeDataRowsForOneRowPerFormatExport(nodes);
            csvWriter.flush();
        } catch (final TextWritingException e) {
            log.error(e.getRecordCharacters(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void writeDataRowsForOneRowPerFormatExport(List<? extends ProfileResourceNode> nodes) {
        if (exportTemplate != null) {
            Map<Integer, ExportTemplateColumnDef> columnPositions = exportTemplate.getColumnOrderMap();
            int maxCols = columnPositions.keySet().stream().max(Integer::compare).get();
            for (ProfileResourceNode node : nodes) {
                List<String> nodeEntries = new ArrayList<>();
                for (Format format : node.getFormatIdentifications()) {
                    for (int i = 0; i <= maxCols; i++) {
                        ExportTemplateColumnDef def = columnPositions.get(i);
                        String columnName = def.getOriginalColumnName();
                        if (PER_FORMAT_HEADERS.contains(columnName)) {
                            String columnValue = getFormatValue(columnName, format);
                            nodeEntries.add(columnValue);
                        } else {
                            addNodeColumn(nodeEntries, node, def);
                        }
                    }
                }
                csvWriter.writeRow(nodeEntries);
            }
            csvWriter.flush();
        } else {
            for (ProfileResourceNode node : nodes) {
                for (Format format : node.getFormatIdentifications()) {
                    List<String> nodeEntries = new ArrayList<>();
                    addNodeColumnsInDefaultOrder(nodeEntries, node);
                    addColumn(nodeEntries, HEADER_NAME_PUID, format.getPuid());
                    addColumn(nodeEntries, HEADER_NAME_MIME_TYPE, format.getMimeType());
                    addColumn(nodeEntries, HEADER_NAME_FORMAT_NAME, format.getName());
                    addColumn(nodeEntries, HEADER_NAME_FORMAT_VERSION, format.getVersion());
                    csvWriter.writeRow(nodeEntries);
                }
            }
            csvWriter.flush();
        }
    }

    private void writeHeadersForOneRowPerFormatExport() {
        if (exportTemplate != null) {
            //for per format, there are no additional headers.
            List<String> headersToWrite = getHeadersToWriteFromTemplate(1);
            csvWriter.writeHeaders(headersToWrite);
        } else {
            List<String> headersToWrite = new ArrayList<>(getHeadersToWrite(allHeaders));
            csvWriter.writeHeaders(headersToWrite);
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
        // following Unix convention about line separators as previously
        format.setLineSeparator("\n");
        csvWriterSettings.setFormat(format);
        csvWriter = new CsvWriter(writer, csvWriterSettings);
        if (allHeaders == null) {
            allHeaders = Arrays.copyOf(HEADERS, HEADERS.length) ;
        }
    }

    private void writeHeadersForOneRowPerFileExport(int maxIdCount) {

        if (options != ExportOptions.ONE_ROW_PER_FILE) {
            throw new RuntimeException("Unexpectedly called per file header creation. Unable to proceed");
        }

        if (exportTemplate != null) {
            List<String> headersToWrite = getHeadersToWriteFromTemplate(maxIdCount);
            csvWriter.writeHeaders(headersToWrite);
        } else {
            List<String> headersToWrite = new ArrayList<>(getHeadersToWrite(allHeaders));
            // Arrays.stream(getHeadersToWrite(allHeaders)).collect(Collectors.toList());

            //if we are writing one row per file, then we tag the "per format" fields as additional columns,
            //if such columns need to be added, we create appropriate headers with a running suffix and write
            //them to the file
            if (!Collections.disjoint(headersToWrite, PER_FORMAT_HEADERS)) {
                if (maxIdCount > 1) { //add headers
                    for (int newColumnSuffix = 1; newColumnSuffix < maxIdCount; newColumnSuffix++) {
                        //"PUID","MIME_TYPE","FORMAT_NAME","FORMAT_VERSION"
                        for (String headerEntry : PER_FORMAT_HEADERS) {
                            if (headersToWrite.contains(headerEntry)) {
                                headersToWrite.add(headerEntry + newColumnSuffix);
                            }
                        }
                    }
                }
            }
            csvWriter.writeHeaders(headersToWrite);
        }
    }


    private static int getMaxIdentificationCount(List<? extends ProfileResourceNode> nodes) {
        Optional<Integer> maxIdentificationsOption = nodes.stream().map(ProfileResourceNode::getIdentificationCount).collect(Collectors.toList()).stream().filter(Objects::nonNull).max(Integer::compare);
        int maxIdentifications = 0;
        if (maxIdentificationsOption.isPresent()) {
            maxIdentifications = maxIdentificationsOption.get();
        }
        return maxIdentifications;
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
        return value == null ? EMPTY_STRING : value.toString();
    }
    
    private static String nullSafeNumber(Number number) {
        return number == null ? EMPTY_STRING : number.toString();
    }
    
    private static String nullSafeDate(Date date, FastDateFormat format) {
        return date == null ? EMPTY_STRING : format.format(date);
    }
    
    private static String toFileName(String name) {
        return FilenameUtils.getName(name);
    }

    private String toFilePath(URI uri) {
        if (uri == null) {
            log.warn("[URI not set]");
            return EMPTY_STRING;
        }
        if (FILE_URI_SCHEME.equals(uri.getScheme())) {
            return Paths.get(uri).toAbsolutePath().toString();
        }

        // for URIs that have other than "file" scheme
        String result = java.net.URLDecoder.decode(uri.toString()).replaceAll("file://", EMPTY_STRING);
        result = result.replace("/", File.separator);

        // Handle substitution of 7z
        final String sevenZedIdentifier = "sevenz:";
        if (result.startsWith(sevenZedIdentifier)) {
            result = "7z:" + result.substring(sevenZedIdentifier.length());
        }

        return result;
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

        if (this.allHeaders == null) {
            this.allHeaders = Arrays.copyOf(HEADERS, HEADERS.length);
        }

        // The header for hash is modified based on algorithm used to generate the hash
        String hashHeader = headersToSet.get("hash");
        if (hashHeader != null) {
            if (exportTemplate != null) {
                Map<Integer, ExportTemplateColumnDef> columnPositions = exportTemplate.getColumnOrderMap();
                List<Map.Entry<Integer, ExportTemplateColumnDef>> profileCols = columnPositions.entrySet()
                        .stream()
                        .filter(e -> e.getValue().getColumnType() == ExportTemplateColumnDef.ColumnType.ProfileResourceNode)
                        .collect(Collectors.toList());
                List<Map.Entry<Integer, ExportTemplateColumnDef>> hashEntry = profileCols.stream()
                        .filter(entry -> entry.getValue().getOriginalColumnName().equals(HEADER_NAME_HASH))
                        .collect(Collectors.toList());
                if (!hashEntry.isEmpty()) {
                    this.allHeaders[HASH_ARRAY_INDEX] = hashHeader;
                }
            } else {
                this.allHeaders[HASH_ARRAY_INDEX] = hashHeader;
            }
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
            populateDefaultColumnsToWrite();
            numColumnsToWrite = columnsToWriteMap.size();
        } else {
            for (int i = 0; i < HEADERS.length; i++) {
                String currentHeader = HEADERS[i];
                if (headersToWrite.contains(currentHeader)) {
                    columnsToWriteMap.put(currentHeader, true);
                    headersToWrite.remove(currentHeader);
                } else {
                    columnsToWriteMap.put(currentHeader, false);
                }
            }
            // Defence: if no valid column names were specified, log a warning then write them all out:
            if (!columnsToWriteMap.containsValue(true)) {
                populateDefaultColumnsToWrite();
                numColumnsToWrite = columnsToWriteMap.size();
                log.warn("-co option: no CSV columns specified are valid, writing all columns: " + columnNames);
            } else {
                numColumnsToWrite = (int) columnsToWriteMap.values().stream().filter(eachVal -> eachVal).count();
                // If there are some columns specified left over, they aren't valid columns - log a warning:
                if (headersToWrite.size() > 0) {
                    String invalidHeaders = String.join(BLANK_SPACE_DELIMITER, headersToWrite);
                    log.warn("-co option - some CSV columns specified were invalid: " + invalidHeaders);
                }
            }
        }
    }

    @Override
    public void setExportTemplate(ExportTemplate template) {
        this.exportTemplate = template;
//        Map<Integer, ExportTemplateColumnDef> columnPositions = template.getColumnOrderMap();
//        List<String> allColumns = columnPositions.values().stream().map(c -> c.getOriginalColumnName()).collect(Collectors.toList());
//        setColumnsToWrite(String.join(BLANK_SPACE_DELIMITER, allColumns));
    }

    private Set<String> getColumnsToWrite(String columnNames) {
        if (columnNames != null && !columnNames.isEmpty()) {
            String[] columns = columnNames.split(BLANK_SPACE_DELIMITER);
            if (columns.length > 0) {
                return Arrays.stream(columns).map(name -> name.toUpperCase(Locale.ROOT)).collect(Collectors.toSet());
            }
        }
        return null;
    }

    private List<String> getHeadersToWriteFromTemplate(int maxIdCount) {
        if (exportTemplate == null) {
            throw new IllegalArgumentException("Export template does not exist, unable to get headers from template");
        }
        List<String> retVal = new LinkedList<>();

        for (int i = 0; i < exportTemplate.getColumnOrderMap().size(); i++) {
            ExportTemplateColumnDef def = exportTemplate.getColumnOrderMap().get(i);
            if (def.getHeaderLabel().equals(HEADER_NAME_HASH)) {
                retVal.add(allHeaders[HASH_ARRAY_INDEX]);
            } else {
                retVal.add(def.getHeaderLabel());
            }
            if (def.getColumnType() != ExportTemplateColumnDef.ColumnType.ConstantString) {
                if (PER_FORMAT_HEADERS.contains(def.getOriginalColumnName())) {
                    for (int newColumnSuffix = 1; newColumnSuffix < maxIdCount; newColumnSuffix++) {
                        retVal.add(def.getHeaderLabel() + newColumnSuffix);
                    }
                }
            }
        }
        return retVal;
    }

    private List<String> getHeadersToWrite(String[] headersToWrite) {
        if (numColumnsToWrite <  columnsToWriteMap.size()) {
            String[] newHeaders = new String[numColumnsToWrite];
            int newHeaderIndex = 0;
            for (int i = 0; i < HEADERS.length; i++) {
                if (columnsToWriteMap.get(HEADERS[i])) {
                    newHeaders[newHeaderIndex++] = allHeaders[i];
                }
            }
            return Arrays.stream(newHeaders).collect(Collectors.toList());
        }
        return Arrays.stream(headersToWrite).collect(Collectors.toList());
    }

    private void addNodeColumnsInDefaultOrder(List<String> row, ProfileResourceNode node) {
        NodeMetaData metaData = node.getMetaData();
        addColumn(row, HEADER_NAME_ID, nullSafeNumber(node.getId()));
        addColumn(row, HEADER_NAME_PARENT_ID, nullSafeNumber(node.getParentId()));
        addColumn(row, HEADER_NAME_URI, DroidUrlFormat.format(node.getUri()));
        addColumn(row, HEADER_NAME_FILE_PATH, toFilePath(node.getUri()));
        addColumn(row, HEADER_NAME_NAME, toFileName(metaData.getName()));
        addColumn(row, HEADER_NAME_METHOD, nullSafeName(metaData.getIdentificationMethod()));
        addColumn(row, HEADER_NAME_STATUS, metaData.getNodeStatus().getStatus());
        addColumn(row, HEADER_NAME_SIZE, nullSafeNumber(metaData.getSize()));
        addColumn(row, HEADER_NAME_TYPE, metaData.getResourceType().getResourceType());
        addColumn(row, HEADER_NAME_EXT, metaData.getExtension());
        addColumn(row, HEADER_NAME_LAST_MODIFIED, nullSafeDate(metaData.getLastModifiedDate(), dateFormat));
        addColumn(row, HEADER_NAME_EXTENSION_MISMATCH, node.getExtensionMismatch().toString());
        addColumn(row, HEADER_NAME_HASH, metaData.getHash());
        addColumn(row, HEADER_NAME_FORMAT_COUNT, nullSafeNumber(node.getIdentificationCount()));
    }

    private void addColumn(List<String> row, String columnName, String value) {
        if (columnsToWriteMap.get(columnName)) {
            row.add(value);
        }
    }
}

