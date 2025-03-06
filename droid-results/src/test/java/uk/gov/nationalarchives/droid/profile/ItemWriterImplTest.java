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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationMethod;
import uk.gov.nationalarchives.droid.core.interfaces.NodeStatus;
import uk.gov.nationalarchives.droid.core.interfaces.ResourceType;
import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalConfig;
import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalProperty;
import uk.gov.nationalarchives.droid.export.interfaces.ExportOptions;
import uk.gov.nationalarchives.droid.export.interfaces.ExportOutputOptions;
import uk.gov.nationalarchives.droid.export.interfaces.ExportTemplate;
import uk.gov.nationalarchives.droid.export.interfaces.ExportTemplateColumnDef;
import uk.gov.nationalarchives.droid.profile.referencedata.Format;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author rflitcroft
 *
 * Amendments
 * ================================================
 * By: Brian O'Reilly
 * Date: 26-Feb-2014
 * Details: Changed expectedString hash component  to  "%s_HASH"  instead of MD5_HASH" .
 * This is because, following the introduction of SHA256 as an additional option over and
 * above MD5, the actual hash header is constructed at run time in the application based on the config.
 * However, the test does not have access to the configuration, so testing against the placeholder is the
 * best approach and avoids introducing additional dependencies.
 */
public class ItemWriterImplTest {

    private static final DateTime testDateTime = new DateTime(12345678L);
    private static final String LINE_SEPARATOR = "\n";
    private ItemWriterImpl itemWriter;
    private DroidGlobalConfig config;
    private String testDateTimeString;
    private static final String[] defaultHeadersArray = new String[] {
            "ID","PARENT_ID","URI","FILE_PATH","NAME","METHOD","STATUS","SIZE","TYPE","EXT","LAST_MODIFIED","EXTENSION_MISMATCH","HASH","FORMAT_COUNT",
            "PUID","MIME_TYPE","FORMAT_NAME","FORMAT_VERSION"
    };
    private static final String defaultHeaders = toCsvRow(defaultHeadersArray);

    @Before
    public void setup() {
        File dir = new File("exports");
        dir.mkdir();
        File destination = new File(dir, "test1.csv");
        destination.delete();
        itemWriter = new ItemWriterImpl();
        
        config = mock(DroidGlobalConfig.class);
        itemWriter.setConfig(config);
        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");
        testDateTimeString = dtf.print(testDateTime);
    }

    @After
    public void tearDown() {
        itemWriter.close();
    }

    private static String toCsvRow(final String[] values) {
        return "\"" + StringUtils.join(values, "\",\"") + "\"";
    }

    private static String toJsonRow(final String[] headers, final Object[] values) {
        return toJsonRows(headers, new Object[][]{values});
    }

    private static String toJsonRows(final String[] headers, final Object[][] rowValues) {
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode arrayNode = objectMapper.createArrayNode();
        for (Object[] values: rowValues) {
            ObjectNode objectNode = objectMapper.createObjectNode();
            for (int i = 0; i < headers.length; i++) {
                switch (values[i]) {
                    case Integer num -> objectNode.put(headers[i], num);
                    case String s -> objectNode.put(headers[i], s);
                    case null -> objectNode.putNull(headers[i]);
                    default -> {}
                }
            }
            arrayNode.add(objectNode);
        }
        return arrayNode.toString();
    }

    @Test
    public void should_write_only_headers_when_there_are_no_nodes_to_be_written() throws IOException {
        when(config.getBooleanProperty(DroidGlobalProperty.CSV_EXPORT_ROW_PER_FORMAT)).thenReturn(false);

        try(final Writer writer = new StringWriter()) {
            List<ProfileResourceNode> nodes = new ArrayList<>();
            itemWriter.open(writer);
            itemWriter.write(nodes);

            String[] writtenLines = writer.toString().split(LINE_SEPARATOR);
            assertEquals(1, writtenLines.length);
            assertEquals(defaultHeaders, writtenLines[0]);
        }
    }

    @Test
    public void should_write_an_empty_json_array_when_there_are_no_nodes_to_be_written() throws IOException {
        when(config.getBooleanProperty(DroidGlobalProperty.CSV_EXPORT_ROW_PER_FORMAT)).thenReturn(false);
        itemWriter.setOutputOptions(ExportOutputOptions.JSON_OUTPUT);
        try(final Writer writer = new StringWriter()) {
            List<ProfileResourceNode> nodes = new ArrayList<>();
            itemWriter.open(writer);
            itemWriter.write(nodes);

            String[] writtenLines = writer.toString().split(LINE_SEPARATOR);
            assertEquals(1, writtenLines.length);
            assertEquals("[]", writtenLines[0]);
        }
    }


    @Test
    public void should_write_one_node_as_one_row_per_format_export() throws IOException {
        when(config.getBooleanProperty(DroidGlobalProperty.CSV_EXPORT_ROW_PER_FORMAT)).thenReturn(false);

        try(final Writer writer = new StringWriter()) {
            List<ProfileResourceNode> nodes = new ArrayList<>();
            Format id = buildFormat(1);
            ProfileResourceNode node = buildProfileResourceNode(1, 1001L);
            node.addFormatIdentification(id);
            nodes.add(node);
            itemWriter.setOptions(ExportOptions.ONE_ROW_PER_FORMAT);
            itemWriter.open(writer);
            itemWriter.write(nodes);

            final String expectedEntry = toCsvRow(new String[] {
                    "", "",
                    isNotWindows() ? "file:/my/file1.txt" : "file:/C:/my/file1.txt",
                    isNotWindows() ? "/my/file1.txt" : "C:\\my\\file1.txt",
                    "file1.txt",
                    "Signature",
                    "Done",
                    "1001",
                    "File",
                    "foo",
                    testDateTimeString,
                    "false",
                    "11111111111111111111111111111111",
                    "1",
                    "fmt/1",
                    "text/plain",
                    "Plain Text",
                    "1.0",
            });

            final String[] lines = writer.toString().split(LINE_SEPARATOR);
            assertEquals(2, lines.length);
            assertEquals(defaultHeaders, lines[0]);
            assertEquals(expectedEntry, lines[1]);
        }
    }

    @Test
    public void should_write_one_node_as_one_json_object_per_format_export() throws IOException {
        when(config.getBooleanProperty(DroidGlobalProperty.CSV_EXPORT_ROW_PER_FORMAT)).thenReturn(false);
        itemWriter.setOutputOptions(ExportOutputOptions.JSON_OUTPUT);
        try(final Writer writer = new StringWriter()) {
            List<ProfileResourceNode> nodes = new ArrayList<>();
            Format id = buildFormat(1);
            ProfileResourceNode node = buildProfileResourceNode(1, 1001L);
            node.addFormatIdentification(id);
            nodes.add(node);
            itemWriter.setOptions(ExportOptions.ONE_ROW_PER_FORMAT);
            itemWriter.open(writer);
            itemWriter.write(nodes);

            final String expectedEntry = toJsonRow(defaultHeadersArray, new Object[] {
                    null, null,
                    isNotWindows() ? "file:/my/file1.txt" : "file:/C:/my/file1.txt",
                    isNotWindows() ? "/my/file1.txt" : "C:\\my\\file1.txt",
                    "file1.txt",
                    "Signature",
                    "Done",
                    1001,
                    "File",
                    "foo",
                    testDateTimeString,
                    "false",
                    "11111111111111111111111111111111",
                    "1",
                    "fmt/1",
                    "text/plain",
                    "Plain Text",
                    "1.0",
            });

            final String[] lines = writer.toString().split(LINE_SEPARATOR);
            assertEquals(1, lines.length);
            assertEquals(expectedEntry, lines[0]);
        }
    }

