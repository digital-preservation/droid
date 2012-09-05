/**
 * Copyright (c) 2012, The National Archives <pronom@nationalarchives.gsi.gov.uk>
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
package uk.gov.nationalarchives.droid.export;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.lang.SystemUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;

import uk.gov.nationalarchives.droid.core.interfaces.IdentificationMethod;
import uk.gov.nationalarchives.droid.core.interfaces.NodeStatus;
import uk.gov.nationalarchives.droid.core.interfaces.ResourceType;
import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalConfig;
import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalProperty;
import uk.gov.nationalarchives.droid.export.interfaces.ExportOptions;
import uk.gov.nationalarchives.droid.export.interfaces.JobOptions;
import uk.gov.nationalarchives.droid.profile.NodeMetaData;
import uk.gov.nationalarchives.droid.profile.ProfileResourceNode;
import uk.gov.nationalarchives.droid.profile.referencedata.Format;

/**
 * @author rflitcroft
 *
 */
public class CsvItemWriterTest {

	private static DateTime testDateTime = new DateTime(12345678L);
    private CsvItemWriter itemWriter;
    private File destination;
    private CSVWriter csvWriter;
    private DroidGlobalConfig config;
    private boolean windows;
    private String testDateTimeString;
    
    @Before
    public void setup() {
        File dir = new File("exports");
        dir.mkdir();
        destination = new File(dir, "test1.csv");
        destination.delete();
        itemWriter = new CsvItemWriter();
        csvWriter = mock(CSVWriter.class);
        itemWriter.setCsvWriter(csvWriter);
        
        config = mock(DroidGlobalConfig.class);
        itemWriter.setConfig(config);
        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");
        testDateTimeString = dtf.print(testDateTime);
        
    }
    
    @Test
    public void testWriteHeadersOnOpen() throws IOException {
        Writer writer = mock(Writer.class);
        
        JobOptions jobOptions = mock(JobOptions.class);
        when(jobOptions.getParameter("location")).thenReturn("test.csv");
        
        when(config.getBooleanProperty(DroidGlobalProperty.CSV_EXPORT_ROW_PER_FORMAT)).thenReturn(false);
        itemWriter.setOptions(ExportOptions.ONE_ROW_PER_FORMAT);
        itemWriter.open(writer);
        String expectedString =
              "\"ID\","
            + "\"PARENT_ID\","
            + "\"URI\","
            + "\"FILE_PATH\","
            + "\"NAME\","
            + "\"METHOD\","
            + "\"STATUS\","
            + "\"SIZE\","
            + "\"TYPE\","
            + "\"EXT\","
            + "\"LAST_MODIFIED\","
            + "\"EXTENSION_MISMATCH\","
            + "\"MD5_HASH\","
            + "\"FORMAT_COUNT\","
            + "\"PUID\","
            + "\"MIME_TYPE\","
            + "\"FORMAT_NAME\","
            + "\"FORMAT_VERSION\"\n";
        
        verify(writer).write(expectedString, 0, expectedString.length());
    }
    
    @Test
    public void testWriteNoNodes() {
        when(config.getBooleanProperty(DroidGlobalProperty.CSV_EXPORT_ROW_PER_FORMAT)).thenReturn(false);
        
        List<ProfileResourceNode> nodes = new ArrayList<ProfileResourceNode>();
        itemWriter.write(nodes);
        verify(csvWriter, never()).writeNext(any(String[].class));
    }

    @Test
    public void testWriteOneNode() {
        when(config.getBooleanProperty(DroidGlobalProperty.CSV_EXPORT_ROW_PER_FORMAT)).thenReturn(false);
        
        List<ProfileResourceNode> nodes = new ArrayList<ProfileResourceNode>();
        Format id = buildFormat(1);
        ProfileResourceNode node = buildProfileResourceNode(1, 1001L);
        node.addFormatIdentification(id);
        nodes.add(node);
        itemWriter.setOptions(ExportOptions.ONE_ROW_PER_FORMAT);
        itemWriter.write(nodes);
        
        String[] expectedEntry = new String[] {
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
        };
        
        verify(csvWriter).writeNext(expectedEntry);
    }

