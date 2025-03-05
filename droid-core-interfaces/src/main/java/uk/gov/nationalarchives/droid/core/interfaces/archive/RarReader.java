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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.junrar.Archive;
import com.github.junrar.io.SeekableReadOnlyByteChannel;
import com.github.junrar.volume.Volume;
import com.github.junrar.volume.VolumeManager;

import net.byteseek.io.reader.WindowReader;

/**
 * A class which adapts a WindowReader to the Rar VolumeManager interface.
 * This allows us to read Rar files from any WindowReader, not just a file in the file system.
 */
public class RarReader implements VolumeManager {

    private static final String IOEXCEPTION_MSG = "IOException when reading length of reader: ";

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final WindowReader reader;

    /**
     * Construct a RarReader.
     * @param reader The WindowReader to wrap.
     */
    public RarReader(WindowReader reader) {
        this.reader = reader;
    }

    @Override
    public Volume nextVolume(Archive archive, Volume lastVolume) throws IOException {
        // If the last volume is null, return a new volume.
        // If we already have a volume, since we don't support multi-volume rar's, just return null.
        return lastVolume == null ? new ReaderVolume(archive) : null;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + reader + ")";
    }

    // A Volume object backed by a WindowReader.
    private class ReaderVolume implements Volume {

        private final Archive archive;

        public ReaderVolume(Archive archive) {
            this.archive = archive;
        }

        @Override
        public SeekableReadOnlyByteChannel getChannel() throws IOException {
            return new ReaderReadOnlyAccess();
        }

        @Override
        public long getLength() {
            try {
                return reader.length();
            } catch (IOException e) {
                log.error(IOEXCEPTION_MSG + reader, e);
                throw new RuntimeException(IOEXCEPTION_MSG + reader, e);
            }
        }

        @Override
        public Archive getArchive() {
            return archive;
        }

        /**
         * A class that adapts the WindowReader to the SeekableReadOnlyByteChannel interface.
         */
        private final class ReaderReadOnlyAccess implements SeekableReadOnlyByteChannel {

            private long position;

            @Override
            public long getPosition() throws IOException {
                return position;
            }

            @Override
            public void setPosition(long pos) throws IOException {
                if (pos < 0) {
                    throw new IOException("Cannot seek to a negative position.");
                }
                position = pos;
            }

            @Override
            public int read() throws IOException {
                return reader.readByte(position++);
            }

            @Override
            public int read(byte[] buffer, int off, int count) throws IOException {
                final int bytesCopied = ArchiveFileUtils.copyToBuffer(reader, position, buffer, off, count);
                position += bytesCopied;
                return bytesCopied;
            }

            @Override
            public int readFully(byte[] buffer, int count) throws IOException {
                int bytesCopied = read(buffer, 0, count);
                if (bytesCopied != count) {
                    throw new EOFException("Read " + bytesCopied + " bytes instead of the requested " + count);
                }
                return bytesCopied;
            }

            @Override
            public void close() throws IOException {
                // Nothing to do - the underlying WindowReader will be closed by its own IdentificationRequest.
            }
        }
    }
}