    @Test
    public void testWriteNodeWithNullFormat() throws IOException {
        when(config.getBooleanProperty(DroidGlobalProperty.CSV_EXPORT_ROW_PER_FORMAT)).thenReturn(false);

        try(final Writer writer = new StringWriter()) {
            List<ProfileResourceNode> nodes = new ArrayList<>();

            Format id = Format.NULL;
            ProfileResourceNode node = buildProfileResourceNode(1, 1001L);
            node.addFormatIdentification(id);

            nodes.add(node);
            itemWriter.setOptions(ExportOptions.ONE_ROW_PER_FORMAT);
            itemWriter.open(writer);
            itemWriter.write(nodes);

            final String expectedEntry = toCsvRow(new String[] {
                    "", "",
                    isNotWindows() ? "file:/my/file1.txt" : "file:/C:/my/file1.txt",
                    isNotWindows() ? "/my/file1.txt" : "C:\\my\\file1.txt",
                    "file1.txt",
                    "Signature",
                    "Done",
                    "1001",
                    "File",
                    "foo",
                    testDateTimeString,
                    "false",
                    "11111111111111111111111111111111",
                    "",
                    null,
                    "",
                    "",
                    "",
            });

            final String[] lines = writer.toString().split(LINE_SEPARATOR);

            assertEquals(2, lines.length);
            assertEquals(expectedEntry, lines[1]);
        }
    }

    @Test
    public void testWriteJsonNodeWithNullFormat() throws IOException {
        when(config.getBooleanProperty(DroidGlobalProperty.CSV_EXPORT_ROW_PER_FORMAT)).thenReturn(false);
        itemWriter.setOutputOptions(ExportOutputOptions.JSON_OUTPUT);
        try(final Writer writer = new StringWriter()) {
            List<ProfileResourceNode> nodes = new ArrayList<>();

            Format id = Format.NULL;
            ProfileResourceNode node = buildProfileResourceNode(1, 1001L);
            node.addFormatIdentification(id);

            nodes.add(node);
            itemWriter.setOptions(ExportOptions.ONE_ROW_PER_FORMAT);
            itemWriter.open(writer);
            itemWriter.write(nodes);

            final String expectedEntry = toJsonRow(defaultHeadersArray, new Object[] {
                    null, null,
                    isNotWindows() ? "file:/my/file1.txt" : "file:/C:/my/file1.txt",
                    isNotWindows() ? "/my/file1.txt" : "C:\\my\\file1.txt",
                    "file1.txt",
                    "Signature",
                    "Done",
                    1001,
                    "File",
                    "foo",
                    testDateTimeString,
                    "false",
                    "11111111111111111111111111111111",
                    "",
                    null,
                    "",
                    "",
                    "",
            });

            final String[] lines = writer.toString().split(LINE_SEPARATOR);

            assertEquals(1, lines.length);
            assertEquals(expectedEntry, lines[0]);
        }
    }

    @Test
    public void testWriteOneNodeWithNullSize() throws IOException {
        when(config.getBooleanProperty(DroidGlobalProperty.CSV_EXPORT_ROW_PER_FORMAT)).thenReturn(false);

        try(final Writer writer = new StringWriter()) {
            List<ProfileResourceNode> nodes = new ArrayList<>();

            Format id = buildFormat(1);
            ProfileResourceNode node = buildProfileResourceNode(1, null);
            node.addFormatIdentification(id);

            nodes.add(node);
            itemWriter.open(writer);
            itemWriter.write(nodes);

            final String expectedEntry = toCsvRow(new String[] {
                    "", "",
                    isNotWindows() ? "file:/my/file1.txt" : "file:/C:/my/file1.txt",
                    isNotWindows() ? "/my/file1.txt" : "C:\\my\\file1.txt",
                    "file1.txt",
                    "Signature",
                    "Done",
                    "",
                    "File",
                    "foo",
                    testDateTimeString,
                    "false",
                    "11111111111111111111111111111111",
                    "1",
                    "fmt/1",
                    "text/plain",
                    "Plain Text",
                    "1.0",
            });

            final String[] lines = writer.toString().split(LINE_SEPARATOR);

            assertEquals(2, lines.length);
            assertEquals(expectedEntry, lines[1]);
        }
    }

    @Test
    public void testWriteOneJsonNodeWithNullSize() throws IOException {
        when(config.getBooleanProperty(DroidGlobalProperty.CSV_EXPORT_ROW_PER_FORMAT)).thenReturn(false);
        itemWriter.setOutputOptions(ExportOutputOptions.JSON_OUTPUT);

        try(final Writer writer = new StringWriter()) {
            List<ProfileResourceNode> nodes = new ArrayList<>();

            Format id = buildFormat(1);
            ProfileResourceNode node = buildProfileResourceNode(1, null);
            node.addFormatIdentification(id);

            nodes.add(node);
            itemWriter.open(writer);
            itemWriter.write(nodes);

            final String expectedEntry = toJsonRow(defaultHeadersArray, new Object[] {
                    null, null,
                    isNotWindows() ? "file:/my/file1.txt" : "file:/C:/my/file1.txt",
                    isNotWindows() ? "/my/file1.txt" : "C:\\my\\file1.txt",
                    "file1.txt",
                    "Signature",
                    "Done",
                    null,
                    "File",
                    "foo",
                    testDateTimeString,
                    "false",
                    "11111111111111111111111111111111",
                    "1",
                    "fmt/1",
                    "text/plain",
                    "Plain Text",
                    "1.0"
            });

            final String[] lines = writer.toString().split(LINE_SEPARATOR);

            assertEquals(1, lines.length);
            assertEquals(expectedEntry, lines[0]);
        }
    }

