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

import com.github.junrar.Archive;
import com.github.junrar.volume.Volume;
import com.github.junrar.io.SeekableReadOnlyByteChannel;
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

public class RarReaderTest {

    private static String RESOURCE_NAME = "sample.rar";
    private WindowReader reader;
    private RarReader rar;
    private Archive archive;
    private Volume vol;
    private SeekableReadOnlyByteChannel access;

    @Before
    public void setup() throws Exception {
        reader = getFileReader(RESOURCE_NAME);
        rar = new RarReader(reader);
        archive = new Archive(rar, null, null);
        vol = rar.nextVolume(archive, null);
        access = vol.getChannel();
    }

    @After
    public void close() throws Exception {
        archive.close();
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
        assertEquals(reader.length(), vol.getLength());
    }

    @Test
    public void testToString() throws Exception {
        assertTrue(rar.toString().contains(rar.getClass().getSimpleName()));
        assertTrue(rar.toString().contains(reader.toString()));
    }

    @Test
    public void testPosition() throws Exception {
        assertEquals(0, access.getPosition());
        access.setPosition(10);
        assertEquals(10, access.getPosition());
        access.setPosition(257);
        assertEquals(257, access.getPosition());
    }

    @Test(expected = IOException.class)
    public void testSetNegativePosition() throws Exception {
        access.setPosition(-23);
    }

    @Test
    public void testReadByte() throws Exception {
        for (int i = 0; i < 256; i++) {
            testReadByte(i);
        }
    }

    private void testReadByte(long position) throws Exception {
        int expected = reader.readByte(position);
        assertEquals(reader.readByte(position), access.read());
    }

    @Test
    public void testReadArray() throws Exception {
        testReadArray(0, 0, 1024, 1024);
        testReadArray(23, 96, 324, 1024);
        testReadArray(23, 96, 1024, 324);
        testReadArray(1096, 3, 999, 1001);
    }

    private void testReadArray(long position, int offset, int length, int bufferSize) throws Exception {
        byte[] buffer = new byte[bufferSize];
        access.setPosition(position);
        access.read(buffer, offset, length);

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

    @Test
    public void testReadFully() throws Exception {
        testReadFully(0, 900, 2048);
        testReadFully(96, 326, 943);
        testReadFully(127, 465, 1024);
        testReadFully(543, 37, 1024);
    }

    private void testReadFully(long position, int length, int bufferSize) throws Exception {
        byte[] buffer = new byte[bufferSize];
        access.setPosition(position);
        access.readFully(buffer, length);

        // Copy from a random access file:
        byte[] expected = new byte[bufferSize];
        try (RandomAccessFile raf = getRAF(RESOURCE_NAME)) {
            if (position < raf.length()) {
                raf.seek(position);
                final int maxBytesToRead = Math.min(expected.length, length);
                raf.read(expected, 0, maxBytesToRead);
            }
        }
        assertArrayEquals(buffer, expected);
    }

}