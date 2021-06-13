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

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An iterable class that gives and iterator across SevenZipEntryInfo objects.
 * It has internal classes for the iterator and a stream for seven zip archive files.
 */
public class SevenZipIteratorAdapter implements Iterable<SevenZipArchiveHandler.SevenZEntryInfo> {

    private static final int BYTE_TO_INT = 0xFF;

    private final SevenZFile zipFile;

    /**
     * Construct a SevenZipIteratorAdapter.
     *
     * @param zipFile The SevenZFile to wrap.
     */
    public SevenZipIteratorAdapter(SevenZFile zipFile) {
        this.zipFile = zipFile;
    }

    @Override
    public Iterator<SevenZipArchiveHandler.SevenZEntryInfo> iterator() {
        return new SevenZipIterator();
    }


    /**
     * An Iterator over SevenZEntryInfo classes, which wrap a SevenZArchiveEntry and an InputStream
     * to give access to the files in the archive.
     */
    private class SevenZipIterator implements Iterator<SevenZipArchiveHandler.SevenZEntryInfo> {

        private final Logger log = LoggerFactory.getLogger(getClass());
        private final InputStream stream = new SevenZipEntryStream();
        private SevenZArchiveEntry entry;

        @Override
        public boolean hasNext() {
            if (entry == null) {
                try {
                    entry = zipFile.getNextEntry();
                } catch (IOException e) {
                    log.error("Exception thrown when walking 7zip archive: " + zipFile, e);
                }
            }
            return entry != null;
        }

        @Override
        public SevenZipArchiveHandler.SevenZEntryInfo next() {
            if (hasNext()) {
                SevenZipArchiveHandler.SevenZEntryInfo info = new SevenZipArchiveHandler.SevenZEntryInfo(entry, stream);
                entry = null;
                return info;
            }
            log.error("No more entries in the seven zip archive: " + zipFile);
            throw new NoSuchElementException();
        }
    }

    /**
     * A simple input stream that takes its data from the SevenZipFile held in the outer class.
     * The SevenZipFile provides stream read methods.  A call to the ZipFile read method reads from
     * the stream associated with its current seven zip entry.
     */
    private class SevenZipEntryStream extends InputStream {

        private final byte[] oneByte = new byte[1];

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return zipFile.read(b, off, len);
        }

        @Override
        public int read() throws IOException {
            final byte[] aByte = oneByte;
            final int bytesRead = read(aByte, 0, 1);
            return bytesRead < 1 ? -1 : aByte[0] & BYTE_TO_INT;
        }

        @Override
        public void close() throws IOException {
            // Don't close the zipFile when each entry stream ends.
        }

    }
}
