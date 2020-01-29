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
package uk.gov.nationalarchives.droid.core.interfaces.archive;

import net.byteseek.io.reader.FileReader;
import net.byteseek.io.reader.WindowReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.NonWritableChannelException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class SevenZipReaderTest {

    private static String RESOURCE_NAME = "saved.7z";
    private WindowReader reader;
    private SevenZipReader zip;

    @Before
    public void setup() throws Exception {
        reader = getFileReader(RESOURCE_NAME);
        zip = new SevenZipReader(reader);
    }

    @After
    public void close() throws Exception {
        zip.close();
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
    public void testRead() throws Exception {
        testRead(0, 232);
        testRead(2, 2);
        testRead(1, 1);
        testRead(96, 116);
    }

    private void testRead(long position, int bufferSize) throws Exception {
        byte[] backing = new byte[bufferSize];
        ByteBuffer buffer = ByteBuffer.wrap(backing);
        zip.position(position);
        zip.read(buffer);

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

    @Test(expected = NonWritableChannelException.class)
    public void testWrite() throws Exception {
        ByteBuffer buf = ByteBuffer.wrap(new byte[1024]);
        zip.write(buf);
    }

    @Test
    public void testGetSetPosition() throws Exception {
        assertEquals(0, zip.position()); // starts at zero.
        for (int pos = 0; pos < 234; pos++) {    // is whatever it is told to be.
            zip.position(pos);
            assertEquals(pos, zip.position());
        }
    }

    @Test
    public void testSize() throws Exception {
        assertEquals(reader.length(), zip.size());
    }

    @Test(expected = NonWritableChannelException.class)
    public void testTruncate() throws Exception {
        zip.truncate(128);
    }

    @Test
    public void testIsOpenClose() throws Exception {
        assertTrue(zip.isOpen());
        zip.close();
        assertFalse(zip.isOpen());
    }

     @Test
    public void testToString() throws Exception {
        assertTrue(zip.toString().contains(zip.getClass().getSimpleName()));
        assertTrue(zip.toString().contains(reader.toString()));
    }

}