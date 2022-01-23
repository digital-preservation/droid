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

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;

import de.waldheinz.fs.BlockDevice;
import de.waldheinz.fs.ReadOnlyException;
import net.byteseek.io.reader.WindowReader;


/**
 * This class adapts a byteseek WindowReader (which all identification requests provide)
 * to the Fat archive BlockDevice interface, which provides a random access interface for
 * the Fat File System.  Using this means we can process Fat archives even if embedded in other
 * archives like zip or iso files, rather than being limited to only processing them if they are
 * directly stored in files.
 * <p>
 * This adapter follows the pattern set by the {@link de.waldheinz.fs.util.FileDisk} class, but
 * using a WindowReader as its backing store rather than a file.
 */
public class FatReader implements BlockDevice {

    private static final int DEFAULT_SECTOR_SIZE = 512; // the value picked by all the other BlockDevice implementations.

    private final WindowReader reader;
    private boolean isClosed;

    /**
     * Constructs a FatReader.
     *
     * @param reader The WindowReader to wrap.
     */
    public FatReader(final WindowReader reader) {
        this.reader = reader;
    }

    @Override
    public long getSize() throws IOException {
        ensureOpen();
        return reader.length();
    }

    @Override
    public void read(final long devOffset, final ByteBuffer dest) throws IOException {
        ensureOpen();
        final int bytesRequested = dest.remaining();
        if (devOffset + bytesRequested > reader.length()) {
            throw new EOFException("Reading past end of device");
        }
        ArchiveFileUtils.copyToBuffer(reader, devOffset, dest);
    }

    @Override
    public void write(long devOffset, ByteBuffer src) throws ReadOnlyException, IOException, IllegalArgumentException {
        ensureOpen();
        throw new ReadOnlyException();
    }

    @Override
    public void flush() throws IOException {
        ensureOpen();
        // Nothing to flush for read only file systems.
    }

    @Override
    public int getSectorSize() throws IOException {
        ensureOpen();
        return DEFAULT_SECTOR_SIZE;
    }

    @Override
    public void close() throws IOException {
        isClosed = true;
        // We don't close the underlying reader, which will be closed by its own original identification request.
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + reader + ")";
    }

    private void ensureOpen() {
        if (isClosed) {
            throw new IllegalStateException("Device " + this + " is closed.");
        }
    }

    @Override
    public boolean isReadOnly() {
        return true; // always read only if wrapping readers.
    }

}
