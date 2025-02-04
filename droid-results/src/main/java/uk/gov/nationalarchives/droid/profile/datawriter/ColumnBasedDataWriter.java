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
import uk.gov.nationalarchives.droid.profile.CsvWriterConstants;
import uk.gov.nationalarchives.droid.profile.NodeMetaData;
import uk.gov.nationalarchives.droid.profile.ProfileResourceNode;
import uk.gov.nationalarchives.droid.profile.referencedata.Format;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class implements the methods for writing headers and data when the export is done using selection of various columns.
 */
public class ColumnBasedDataWriter extends FormattedDataWriter {
    private final Map<String, Boolean> columnsToWriteMap;

    public ColumnBasedDataWriter(Map<String, Boolean> columnsToWriteMap) {
        this.columnsToWriteMap = columnsToWriteMap;
    }

    @Override
    public void writeDataRowsForOneRowPerFile(List<? extends ProfileResourceNode> nodes, CsvWriter csvWriter) {
        int maxIdCount = getMaxIdentificationCount(nodes);
        for (ProfileResourceNode node : nodes) {
            List<String> nodeEntries = new ArrayList<>();
            addNodeColumnsInDefaultOrder(nodeEntries, node);
            List<Format> formatIdentifications = node.getFormatIdentifications();
            for (int i = 0; i < maxIdCount; i++) {
                addColumn(nodeEntries, CsvWriterConstants.HEADER_NAME_PUID,  (i < formatIdentifications.size()) ?  formatIdentifications.get(i).getPuid() : CsvWriterConstants.EMPTY_STRING);
                addColumn(nodeEntries, CsvWriterConstants.HEADER_NAME_MIME_TYPE, (i < formatIdentifications.size()) ?  formatIdentifications.get(i).getMimeType() : CsvWriterConstants.EMPTY_STRING);
                addColumn(nodeEntries, CsvWriterConstants.HEADER_NAME_FORMAT_NAME, (i < formatIdentifications.size()) ?  formatIdentifications.get(i).getName() : CsvWriterConstants.EMPTY_STRING);
                addColumn(nodeEntries, CsvWriterConstants.HEADER_NAME_FORMAT_VERSION, (i < formatIdentifications.size()) ?  formatIdentifications.get(i).getVersion(): CsvWriterConstants.EMPTY_STRING);
            }
            csvWriter.writeRow(nodeEntries);
        }
        csvWriter.flush();
    }

    @Override
    public void writeJsonForOneRowPerFile(List<? extends ProfileResourceNode> nodes, String[] headers, Writer writer) {
        OutputJson outputJson = new OutputJson();
        int maxIdCount = getMaxIdentificationCount(nodes);
        String hashHeader = headers[CsvWriterConstants.HASH_ARRAY_INDEX];
        for (ProfileResourceNode node : nodes) {
            List<Format> formatIdentifications = node.getFormatIdentifications();
            ObjectNode objectNode = outputJson.createObjectNode();
            addNodeJsonEntriesInDefaultOrder(objectNode, node, hashHeader);
            for (int i = 0; i < maxIdCount; i++) {
                String suffix = i == 0 ? "" : Integer.toString(i);
                addEntry(objectNode, CsvWriterConstants.HEADER_NAME_PUID,  (i < formatIdentifications.size()) ?  formatIdentifications.get(i).getPuid() : CsvWriterConstants.EMPTY_STRING, suffix);
                addEntry(objectNode, CsvWriterConstants.HEADER_NAME_MIME_TYPE, (i < formatIdentifications.size()) ?  formatIdentifications.get(i).getMimeType() : CsvWriterConstants.EMPTY_STRING, suffix);
                addEntry(objectNode, CsvWriterConstants.HEADER_NAME_FORMAT_NAME, (i < formatIdentifications.size()) ?  formatIdentifications.get(i).getName() : CsvWriterConstants.EMPTY_STRING, suffix);
                addEntry(objectNode, CsvWriterConstants.HEADER_NAME_FORMAT_VERSION, (i < formatIdentifications.size()) ?  formatIdentifications.get(i).getVersion(): CsvWriterConstants.EMPTY_STRING, suffix);
            }
            outputJson.getArrayNode().add(objectNode);
        }
        outputJson.writeJson(writer);

    }

