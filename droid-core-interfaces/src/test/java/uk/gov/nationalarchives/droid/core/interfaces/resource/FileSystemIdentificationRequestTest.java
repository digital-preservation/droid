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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;

public class FileSystemIdentificationRequestTest {

    private String fileData;

    private FileSystemIdentificationRequest fileRequest;
    private File file;

    private RequestMetaData metaData;
    private RequestIdentifier identifier;
    
    @Before
    public void setup() throws IOException {
    
        file = new File(getClass().getResource("/testXmlFile.xml").getFile());
        metaData = new RequestMetaData(file.length(), file.lastModified(), "testXmlFile.xml");
        identifier = new RequestIdentifier(file.toURI());
        fileRequest = new FileSystemIdentificationRequest(
                metaData, identifier,
                3, 5);
        fileRequest.open(new FileInputStream(file));

        BufferedReader reader = new BufferedReader(new FileReader(file));
        char[] buffer = new char[8192];
        int length;
        StringBuilder sb = new StringBuilder();
        while ((length = reader.read(buffer)) != -1) {
            sb.append(buffer, 0, length);
        }
        fileData = sb.toString();
    }
    
    @After
    public void tearDown() throws IOException {
        fileRequest.close();
    }
    
    @Test
    public void testOneArgContructor() throws IOException {
        file = new File(getClass().getResource("/testXmlFile.xml").getFile());
        fileRequest = new FileSystemIdentificationRequest(
                new RequestMetaData(12L, 13L, "testXmlFile.xml"), identifier);
        
        fileRequest.open(new FileInputStream(file));
        CachedBytes cache = fileRequest.getCache();
        //assertEquals(1, cache.getBuffers().size());
        assertNotNull(cache.getSourceFile());
    }
    
    @Test
    public void testGetSize() {
        assertEquals(file.length(), fileRequest.size());
    }
    
    @Test
    public void testGetEveryByteSequencially() {
        
        int size = (int) fileRequest.size();
        byte[] bin = new byte[size];
        
        int i;
        for (i = 0; i < size; i++) {
            bin[i] = fileRequest.getByte(i);
        }
        //assertEquals(3, fileRequest.getCache().getBuffers().size());
        
        assertEquals(fileData, new String(bin));
        
        try {
            fileRequest.getByte(i);
            //fail("Expected IndexOutOfBoundsException.");
        } catch (IndexOutOfBoundsException e) {
            //assertEquals("No byte at position [" + i + "]", e.getMessage());
        }
        
    }
    
    @Test
    public void testGetByte3FollowedByByte42() {
        
        assertEquals(fileData.getBytes()[3], fileRequest.getByte(3));
        assertEquals(fileData.getBytes()[42], fileRequest.getByte(42));
        //assertEquals(2, fileRequest.getCache().getBuffers().size());
        
    }

    @Test
    public void testGetByte42FollowedByByte3() {
        
        assertEquals(fileData.getBytes()[42], fileRequest.getByte(42));
        assertEquals(fileData.getBytes()[3], fileRequest.getByte(3));
        //assertEquals(2, fileRequest.getCache().getBuffers().size());
        
    }

    @Test
    public void testGetLastByteFollowedByOneByteTooMany() {
        
        assertEquals(fileData.getBytes()[(int) file.length() - 1], fileRequest.getByte(file.length() - 1));
        try {
            fileRequest.getByte(file.length());
            //fail("Expected IndexOutOfBoundsException.");
        } catch (IndexOutOfBoundsException e) {
            //assertEquals("No byte at position [" + file.length() + "]", e.getMessage());
        }
        //assertEquals(3, fileRequest.getCache().getBuffers().size());
        
    }

    @Test
    public void testGetByte42FollowedByByteMillion() {
        
        assertEquals(fileData.getBytes()[42], fileRequest.getByte(42));
        try {
            fileRequest.getByte(1000000L);
            //fail("Expected IndexOutOfBoundsException.");
        } catch (IndexOutOfBoundsException e) {
            //assertEquals("No byte at position [1000000]", e.getMessage());
        }
        
    }
    
    @Test
    public void testGetMetaData() {
        assertEquals(identifier, fileRequest.getIdentifier());

        assertEquals("xml", fileRequest.getExtension());
        assertEquals(file.getName(), fileRequest.getFileName());
        assertEquals(metaData, fileRequest.getRequestMetaData());
        assertEquals(file.length(), fileRequest.size());
        
    }
    
}