    @Test
    public void shouldCreateAdditionalHeadersForIdentificationFormatsWhenWritingAnEntryPerFile() throws IOException {
        when(config.getBooleanProperty(DroidGlobalProperty.CSV_EXPORT_ROW_PER_FORMAT)).thenReturn(false);

        try(final Writer writer = new StringWriter()) {
            List<ProfileResourceNode> nodes = new ArrayList<>();

            Format id1 = buildFormat(1);
            Format id2 = buildFormat(2);

            ProfileResourceNode node = buildProfileResourceNode(1, 1000L);
            node.addFormatIdentification(id1);
            node.addFormatIdentification(id2);

            nodes.add(node);
            itemWriter.setOptions(ExportOptions.ONE_ROW_PER_FILE);
            itemWriter.open(writer);
            itemWriter.write(nodes);

            final String expectedHeaders = toCsvRow(new String[] {
                    "ID","PARENT_ID","URI","FILE_PATH","NAME","METHOD","STATUS","SIZE","TYPE","EXT","LAST_MODIFIED","EXTENSION_MISMATCH","HASH","FORMAT_COUNT",
                    "PUID","MIME_TYPE","FORMAT_NAME","FORMAT_VERSION", //per format columns
                    "PUID1","MIME_TYPE1","FORMAT_NAME1","FORMAT_VERSION1", //per format columns
            });

            final String expectedEntry1 = toCsvRow(new String[] {
                    "", "",
                    isNotWindows() ? "file:/my/file1.txt" : "file:/C:/my/file1.txt",
                    isNotWindows() ? "/my/file1.txt" : "C:\\my\\file1.txt",
                    "file1.txt",
                    "Signature",
                    "Done",
                    "1000",
                    "File",
                    "foo",
                    testDateTimeString,
                    "false",
                    "11111111111111111111111111111111",
                    "2",
                    "fmt/1",
                    "text/plain",
                    "Plain Text",
                    "1.0",
                    "fmt/2",
                    "text/plain",
                    "Plain Text",
                    "1.0",
            });

            final String[] lines = writer.toString().split(LINE_SEPARATOR);

            assertEquals(2, lines.length);
            assertEquals(expectedHeaders, lines[0]);
            assertEquals(expectedEntry1, lines[1]);
        }
    }

    @Test
    public void shouldCreateAdditionalKeysForIdentificationFormatsWhenWritingAnEntryPerFile() throws IOException {
        when(config.getBooleanProperty(DroidGlobalProperty.CSV_EXPORT_ROW_PER_FORMAT)).thenReturn(false);
        itemWriter.setOutputOptions(ExportOutputOptions.JSON_OUTPUT);

        try(final Writer writer = new StringWriter()) {
            List<ProfileResourceNode> nodes = new ArrayList<>();

            Format id1 = buildFormat(1);
            Format id2 = buildFormat(2);

            ProfileResourceNode node = buildProfileResourceNode(1, 1000L);
            node.addFormatIdentification(id1);
            node.addFormatIdentification(id2);

            nodes.add(node);
            itemWriter.setOptions(ExportOptions.ONE_ROW_PER_FILE);
            itemWriter.open(writer);
            itemWriter.write(nodes);

            final String[] expectedHeaders = new String[] {
                    "ID","PARENT_ID","URI","FILE_PATH","NAME","METHOD","STATUS","SIZE","TYPE","EXT","LAST_MODIFIED","EXTENSION_MISMATCH","HASH","FORMAT_COUNT",
                    "PUID","MIME_TYPE","FORMAT_NAME","FORMAT_VERSION", //per format columns
                    "PUID1","MIME_TYPE1","FORMAT_NAME1","FORMAT_VERSION1", //per format columns
            };

            Object[] values = new Object[] {
                    null, null,
                    isNotWindows() ? "file:/my/file1.txt" : "file:/C:/my/file1.txt",
                    isNotWindows() ? "/my/file1.txt" : "C:\\my\\file1.txt",
                    "file1.txt",
                    "Signature",
                    "Done",
                    1000,
                    "File",
                    "foo",
                    testDateTimeString,
                    "false",
                    "11111111111111111111111111111111",
                    "2",
                    "fmt/1",
                    "text/plain",
                    "Plain Text",
                    "1.0",
                    "fmt/2",
                    "text/plain",
                    "Plain Text",
                    "1.0",
            };

            final String expectedEntry1 = toJsonRows(expectedHeaders, new Object[][]{values});

            final String[] lines = writer.toString().split(LINE_SEPARATOR);

            assertEquals(1, lines.length);
            assertEquals(expectedEntry1, lines[0]);
        }
    }

    @Test
    public void should_create_additional_headers_for_identification_format_in_order_of_template_when_writing_entries_per_file() throws IOException {
        when(config.getBooleanProperty(DroidGlobalProperty.CSV_EXPORT_ROW_PER_FORMAT)).thenReturn(false);
        ExportTemplate template = mock(ExportTemplate.class);

        Map<Integer, ExportTemplateColumnDef> columnPositions = new HashMap<>();

        ExportTemplateColumnDef def1 = getMockColumnDef("ID", "Identifier", ExportTemplateColumnDef.ColumnType.ProfileResourceNode);
        ExportTemplateColumnDef def2 = getMockColumnDef("PUID", "Puid", ExportTemplateColumnDef.ColumnType.ProfileResourceNode);
        ExportTemplateColumnDef def3 = getMockColumnDef("HASH", "Hash123", ExportTemplateColumnDef.ColumnType.ProfileResourceNode);
        ExportTemplateColumnDef def4 = getMockColumnDef("FORMAT_NAME", "Format_Name", ExportTemplateColumnDef.ColumnType.ProfileResourceNode);
        ExportTemplateColumnDef def5 = getMockColumnDef("Simple Column", "Simple_Header", ExportTemplateColumnDef.ColumnType.ConstantString);

        columnPositions.put(0, def1);
        columnPositions.put(1, def2);
        columnPositions.put(2, def3);
        columnPositions.put(3, def4);
        columnPositions.put(4, def5);

        when(template.getColumnOrderMap()).thenReturn(columnPositions);

        try(final Writer writer = new StringWriter()) {
            List<ProfileResourceNode> nodes = new ArrayList<>();

            Format id1 = buildFormat(1);
            Format id2 = buildFormat(2);

            ProfileResourceNode node = buildProfileResourceNode(1, 1000L);
            node.addFormatIdentification(id1);
            node.addFormatIdentification(id2);
            nodes.add(node);

            itemWriter.setOptions(ExportOptions.ONE_ROW_PER_FILE);
            itemWriter.setExportTemplate(template);
            itemWriter.open(writer);
            itemWriter.write(nodes);

            final String expectedHeaders = toCsvRow(new String[] {
                    "Identifier", "Puid", "Puid1", "Hash123", "Format_Name", "Format_Name1", "Simple_Header"
            });

            final String[] lines = writer.toString().split(LINE_SEPARATOR);

            assertEquals(2, lines.length);
            assertEquals(expectedHeaders, lines[0]);
            assertEquals(7, lines[1].split(",").length);
        }
    }