    @Override
    public void writeDataRowsForOneRowPerFormat(List<? extends ProfileResourceNode> nodes, CsvWriter csvWriter) {
        for (ProfileResourceNode node : nodes) {
            for (Format format : node.getFormatIdentifications()) {
                List<String> nodeEntries = new ArrayList<>();
                addNodeColumnsInDefaultOrder(nodeEntries, node);
                addColumn(nodeEntries, CsvWriterConstants.HEADER_NAME_PUID, format.getPuid());
                addColumn(nodeEntries, CsvWriterConstants.HEADER_NAME_MIME_TYPE, format.getMimeType());
                addColumn(nodeEntries, CsvWriterConstants.HEADER_NAME_FORMAT_NAME, format.getName());
                addColumn(nodeEntries, CsvWriterConstants.HEADER_NAME_FORMAT_VERSION, format.getVersion());
                csvWriter.writeRow(nodeEntries);
            }
        }
        csvWriter.flush();
    }

    @Override
    public void writeJsonForOneRowPerFormat(List<? extends ProfileResourceNode> nodes, String[] headers, Writer writer) {
        OutputJson outputJson = new OutputJson();
        String hashHeader = headers[CsvWriterConstants.HASH_ARRAY_INDEX];
        for (ProfileResourceNode node : nodes) {
            for (Format format : node.getFormatIdentifications()) {
                ObjectNode objectNode = outputJson.createObjectNode();
                addNodeJsonEntriesInDefaultOrder(objectNode, node, hashHeader);

                addEntry(objectNode, CsvWriterConstants.HEADER_NAME_PUID, format.getPuid());
                addEntry(objectNode, CsvWriterConstants.HEADER_NAME_MIME_TYPE, format.getMimeType());
                addEntry(objectNode, CsvWriterConstants.HEADER_NAME_FORMAT_NAME, format.getName());
                addEntry(objectNode, CsvWriterConstants.HEADER_NAME_FORMAT_VERSION, format.getVersion());
                outputJson.getArrayNode().add(objectNode);
            }
        }
        outputJson.writeJson(writer);
    }

    @Override
    public void writeHeadersForOneRowPerFile(List<? extends ProfileResourceNode> nodes, String[] headers, CsvWriter csvWriter) {
        super.setCustomisedHeaders(headers);
        List<String> headersToWrite = new ArrayList<>(getHeadersToWrite(getCustomisedHeaders()));
        int maxIdCount = getMaxIdentificationCount(nodes);

        //if we are writing one row per file, then we tag the "per format" fields as additional columns,
        //if such columns need to be added, we create appropriate headers with a running suffix and write
        //them to the file
        if (!Collections.disjoint(headersToWrite, CsvWriterConstants.PER_FORMAT_HEADERS) && (maxIdCount > 1)) { //add headers
            for (int newColumnSuffix = 1; newColumnSuffix < maxIdCount; newColumnSuffix++) {
                //"PUID","MIME_TYPE","FORMAT_NAME","FORMAT_VERSION"
                for (String headerEntry : CsvWriterConstants.PER_FORMAT_HEADERS) {
                    if (headersToWrite.contains(headerEntry)) {
                        headersToWrite.add(headerEntry + newColumnSuffix);
                    }
                }
            }
        }

        csvWriter.writeHeaders(headersToWrite);
        csvWriter.flush();
    }

    @Override
    public void writeHeadersForOneRowPerFormat(List<? extends ProfileResourceNode> nodes, String[] headers, CsvWriter csvWriter) {
        super.setCustomisedHeaders(headers);
        List<String> headersToWrite = new ArrayList<>(getHeadersToWrite(getCustomisedHeaders()));
        csvWriter.writeHeaders(headersToWrite);
        csvWriter.flush();
    }

    private void addNodeJsonEntriesInDefaultOrder(ObjectNode objectNode, ProfileResourceNode resourceNode, String hashHeader) {
        NodeMetaData metaData = resourceNode.getMetaData();
        addEntry(objectNode, CsvWriterConstants.HEADER_NAME_ID, resourceNode.getId());
        addEntry(objectNode, CsvWriterConstants.HEADER_NAME_ID, resourceNode.getId());
        addEntry(objectNode, CsvWriterConstants.HEADER_NAME_PARENT_ID, resourceNode.getParentId());
        addEntry(objectNode, CsvWriterConstants.HEADER_NAME_URI, DroidUrlFormat.format(resourceNode.getUri()));
        addEntry(objectNode, CsvWriterConstants.HEADER_NAME_FILE_PATH, toFilePath(resourceNode.getUri()));
        addEntry(objectNode, CsvWriterConstants.HEADER_NAME_NAME, toFileName(metaData.getName()));
        addEntry(objectNode, CsvWriterConstants.HEADER_NAME_METHOD, nullSafeName(metaData.getIdentificationMethod()));
        addEntry(objectNode, CsvWriterConstants.HEADER_NAME_STATUS, metaData.getNodeStatus().getStatus());
        addEntry(objectNode, CsvWriterConstants.HEADER_NAME_SIZE, metaData.getSize());
        addEntry(objectNode, CsvWriterConstants.HEADER_NAME_TYPE, metaData.getResourceType().getResourceType());
        addEntry(objectNode, CsvWriterConstants.HEADER_NAME_EXT, metaData.getExtension());
        addEntry(objectNode, CsvWriterConstants.HEADER_NAME_LAST_MODIFIED, nullSafeDate(metaData.getLastModifiedDate(), CsvWriterConstants.DATE_FORMAT));
        addEntry(objectNode, CsvWriterConstants.HEADER_NAME_EXTENSION_MISMATCH, resourceNode.getExtensionMismatch().toString());
        addEntry(objectNode, hashHeader, metaData.getHash(), "", CsvWriterConstants.HEADER_NAME_HASH);
        addEntry(objectNode, CsvWriterConstants.HEADER_NAME_FORMAT_COUNT, nullSafeNumber(resourceNode.getIdentificationCount()));
    }

