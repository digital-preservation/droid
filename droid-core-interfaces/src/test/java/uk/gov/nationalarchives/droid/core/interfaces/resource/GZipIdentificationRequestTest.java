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
package uk.gov.nationalarchives.droid.core.interfaces.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;

/**
 * @author rflitcroft
 *
 */
public class GZipIdentificationRequestTest {
    private static File tmpDir;

    private static String fileData;

    private GZipIdentificationRequest gzRequest;
    private File file;

    private RequestMetaData metaData;
    private RequestIdentifier identifier;
    
    
    @AfterClass
    public static void removeTmpDir() {
        FileUtils.deleteQuietly(tmpDir);
    }
    
    @BeforeClass
    public static void setupTestData() throws IOException {
        tmpDir = new File("tmp");
        tmpDir.mkdir();

        File file = new File(GZipIdentificationRequestTest.class.getResource("/testXmlFile.xml.gz").getFile());
        GzipCompressorInputStream in = new GzipCompressorInputStream(new FileInputStream(file));
        
        Reader reader = new InputStreamReader(in);
        char[] buffer = new char[8192];
        int length;
        StringBuilder sb = new StringBuilder();
        while ((length = reader.read(buffer)) != -1) {
            sb.append(buffer, 0, length);
        }
        fileData = sb.toString();
    }    
    
    @Before
    public void setup() throws Exception {
    
        file = new File(getClass().getResource("/testXmlFile.xml.gz").getFile());
        
        metaData = new RequestMetaData(null, null, "foo");
        identifier = new RequestIdentifier(URI.create(GzipUtils.getUncompressedFilename(file.toURI().toString())));
        gzRequest = new GZipIdentificationRequest(
                metaData, identifier,
                100, 12000, new File("tmp"));
        GzipCompressorInputStream in = new GzipCompressorInputStream(new FileInputStream(file));
        gzRequest.open(in);
    }
    
    @After
    public void tearDown() throws IOException {
        gzRequest.close();
    }
    
    @Test
    public void testOneArgContructor() throws IOException {
        file = new File(getClass().getResource("/testXmlFile.xml.gz").getFile());
        identifier = new RequestIdentifier(URI.create(GzipUtils.getUncompressedFilename(file.toURI().toString())));
        gzRequest = new GZipIdentificationRequest(
                new RequestMetaData(12L, 13L, file.getName()), identifier, tmpDir);
        
        GzipCompressorInputStream in = new GzipCompressorInputStream(
                new FileInputStream(file));
        gzRequest.open(in);
        CachedBytes cache = gzRequest.getCache();
        //assertEquals(1, cache.getBuffers().size());
        assertNotNull(cache.getSourceFile());
    }
    
    @Test
    public void testGetSize() {
        assertEquals(fileData.length(), gzRequest.size());
    }
    
    @Test
    public void testGetEveryByteSequencially() {
        
        int size = (int) gzRequest.size();
        byte[] bin = new byte[size];
        
        int i;
        for (i = 0; i < size; i++) {
            bin[i] = gzRequest.getByte(i);
        }
        
        assertEquals(fileData, new String(bin));
        
        try {
            gzRequest.getByte(i);
            //fail("Expected IndexOutOfBoundsException.");
        } catch (IndexOutOfBoundsException e) {
            //assertEquals("No byte at position [" + i + "]", e.getMessage());
        }
        
    }
    
    @Test
    public void testGetByte3FollowedByByte42() {
        
        assertEquals(fileData.getBytes()[3], gzRequest.getByte(3));
        assertEquals(fileData.getBytes()[42], gzRequest.getByte(42));
        
    }

    @Test
    public void testGetByte42FollowedByByte3() {
        
        assertEquals(fileData.getBytes()[42], gzRequest.getByte(42));
        assertEquals(fileData.getBytes()[3], gzRequest.getByte(3));
        
    }

    @Test
    public void testGetLastByteFollowedByOneByteTooMany() {
        
        assertEquals(fileData.getBytes()[(int) file.length() - 1], gzRequest.getByte(file.length() - 1));
        try {
            gzRequest.getByte(fileData.length());
            //fail("Expected IndexOutOfBoundsException.");
        } catch (IndexOutOfBoundsException e) {
            //assertEquals("No byte at position [" + fileData.length() + "]", e.getMessage());
        }
        
    }

    @Test
    public void testGetByte42FollowedByByteMillion() {
        
        assertEquals(fileData.getBytes()[42], gzRequest.getByte(42));
        try {
            gzRequest.getByte(1000000L);
            //fail("Expected IndexOutOfBoundsException.");
        } catch (IndexOutOfBoundsException e) {
            //assertEquals("No byte at position [1000000]", e.getMessage());
        }
        
    }
    
    @Test
    public void testGetMetaData() {
        
        identifier = new RequestIdentifier(URI.create(GzipUtils.getUncompressedFilename(file.toURI().toString())));
        assertEquals(identifier, gzRequest.getIdentifier());
        
        assertEquals("xml", gzRequest.getExtension());
        assertEquals(StringUtils.substringBeforeLast(file.getName(), ".gz"), gzRequest.getFileName());
        assertEquals(metaData, gzRequest.getRequestMetaData());
        assertEquals(fileData.length(), gzRequest.size());
        
    }
    
    @Test
    public void testCloseDeletesTheBinaryCacheFile() throws IOException {
        assertTrue(gzRequest.getTempFile().exists());
        gzRequest.close();
        assertTrue(gzRequest.getTempFile() == null);
    }
}