    @Test
    public void should_create_additional_keys_for_identification_format_in_order_of_template_when_writing_entries_per_file() throws IOException {
        when(config.getBooleanProperty(DroidGlobalProperty.CSV_EXPORT_ROW_PER_FORMAT)).thenReturn(false);
        itemWriter.setOutputOptions(ExportOutputOptions.JSON_OUTPUT);

        ExportTemplate template = mock(ExportTemplate.class);

        Map<Integer, ExportTemplateColumnDef> columnPositions = new HashMap<>();

        ExportTemplateColumnDef def1 = getMockColumnDef("ID", "Identifier", ExportTemplateColumnDef.ColumnType.ProfileResourceNode);
        ExportTemplateColumnDef def2 = getMockColumnDef("PUID", "Puid", ExportTemplateColumnDef.ColumnType.ProfileResourceNode);
        ExportTemplateColumnDef def3 = getMockColumnDef("HASH", "Hash123", ExportTemplateColumnDef.ColumnType.ProfileResourceNode);
        ExportTemplateColumnDef def4 = getMockColumnDef("FORMAT_NAME", "Format_Name", ExportTemplateColumnDef.ColumnType.ProfileResourceNode);
        ExportTemplateColumnDef def5 = getMockColumnDef("Simple Column", "Simple_Header", ExportTemplateColumnDef.ColumnType.ConstantString);

        columnPositions.put(0, def1);
        columnPositions.put(1, def2);
        columnPositions.put(2, def3);
        columnPositions.put(3, def4);
        columnPositions.put(4, def5);

        when(template.getColumnOrderMap()).thenReturn(columnPositions);

        try(final Writer writer = new StringWriter()) {
            List<ProfileResourceNode> nodes = new ArrayList<>();

            Format id1 = buildFormat(1);
            Format id2 = buildFormat(2);

            ProfileResourceNode node = buildProfileResourceNode(1, 1000L);
            node.addFormatIdentification(id1);
            node.addFormatIdentification(id2);
            nodes.add(node);

            itemWriter.setOptions(ExportOptions.ONE_ROW_PER_FILE);
            itemWriter.setExportTemplate(template);
            itemWriter.open(writer);
            itemWriter.write(nodes);

            final String[] expectedHeaders = new String[] {
                    "Identifier", "Puid", "Puid1", "Hash123", "Format_Name", "Format_Name1", "Simple_Header"
            };

            final String[] expectedValues = new String[] {
                "", id1.getPuid(), id2.getPuid(), node.getMetaData().getHash(), id1.getName(), id2.getName(), "Simple Column"
            };

            String expectedJson = toJsonRow(expectedHeaders, expectedValues);

            final String[] lines = writer.toString().split(LINE_SEPARATOR);

            assertEquals(1, lines.length);
            assertEquals(expectedJson, lines[0]);
        }
    }

    @Test
    public void should_write_a_column_with_pre_defined_constant_value() throws IOException {
        when(config.getBooleanProperty(DroidGlobalProperty.CSV_EXPORT_ROW_PER_FORMAT)).thenReturn(false);
        ExportTemplate template = mock(ExportTemplate.class);

        Map<Integer, ExportTemplateColumnDef> columnPositions = new HashMap<>();

        ExportTemplateColumnDef def1 = getMockColumnDef("ID", "Identifier", ExportTemplateColumnDef.ColumnType.ProfileResourceNode);
        ExportTemplateColumnDef def2 = getMockColumnDef("Simplified English", "Language", ExportTemplateColumnDef.ColumnType.ConstantString);
        ExportTemplateColumnDef def3 = getMockColumnDef("FORMAT_NAME", "Format_Name", ExportTemplateColumnDef.ColumnType.ProfileResourceNode);

        columnPositions.put(0, def1);
        columnPositions.put(1, def2);
        columnPositions.put(2, def3);

        when(template.getColumnOrderMap()).thenReturn(columnPositions);

        try(final Writer writer = new StringWriter()) {
            List<ProfileResourceNode> nodes = new ArrayList<>();

            Format id1 = buildFormat(1);

            ProfileResourceNode node = buildProfileResourceNode(1, 1000L);
            node.addFormatIdentification(id1);
            nodes.add(node);

            itemWriter.setOptions(ExportOptions.ONE_ROW_PER_FILE);
            itemWriter.setExportTemplate(template);
            itemWriter.open(writer);
            itemWriter.write(nodes);

            final String expectedHeaders = toCsvRow(new String[] {
                    "Identifier", "Language", "Format_Name"
            });

            final String expectedEntry = toCsvRow(new String[] {
                    "",
                    "Simplified English",
                    "Plain Text"
            });


            final String[] lines = writer.toString().split(LINE_SEPARATOR);

            assertEquals(2, lines.length);
            assertEquals(expectedHeaders, lines[0]);
            assertEquals(expectedEntry, lines[1]);
        }

    }

    @Test
    public void should_write_a_json_entry_with_pre_defined_constant_value() throws IOException {
        when(config.getBooleanProperty(DroidGlobalProperty.CSV_EXPORT_ROW_PER_FORMAT)).thenReturn(false);
        itemWriter.setOutputOptions(ExportOutputOptions.JSON_OUTPUT);

        ExportTemplate template = mock(ExportTemplate.class);

        Map<Integer, ExportTemplateColumnDef> columnPositions = new HashMap<>();

        ExportTemplateColumnDef def1 = getMockColumnDef("ID", "Identifier", ExportTemplateColumnDef.ColumnType.ProfileResourceNode);
        ExportTemplateColumnDef def2 = getMockColumnDef("Simplified English", "Language", ExportTemplateColumnDef.ColumnType.ConstantString);
        ExportTemplateColumnDef def3 = getMockColumnDef("FORMAT_NAME", "Format_Name", ExportTemplateColumnDef.ColumnType.ProfileResourceNode);

        columnPositions.put(0, def1);
        columnPositions.put(1, def2);
        columnPositions.put(2, def3);

        when(template.getColumnOrderMap()).thenReturn(columnPositions);

        try(final Writer writer = new StringWriter()) {
            List<ProfileResourceNode> nodes = new ArrayList<>();

            Format id1 = buildFormat(1);

            ProfileResourceNode node = buildProfileResourceNode(1, 1000L);
            node.addFormatIdentification(id1);
            nodes.add(node);

            itemWriter.setOptions(ExportOptions.ONE_ROW_PER_FILE);
            itemWriter.setExportTemplate(template);
            itemWriter.open(writer);
            itemWriter.write(nodes);

            final String[] expectedHeaders = new String[] {
                    "Identifier", "Language", "Format_Name"
            };

            final String[] expectedEntry = new String[] {
                    "",
                    "Simplified English",
                    "Plain Text"
            };

            final String expectedJson = toJsonRow(expectedHeaders, expectedEntry);


            final String[] lines = writer.toString().split(LINE_SEPARATOR);

            assertEquals(1, lines.length);
            assertEquals(expectedJson, lines[0]);
        }

    }

    @Test
    public void should_write_a_column_with_modified_value_and_a_pre_defined_constant_value() throws IOException {
        when(config.getBooleanProperty(DroidGlobalProperty.CSV_EXPORT_ROW_PER_FORMAT)).thenReturn(false);
        ExportTemplate template = mock(ExportTemplate.class);

        Map<Integer, ExportTemplateColumnDef> columnPositions = new HashMap<>();

        ExportTemplateColumnDef def1 = getMockColumnDef("ID", "Identifier", ExportTemplateColumnDef.ColumnType.ProfileResourceNode);
        ExportTemplateColumnDef def2 = getMockColumnDef("Simplified English", "Language", ExportTemplateColumnDef.ColumnType.ConstantString);
        ExportTemplateColumnDef def3 = getMockColumnDef("FORMAT_NAME", "Format_Name", ExportTemplateColumnDef.ColumnType.DataModifier);


        columnPositions.put(0, def1);
        columnPositions.put(1, def2);
        columnPositions.put(2, def3);

        when(template.getColumnOrderMap()).thenReturn(columnPositions);

        try(final Writer writer = new StringWriter()) {
            List<ProfileResourceNode> nodes = new ArrayList<>();

            Format id1 = buildFormat(1);

            ProfileResourceNode node = buildProfileResourceNode(1, 1000L);
            node.addFormatIdentification(id1);
            nodes.add(node);

            itemWriter.setOptions(ExportOptions.ONE_ROW_PER_FILE);
            itemWriter.setExportTemplate(template);
            itemWriter.open(writer);
            itemWriter.write(nodes);

            final String expectedHeaders = toCsvRow(new String[] {
                    "Identifier", "Language", "Format_Name"
            });

            final String expectedEntry = toCsvRow(new String[] {
                    "",
                    "Simplified English",
                    "plain text"
            });


            final String[] lines = writer.toString().split(LINE_SEPARATOR);

            assertEquals(2, lines.length);
            assertEquals(expectedHeaders, lines[0]);
            assertEquals(expectedEntry, lines[1]);
        }

    }

