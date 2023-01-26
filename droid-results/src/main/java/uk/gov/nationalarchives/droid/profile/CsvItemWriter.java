//CHECKSTYLE:OFF

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

import java.io.Writer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.univocity.parsers.common.TextWritingException;
import com.univocity.parsers.csv.CsvFormat;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

import uk.gov.nationalarchives.droid.core.interfaces.util.DroidUrlFormat;
import uk.gov.nationalarchives.droid.export.interfaces.ExportOptions;
import uk.gov.nationalarchives.droid.profile.referencedata.Format;

/**
 * This class wraps a com.univocity.parsers.csv.CsvWriter.
 * It can output in normalised form: one row per file-and-format combination (fixed width). 
 * It can output in denormalised form: one row per file with all matching formats (variable width).
 * In denormalised form files (rows) with fewer matched formats are padded with empty values to keep a constant width under the headers. 
 * There is an option to quote all values.
 * There is an option to set a custom header for the hash column.  
 */
public class CsvItemWriter
{
    private static final FastDateFormat DATE_FORMAT = DateFormatUtils.ISO_DATETIME_FORMAT;
    private static final String FILE_URI_SCHEME = "file";
    private static final Logger LOG = LoggerFactory.getLogger(CsvItemWriter.class);

    private static abstract class ColumnType
    {
        private final String header;

        private ColumnType(String header)
        {
            this.header = header;
        }
        
        abstract String getValue(ProfileResourceNode profileResourceNode, int formatIndex);
    }
    
    private static class FixedColumnType extends ColumnType
    {
        private interface FixedFieldWriter
        {
            Object value(ProfileResourceNode profileResourceNode);
        }
        
        private static final FixedColumnType HASH_COLUMN = new FixedColumnType("HASH", (node) -> node.getMetaData().getHash()); 
        
        private static final FixedColumnType[] FIXED_COLUMNS = {
                new FixedColumnType("ID", (node) -> node.getId()),
                new FixedColumnType("PARENT_ID", (node) -> node.getParentId()),
                new FixedColumnType("URI", (node) -> DroidUrlFormat.format(node.getUri())),
                new FixedColumnType("FILE_PATH", (node) -> FILE_URI_SCHEME.equals(node.getUri().getScheme()) ? Paths.get(node.getUri()).toAbsolutePath() : null),
                new FixedColumnType("NAME", (node) -> FilenameUtils.getName(node.getMetaData().getName())),
                new FixedColumnType("METHOD", (node) -> node.getMetaData().getIdentificationMethod()),
                new FixedColumnType("STATUS", (node) -> node.getMetaData().getNodeStatus().getStatus()),
                new FixedColumnType("SIZE", (node) -> node.getMetaData().getSize()),
                new FixedColumnType("TYPE", (node) -> node.getMetaData().getResourceType().getResourceType()),
                new FixedColumnType("EXT", (node) -> node.getMetaData().getExtension()),
                new FixedColumnType("LAST_MODIFIED", (node) -> node.getMetaData().getLastModifiedDate() != null ? DATE_FORMAT.format(node.getMetaData().getLastModifiedDate()) : null),
                new FixedColumnType("EXTENSION_MISMATCH", (node) -> node.getExtensionMismatch()),
                HASH_COLUMN,
                new FixedColumnType("FORMAT_COUNT", (node) -> node.getIdentificationCount())
        };
    
        private final FixedFieldWriter fieldWriter;
        
        private FixedColumnType(String header, FixedFieldWriter fieldWriter)
        {
            super(header);
            this.fieldWriter = fieldWriter;
        }
        
        private static List<ColumnInstance> createColumnInstances()
        {
            final List<ColumnInstance> result = new ArrayList<>();
            for (FixedColumnType fixedColumnType : FIXED_COLUMNS)
                result.add(new ColumnInstance(fixedColumnType));
            return Collections.unmodifiableList(result);
        }
        
        @Override
        String getValue(ProfileResourceNode profileResourceNode, int formatIndex)
        {
            // The format index is ignored for fixed columns
            
            final Object rawValue = fieldWriter.value(profileResourceNode);
            if (rawValue == null)
                return "";
            return rawValue.toString();
        }
    }
    
    private static class PerFormatColumnType extends ColumnType
    {
        private static interface PerFormatFieldWriter
        {
            String value(Format format);
        }
        
