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
package uk.gov.nationalarchives.droid.core.interfaces.resource;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipUtils;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.apache.commons.lang3.StringUtils;

import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author rflitcroft
 *
 */
public class GZipIdentificationRequestTest {
    private static Path tmpDir;

    private static String fileData;

    private static GZipIdentificationRequest gzRequest;
    private static Path file;

    private static RequestMetaData metaData;
    private static RequestIdentifier identifier;
    
    
    @AfterAll
    public static void removeTmpDir() {
        FileUtils.deleteQuietly(tmpDir.toFile());
    }
    
    @BeforeAll
    public static void setupTestData() throws IOException, URISyntaxException {
        tmpDir = Paths.get("tmp");
        Files.createDirectories(tmpDir);

        final Path file = Paths.get(GZipIdentificationRequestTest.class.getResource("/testXmlFile.xml.gz").toURI());

        try(final GzipCompressorInputStream in = new GzipCompressorInputStream(Files.newInputStream(file));
                final Reader reader = new InputStreamReader(in)) {
            char[] buffer = new char[8192];
            int length;
            StringBuilder sb = new StringBuilder();
            while ((length = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, length);
            }
            fileData = sb.toString();
        }
    }    
    
    @BeforeAll
    public static void setup() throws Exception {
    
        file = Paths.get(GZipIdentificationRequestTest.class.getResource("/testXmlFile.xml.gz").toURI());
        
        metaData = new RequestMetaData(null, null, "foo");
        identifier = new RequestIdentifier(URI.create(GzipUtils.getUncompressedFilename(file.toUri().toString())));
        gzRequest = new GZipIdentificationRequest(
                metaData, identifier,
                Paths.get("tmp"));
        GzipCompressorInputStream in = new GzipCompressorInputStream(Files.newInputStream(file));
        gzRequest.open(in);
    }
    
    @AfterAll
    public static void tearDown() throws IOException {
        gzRequest.close();
    }

    //TODO:MP no longer have cache objects - rewrite this test.
    /*
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
    */

    @Test
    public void testGetSize() {
        assertEquals(fileData.length(), gzRequest.size());
    }
    
    @Test
    public void testGetEveryByteSequencially() throws IOException {
        
        int size = (int) gzRequest.size();
        byte[] bin = new byte[size];
        
        int i;
        for (i = 0; i < size; i++) {
            bin[i] = gzRequest.getByte(i);
        }
        
        assertEquals(fileData, new String(bin));
        
        try {
            gzRequest.getByte(i);
            fail("Expected IOException.");
        } catch (IOException e) {
        }
        
    }
    
    @Test
    public void testGetByte3FollowedByByte42() throws IOException {
        
        assertEquals(fileData.getBytes()[3], gzRequest.getByte(3));
        assertEquals(fileData.getBytes()[42], gzRequest.getByte(42));
        
    }

    @Test
    public void testGetByte42FollowedByByte3() throws IOException {
        
        assertEquals(fileData.getBytes()[42], gzRequest.getByte(42));
        assertEquals(fileData.getBytes()[3], gzRequest.getByte(3));
        
    }

    @Test
    public void testGetLastByteFollowedByOneByteTooMany() throws IOException {
        
        assertEquals(fileData.getBytes()[(int) Files.size(file) - 1], gzRequest.getByte(Files.size(file) - 1));
        try {
            gzRequest.getByte(fileData.length());
            fail("Expected IOException");
        } catch (IOException e) {
        }
        
    }

    @Test
    public void testGetByte42FollowedByByteMillion() throws IOException {
        
        assertEquals(fileData.getBytes()[42], gzRequest.getByte(42));
        try {
            gzRequest.getByte(1000000L);
            fail("Expected IOException.");
        } catch (IOException e) {
        }
        
    }
    
    @Test
    public void testGetMetaData() {
        
        identifier = new RequestIdentifier(URI.create(GzipUtils.getUncompressedFilename(file.toUri().toString())));
        assertEquals(identifier, gzRequest.getIdentifier());
        
        assertEquals("xml", gzRequest.getExtension());
        assertEquals(StringUtils.substringBeforeLast(file.getFileName().toString(), ".gz"), gzRequest.getFileName());
        assertEquals(metaData, gzRequest.getRequestMetaData());
        assertEquals(fileData.length(), gzRequest.size());
        
    }
    
}
