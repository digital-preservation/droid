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

import net.byteseek.io.reader.WindowReader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SeekableByteChannel;

/**
 * A wrapper class which implements the SeekableByteChannel interface which the TrueVFS zip library works with.
 * The class delegates most of the work to the underlying WindowReader except keeping count of the current position
 * of the reader
 * <p>
 * This allows us to use an existing WindowReader, which will already have cached much of the underlying
 * file to supply the data for TrueVFS to process a zip file, without having to write the entire
 * WindowReader back out to a normal temporary file.
 *
  */
public final class ByteseekWindowWrapper implements SeekableByteChannel {

    private static final String NOT_IMPLEMENTED = "This method from the SeekableByteChannel interface is not implemented";
    private final WindowReader reader;
    private final boolean closeReaderIfClosed;
    private boolean closed;
    private long currentPosition;

    /**
     * Constructs a ByteseekWindowWrapper backed by a WindowReader.
     * <p>
     * When the instance is closed, the backing window reader will be closed if
     * closeReaderIfClosed is true.
     *
     * @param reader The WindowReader backing this ReadOnlyFile.
     */
    public ByteseekWindowWrapper(final WindowReader reader) {
        this(reader, false);
    }

    /**
     * Constructs a ByteseekWindowWrapper backed by a WindowReader.
     * <p>
     * When the instance is closed, the backing window reader will be closed if
     * closeReaderIfClosed is true.
     *
     * @param reader The WindowReader backing this ReadOnlyFile.
     * @param closeReaderIfClosed If true, then the backing WindowReader will be closed when this is closed.
     */
    public ByteseekWindowWrapper(final WindowReader reader, final boolean closeReaderIfClosed) {
        super();
        this.reader = reader;
        this.closeReaderIfClosed = closeReaderIfClosed;
    }


    @Override
    public int read(ByteBuffer dst) throws IOException {
        ensureOpen();
        if (currentPosition >= size()) {
            return -1;
        }
        final int bytesCopied = ArchiveFileUtils.copyToBuffer(reader, currentPosition, dst);
        currentPosition += bytesCopied;
        return bytesCopied;
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        throw new IOException(NOT_IMPLEMENTED);
    }

    @Override
    public long position() throws IOException {
        return currentPosition;
    }

    @Override
    public SeekableByteChannel position(long newPosition) throws IOException {
        currentPosition = newPosition;
        return this;
    }

    @Override
    public long size() throws IOException {
        return reader.length();
    }

    @Override
    public SeekableByteChannel truncate(long size) throws IOException {
        throw new IOException(NOT_IMPLEMENTED);
    }

    @Override
    public boolean isOpen() {
        return !closed;
    }

    @Override
    public void close() throws IOException {
        if (!closed && closeReaderIfClosed) {
            reader.close();
        }
        closed = true;
    }

    private void ensureOpen() throws IOException {
        if (closed) {
            throw new ClosedChannelException();
        }
    }
}