    private void addNodeColumnsInDefaultOrder(List<String> row, ProfileResourceNode node) {
        NodeMetaData metaData = node.getMetaData();
        addColumn(row, CsvWriterConstants.HEADER_NAME_ID, nullSafeNumber(node.getId()));
        addColumn(row, CsvWriterConstants.HEADER_NAME_PARENT_ID, nullSafeNumber(node.getParentId()));
        addColumn(row, CsvWriterConstants.HEADER_NAME_URI, DroidUrlFormat.format(node.getUri()));
        addColumn(row, CsvWriterConstants.HEADER_NAME_FILE_PATH, toFilePath(node.getUri()));
        addColumn(row, CsvWriterConstants.HEADER_NAME_NAME, toFileName(metaData.getName()));
        addColumn(row, CsvWriterConstants.HEADER_NAME_METHOD, nullSafeName(metaData.getIdentificationMethod()));
        addColumn(row, CsvWriterConstants.HEADER_NAME_STATUS, metaData.getNodeStatus().getStatus());
        addColumn(row, CsvWriterConstants.HEADER_NAME_SIZE, nullSafeNumber(metaData.getSize()));
        addColumn(row, CsvWriterConstants.HEADER_NAME_TYPE, metaData.getResourceType().getResourceType());
        addColumn(row, CsvWriterConstants.HEADER_NAME_EXT, metaData.getExtension());
        addColumn(row, CsvWriterConstants.HEADER_NAME_LAST_MODIFIED, nullSafeDate(metaData.getLastModifiedDate(), CsvWriterConstants.DATE_FORMAT));
        addColumn(row, CsvWriterConstants.HEADER_NAME_EXTENSION_MISMATCH, node.getExtensionMismatch().toString());
        addColumn(row, CsvWriterConstants.HEADER_NAME_HASH, metaData.getHash());
        addColumn(row, CsvWriterConstants.HEADER_NAME_FORMAT_COUNT, nullSafeNumber(node.getIdentificationCount()));
    }

    private void addColumn(List<String> row, String columnName, String value) {
        if (columnsToWriteMap.get(columnName)) {
            row.add(value);
        }
    }

    private void addEntry(ObjectNode objectNode, String columnName, Object value) {
        addEntry(objectNode, columnName, value, "", null);
    }

    private void addEntry(ObjectNode objectNode, String columnName, Object value, String suffix) {
        addEntry(objectNode, columnName, value, suffix, null);
    }

    private void addEntry(ObjectNode objectNode, String columnName, Object value, String suffix, String columnLookup) {
        if (columnsToWriteMap.get(columnLookup == null ? columnName : columnLookup)) {
            switch (value) {
                case Integer num -> objectNode.put(columnName + suffix, num);
                case Long lon -> objectNode.put(columnName + suffix, lon);
                case Double db -> objectNode.put(columnName + suffix, db);
                case String s -> objectNode.put(columnName + suffix, s);
                case null -> objectNode.putNull(columnName + suffix);
                default -> {}
            }
        }
    }

    private List<String> getHeadersToWrite(String[] headersToWrite) {
        if (columnsToWriteMap.containsValue(false)) {
            int numColumnsToWrite = (int) columnsToWriteMap.values().stream().filter(eachVal -> eachVal).count();
            String[] newHeaders = new String[numColumnsToWrite];
            int newHeaderIndex = 0;
            for (int i = 0; i < CsvWriterConstants.HEADERS.length; i++) {
                if (columnsToWriteMap.get(CsvWriterConstants.HEADERS[i])) {
                    newHeaders[newHeaderIndex++] = getCustomisedHeaders()[i];
                }
            }
            return Arrays.stream(newHeaders).collect(Collectors.toList());
        }
        return Arrays.stream(headersToWrite).collect(Collectors.toList());
    }

}
