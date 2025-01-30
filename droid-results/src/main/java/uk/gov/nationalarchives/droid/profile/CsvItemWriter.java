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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalConfig;
import uk.gov.nationalarchives.droid.export.interfaces.ExportOptions;
import uk.gov.nationalarchives.droid.export.interfaces.ExportTemplate;
import uk.gov.nationalarchives.droid.export.interfaces.ExportTemplateColumnDef;
import uk.gov.nationalarchives.droid.export.interfaces.ItemWriter;
import uk.gov.nationalarchives.droid.profile.datawriter.DataWriterProvider;
import uk.gov.nationalarchives.droid.profile.datawriter.FormattedDataWriter;

import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author rflitcroft
 *
 */
public class CsvItemWriter implements ItemWriter<ProfileResourceNode> {

    private static final String BLANK_SPACE_DELIMITER       = " ";
    private final Map<String, Boolean> columnsToWriteMap = new HashMap<>();

    private final Logger log = LoggerFactory.getLogger(getClass());

    private CsvWriter csvWriter;
    private ExportOptions options = ExportOptions.ONE_ROW_PER_FILE;
    
    private String[] allHeaders;
    private boolean quoteAllFields;
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
        populateDefaultColumnsToWrite();
    }

    private void populateDefaultColumnsToWrite() {
        columnsToWriteMap.put(CsvWriterConstants.HEADER_NAME_ID, true);
        columnsToWriteMap.put(CsvWriterConstants.HEADER_NAME_PARENT_ID, true);
        columnsToWriteMap.put(CsvWriterConstants.HEADER_NAME_URI, true);
        columnsToWriteMap.put(CsvWriterConstants.HEADER_NAME_FILE_PATH, true);
        columnsToWriteMap.put(CsvWriterConstants.HEADER_NAME_NAME, true);
        columnsToWriteMap.put(CsvWriterConstants.HEADER_NAME_METHOD, true);
        columnsToWriteMap.put(CsvWriterConstants.HEADER_NAME_STATUS, true);
        columnsToWriteMap.put(CsvWriterConstants.HEADER_NAME_SIZE, true);
        columnsToWriteMap.put(CsvWriterConstants.HEADER_NAME_TYPE, true);
        columnsToWriteMap.put(CsvWriterConstants.HEADER_NAME_EXT, true);
        columnsToWriteMap.put(CsvWriterConstants.HEADER_NAME_LAST_MODIFIED, true);
        columnsToWriteMap.put(CsvWriterConstants.HEADER_NAME_EXTENSION_MISMATCH, true);
        columnsToWriteMap.put(CsvWriterConstants.HEADER_NAME_HASH, true);
        columnsToWriteMap.put(CsvWriterConstants.HEADER_NAME_FORMAT_COUNT, true);
        columnsToWriteMap.put(CsvWriterConstants.HEADER_NAME_PUID, true);
        columnsToWriteMap.put(CsvWriterConstants.HEADER_NAME_MIME_TYPE, true);
        columnsToWriteMap.put(CsvWriterConstants.HEADER_NAME_FORMAT_NAME, true);
        columnsToWriteMap.put(CsvWriterConstants.HEADER_NAME_FORMAT_VERSION, true);
    }

    @Override
    public void write(List<? extends ProfileResourceNode> nodes) {
        FormattedDataWriter dataWriter = DataWriterProvider.getDataWriter(columnsToWriteMap, exportTemplate);
        switch (options) {
            case ONE_ROW_PER_FILE: {
                writeOneRowPerFile(nodes, dataWriter);
                break;
            }
            case ONE_ROW_PER_FORMAT: {
                writeOneRowPerFormat(nodes, dataWriter);
                break;
            }
            default: {
                //what? unknown option
                log.warn("Unable to handle ExportOptions = " + options + ", was there a new option created?");
                options = ExportOptions.ONE_ROW_PER_FILE;
                writeOneRowPerFile(nodes, dataWriter);
            }
        }
    }
    
    private void writeOneRowPerFile(List<? extends ProfileResourceNode> nodes, FormattedDataWriter dataWriter) {
        if (csvWriter.getRecordCount() == 0) {
            dataWriter.writeHeadersForOneRowPerFile(nodes, allHeaders, csvWriter);
        }
        try {
            dataWriter.writeDataRowsForOneRowPerFile(nodes, csvWriter);
        } catch (final TextWritingException e) {
            log.error(e.getRecordCharacters(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void writeOneRowPerFormat(List<? extends ProfileResourceNode> nodes, FormattedDataWriter dataWriter) {
        if (csvWriter.getRecordCount() == 0) {
            dataWriter.writeHeadersForOneRowPerFormat(nodes, allHeaders, csvWriter);
        }
        try {
            dataWriter.writeDataRowsForOneRowPerFormat(nodes, csvWriter);
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
        // following Unix convention about line separators as previously
        format.setLineSeparator("\n");
        csvWriterSettings.setFormat(format);
        csvWriter = new CsvWriter(writer, csvWriterSettings);
        if (allHeaders == null) {
            allHeaders = Arrays.copyOf(CsvWriterConstants.HEADERS, CsvWriterConstants.HEADERS.length) ;
        }
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
            this.allHeaders = Arrays.copyOf(CsvWriterConstants.HEADERS, CsvWriterConstants.HEADERS.length);
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
                        .filter(entry -> entry.getValue().getOriginalColumnName().equals(CsvWriterConstants.HEADER_NAME_HASH))
                        .collect(Collectors.toList());
                if (!hashEntry.isEmpty()) {
                    this.allHeaders[CsvWriterConstants.HASH_ARRAY_INDEX] = hashHeader;
                }
            } else {
                this.allHeaders[CsvWriterConstants.HASH_ARRAY_INDEX] = hashHeader;
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
        } else {
            for (int i = 0; i < CsvWriterConstants.HEADERS.length; i++) {
                String currentHeader = CsvWriterConstants.HEADERS[i];
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
                log.warn("-co option: no CSV columns specified are valid, writing all columns: " + columnNames);
            } else {
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
}

