/**
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gsi.gov.uk>
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

import net.byteseek.io.reader.InputStreamReader;
import net.byteseek.io.reader.cache.AllWindowsCache;
import net.byteseek.io.reader.cache.TempFileCache;
import net.byteseek.io.reader.windows.Window;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Random;

import static org.junit.Assert.assertEquals;


/**
 * @author rflitcroft, boreilly
 * TODO: Commented out tests refer to Byteseek 1 based classes which are to be deleted folowing
 * the move to Byteseek2.  The remaining tests use the byteseek 2 InputStreamReader but we can delete
 * the entire class once the Byteseek2 test suite has been fully updated.
 *
 */
public class CachedBinaryTest {

    //private CachedByteBuffers cache;
    
    @Before
    public void setup() {
    }
    /*
    @Test 
    public void testGetInputStreamWithNoBackingFileCache() throws Exception {
        
        byte[] rawBytes = new byte[800];
        new Random().nextBytes(rawBytes);
        
        ByteArrayInputStream in = new ByteArrayInputStream(rawBytes);
        ByteBuffer blockZero = ByteBuffer.allocate(1000);
        Channels.newChannel(in).read(blockZero);
        
        cache = new CachedByteBuffers(1, 1000, blockZero);
        InputStreamReader reader = new InputStreamReader(in);

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
    */
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetInputStreamWithNoBackingFileCache1() throws Exception {

        byte[] rawBytes = new byte[800];
        new Random().nextBytes(rawBytes);

        ByteArrayInputStream in = new ByteArrayInputStream(rawBytes);

        InputStreamReader reader = new InputStreamReader(in, rawBytes.length, new AllWindowsCache());

        // We need to do this first otherwise the next statement always returns -1 due to the reader,
        // retrieving a null window, not clear why...
        reader.readByte(4096);
        //The cast is required to allow for negative numbers (since readByte returns an int)
        int someByte = (byte)reader.readByte(799);

        assertEquals(rawBytes[799], someByte);

        Window window = reader.getWindow(0);

        int byteIn;
        //int count = 0;

        for(int count =0;count<rawBytes.length; count++) {
            byteIn = window.getByte(count);
            assertEquals("Incorrect byte: " + count, rawBytes[count], (byte) byteIn);
        }

        //This should throw the IndexOutOfBoundsException
        byteIn = window.getByte(rawBytes.length);
    }
    /*
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
    */
    @Ignore
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetInputStreamWithBackingFileCache1() throws Exception {

        byte[] rawBytes = new byte[8500];
        new Random().nextBytes(rawBytes);

        ByteArrayInputStream in = new ByteArrayInputStream(rawBytes);

        final File tempDir = new File("tmp");
        tempDir.mkdir();

        InputStreamReader reader = new InputStreamReader(in, rawBytes.length, new TempFileCache(tempDir));

        // We need to do this first otherwise the next statement always returns -1 due to the reader,
        // retrieving a null window, not clear why...
        reader.readByte(12228);
        //The cast is required to allow for negative numbers (since readByte returns an int)
        int someByte = (byte)reader.readByte(8499);

        assertEquals(rawBytes[8499], someByte);

        Window window = reader.getWindow(0);

        int byteIn;
        //int count = 0;

        for(int count =0;count<rawBytes.length; count++) {
            byteIn = window.getByte(count);
            assertEquals("Incorrect byte: " + count, rawBytes[count], (byte) byteIn);
        }

        //This should throw the IndexOutOfBoundsException
        byteIn = window.getByte(rawBytes.length);
    }
}
