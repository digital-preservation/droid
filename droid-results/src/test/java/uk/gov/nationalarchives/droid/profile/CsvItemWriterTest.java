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

//CHECKSTYLE:OFF

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

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
import uk.gov.nationalarchives.droid.export.interfaces.ExportOptions;
import uk.gov.nationalarchives.droid.profile.referencedata.Format;

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

	private static DateTime testDateTime = new DateTime(12345678L);
	private static final String LINE_SEPARATOR = "\n";
    private CsvItemWriter itemWriter;
    private File destination;
    private String testDateTimeString;
    
    @Before
    public void setup() {
        File dir = new File("exports");
        dir.mkdir();
        destination = new File(dir, "test1.csv");
        destination.delete();
        itemWriter = new CsvItemWriter();
        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");
        testDateTimeString = dtf.print(testDateTime);
    }

    @After
    public void tearDown() {
        itemWriter.close();
    }

    private static String toCsvRow(final String...values) {
        return "\"" + StringUtils.join(values, "\",\"") + "\"";
    }

    @Test
    public void testWriteNoNodes() throws IOException {
        try(final Writer writer = new StringWriter()) {
            List<ProfileResourceNode> nodes = new ArrayList<>();
            itemWriter.open(writer);
            itemWriter.write(nodes);

            assertEquals(1, writer.toString().split("\n").length);
        }
    }

    @Test
    public void testWriteOneNode() throws IOException {
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
            assertEquals(expectedEntry, lines[1]);
        }
    }

    @Test
    public void testWriteNodeWithNullFormat() throws IOException {
        try(final Writer writer = new StringWriter()) {
            List<ProfileResourceNode> nodes = new ArrayList<>();

            Format id = Format.NULL;
            ProfileResourceNode node = buildProfileResourceNode(1, 1001L);
            node.addFormatIdentification(id);

            nodes.add(node);
            itemWriter.setOptions(ExportOptions.ONE_ROW_PER_FORMAT);
            itemWriter.open(writer);
            itemWriter.write(nodes);

            final String expectedEntry = toCsvRow(
                    "",
                    "",
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
                    "",
                    "",
                    "",
                    ""
            );

            final String[] lines = writer.toString().split(LINE_SEPARATOR);

            assertEquals(2, lines.length);
            assertEquals(expectedEntry, lines[1]);
        }
    }

    @Test
    public void testWriteOneNodeWithNullSize() throws IOException {
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
    public void testWriteOneNodeWithTwoFormatsWithOneRowPerFile() throws IOException {
        try(final Writer writer = new StringWriter()) {
            List<ProfileResourceNode> nodes = new ArrayList<>();

            Format format1 = buildFormat(1);
            Format format2 = buildFormat(2);

            ProfileResourceNode node = buildProfileResourceNode(1, 1000L);
            node.addFormatIdentification(format1);
            node.addFormatIdentification(format2);

            nodes.add(node);
            itemWriter.setOptions(ExportOptions.ONE_ROW_PER_FILE);
            itemWriter.open(writer);
            itemWriter.write(nodes);

            // Check the number of lines
            final String[] lines = writer.toString().split(LINE_SEPARATOR);
            assertEquals(2, lines.length);
            
            // Check the header row
            final String expectedHeaderRow = toCsvRow(
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
	    		
	    		// Headers for first format
	    		"PUID",
	    		"MIME_TYPE",
	    		"FORMAT_NAME",
	    		"FORMAT_VERSION",
	    		
	    		// Headers for second format
	    		"PUID2",
	    		"MIME_TYPE2",
	    		"FORMAT_NAME2",
	    		"FORMAT_VERSION2");
            assertEquals(expectedHeaderRow, lines[0]);
            
            // Check the data row
            final String expectedEntry1 = toCsvRow(
           		"", 
           		"",
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
				"1.0");
            assertEquals(expectedEntry1, lines[1]);
        }
    }


    @Test
    public void testWriteMultipleNodesWithMixedFormatsWithOneRowPerFile() throws IOException {
        try(final Writer writer = new StringWriter()) {
            List<ProfileResourceNode> nodes = new ArrayList<>();

            // Create the first node with one format
            ProfileResourceNode nodeWithOneFormat = buildProfileResourceNode(1, 1000L);
            nodeWithOneFormat.addFormatIdentification(buildFormat(1));
            nodes.add(nodeWithOneFormat);
            
            // Create the second node with three formats
            ProfileResourceNode nodeWithThreeFormats = buildProfileResourceNode(1, 1000L);
            nodeWithThreeFormats.addFormatIdentification(buildFormat(2));
            nodeWithThreeFormats.addFormatIdentification(buildFormat(3));
            nodeWithThreeFormats.addFormatIdentification(buildFormat(4));
            nodes.add(nodeWithThreeFormats);
            
            itemWriter.setOptions(ExportOptions.ONE_ROW_PER_FILE);
            itemWriter.open(writer);
            itemWriter.write(nodes);

            // Check the number of lines
            final String[] lines = writer.toString().split(LINE_SEPARATOR);
            assertEquals(3, lines.length);
            
            // Check the header row
            final String expectedHeaderRow = toCsvRow(
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
	    		
	    		// Headers for first format
	    		"PUID",
	    		"MIME_TYPE",
	    		"FORMAT_NAME",
	    		"FORMAT_VERSION",
	    		
	    		// Headers for second format
	    		"PUID2",
	    		"MIME_TYPE2",
	    		"FORMAT_NAME2",
	    		"FORMAT_VERSION2",
	            
	            // Headers for third format
	            "PUID3",
	            "MIME_TYPE3",
	            "FORMAT_NAME3",
	            "FORMAT_VERSION3");
            
            assertEquals(expectedHeaderRow, lines[0]);
            
            // Check the first data row which contains only one file format identification
            final String expectedEntry1 = toCsvRow(
           		"", 
           		"",
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
				"1",
				"fmt/1",
				"text/plain",
				"Plain Text",
				"1.0",
				
				// Unused format padding to make the value row match the header row
				"","","","","","","","");
            assertEquals(expectedEntry1, lines[1]);

            // Check the second data row which contains three file format identifications
            final String expectedEntry2 = toCsvRow(
        		"", 
        		"",
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
				"3",
				
				"fmt/2",
				"text/plain",
				"Plain Text",
				"1.0",
				
				"fmt/3",
				"text/plain",
				"Plain Text",
				"1.0",
				
				"fmt/4",
				"text/plain",
				"Plain Text",
				"1.0");
            assertEquals(expectedEntry2, lines[2]);
        }
    }
    
    @Test
    public void testWriteOneNodeWithTwoFormatsWithOneRowPerFormat() throws IOException {
        try(final Writer writer = new StringWriter()) {
            List<ProfileResourceNode> nodes = new ArrayList<>();

            Format id1 = buildFormat(1);
            Format id2 = buildFormat(2);

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
                    "1.0",
            });

            final String[] lines = writer.toString().split(LINE_SEPARATOR);

            assertEquals(3, lines.length);
            assertEquals(expectedEntry1, lines[1]);
            assertEquals(expectedEntry2, lines[2]);
        }
    }

    @Test
    public void testWriteTenNodes() throws IOException {
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
    
    private static boolean isNotWindows() {
        return !SystemUtils.IS_OS_WINDOWS;
    }
    
    private static ProfileResourceNode buildProfileResourceNode(int i, Long size) {
        
        File f = isNotWindows() ? new File("/my/file" + i + ".txt") : new File("C:/my/file" + i + ".txt");
        ProfileResourceNode node = new ProfileResourceNode(f.toURI());
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
    
    
    private static Format buildFormat(int puidSuffix) {
        Format format = new Format();
        format.setPuid("fmt/" + puidSuffix);
        format.setMimeType("text/plain");
        format.setName("Plain Text");
        format.setVersion("1.0");
        
        return format;
    }
}

//CHECKSTYLE:ON