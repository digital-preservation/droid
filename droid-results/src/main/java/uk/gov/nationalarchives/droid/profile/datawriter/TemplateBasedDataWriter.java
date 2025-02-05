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
package uk.gov.nationalarchives.droid.profile.datawriter;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.univocity.parsers.csv.CsvWriter;
import uk.gov.nationalarchives.droid.core.interfaces.util.DroidUrlFormat;
import uk.gov.nationalarchives.droid.export.interfaces.ExportTemplate;
import uk.gov.nationalarchives.droid.export.interfaces.ExportTemplateColumnDef;
import uk.gov.nationalarchives.droid.profile.CsvWriterConstants;
import uk.gov.nationalarchives.droid.profile.NodeMetaData;
import uk.gov.nationalarchives.droid.profile.ProfileResourceNode;
import uk.gov.nationalarchives.droid.profile.referencedata.Format;

import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class implements the methods for writing headers and data when the export is done using an export template.
 */
public class TemplateBasedDataWriter extends FormattedDataWriter{
    private final ExportTemplate template;
    public TemplateBasedDataWriter(ExportTemplate exportTemplate) {
        this.template = exportTemplate;
    }

    @Override
    public void writeDataRowsForOneRowPerFile(List<? extends ProfileResourceNode> nodes, CsvWriter csvWriter) {
        int maxIdCount = getMaxIdentificationCount(nodes);
        Map<Integer, ExportTemplateColumnDef> columnPositions = template.getColumnOrderMap();
        int maxCols = columnPositions.keySet().stream().max(Integer::compare).get();
        for (ProfileResourceNode node : nodes) {
            List<String> nodeEntries = getOneRowPerFileNodeEntries(node, maxCols, columnPositions, maxIdCount);
            csvWriter.writeRow(nodeEntries);
        }
        csvWriter.flush();
    }

    @Override
    public void writeJsonForOneRowPerFile(List<? extends ProfileResourceNode> nodes, String[] headers, Writer writer) {
        OutputJson outputJson = new OutputJson();

        int maxIdCount = getMaxIdentificationCount(nodes);
        List<String> headersToWrite = getHeadersToWrite(maxIdCount);
        Map<Integer, ExportTemplateColumnDef> columnPositions = template.getColumnOrderMap();
        int maxCols = columnPositions.keySet().stream().max(Integer::compare).get();
        for (ProfileResourceNode node : nodes) {
            List<String> nodeEntries = getOneRowPerFileNodeEntries(node, maxCols, columnPositions, maxIdCount);
            ObjectNode rowNode = outputJson.createObjectNode();
            for (int i=0; i < headersToWrite.size(); i++) {
                rowNode.put(headersToWrite.get(i), nodeEntries.get(i));
            }
            outputJson.getArrayNode().add(rowNode);
        }
        outputJson.writeJson(writer);

    }

    @Override
    public void writeDataRowsForOneRowPerFormat(List<? extends ProfileResourceNode> nodes, CsvWriter csvWriter) {
        Map<Integer, ExportTemplateColumnDef> columnPositions = template.getColumnOrderMap();
        int maxCols = columnPositions.keySet().stream().max(Integer::compare).get();
        for (ProfileResourceNode node : nodes) {
            for (Format format : node.getFormatIdentifications()) {
                List<String> nodeEntries = getOneRowPerFormatNodeEntries(node, format, maxCols, columnPositions);
                csvWriter.writeRow(nodeEntries);
            }
        }
        csvWriter.flush();
    }

    @Override
    public void writeJsonForOneRowPerFormat(List<? extends ProfileResourceNode> nodes, String[] headers, Writer writer) {
        super.setCustomisedHeaders(headers);
        OutputJson outputJson = new OutputJson();
        int maxIdCount = 1;
        List<String> headersToWrite = getHeadersToWrite(maxIdCount);
        Map<Integer, ExportTemplateColumnDef> columnPositions = template.getColumnOrderMap();
        int maxCols = columnPositions.keySet().stream().max(Integer::compare).get();
        for (ProfileResourceNode node : nodes) {
            for (Format format : node.getFormatIdentifications()) {
                List<String> nodeEntries = getOneRowPerFormatNodeEntries(node, format, maxCols, columnPositions);
                ObjectNode rowNode = outputJson.createObjectNode();
                for (int i=0; i < headersToWrite.size(); i++) {
                    rowNode.put(headersToWrite.get(i), nodeEntries.get(i));
                }
                outputJson.getArrayNode().add(rowNode);
            }
        }
        outputJson.writeJson(writer);
    }