    @Test
    public void should_write_a_json_entry_with_modified_value_and_a_pre_defined_constant_value() throws IOException {
        when(config.getBooleanProperty(DroidGlobalProperty.CSV_EXPORT_ROW_PER_FORMAT)).thenReturn(false);
        itemWriter.setOutputOptions(ExportOutputOptions.JSON_OUTPUT);

        ExportTemplate template = mock(ExportTemplate.class);

        Map<Integer, ExportTemplateColumnDef> columnPositions = new HashMap<>();

        ExportTemplateColumnDef def1 = getMockColumnDef("ID", "Identifier", ExportTemplateColumnDef.ColumnType.ProfileResourceNode);
        ExportTemplateColumnDef def2 = getMockColumnDef("Simplified English", "Language", ExportTemplateColumnDef.ColumnType.ConstantString);
        ExportTemplateColumnDef def3 = getMockColumnDef("FORMAT_NAME", "Format_Name", ExportTemplateColumnDef.ColumnType.DataModifier);


        columnPositions.put(0, def1);
        columnPositions.put(1, def2);
        columnPositions.put(2, def3);

        when(template.getColumnOrderMap()).thenReturn(columnPositions);

        try(final Writer writer = new StringWriter()) {
            List<ProfileResourceNode> nodes = new ArrayList<>();

            Format id1 = buildFormat(1);

            ProfileResourceNode node = buildProfileResourceNode(1, 1000L);
            node.addFormatIdentification(id1);
            nodes.add(node);

            itemWriter.setOptions(ExportOptions.ONE_ROW_PER_FILE);
            itemWriter.setExportTemplate(template);
            itemWriter.open(writer);
            itemWriter.write(nodes);

            final String[] expectedHeaders = new String[] {
                    "Identifier", "Language", "Format_Name"
            };

            final String[] expectedEntry = new String[] {
                    "",
                    "Simplified English",
                    "plain text"
            };

            final String expectedJson = toJsonRow(expectedHeaders, expectedEntry);

            final String[] lines = writer.toString().split(LINE_SEPARATOR);

            assertEquals(1, lines.length);
            assertEquals(expectedJson, lines[0]);
        }

    }

    @Test
    public void should_restrict_to_limited_number_of_columns_when_the_column_names_are_set_on_the_writer_writing_per_file() throws IOException {

        try(final Writer writer = new StringWriter()) {
            List<ProfileResourceNode> nodes = new ArrayList<>();

            Format id1 = buildFormat(1);
            Format id2 = buildFormat(2);

            ProfileResourceNode node = buildProfileResourceNode(1, 1000L);
            node.addFormatIdentification(id1);
            node.addFormatIdentification(id2);

            nodes.add(node);
            itemWriter.setOptions(ExportOptions.ONE_ROW_PER_FILE);
            itemWriter.setColumnsToWrite("ID FORMAT_NAME");
            itemWriter.open(writer);
            itemWriter.write(nodes);

            final String expectedHeaders = toCsvRow(new String[] {
                    "ID", "FORMAT_NAME", "FORMAT_NAME1" //per format columns
            });

            final String expectedEntry1 = toCsvRow(new String[] {
                    "",
                    "Plain Text",
                    "Plain Text"
            });

            final String[] lines = writer.toString().split(LINE_SEPARATOR);

            assertEquals(2, lines.length);
            assertEquals(expectedHeaders, lines[0]);
            assertEquals(expectedEntry1, lines[1]);
        }
    }

    @Test
    public void should_restrict_to_limited_number_of_keys_when_the_column_names_are_set_on_the_writer_writing_per_file() throws IOException {

        try(final Writer writer = new StringWriter()) {
            List<ProfileResourceNode> nodes = new ArrayList<>();

            Format id1 = buildFormat(1);
            Format id2 = buildFormat(2);

            ProfileResourceNode node = buildProfileResourceNode(1, 1000L);
            node.addFormatIdentification(id1);
            node.addFormatIdentification(id2);

            nodes.add(node);
            itemWriter.setOptions(ExportOptions.ONE_ROW_PER_FILE);
            itemWriter.setOutputOptions(ExportOutputOptions.JSON_OUTPUT);
            itemWriter.setColumnsToWrite("ID FORMAT_NAME");
            itemWriter.open(writer);
            itemWriter.write(nodes);

            final String[] expectedHeaders = new String[] {
                    "ID", "FORMAT_NAME", "FORMAT_NAME1" //per format columns
            };

            final String[] expectedEntry1 = new String[] {
                    null,
                    "Plain Text",
                    "Plain Text"
            };

            final String expectedJson = toJsonRow(expectedHeaders, expectedEntry1);

            final String[] lines = writer.toString().split(LINE_SEPARATOR);

            assertEquals(1, lines.length);
            assertEquals(expectedJson, lines[0]);
        }
    }

    @Test
    public void should_restrict_to_limited_number_of_columns_when_the_column_names_are_set_on_the_writer_writing_per_format() throws IOException {

        try(final Writer writer = new StringWriter()) {
            List<ProfileResourceNode> nodes = new ArrayList<>();

            Format id1 = buildFormat(1);
            Format id2 = buildFormat(2);

            ProfileResourceNode node = buildProfileResourceNode(1, 1000L);
            node.addFormatIdentification(id1);
            node.addFormatIdentification(id2);

            nodes.add(node);
            itemWriter.setOptions(ExportOptions.ONE_ROW_PER_FORMAT);
            itemWriter.setColumnsToWrite("ID NAME FORMAT_NAME FORMAT_VERSION");
            itemWriter.open(writer);
            itemWriter.write(nodes);

            final String expectedHeaders = toCsvRow(new String[] {
                    "ID", "NAME", "FORMAT_NAME", "FORMAT_VERSION" //per format columns
            });

            final String expectedEntry1 = toCsvRow(new String[] {
                    "",
                    "file1.txt",
                    "Plain Text",
                    "1.0"
            });

            final String[] lines = writer.toString().split(LINE_SEPARATOR);

            assertEquals(3, lines.length);
            assertEquals(expectedHeaders, lines[0]);
            assertEquals(expectedEntry1, lines[1]);
            assertEquals(expectedEntry1, lines[2]);
        }
    }

