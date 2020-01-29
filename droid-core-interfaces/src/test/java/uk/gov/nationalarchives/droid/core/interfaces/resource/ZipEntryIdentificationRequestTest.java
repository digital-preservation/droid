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
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;

import static org.junit.Assert.*;

public class ZipEntryIdentificationRequestTest {

    private static Path tmpDir;

    private ZipEntryIdentificationRequest zipResource;
    private URI droidZipFileName;
    private InputStream in;
    private ZipEntry entry;

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
        
        droidZipFileName = getClass().getResource("/saved.zip").toURI();
        ZipFile zip = new ZipFile(Paths.get(droidZipFileName).toFile());
        entry = zip.getEntry("profile.xml");
        in = zip.getInputStream(entry);
        
        metaData = new RequestMetaData(entry.getSize(), null, "profile.xml");
        //metaData = mock(RequestMetaData.class);
        URI parentUri = Paths.get(droidZipFileName).toUri();
        URI entryUri = new URI("zip:" + parentUri + "!/profile.xml");
        
        identifier = new RequestIdentifier(entryUri);
        
        //when(metaData.getSize()).thenReturn(entry.getSize());
        //when(metaData.getName()).thenReturn("profile.xml");
        
        zipResource = new ZipEntryIdentificationRequest(metaData, identifier, tmpDir);
        zipResource.open(in);
        //assertEquals(1, zipResource.getCache().getBuffers().size());
        //assertNotNull(zipResource.getCache().getSourceFile());
    }
    
    @After
    public void tearDown() throws IOException {
        zipResource.close();
        in.close();
    }

    //TODO:MP: no longer have binary cache or getsourcefile.  rewrite test?>
    /*
    @Test
    public void testTwoArgContructor() throws Exception {

        //when(metaData.getSize()).thenReturn(entry.getSize());
        
        zipResource = new ZipEntryIdentificationRequest(metaData, identifier, tmpDir);
        zipResource.open(in);
        //assertEquals(1, zipResource.getCache().getBuffers().size());
        assertNull(zipResource.getCache().getSourceFile());
        assertEquals(identifier, zipResource.getIdentifier());
    }
    */

    @Test
    public void testGetSize() {
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><profiles/>";
        byte[] expectedBytes = expected.getBytes();
        assertEquals(expectedBytes.length, zipResource.size());
    }
    
    @Test
    public void testGetEveryByteSequencially() throws IOException {
        
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><profiles/>";
        int size = (int) zipResource.size();
        byte[] bin = new byte[size];
        
        int i;
        for (i = 0; i < size; i++) {
            bin[i] = zipResource.getByte(i);
        }
        //assertEquals(3, zipResource.getCache().getBuffers().size());
        
        assertEquals(expected, new String(bin));
        
        try {
            zipResource.getByte(i);
            fail("Expected IOException");
        } catch (IOException e) {
        }
        
    }
    
    @Test
    public void testGetByte3FollowedByByte42() throws IOException {
        
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><profiles/>";
        assertEquals(expected.getBytes()[3], zipResource.getByte(3));
        assertEquals(expected.getBytes()[42], zipResource.getByte(42));
        //assertEquals(2, zipResource.getCache().getBuffers().size());
        
    }

    @Test
    public void testGetByte42FollowedByByte3() throws IOException {
        
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><profiles/>";
        assertEquals(expected.getBytes()[42], zipResource.getByte(42));
        assertEquals(expected.getBytes()[3], zipResource.getByte(3));
        //assertEquals(2, zipResource.getCache().getBuffers().size());
        
    }

    @Test
    public void testGetByte42FollowedByByte49() throws IOException {
        
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><profiles/>";
        assertEquals(expected.getBytes()[42], zipResource.getByte(42));
        try {
            zipResource.getByte(49);
            fail("Expected IOException");
        } catch (IOException e) {
        }
        //assertEquals(3, zipResource.getCache().getBuffers().size());
        
    }

    @Test
    public void testGetByte42FollowedByByte10000() throws IOException {
        
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><profiles/>";
        assertEquals(expected.getBytes()[42], zipResource.getByte(42));
        try {
            zipResource.getByte(10000);
            fail("Expected IOException");
        } catch (IOException e) {
        }
        
    }
    
    @Test
    public void testGetExtension() {

        assertEquals("xml", zipResource.getExtension());
        assertEquals("profile.xml", zipResource.getFileName());
        assertEquals("zip:" + Paths.get(droidZipFileName).toUri() + "!/profile.xml",
                zipResource.getIdentifier().getUri().toString());
        assertEquals(metaData, zipResource.getRequestMetaData());
        
    }
    

}
