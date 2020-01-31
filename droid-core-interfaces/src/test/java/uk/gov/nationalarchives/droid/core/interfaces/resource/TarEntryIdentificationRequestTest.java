/**
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
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.archive.ArchiveFileUtils;

import static org.junit.Assert.*;


public class TarEntryIdentificationRequestTest {

    private static Path tmpDir;
    private TarEntryIdentificationRequest tarResource;
    private URI fileName;
    private TarArchiveInputStream in;
    private String entryName;
    private long size;
    private Date modTime;
    private RequestMetaData metaData;
    private RequestIdentifier identifier;
    
    @BeforeClass
    public static void createTmpFileDirectory() throws IOException {
        tmpDir = Paths.get("tmp");
        Files.createDirectories(tmpDir);
    }
    
    @AfterClass
    public static void removeTmpDir() {
        FileUtils.deleteQuietly(tmpDir.toFile());
    }
    
    @Before
    public void setup() throws Exception {
        
        fileName = getClass().getResource("/saved.tar").toURI();
        
        in = new TarArchiveInputStream(Files.newInputStream(Paths.get(fileName)));
        TarArchiveEntry entry;
        while ((entry = in.getNextTarEntry()) != null) {
            entryName = entry.getName();
            if ("saved/profile.xml".equals(entryName)) {
                size = entry.getSize();
                modTime = entry.getModTime();
                metaData = new RequestMetaData(modTime.getTime(), size, "profile.xml");
                identifier = new RequestIdentifier(ArchiveFileUtils.toTarUri(Paths.get(fileName).toUri(), entryName));
                break;
            }
        }
        tarResource = new TarEntryIdentificationRequest(metaData, identifier, tmpDir);
        tarResource.open(in);
    }
    
    @After
    public void tearDown() throws IOException {
        in.close();
        tarResource.close();
    }

    //TODO:MP: no longer have source files or binary caches, rewrite this test.
    /*
    @Test
    public void testTwoArgContructor() throws IOException {
        tarResource = new TarEntryIdentificationRequest(metaData, identifier, tmpDir);
        tarResource.open(in);
        //assertEquals(1, tarResource.getCache().getBuffers().size());
        assertNull(tarResource.getCache().getSourceFile());
    }
    */

    @Test
    public void testGetSize() {
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><profiles/>";
        byte[] expectedBytes = expected.getBytes();
        assertEquals(size, tarResource.size());
        assertEquals(expectedBytes.length, tarResource.size());
    }
    
    @Test
    public void testGetEveryByteSequencially() throws IOException {
        
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><profiles/>";
        int resourceSize = (int) tarResource.size();
        byte[] bin = new byte[resourceSize];
        
        int i;
        for (i = 0; i < size; i++) {
            bin[i] = tarResource.getByte(i);
        }
        //assertEquals(3, tarResource.getCache().getBuffers().size());
        
        assertEquals(expected, new String(bin));
        
        try {
            tarResource.getByte(i);
            fail("Expected IOException.");
        } catch (IOException io) {
        }
        
    }
    
    @Test
    public void testGetByte3FollowedByByte42()  throws IOException  {
        
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><profiles/>";
        assertEquals(expected.getBytes()[3], tarResource.getByte(3));
        assertEquals(expected.getBytes()[42], tarResource.getByte(42));
        //assertEquals(2, tarResource.getCache().getBuffers().size());
        
    }

    @Test
    public void testGetByte42FollowedByByte3()  throws IOException {
        
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><profiles/>";
        assertEquals(expected.getBytes()[42], tarResource.getByte(42));
        assertEquals(expected.getBytes()[3], tarResource.getByte(3));
        //assertEquals(2, tarResource.getCache().getBuffers().size());
        
    }

    @Test
    public void testGetByte42FollowedByByte49()  throws IOException {
        
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><profiles/>";
        assertEquals(expected.getBytes()[42], tarResource.getByte(42));
        try {
            tarResource.getByte(49);
            fail("Expected IOException");
        } catch (IOException e) {
        }
        //assertEquals(3, tarResource.getCache().getBuffers().size());
        
    }

    @Test
    public void testGetByte42FollowedByByte10000()  throws IOException  {
        
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><profiles/>";
        assertEquals(expected.getBytes()[42], tarResource.getByte(42));
        try {
            tarResource.getByte(10000);
            fail("Expected IOException.");
        } catch (IOException e) {
        }
        
    }
    
    @Test
    public void testGetExtension() {
        assertEquals(identifier, tarResource.getIdentifier());
        
        assertEquals("xml", tarResource.getExtension());
        assertEquals("profile.xml", tarResource.getFileName());
        assertEquals("tar:" + Paths.get(fileName).toUri() + "!/saved/profile.xml",
                tarResource.getIdentifier().getUri().toString());
        assertEquals(metaData, tarResource.getRequestMetaData());
        
    }

}