    @Test
    public void should_restrict_to_limited_number_of_entries_when_the_column_names_are_set_on_the_writer_writing_per_format() throws IOException {

        try(final Writer writer = new StringWriter()) {
            List<ProfileResourceNode> nodes = new ArrayList<>();

            Format id1 = buildFormat(1);
            Format id2 = buildFormat(2);

            ProfileResourceNode node = buildProfileResourceNode(1, 1000L);
            node.addFormatIdentification(id1);
            node.addFormatIdentification(id2);

            nodes.add(node);
            itemWriter.setOptions(ExportOptions.ONE_ROW_PER_FORMAT);
            itemWriter.setOutputOptions(ExportOutputOptions.JSON_OUTPUT);
            itemWriter.setColumnsToWrite("ID NAME FORMAT_NAME FORMAT_VERSION");
            itemWriter.open(writer);
            itemWriter.write(nodes);

            final String[] expectedHeaders = new String[] {
                    "ID", "NAME", "FORMAT_NAME", "FORMAT_VERSION" //per format columns
            };

            final String[] expectedEntry1 = new String[] {
                    null,
                    "file1.txt",
                    "Plain Text",
                    "1.0"
            };

            final String expectedJson = toJsonRows(expectedHeaders, new String[][]{expectedEntry1, expectedEntry1});

            final String[] lines = writer.toString().split(LINE_SEPARATOR);

            assertEquals(1, lines.length);
            assertEquals(expectedJson, lines[0]);
        }
    }

    @Test
    public void should_do_header_customisations_for_hash_column_when_an_algorithm_is_present() throws IOException {
        try(final Writer writer = new StringWriter()) {
            List<ProfileResourceNode> nodes = new ArrayList<>();

            Format id1 = buildFormat(1);

            ProfileResourceNode node = buildProfileResourceNode(1, 1000L);
            node.addFormatIdentification(id1);

            nodes.add(node);
            itemWriter.setOptions(ExportOptions.ONE_ROW_PER_FORMAT);

            Map<String, String> headerCustomisation = new HashMap<>();
            headerCustomisation.put("hash", "XTRA_STRONG_HASH");
            itemWriter.setHeaders(headerCustomisation);
            itemWriter.setColumnsToWrite("ID NAME FORMAT_NAME HASH");
            itemWriter.open(writer);
            itemWriter.write(nodes);

            final String expectedHeaders = toCsvRow(new String[] {
                    "ID", "NAME", "XTRA_STRONG_HASH", "FORMAT_NAME"
            });

            final String expectedEntry1 = toCsvRow(new String[] {
                    "",
                    "file1.txt",
                    "11111111111111111111111111111111",
                    "Plain Text"
            });

            final String[] lines = writer.toString().split(LINE_SEPARATOR);

            assertEquals(2, lines.length);
            assertEquals(expectedHeaders, lines[0]);
            assertEquals(expectedEntry1, lines[1]);
        }

    }

    @Test
    public void should_do_entry_customisations_for_hash_column_when_an_algorithm_is_present() throws IOException {
        try(final Writer writer = new StringWriter()) {
            List<ProfileResourceNode> nodes = new ArrayList<>();

            Format id1 = buildFormat(1);

            ProfileResourceNode node = buildProfileResourceNode(1, 1000L);
            node.addFormatIdentification(id1);

            nodes.add(node);
            itemWriter.setOptions(ExportOptions.ONE_ROW_PER_FORMAT);
            itemWriter.setOutputOptions(ExportOutputOptions.JSON_OUTPUT);

            Map<String, String> headerCustomisation = new HashMap<>();
            headerCustomisation.put("hash", "XTRA_STRONG_HASH");
            itemWriter.setHeaders(headerCustomisation);
            itemWriter.setColumnsToWrite("ID NAME FORMAT_NAME HASH");
            itemWriter.open(writer);
            itemWriter.write(nodes);

            final String[] expectedHeaders = new String[] {
                    "ID", "NAME", "XTRA_STRONG_HASH", "FORMAT_NAME"
            };

            final String[] expectedEntry1 = new String[] {
                    null,
                    "file1.txt",
                    "11111111111111111111111111111111",
                    "Plain Text"
            };

            final String expectedJson = toJsonRow(expectedHeaders, expectedEntry1);

            final String[] lines = writer.toString().split(LINE_SEPARATOR);

            assertEquals(1, lines.length);
            assertEquals(expectedJson, lines[0]);
        }

    }

    @Test
    public void testWriteOneNodeWithTwoFormatsWithOneRowPerFormat() throws IOException {
        when(config.getBooleanProperty(DroidGlobalProperty.CSV_EXPORT_ROW_PER_FORMAT)).thenReturn(true);

        try(final Writer writer = new StringWriter()) {
            List<ProfileResourceNode> nodes = new ArrayList<>();

            Format id1 = buildFormat(1);
            Format id2 = buildFormat(2, "2.0");

            ProfileResourceNode node = buildProfileResourceNode(1, 1000L);
            node.addFormatIdentification(id1);
            node.addFormatIdentification(id2);

            nodes.add(node);
            itemWriter.setOptions(ExportOptions.ONE_ROW_PER_FORMAT);
            itemWriter.open(writer);
            itemWriter.write(nodes);

            final String expectedEntry1 = toCsvRow(new String[] {
                    "", "",
                    isNotWindows() ? "file:/my/file1.txt" : "file:/C:/my/file1.txt",
                    isNotWindows() ? "/my/file1.txt" : "C:\\my\\file1.txt",
                    "file1.txt",
                    "Signature",
                    "Done",
                    "1000",
                    "File",
                    "foo",
                    testDateTimeString,
                    "false",
                    "11111111111111111111111111111111",
                    "2",
                    "fmt/1",
                    "text/plain",
                    "Plain Text",
                    "1.0",
            });

            final String expectedEntry2 = toCsvRow(new String[] {
                    "", "",
                    isNotWindows() ? "file:/my/file1.txt" : "file:/C:/my/file1.txt",
                    isNotWindows() ? "/my/file1.txt" : "C:\\my\\file1.txt",
                    "file1.txt",
                    "Signature",
                    "Done",
                    "1000",
                    "File",
                    "foo",
                    testDateTimeString,
                    "false",
                    "11111111111111111111111111111111",
                    "2",
                    "fmt/2",
                    "text/plain",
                    "Plain Text",
                    "2.0",
            });

            final String[] lines = writer.toString().split(LINE_SEPARATOR);

            assertEquals(3, lines.length);
            assertEquals(expectedEntry1, lines[1]);
            assertEquals(expectedEntry2, lines[2]);
        }
    }

