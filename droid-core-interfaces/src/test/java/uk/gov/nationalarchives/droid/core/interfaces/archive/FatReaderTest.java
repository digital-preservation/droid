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
package uk.gov.nationalarchives.droid.core.interfaces.archive;

import de.waldheinz.fs.ReadOnlyException;
import net.byteseek.io.reader.FileReader;
import net.byteseek.io.reader.WindowReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class FatReaderTest {

    private static String RESOURCE_NAME = "saved.zip";
    private WindowReader reader;
    private FatReader fat;

    @Before
    public void setup() throws Exception {
        reader = getFileReader(RESOURCE_NAME);
        fat = new FatReader(reader);
    }

    @After
    public void close() throws Exception {
        fat.close();
        reader.close();
    }


    private WindowReader getFileReader(String resourceName) throws IOException {
        Path p = Paths.get("./src/test/resources/" + resourceName);
        return new FileReader(p.toFile(), 127); // use a small odd window size so we cross window boundaries.
    }

    private RandomAccessFile getRAF(String resourceName) throws IOException {
        Path p = Paths.get("./src/test/resources/" + resourceName);
        return new RandomAccessFile(p.toFile(), "r");
    }

    @Test
    public void testGetSize() throws Exception {
        assertEquals(reader.length(), fat.getSize());
    }

    @Test
    public void testRead() throws Exception {
        testRead(0, 1024);
        testRead(1024, 96);
        testRead(33, 2);
    }

    @Test(expected = EOFException.class)
    public void testReadPastEnd() throws Exception {
        testRead(943, 100000);
    }

    private void testRead(long position, int bufferSize) throws Exception {
        byte[] backing = new byte[bufferSize];
        ByteBuffer buffer = ByteBuffer.wrap(backing);
        fat.read(position, buffer);

        // Copy from a random access file:
        byte[] expected = new byte[bufferSize];
        try (RandomAccessFile raf = getRAF(RESOURCE_NAME)) {
            if (position < raf.length()) {
                raf.seek(position);
                raf.read(expected, 0, bufferSize);
            }
        }
        assertArrayEquals(backing, expected);
    }

    @Test(expected = ReadOnlyException.class)
    public void testwrite() throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[1024]);
        fat.write(21, buffer);
    }

    @Test(expected = IllegalStateException.class)
    public void testwriteAfterClose() throws Exception {
        fat.close();
        ByteBuffer buffer = ByteBuffer.wrap(new byte[1024]);
        fat.write(21, buffer);
    }

    @Test(expected = IllegalStateException.class)
    public void testflush() throws Exception {
        fat.flush(); // flushing while open does nothing.
        fat.close();
        fat.flush(); // should throw IllegalStateException.

    }

    @Test(expected = IllegalStateException.class)
    public void testgetSectorSize() throws Exception {
        assertEquals(512, fat.getSectorSize());
        fat.close();
        fat.getSectorSize(); // should throw IllegalStateException after closing.
    }

    @Test
    public void testClose() throws Exception {
        assertFalse(fat.isClosed());
        fat.close();
        assertTrue(fat.isClosed());
    }

    @Test
    public void testisClosed() throws Exception {
        assertFalse(fat.isClosed());
        fat.close();
        assertTrue(fat.isClosed());
    }

    @Test
    public void testToString() throws Exception {
        assertTrue(fat.toString().contains(fat.getClass().getSimpleName()));
        assertTrue(fat.toString().contains(reader.toString()));
    }

    @Test
    public void testIsReadOnly() throws Exception {
        assertTrue(fat.isReadOnly());
    }


}