        private static final PerFormatColumnType[] PER_FORMAT_COLUMNS = {
                new PerFormatColumnType("PUID", (format) -> format.getPuid()),
                new PerFormatColumnType("MIME_TYPE", (format) -> format.getMimeType()),
                new PerFormatColumnType("FORMAT_NAME", (format) -> format.getName()),
                new PerFormatColumnType("FORMAT_VERSION", (format) -> format.getVersion()),
        };
        
        private final PerFormatFieldWriter fieldWriter;
        
        private PerFormatColumnType(String header, PerFormatFieldWriter fieldWriter)
        {
            super(header);
            this.fieldWriter = fieldWriter;
        }
        
        private static List<ColumnInstance> createColumnInstances()
        {
            final List<ColumnInstance> result = new ArrayList<>();
            for (ColumnType columnType : PER_FORMAT_COLUMNS)
                result.add(new ColumnInstance(columnType));
            return Collections.unmodifiableList(result);
        }
        
        @Override
        String getValue(ProfileResourceNode profileResourceNode, int formatIndex)
        {
            // A null identification count means zero matches found
            if (profileResourceNode.getIdentificationCount() == null)
                return "";
            
            // If the format index requested for this node is out of range then we just pad the row with empty values
            if (formatIndex >= profileResourceNode.getIdentificationCount())
                return "";
            
            return fieldWriter.value(profileResourceNode.getFormatIdentifications().get(formatIndex));
        }
    }

    /**
     * Each instance of the CsvItemWriter has instance versions which hold the column state 
     */
    private static class ColumnInstance
    {
        private final ColumnType columnType;
        private boolean active;
        private String customHeader;

        private ColumnInstance(ColumnType columnType)
        {
            this.columnType = columnType;
            active = true;
            customHeader = columnType.header;
        }

        public void activateIfListed(List<String> includedHeaderValues) 
        {
            active = includedHeaderValues.contains(customHeader);
        }

        public String write(ProfileResourceNode profileResourceNode, int formatIndex) 
        {
            return columnType.getValue(profileResourceNode, formatIndex);
        }
    }

    // These columns are present at the start of every row
    private final List<ColumnInstance> fixedColumnInstances = FixedColumnType.createColumnInstances();
    
    // These columns may be used multiple times, once for each matched format
    private final List<ColumnInstance> perFormatColumnInstances = PerFormatColumnType.createColumnInstances();

    // These field values control how the output is written
    private CsvWriter csvWriter;
    private ExportOptions exportOption = ExportOptions.ONE_ROW_PER_FILE;
    private boolean quoteAllFields = true;

    public CsvItemWriter() 
    {
       this(null);
    }

    public CsvItemWriter(CsvWriter writer) 
    {
        this.csvWriter = writer;
    }
    
    public void setCsvWriter(CsvWriter csvWriter) 
    {
        this.csvWriter = csvWriter;
    }

    public void setOptions(ExportOptions exportOption) 
    {
        this.exportOption = exportOption;
    }

    public void setQuoteAllFields(boolean quoteAll) 
    {
        this.quoteAllFields = quoteAll;
    }
    
    public void setHeaders(Map<String, String> headersToSet) 
    {
        // We receive a map of standard -> custom header values.
        // However, all we do is look in the map for a key called "hash" and override that column with the corresponding value in the set
        final String hashHeaderValue = headersToSet.get("hash");
        if (hashHeaderValue != null)
            for (ColumnInstance fixedColumnInstance : fixedColumnInstances)
                if (fixedColumnInstance.columnType == FixedColumnType.HASH_COLUMN)
                    fixedColumnInstance.customHeader = hashHeaderValue;
    }

    public void setColumnsToWrite(String spaceSeparatedListOfHeaderValuesToControlWhichColumnsToIncludeInTheOutput) 
    {
        // For backwards compatibility, setting included columns to null or an empty string activates all columns (they are already active by default)
        if (spaceSeparatedListOfHeaderValuesToControlWhichColumnsToIncludeInTheOutput == null)
            return;
        final List<String> includedHeaderValues = Arrays.asList(spaceSeparatedListOfHeaderValuesToControlWhichColumnsToIncludeInTheOutput.split(" "));
        if (includedHeaderValues.isEmpty())
            return;
        
        // Some columns have been selected to filter in, so let them look for their own identifier in the list of included columns
        for (ColumnInstance fixedColumnInstance : fixedColumnInstances)
            fixedColumnInstance.activateIfListed(includedHeaderValues);
        for (ColumnInstance perFormatColumnInstance : perFormatColumnInstances)
            perFormatColumnInstance.activateIfListed(includedHeaderValues);
    }
    
