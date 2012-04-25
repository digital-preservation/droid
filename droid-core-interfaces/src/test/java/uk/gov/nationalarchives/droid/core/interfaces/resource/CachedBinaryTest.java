/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.core.interfaces.resource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Random;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import uk.gov.nationalarchives.droid.core.interfaces.archive.ArchiveFileUtils;



/**
 * @author rflitcroft
 *
 */
public class CachedBinaryTest {

    private CachedByteBuffers cache;
    
    @Before
    public void setup() {
    }
    
    @Test 
    public void testGetInputStreamWithNoBackingFileCache() throws Exception {
        
        byte[] rawBytes = new byte[800];
        new Random().nextBytes(rawBytes);
        
        ByteArrayInputStream in = new ByteArrayInputStream(rawBytes);
        ByteBuffer blockZero = ByteBuffer.allocate(1000);
        Channels.newChannel(in).read(blockZero);
        
        cache = new CachedByteBuffers(1, 1000, blockZero);
        assertEquals(rawBytes[799], cache.readByte(799));
        
        InputStream sourceIn = cache.getSourceInputStream();
        
        int byteIn;
        int count = 0;
        while ((byteIn = sourceIn.read()) >= 0) {
            assertEquals("Incorrect byte: " + count, rawBytes[count], (byte) byteIn);
            count++;
        }
        
        assertEquals(800, count);
    }

    @Test 
    public void testGetInputStreamWithBackingFileCache() throws Exception {
        
        byte[] rawBytes = new byte[8500];
        new Random().nextBytes(rawBytes);
        
        ByteArrayInputStream in = new ByteArrayInputStream(rawBytes);
        ByteBuffer blockZero = ByteBuffer.allocate(1000);
        ReadableByteChannel ch = Channels.newChannel(in);
        ch.read(blockZero);
        
        cache = new CachedByteBuffers(1, 1000, blockZero);
        final File tempDir = new File("tmp");
        tempDir.mkdir();
        cache.setSourceFile(ArchiveFileUtils.writeEntryToTemp(tempDir, blockZero, ch));
        
        assertEquals(rawBytes[8499], cache.readByte(8499));
        
        InputStream sourceIn = cache.getSourceInputStream();
        
        int byteIn;
        int count = 0;
        while ((byteIn = sourceIn.read()) >= 0) {
            assertEquals("Incorrect byte: " + count, rawBytes[count], (byte) byteIn);
            count++;
        }
        
        assertEquals(8500, count);
    }
}
