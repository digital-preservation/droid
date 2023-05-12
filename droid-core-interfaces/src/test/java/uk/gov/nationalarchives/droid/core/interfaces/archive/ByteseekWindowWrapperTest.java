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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;


public class ByteseekWindowWrapperTest {
    private static String RESOURCE_NAME = "saved.zip";
    private WindowReader reader;
    private ByteseekWindowWrapper windowWrapper;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void setup() throws Exception {
        reader = getFileReader(RESOURCE_NAME);
        windowWrapper = new ByteseekWindowWrapper(reader);
    }

    @Test
    public void should_return_correct_length_from_the_underlying_reader() throws IOException {
        assertEquals(reader.length(), windowWrapper.size());
    }

    @Test
    public void initial_position_for_reading_the_bytes_in_the_reader_should_be_zero() throws IOException {
        assertEquals(0, windowWrapper.position());
    }

    @Test
    public void should_read_the_full_length_of_underlying_window() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        long remainingBytes = windowWrapper.size();
        int timesRead = 1;
        int bytesRead = 0;
        while (remainingBytes > 1024) {
            bytesRead = windowWrapper.read(buffer);
            assertEquals(1024, bytesRead);
            assertEquals(1024 * timesRead, windowWrapper.position());
            timesRead++;
            buffer.clear();
            remainingBytes -= 1024;
        }

        bytesRead = windowWrapper.read(buffer);
        assertEquals(windowWrapper.size() % 1024, bytesRead);
    }

    @Test
    public void should_return_negative_value_for_bytes_read_if_current_position_is_beyond_the_size() throws IOException {
        long maxSize = windowWrapper.size();
        windowWrapper.position(maxSize + 10);
        ByteBuffer buffer = ByteBuffer.allocate(100);
        assertEquals(-1, windowWrapper.read(buffer));
    }

    @Test
    public void should_throw_exception_indicating_that_write_method_is_not_implemented() throws IOException {
        expectedEx.expect(IOException.class);
        expectedEx.expectMessage("This method from the SeekableByteChannel interface is not implemented");
        windowWrapper.write(ByteBuffer.allocate(10));
    }

    @Test
    public void should_throw_exception_indicating_that_truncate_method_is_not_implemented() throws IOException {
        expectedEx.expect(IOException.class);
        expectedEx.expectMessage("This method from the SeekableByteChannel interface is not implemented");
        windowWrapper.truncate(2);
    }
    @Test
    public void should_throw_exception_when_trying_to_read_after_closing_the_channel() throws IOException {
        expectedEx.expect(ClosedChannelException.class);
        ByteBuffer buffer = ByteBuffer.allocate(10);
        windowWrapper.read(buffer);
        windowWrapper.close();
        buffer.clear();
        windowWrapper.read(buffer);
    }
    private WindowReader getFileReader(String resourceName) throws IOException {
        Path p = Paths.get("./src/test/resources/" + resourceName);
        return new FileReader(p.toFile(), 127); // use a small window.
    }
}