    @Test
    public void testWriteOneNodeWithTwoFormatsWithOneEntryPerFormat() throws IOException {
        when(config.getBooleanProperty(DroidGlobalProperty.CSV_EXPORT_ROW_PER_FORMAT)).thenReturn(true);

        try(final Writer writer = new StringWriter()) {
            List<ProfileResourceNode> nodes = new ArrayList<>();

            Format id1 = buildFormat(1);
            Format id2 = buildFormat(2, "2.0");

            ProfileResourceNode node = buildProfileResourceNode(1, 1000L);
            node.addFormatIdentification(id1);
            node.addFormatIdentification(id2);

            nodes.add(node);
            itemWriter.setOptions(ExportOptions.ONE_ROW_PER_FORMAT);
            itemWriter.setOutputOptions(ExportOutputOptions.JSON_OUTPUT);
            itemWriter.open(writer);
            itemWriter.write(nodes);

            final Object[] expectedEntry1 = new Object[] {
                    null, null,
                    isNotWindows() ? "file:/my/file1.txt" : "file:/C:/my/file1.txt",
                    isNotWindows() ? "/my/file1.txt" : "C:\\my\\file1.txt",
                    "file1.txt",
                    "Signature",
                    "Done",
                    1000,
                    "File",
                    "foo",
                    testDateTimeString,
                    "false",
                    "11111111111111111111111111111111",
                    "2",
                    "fmt/1",
                    "text/plain",
                    "Plain Text",
                    "1.0",
            };

            final Object[] expectedEntry2 = new Object[] {
                    null, null,
                    isNotWindows() ? "file:/my/file1.txt" : "file:/C:/my/file1.txt",
                    isNotWindows() ? "/my/file1.txt" : "C:\\my\\file1.txt",
                    "file1.txt",
                    "Signature",
                    "Done",
                    1000,
                    "File",
                    "foo",
                    testDateTimeString,
                    "false",
                    "11111111111111111111111111111111",
                    "2",
                    "fmt/2",
                    "text/plain",
                    "Plain Text",
                    "2.0",
            };

            final String expectedJson = toJsonRows(defaultHeadersArray, new Object[][]{expectedEntry1, expectedEntry2});

            final String[] lines = writer.toString().split(LINE_SEPARATOR);

            assertEquals(1, lines.length);
            assertEquals(expectedJson, lines[0]);
        }
    }

    @Test
    public void testWriteTenNodes() throws IOException {
        when(config.getBooleanProperty(DroidGlobalProperty.CSV_EXPORT_ROW_PER_FORMAT)).thenReturn(true);

        try(final Writer writer = new StringWriter()) {
            List<ProfileResourceNode> nodes = new ArrayList<>();

            for (int i = 1; i <= 10; i++) {
                ProfileResourceNode node = buildProfileResourceNode(i, (long) i);
                Format id = buildFormat(i);
                node.addFormatIdentification(id);
                nodes.add(node);
            }

            itemWriter.open(writer);
            itemWriter.write(nodes);

            final String[] lines = writer.toString().split("\n");

            assertEquals(11, lines.length);
        }
    }

    @Test
    public void testWriteTenNodesToJson() throws IOException {
        when(config.getBooleanProperty(DroidGlobalProperty.CSV_EXPORT_ROW_PER_FORMAT)).thenReturn(true);
        itemWriter.setOutputOptions(ExportOutputOptions.JSON_OUTPUT);
        try(final Writer writer = new StringWriter()) {
            List<ProfileResourceNode> nodes = new ArrayList<>();

            for (int i = 1; i <= 10; i++) {
                ProfileResourceNode node = buildProfileResourceNode(i, (long) i);
                Format id = buildFormat(i);
                node.addFormatIdentification(id);
                nodes.add(node);
            }

            itemWriter.open(writer);
            itemWriter.write(nodes);

            final String[] lines = writer.toString().split("\n");

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(lines[0]);

            assertEquals(10, jsonNode.size());
        }
    }

    @Test
    public void should_write_nodes_inside_archives_using_separators_depending_on_OS() throws IOException, URISyntaxException {
        when(config.getBooleanProperty(DroidGlobalProperty.CSV_EXPORT_ROW_PER_FORMAT)).thenReturn(false);

        try(final Writer writer = new StringWriter()) {
            List<ProfileResourceNode> nodes = new ArrayList<>();
            Format id = buildFormat(999);
            ProfileResourceNode node = buildProfileResourceNode(34, 2000L,
                    new URI("sevenz:file:///home/user/test-data/droid/archives/samples.7z!/Screenshot%20from%202020-01-22%2021-36-16.png"));
            node.addFormatIdentification(id);
            nodes.add(node);

            ProfileResourceNode node2 = buildProfileResourceNode(35, 2000L,
                    new URI("rar:file:///home/user/test-data/droid/archives/custom%20sample.rar!/dir/another%20dir/fmt-143-signature-id-608.wav"));
            node2.addFormatIdentification(id);
            nodes.add(node2);
            itemWriter.setOptions(ExportOptions.ONE_ROW_PER_FORMAT);
            itemWriter.open(writer);
            itemWriter.write(nodes);
            final String[] lines = writer.toString().split(LINE_SEPARATOR);
            String expectedFilePath = isNotWindows() ?
                    "\"7z:/home/user/test-data/droid/archives/samples.7z!/Screenshot from 2020-01-22 21-36-16.png\"" :
                    "\"7z:\\home\\user\\test-data\\droid\\archives\\samples.7z!\\Screenshot from 2020-01-22 21-36-16.png\"";
            assertEquals(expectedFilePath, lines[1].split(",")[3]);

            String expectedFilePath2 = isNotWindows() ?
                    "\"rar:/home/user/test-data/droid/archives/custom sample.rar!/dir/another dir/fmt-143-signature-id-608.wav\"" :
                    "\"rar:\\home\\user\\test-data\\droid\\archives\\custom sample.rar!\\dir\\another dir\\fmt-143-signature-id-608.wav\"";

            assertEquals(expectedFilePath2, lines[2].split(",")[3]);
        }
    }

    @Test
    public void should_write_json_values_inside_archives_using_separators_depending_on_OS() throws IOException, URISyntaxException {
        when(config.getBooleanProperty(DroidGlobalProperty.CSV_EXPORT_ROW_PER_FORMAT)).thenReturn(false);

        try(final Writer writer = new StringWriter()) {
            List<ProfileResourceNode> nodes = new ArrayList<>();
            Format id = buildFormat(999);
            ProfileResourceNode node = buildProfileResourceNode(34, 2000L,
                    new URI("sevenz:file:///home/user/test-data/droid/archives/samples.7z!/Screenshot%20from%202020-01-22%2021-36-16.png"));
            node.addFormatIdentification(id);
            nodes.add(node);

            ProfileResourceNode node2 = buildProfileResourceNode(35, 2000L,
                    new URI("rar:file:///home/user/test-data/droid/archives/custom%20sample.rar!/dir/another%20dir/fmt-143-signature-id-608.wav"));
            node2.addFormatIdentification(id);
            nodes.add(node2);
            itemWriter.setOptions(ExportOptions.ONE_ROW_PER_FORMAT);
            itemWriter.setOutputOptions(ExportOutputOptions.JSON_OUTPUT);
            itemWriter.open(writer);
            itemWriter.write(nodes);
            final String[] lines = writer.toString().split(LINE_SEPARATOR);

            JsonNode jsonNode = new ObjectMapper().readTree(lines[0]);
            String path1 = jsonNode.get(0).get("FILE_PATH").asText();
            String expectedFilePath = isNotWindows() ?
                    "7z:/home/user/test-data/droid/archives/samples.7z!/Screenshot from 2020-01-22 21-36-16.png" :
                    "7z:\\home\\user\\test-data\\droid\\archives\\samples.7z!\\Screenshot from 2020-01-22 21-36-16.png";
            assertEquals(expectedFilePath, path1);

            String path2 = jsonNode.get(1).get("FILE_PATH").asText();
            String expectedFilePath2 = isNotWindows() ?
                    "rar:/home/user/test-data/droid/archives/custom sample.rar!/dir/another dir/fmt-143-signature-id-608.wav" :
                    "rar:\\home\\user\\test-data\\droid\\archives\\custom sample.rar!\\dir\\another dir\\fmt-143-signature-id-608.wav";

            assertEquals(expectedFilePath2, path2);
        }
    }

