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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;

import net.byteseek.io.reader.WindowReader;


/**
 * A simple class that adapts a WindowReader to a SeekableByteChannel interface.
 * This allows us to pass in the underlying WindowReader in a request to the SevenZip Archive handler,
 * so we can process archives no matter where they come from (not just from file system objects).
 */
public class SevenZipReader implements SeekableByteChannel {

    private final WindowReader reader;
    private long position;
    private boolean isOpen = true;

    /**
     * Constructs a SevenZipReader.
     *
     * @param reader A WindowReader to wrap.
     */
    public SevenZipReader(final WindowReader reader) {
        this.reader = reader;
    }

    @Override
    public int read(final ByteBuffer dst) throws IOException {
        if (isOpen) {
            final int bytesCopied = ArchiveFileUtils.copyToBuffer(reader, position, dst);
            position += bytesCopied;
            return bytesCopied;
        }
        return -1;
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        throw new NonWritableChannelException();
    }

    @Override
    public long position() throws IOException {
        return position;
    }

    @Override
    public SeekableByteChannel position(long newPosition) throws IOException {
        position = newPosition;
        return this;
    }

    @Override
    public long size() throws IOException {
        return reader.length();
    }

    @Override
    public SeekableByteChannel truncate(long size) throws IOException {
        throw new NonWritableChannelException();
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public void close() throws IOException {
        isOpen = false;
        // Do not close underlying reader - we only wrap the reader here to process the seven zip.
        // The underlying reader will be closed by its own identification request processing.
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + reader + ")";
    }

}