    public void open(final Writer writer) 
    {
        // Prepare the CSV writer settings
        final CsvWriterSettings csvWriterSettings = new CsvWriterSettings();
        csvWriterSettings.setQuoteAllFields(quoteAllFields);
        
        // Following Unix convention on line separators as previously
        CsvFormat format = new CsvFormat();
        format.setLineSeparator("\n");
        csvWriterSettings.setFormat(format);
        
        // Create the wrapped CSV writer
        csvWriter = new CsvWriter(writer, csvWriterSettings);
    }

    public void write(List<ProfileResourceNode> nodes) 
    {
        // Write according to the export option
        if (exportOption == ExportOptions.ONE_ROW_PER_FILE)
            writeAsOneRowPerFile(nodes);
        else
            writeAsOneRowPerFormat(nodes);

        // Indicate that we have finished writing
        csvWriter.flush();
    }
    
    public void close() 
    {
        csvWriter.close();
    }
    
    private void writeAsOneRowPerFormat(List<ProfileResourceNode> profileResourceNodes) 
    {
        // Write all of the headers
        final List<String> allHeaders = new ArrayList<>();
        for (ColumnInstance columnInstance : fixedColumnInstances)
            if (columnInstance.active)
                allHeaders.add(columnInstance.customHeader);
        for (ColumnInstance columnInstance : perFormatColumnInstances)
            if (columnInstance.active)
                allHeaders.add(columnInstance.customHeader);
        csvWriter.writeHeaders(allHeaders);
        
        // Write all of the value rows
        try 
        {
            for (ProfileResourceNode node : profileResourceNodes) 
                writeEachFormatsOnASeparateLine(node);
        } 
        catch (final TextWritingException e) 
        {
            LOG.error(e.getRecordCharacters(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void writeEachFormatsOnASeparateLine(ProfileResourceNode profileResourceNode) 
    {
        // For backwards compatibility, if a file has no identifiable format, we still write a single blank format
        final int identificationCount = profileResourceNode.getIdentificationCount() != null ? profileResourceNode.getIdentificationCount() : 1;
        
        for (int formatIndex = 0; formatIndex < identificationCount; formatIndex++) 
        {
            final List<String> rowValues = new ArrayList<>();
            
            // Write the fixed values
            for (ColumnInstance fixedColumnInstance : fixedColumnInstances)
                rowValues.add(fixedColumnInstance.write(profileResourceNode, formatIndex));
            
            // Write the per format values
            for (ColumnInstance perFormatColumnInstance : perFormatColumnInstances)
                rowValues.add(perFormatColumnInstance.write(profileResourceNode, formatIndex));
            
            csvWriter.writeRow(rowValues);
        }
    }

    private void writeAsOneRowPerFile(List<ProfileResourceNode> profileResourceNodes)
    {
        // Find the node with the most formats so we know how many headers to write
        int largestCountOfFormats = 0;
        for (ProfileResourceNode profileResourceNode : profileResourceNodes)
            if (profileResourceNode.getIdentificationCount() > largestCountOfFormats)
                largestCountOfFormats = profileResourceNode.getIdentificationCount(); 
        final int numberOfFormatsToWrite = largestCountOfFormats;
        
        // Write the header row
        final List<String> allHeaders = new ArrayList<>();
        for (ColumnInstance columnInstance : fixedColumnInstances)
            if (columnInstance.active)
                allHeaders.add(columnInstance.customHeader);
        for (int formatIndex = 0; formatIndex < numberOfFormatsToWrite; formatIndex++)
            for (ColumnInstance columnInstance : perFormatColumnInstances)
                if (columnInstance.active)
                    allHeaders.add(columnInstance.customHeader + (formatIndex > 0 ? formatIndex + 1 : ""));
        csvWriter.writeHeaders(allHeaders);
        
        // Write the value rows
        for (ProfileResourceNode profileResourceNode : profileResourceNodes)
        {
            final List<String> rowValues = new ArrayList<>();
            
            // Write the fixed values (the format index is ignored for the fixed values, so we use zero)
            for (ColumnInstance fixedColumnInstance : fixedColumnInstances)
                rowValues.add(fixedColumnInstance.write(profileResourceNode, 0));
            
            // Write the per format values
            for (int formatIndex = 0; formatIndex < numberOfFormatsToWrite; formatIndex++)
                for (ColumnInstance perFormatColumnInstance : perFormatColumnInstances)
                    rowValues.add(perFormatColumnInstance.write(profileResourceNode, formatIndex));
            
            csvWriter.writeRow(rowValues);
        }
    }
}

//CHECKSTYLE:ON