    @Test
    public void should_write_data_row_with_additional_blank_elements_when_other_rows_have_more_identified_formats() throws IOException {
        when(config.getBooleanProperty(DroidGlobalProperty.CSV_EXPORT_ROW_PER_FORMAT)).thenReturn(false);

        try(final Writer writer = new StringWriter()) {
            List<ProfileResourceNode> nodes = new ArrayList<>();

            Format id1 = buildFormat(1);
            Format id2 = buildFormat(2);

            ProfileResourceNode nodeWithTwoIdentifications = buildProfileResourceNode(1, 1000L);
            nodeWithTwoIdentifications.addFormatIdentification(id1);
            nodeWithTwoIdentifications.addFormatIdentification(id2);

            ProfileResourceNode nodeWithOneIentifications = buildProfileResourceNode(2, 500L);
            Format id3 = buildFormat(3);
            nodeWithOneIentifications.addFormatIdentification(id3);

            nodes.add(nodeWithTwoIdentifications);
            nodes.add(nodeWithOneIentifications);

            itemWriter.setOptions(ExportOptions.ONE_ROW_PER_FILE);
            itemWriter.setColumnsToWrite("ID PUID FORMAT_NAME METHOD");
            itemWriter.open(writer);
            itemWriter.write(nodes);

            final String expectedHeaders = toCsvRow(new String[] {
                    "ID","METHOD","PUID","FORMAT_NAME","PUID1","FORMAT_NAME1"
            });

            final String expectedEntry1 = toCsvRow(new String[] {
                    "",
                    "Signature",
                    "fmt/1",
                    "Plain Text",
                    "fmt/2",
                    "Plain Text"
            });

            final String expectedEntry2 = toCsvRow(new String[] {
                    "",
                    "Signature",
                    "fmt/3",
                    "Plain Text",
                    "",
                    ""
            });

            final String[] lines = writer.toString().split(LINE_SEPARATOR);

            assertEquals(3, lines.length);
            assertEquals(expectedHeaders, lines[0]);
            assertEquals(expectedEntry1, lines[1]);
            assertEquals(expectedEntry2, lines[2]);
        }
    }

    @Test
    public void should_write_data_entry_with_additional_blank_elements_when_other_rows_have_more_identified_formats() throws IOException {
        when(config.getBooleanProperty(DroidGlobalProperty.CSV_EXPORT_ROW_PER_FORMAT)).thenReturn(false);

        try(final Writer writer = new StringWriter()) {
            List<ProfileResourceNode> nodes = new ArrayList<>();

            Format id1 = buildFormat(1);
            Format id2 = buildFormat(2);

            ProfileResourceNode nodeWithTwoIdentifications = buildProfileResourceNode(1, 1000L);
            nodeWithTwoIdentifications.addFormatIdentification(id1);
            nodeWithTwoIdentifications.addFormatIdentification(id2);

            ProfileResourceNode nodeWithOneIentifications = buildProfileResourceNode(2, 500L);
            Format id3 = buildFormat(3);
            nodeWithOneIentifications.addFormatIdentification(id3);

            nodes.add(nodeWithTwoIdentifications);
            nodes.add(nodeWithOneIentifications);

            itemWriter.setOptions(ExportOptions.ONE_ROW_PER_FILE);
            itemWriter.setOutputOptions(ExportOutputOptions.JSON_OUTPUT);
            itemWriter.setColumnsToWrite("ID PUID FORMAT_NAME METHOD");
            itemWriter.open(writer);
            itemWriter.write(nodes);

            final String[] expectedHeaders = new String[] {
                    "ID","METHOD","PUID","FORMAT_NAME","PUID1","FORMAT_NAME1"
            };

            final String[] expectedEntry1 = new String[] {
                    null,
                    "Signature",
                    "fmt/1",
                    "Plain Text",
                    "fmt/2",
                    "Plain Text"
            };

            final String[] expectedEntry2 = new String[] {
                    null,
                    "Signature",
                    "fmt/3",
                    "Plain Text",
                    "",
                    ""
            };

            final String expectedJson = toJsonRows(expectedHeaders, new Object[][]{expectedEntry1, expectedEntry2});

            final String[] lines = writer.toString().split(LINE_SEPARATOR);

            assertEquals(1, lines.length);
            assertEquals(expectedJson, lines[0]);
        }
    }


    private static ExportTemplateColumnDef getMockColumnDef(String param1, String header, ExportTemplateColumnDef.ColumnType columnType) {
        ExportTemplateColumnDef def = mock(ExportTemplateColumnDef.class);
        when(def.getColumnType()).thenReturn(columnType);
        switch (columnType) {
            case ProfileResourceNode:
                when(def.getOriginalColumnName()).thenReturn(param1);
                when(def.getHeaderLabel()).thenReturn(header);
                when(def.getDataValue()).thenThrow(new RuntimeException("Profile resource node column uses data from the profile results"));
                when(def.getOperatedValue(anyString())).thenAnswer(i -> i.getArguments()[0]);
                break;
            case ConstantString:
                when(def.getOriginalColumnName()).thenThrow(new RuntimeException("Constant String Columns do not have an associated original column name"));
                when(def.getHeaderLabel()).thenReturn(header);
                when(def.getDataValue()).thenReturn(param1);
                break;
            case DataModifier:
                when(def.getOriginalColumnName()).thenReturn(param1);
                when(def.getHeaderLabel()).thenReturn(header);
                when(def.getDataValue()).thenThrow(new RuntimeException("Profile resource node column uses data from the profile results"));
                when(def.getOperatedValue(anyString())).thenAnswer(i -> i.getArguments()[0].toString().toLowerCase());
                break;
        }
        return def;
    }

    private static boolean isNotWindows() {
        return !SystemUtils.IS_OS_WINDOWS;
    }
    
    private static ProfileResourceNode buildProfileResourceNode(int i, Long size) {
        
        File f = isNotWindows() ? new File("/my/file" + i + ".txt") : new File("C:/my/file" + i + ".txt");
        return buildProfileResourceNode(i, size, f.toURI());
    }

    private static ProfileResourceNode buildProfileResourceNode(int i, Long size, URI uriOfNode) {
        ProfileResourceNode node = new ProfileResourceNode(uriOfNode);
        node.setExtensionMismatch(false);
        NodeMetaData metaData = new NodeMetaData();
        metaData.setExtension("foo");
        metaData.setIdentificationMethod(IdentificationMethod.BINARY_SIGNATURE);
        metaData.setLastModified(testDateTime.getMillis());
        metaData.setName("file" + i + ".txt");
        metaData.setNodeStatus(NodeStatus.DONE);
        metaData.setResourceType(ResourceType.FILE);
        metaData.setSize(size);
        metaData.setHash("1111111111111111111111111111111" + i);
        node.setMetaData(metaData);

        return node;
    }

    
    private static Format buildFormat(int i) {
        return buildFormat(i, "1.0");
    }

    private static Format buildFormat(int i, String version) {
        Format format = new Format();
        format.setPuid("fmt/" + i);
        format.setMimeType("text/plain");
        format.setName("Plain Text");
        format.setVersion(version);

        return format;
    }

}
