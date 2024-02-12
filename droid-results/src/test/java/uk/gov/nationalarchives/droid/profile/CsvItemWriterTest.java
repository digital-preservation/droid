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
public class CsvItemWriterTest {

    private static final DateTime testDateTime = new DateTime(12345678L);
    private static final String LINE_SEPARATOR = "\n";
    private CsvItemWriter itemWriter;
    private DroidGlobalConfig config;
    private String testDateTimeString;

    private static final String defaultHeaders = toCsvRow(new String[] {
            "ID","PARENT_ID","URI","FILE_PATH","NAME","METHOD","STATUS","SIZE","TYPE","EXT","LAST_MODIFIED","EXTENSION_MISMATCH","HASH","FORMAT_COUNT",
            "PUID","MIME_TYPE","FORMAT_NAME","FORMAT_VERSION"
    });

    @Before
    public void setup() {
        File dir = new File("exports");
        dir.mkdir();
        File destination = new File(dir, "test1.csv");
        destination.delete();
        itemWriter = new CsvItemWriter();
        
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
    public void should_write_nodes_inside_archives_using_seaparators_depending_on_OS() throws IOException, URISyntaxException {
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
