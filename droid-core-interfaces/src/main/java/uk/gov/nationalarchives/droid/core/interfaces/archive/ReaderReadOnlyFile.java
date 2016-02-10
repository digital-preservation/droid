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

import java.io.IOException;

import de.schlichtherle.io.rof.AbstractReadOnlyFile;
import net.byteseek.io.reader.WindowReader;
import net.byteseek.io.reader.windows.Window;

/**
 * This class adapts a byteseek 2 WindowReader to behave as a ReadOnlyFile interface defined in TrueZip.
 * <p>
 * This allows us to use an existing WindowReader, which will already have cached much of the underlying
 * file to supply the data for TrueZip to process a zip file, without having to write the entire
 * WindowReader back out to a normal temporary file.
 *
 * Created by matt on 30/05/15.
 */
public final class ReaderReadOnlyFile extends AbstractReadOnlyFile {

    private final WindowReader reader;
    private long filePointer;
    private boolean closeReaderIfClosed;
    private boolean closed;

    /**
     * Constructs a ReaderReadOnlyFiule backed by a WindowReader.
     * <p>
     * The underlying WindowReader will not be closed when this ReaderReadOnlyFile is closed.
     *
     * @param reader The WindowReader to back this ReadOnlyFile.
     */
    public ReaderReadOnlyFile(final WindowReader reader) {
        this(reader, false);
    }

    /**
     * Constructs a ReaderReadOnlyFile backed by a WindowReader.
     * <p>
     * When the instance is closed, the backing window reader will be closed if
     * closeReaderIfClosed is true.
     *
     * @param reader The WindowReader backing this ReadOnlyFile.
     * @param closeReaderIfClosed If true, then the backing WindowReader will be closed when this is closed.
     */
    public ReaderReadOnlyFile(final WindowReader reader, final boolean closeReaderIfClosed) {
        super();
        this.reader = reader;
        this.closeReaderIfClosed = closeReaderIfClosed;
    }

    @Override
    public long length() throws IOException {
        return reader.length();
    }

    @Override
    public long getFilePointer() throws IOException {
        ensureOpen();
        return filePointer;
    }

    @Override
    public int read() throws IOException {
        ensureOpen();
        final int result = reader.readByte(filePointer);
        if (result >= 0) {
            filePointer++;
        }
        return result;
    }

    @Override
    public void seek(long position) throws IOException {
        ensureOpen();
        if (position < 0) {
            throw new IOException("Cannot seek to a negative position: " + position);
        }
        if (position > reader.length()) {
            throw new IOException("Cannot seek past the end of data with length "
                    + reader.length() + ".  Seek position was " + position);
        }
        filePointer = position;
    }

    @Override
    public int read(byte[] bytes, int offset, int length) throws IOException {
        ensureOpen();
        if ((offset | length) < 0) {
            throw new IndexOutOfBoundsException("Offset or length cannot be negative: {" + offset + "," + length + "}");
        }
        if (offset + length > bytes.length) {
            throw new IndexOutOfBoundsException("The offset "
                    + offset + " plus length " + length
                    + " cannot be greater than the length of the bytes " + bytes.length);
        }
        if (filePointer >= length() || length == 0) {
            return -1;
        }

        final int bytesToRead = length - offset;
        int totalRead = 0;
        Window window = reader.getWindow(filePointer);
        while (window != null && totalRead < bytesToRead) {
            final int windowOffset = reader.getWindowOffset(filePointer);
            final int availableBytes = window.length() - windowOffset;
            final int remainingBytes = length - totalRead;
            final int copyBytes    = remainingBytes < availableBytes ? remainingBytes : availableBytes;
            System.arraycopy(window.getArray(), windowOffset, bytes, offset + totalRead, copyBytes);
            totalRead += copyBytes;
            filePointer += copyBytes;
            window = reader.getWindow(filePointer);
        }
        return totalRead;
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
            throw new IOException("The ReaderReadOnlyFile is closed.");
        }
    }
}