    @Override
    public void writeHeadersForOneRowPerFile(List<? extends ProfileResourceNode> nodes, String[] headers, CsvWriter csvWriter) {
        super.setCustomisedHeaders(headers);
        int maxIdCount = getMaxIdentificationCount(nodes);
        List<String> headersToWrite = getHeadersToWrite(maxIdCount);
        csvWriter.writeHeaders(headersToWrite);
        csvWriter.flush();
    }

    @Override
    public void writeHeadersForOneRowPerFormat(List<? extends ProfileResourceNode> nodes, String[] headers, CsvWriter csvWriter) {
        super.setCustomisedHeaders(headers);
        int maxIdColumns = 1; //for "per format" export, the additional identified formats are written as new row hence no additional id headers
        List<String> headersToWrite = getHeadersToWrite(maxIdColumns);
        csvWriter.writeHeaders(headersToWrite);
        csvWriter.flush();
    }

    private List<String> getOneRowPerFormatNodeEntries(ProfileResourceNode node, Format format, int maxCols, Map<Integer, ExportTemplateColumnDef> columnPositions) {
        List<String> nodeEntries = new ArrayList<>();
        for (int i = 0; i <= maxCols; i++) {
            ExportTemplateColumnDef def = columnPositions.get(i);
            if (def.getColumnType() == ExportTemplateColumnDef.ColumnType.ConstantString) {
                nodeEntries.add(def.getDataValue());
            } else {
                String columnName = def.getOriginalColumnName();
                if (CsvWriterConstants.PER_FORMAT_HEADERS.contains(columnName)) {
                    String columnValue = getFormatValue(columnName, format);
                    nodeEntries.add(columnValue);
                } else {
                    addNodeColumn(nodeEntries, node, def);
                }
            }
        }
        return nodeEntries;
    }

    private List<String> getOneRowPerFileNodeEntries(ProfileResourceNode node, int maxCols, Map<Integer, ExportTemplateColumnDef> columnPositions, int maxIdCount) {
        List<String> nodeEntries = new ArrayList<>();
        for (int i = 0; i <= maxCols; i++) {
            ExportTemplateColumnDef def = columnPositions.get(i);
            if (def.getColumnType() == ExportTemplateColumnDef.ColumnType.ConstantString) {
                nodeEntries.add(def.getDataValue());
            } else {
                String columnName = def.getOriginalColumnName();
                if (CsvWriterConstants.PER_FORMAT_HEADERS.contains(columnName)) {
                    addFormatColumnTemplate(nodeEntries, node, def, maxIdCount);
                } else {
                    addNodeColumn(nodeEntries, node, def);
                }
            }
        }
        return nodeEntries;
    }

    private void addFormatColumnTemplate(List<String> nodeEntries, ProfileResourceNode node, ExportTemplateColumnDef def, int maxIdCount) {
        List<Format> formats = node.getFormatIdentifications();
        for (int i = 0; i < maxIdCount; i++) {
            Format format = i < formats.size() ? formats.get(i) : null;
            nodeEntries.add(def.getOperatedValue(getFormatValue(def.getOriginalColumnName(), format)));
        }
    }

