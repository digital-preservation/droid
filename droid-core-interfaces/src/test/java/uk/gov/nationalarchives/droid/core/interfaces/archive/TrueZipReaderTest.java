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
package uk.gov.nationalarchives.droid.core.interfaces.archive;

import net.byteseek.io.reader.FileReader;
import net.byteseek.io.reader.WindowReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class TrueZipReaderTest {

    private static String RESOURCE_NAME = "saved.zip";
    private WindowReader reader;
    private TrueZipReader zip;

    @Before
    public void setup() throws Exception {
        reader = getFileReader(RESOURCE_NAME);
        zip = new TrueZipReader(reader);
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
    public void testLength() throws Exception {
        assertEquals(reader.length(), zip.length());
    }

    @Test
    public void testGetFilePointer() throws Exception {
        assertEquals(0, zip.getFilePointer()); // starts at zero.
        zip.close();
    }

    @Test(expected = IOException.class)
    public void testGetFilePointerIfClosed() throws Exception {
        zip.close();
        zip.getFilePointer(); // will throw IOException if closed.
    }

    @Test
    public void testReadArray() throws Exception {
        testReadArray(0, 0, 0, 1024);
        testReadArray(0, 0, 1024, 1024);
        testReadArray(23, 1, 1024, 512);
        testReadArray(27, 2, 1024, 1023);
        testReadArray(13, 3, 1023, 1024);
        testReadArray(23, 1, 511, 1023);
    }

    @Test(expected = IOException.class)
    public void testReadIfClosed() throws Exception {
        zip.close();
        zip.read(new byte[1024], 0, 1024);
    }

    @Test(expected = IOException.class)
    public void testReadNegativeOffset() throws Exception {
        zip.close();
        zip.read(new byte[1024], -1, 1000);
    }

    @Test
    public void testReadBytes() throws Exception {
        for (long pos = 0; pos < 1024; pos++) {
            assertEquals(reader.readByte(pos), zip.read());
        }
    }

    @Test(expected = IOException.class)
    public void testReadByteIfClosed() throws Exception {
        zip.close();
        zip.read();
    }


    @Test
    public void testSeek() throws Exception {
        for (long pos = 0; pos < 1935; pos += 23) {
            testSeek(pos);
        }
    }

    private void testSeek(long position) throws Exception {
        zip.seek(10);
        assertEquals(10, zip.getFilePointer());
    }

    @Test(expected = IOException.class)
    public void testSeekIfClosed() throws Exception {
        zip.close();
        zip.seek(1);
    }

    @Test(expected = IOException.class)
    public void testSeekNegative() throws Exception {
        zip.seek(-1);
    }

    @Test(expected = IOException.class)
    public void testSeekPastEnd() throws Exception {
        zip.seek(1000000);
    }

    private void testReadArray(long position, int offset, int length, int bufferSize) throws Exception {
        byte[] buffer = new byte[bufferSize];
        zip.seek(position);
        zip.read(buffer, offset, length);

        // Copy from a random access file:
        byte[] expected = new byte[bufferSize];
        try (RandomAccessFile raf = getRAF(RESOURCE_NAME)) {
            if (position < raf.length()) {
                raf.seek(position);
                final int maxBytesToRead = Math.min(expected.length - offset, length);
                raf.read(expected, offset, maxBytesToRead);
            }
        }
        assertArrayEquals(buffer, expected);
    }


}