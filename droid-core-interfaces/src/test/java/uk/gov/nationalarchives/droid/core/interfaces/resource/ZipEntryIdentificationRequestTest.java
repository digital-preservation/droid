/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.core.interfaces.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;

public class ZipEntryIdentificationRequestTest {

    private static File tmpDir;

    private ZipEntryIdentificationRequest zipResource;
    private String droidZipFileName;
    private InputStream in;
    private ZipEntry entry;

    private RequestMetaData metaData;
    private RequestIdentifier identifier;
    
    
    @BeforeClass
    public static void createTmpFileDirectory() {
        tmpDir = new File("tmp");
        tmpDir.mkdir();
    }
    
    @AfterClass
    public static void removeTmpDir() {
        FileUtils.deleteQuietly(tmpDir);
    }
    
    @Before
    public void setup() throws Exception {
        
        droidZipFileName = getClass().getResource("/saved.zip").getFile();
        ZipFile zip = new ZipFile(droidZipFileName);
        entry = zip.getEntry("profile.xml");
        in = zip.getInputStream(entry);
        
        metaData = new RequestMetaData(entry.getSize(), null, "profile.xml");
        //metaData = mock(RequestMetaData.class);
        URI parentUri = new File(droidZipFileName).toURI();
        URI entryUri = new URI("zip:" + parentUri + "!/profile.xml");
        
        identifier = new RequestIdentifier(entryUri);
        
        //when(metaData.getSize()).thenReturn(entry.getSize());
        //when(metaData.getName()).thenReturn("profile.xml");
        
        zipResource = new ZipEntryIdentificationRequest(metaData, identifier, 3, 5, tmpDir);
        zipResource.open(in);
        //assertEquals(1, zipResource.getCache().getBuffers().size());
        assertNotNull(zipResource.getCache().getSourceFile());
    }
    
    @After
    public void tearDown() throws IOException {
        zipResource.close();
        in.close();
    }
    
    @Test
    public void testTwoArgContructor() throws Exception {

        //when(metaData.getSize()).thenReturn(entry.getSize());
        
        zipResource = new ZipEntryIdentificationRequest(metaData, identifier, tmpDir);
        zipResource.open(in);
        //assertEquals(1, zipResource.getCache().getBuffers().size());
        assertNull(zipResource.getCache().getSourceFile());
        assertEquals(identifier, zipResource.getIdentifier());
    }
    
    @Test
    public void testGetSize() {
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><profiles/>";
        byte[] expectedBytes = expected.getBytes();
        assertEquals(expectedBytes.length, zipResource.size());
    }
    
    @Test
    public void testGetEveryByteSequencially() {
        
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
            //fail("Expected IndexOutOfBoundsException.");
        } catch (IndexOutOfBoundsException e) {
            //assertEquals("No byte at position [" + i + "]", e.getMessage());
        }
        
    }
    
    @Test
    public void testGetByte3FollowedByByte42() {
        
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><profiles/>";
        assertEquals(expected.getBytes()[3], zipResource.getByte(3));
        assertEquals(expected.getBytes()[42], zipResource.getByte(42));
        //assertEquals(2, zipResource.getCache().getBuffers().size());
        
    }

    @Test
    public void testGetByte42FollowedByByte3() {
        
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><profiles/>";
        assertEquals(expected.getBytes()[42], zipResource.getByte(42));
        assertEquals(expected.getBytes()[3], zipResource.getByte(3));
        //assertEquals(2, zipResource.getCache().getBuffers().size());
        
    }

    @Test
    public void testGetByte42FollowedByByte49() {
        
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><profiles/>";
        assertEquals(expected.getBytes()[42], zipResource.getByte(42));
        try {
            zipResource.getByte(49);
            //fail("Expected IndexOutOfBoundsException.");
        } catch (IndexOutOfBoundsException e) {
            //assertEquals("No byte at position [49]", e.getMessage());
        }
        //assertEquals(3, zipResource.getCache().getBuffers().size());
        
    }

    @Test
    public void testGetByte42FollowedByByte10000() {
        
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><profiles/>";
        assertEquals(expected.getBytes()[42], zipResource.getByte(42));
        try {
            zipResource.getByte(10000);
            //fail("Expected IndexOutOfBoundsException.");
        } catch (IndexOutOfBoundsException e) {
            //assertEquals("No byte at position [10000]", e.getMessage());
        }
        
    }
    
    @Test
    public void testGetExtension() {

        assertEquals("xml", zipResource.getExtension());
        assertEquals("profile.xml", zipResource.getFileName());
        assertEquals("zip:" + new File(droidZipFileName).toURI() + "!/profile.xml",
                zipResource.getIdentifier().getUri().toString());
        assertEquals(metaData, zipResource.getRequestMetaData());
        
    }
    
    @Test
    public void testCloseDeletesTheBinaryCacheFile() throws IOException {
        assertTrue(zipResource.getTempFile().exists());
        zipResource.close();
        //assertFalse(zipResource.getTempFile().exists());
        assertTrue(zipResource.getTempFile() == null);
    }

}