    //CHECKSTYLE:OFF Swith without default
    private String getFormatValue(String columnName, Format format) {
        String retVal = CsvWriterConstants.EMPTY_STRING;
        switch (columnName) {
            case CsvWriterConstants.HEADER_NAME_PUID:
                retVal = format == null ? CsvWriterConstants.EMPTY_STRING : format.getPuid();
                break;
            case CsvWriterConstants.HEADER_NAME_MIME_TYPE:
                retVal = format == null ? CsvWriterConstants.EMPTY_STRING : format.getMimeType();
                break;
            case CsvWriterConstants.HEADER_NAME_FORMAT_NAME:
                retVal = format == null ? CsvWriterConstants.EMPTY_STRING : format.getName();
                break;
            case CsvWriterConstants.HEADER_NAME_FORMAT_VERSION:
                retVal = format == null ? CsvWriterConstants.EMPTY_STRING : format.getVersion();
        }
        return retVal;
    }
    //CHECKSTYLE:ON

    //CHECKSTYLE:OFF - cyclomatic complexity is too high but we have to go through so many column names!!
    private void addNodeColumn(List<String> nodeEntries, ProfileResourceNode node, ExportTemplateColumnDef def) {
        NodeMetaData metaData = node.getMetaData();

        String columnValue = "";
        switch (def.getOriginalColumnName()) {
            case CsvWriterConstants.HEADER_NAME_ID:
                columnValue = nullSafeNumber(node.getId());
                break;
            case CsvWriterConstants.HEADER_NAME_PARENT_ID:
                columnValue = nullSafeNumber(node.getParentId());
                break;
            case CsvWriterConstants.HEADER_NAME_URI:
                columnValue = DroidUrlFormat.format(node.getUri());
                break;
            case CsvWriterConstants.HEADER_NAME_FILE_PATH:
                columnValue = toFilePath(node.getUri());
                break;
            case CsvWriterConstants.HEADER_NAME_NAME:
                columnValue = toFileName(metaData.getName());
                break;
            case CsvWriterConstants.HEADER_NAME_METHOD:
                columnValue = nullSafeName(metaData.getIdentificationMethod());
                break;
            case CsvWriterConstants.HEADER_NAME_STATUS:
                columnValue = metaData.getNodeStatus().getStatus();
                break;
            case CsvWriterConstants.HEADER_NAME_SIZE:
                columnValue = nullSafeNumber(metaData.getSize());
                break;
            case CsvWriterConstants.HEADER_NAME_TYPE:
                columnValue = metaData.getResourceType().getResourceType();
                break;
            case CsvWriterConstants.HEADER_NAME_EXT:
                columnValue = metaData.getExtension();
                break;
            case CsvWriterConstants.HEADER_NAME_LAST_MODIFIED:
                columnValue = nullSafeDate(metaData.getLastModifiedDate(), CsvWriterConstants.DATE_FORMAT);
                break;
            case CsvWriterConstants.HEADER_NAME_EXTENSION_MISMATCH:
                columnValue = node.getExtensionMismatch().toString();
                break;
            case CsvWriterConstants.HEADER_NAME_HASH:
                columnValue = metaData.getHash();
                break;
            case CsvWriterConstants.HEADER_NAME_FORMAT_COUNT:
                columnValue = nullSafeNumber(node.getIdentificationCount());
                break;
        }
        nodeEntries.add(def.getOperatedValue(columnValue));
    }
    //CHECKSTYLE:ON

    private List<String> getHeadersToWrite(int maxIdCount) {
        if (template == null) {
            throw new IllegalArgumentException("Export template does not exist, unable to get headers from template");
        }
        List<String> retVal = new LinkedList<>();

        Map<Integer, ExportTemplateColumnDef> columnOrderMap =  template.getColumnOrderMap();
        for (int i = 0; i < columnOrderMap.size(); i++) {
            ExportTemplateColumnDef def = columnOrderMap.get(i);
            retVal.add(def.getHeaderLabel());
            if (def.getColumnType() != ExportTemplateColumnDef.ColumnType.ConstantString) {
                if (CsvWriterConstants.PER_FORMAT_HEADERS.contains(def.getOriginalColumnName())) {
                    for (int newColumnSuffix = 1; newColumnSuffix < maxIdCount; newColumnSuffix++) {
                        retVal.add(def.getHeaderLabel() + newColumnSuffix);
                    }
                }
            }
        }
        return retVal;
    }
}