    @Test
    public void testWriteNodeWithNullFormat() {
        when(config.getBooleanProperty(DroidGlobalProperty.CSV_EXPORT_ROW_PER_FORMAT)).thenReturn(false);
        
        List<ProfileResourceNode> nodes = new ArrayList<ProfileResourceNode>();
        
        Format id = Format.NULL;
        ProfileResourceNode node = buildProfileResourceNode(1, 1001L);
        node.addFormatIdentification(id);
        
        nodes.add(node);
        itemWriter.setOptions(ExportOptions.ONE_ROW_PER_FORMAT);
        itemWriter.write(nodes);
        
        String[] expectedEntry = new String[] {
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
        };
        
        verify(csvWriter).writeNext(expectedEntry);
    }

    @Test
    public void testWriteOneNodeWithNullSize() {
        when(config.getBooleanProperty(DroidGlobalProperty.CSV_EXPORT_ROW_PER_FORMAT)).thenReturn(false);
        
        List<ProfileResourceNode> nodes = new ArrayList<ProfileResourceNode>();
        
        Format id = buildFormat(1);
        ProfileResourceNode node = buildProfileResourceNode(1, null);
        node.addFormatIdentification(id);
        
        nodes.add(node);
        itemWriter.write(nodes);
        
        String[] expectedEntry = new String[] {
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
        };
        
        verify(csvWriter).writeNext(expectedEntry);
    }

    @Test
    public void testWriteOneNodeWithTwoFormatsWithOneRowPerFile() {
        when(config.getBooleanProperty(DroidGlobalProperty.CSV_EXPORT_ROW_PER_FORMAT)).thenReturn(false);
        
        List<ProfileResourceNode> nodes = new ArrayList<ProfileResourceNode>();
        
        Format id1 = buildFormat(1);
        Format id2 = buildFormat(2);
        
        ProfileResourceNode node = buildProfileResourceNode(1, 1000L);
        node.addFormatIdentification(id1);
        node.addFormatIdentification(id2);

        nodes.add(node);
        itemWriter.setOptions(ExportOptions.ONE_ROW_PER_FILE);
        itemWriter.write(nodes);
        
        String[] expectedEntry1 = new String[] {
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
        };
        
        verify(csvWriter).writeNext(expectedEntry1);
    }

    @Test
    public void testWriteOneNodeWithTwoFormatsWithOneRowPerFormat() {
        when(config.getBooleanProperty(DroidGlobalProperty.CSV_EXPORT_ROW_PER_FORMAT)).thenReturn(true);
        
        List<ProfileResourceNode> nodes = new ArrayList<ProfileResourceNode>();
        
        Format id1 = buildFormat(1);
        Format id2 = buildFormat(2);
        
        ProfileResourceNode node = buildProfileResourceNode(1, 1000L);
        node.addFormatIdentification(id1);
        node.addFormatIdentification(id2);

        nodes.add(node);
        itemWriter.setOptions(ExportOptions.ONE_ROW_PER_FORMAT);
        itemWriter.write(nodes);
        
        String[] expectedEntry1 = new String[] {
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
        };
        
        String[] expectedEntry2 = new String[] {
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
        };

        verify(csvWriter).writeNext(expectedEntry1);
        verify(csvWriter).writeNext(expectedEntry2);
    }

    @Test
    public void testWriteTenNodes() {
        when(config.getBooleanProperty(DroidGlobalProperty.CSV_EXPORT_ROW_PER_FORMAT)).thenReturn(true);

        List<ProfileResourceNode> nodes = new ArrayList<ProfileResourceNode>();
        
        for (int i = 1; i <= 10; i++) {
            ProfileResourceNode node = buildProfileResourceNode(i, (long) i); 
            Format id = buildFormat(i);
            node.addFormatIdentification(id);
            nodes.add(node);
        }
        
        itemWriter.write(nodes);
        verify(csvWriter, times(10)).writeNext(any(String[].class));
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
    
    
    private static Format buildFormat(int i) {
        Format format = new Format();
        format.setPuid("fmt/" + i);
        format.setMimeType("text/plain");
        format.setName("Plain Text");
        format.setVersion("1.0");
        
        return format;
    }
}
