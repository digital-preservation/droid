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
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Iterator adapter to allow pass <i>ArchiveInputStream</i> to the <i>SevenZArchiveWalker</i>.
 * Walker expect Iterable and with this class we can fake it. This class don't fully implement Iterable and
 * iterator functionality, it just wrap iterator calls and pass them to ArchiveInputStream.
 *
 * <b>!!!!This class is not thread safe!!!!</b>
 */
public class SevenZipIteratorAdapter implements Iterable<SevenZArchiveEntry> {

    private final ArchiveInputStream archiveStream;

    /**
     * Create new instance.
     * @param archiveStream the original archive input stream.
     */
    public SevenZipIteratorAdapter(ArchiveInputStream archiveStream) {
        this.archiveStream = archiveStream;
    }

    @Override
    public Iterator<SevenZArchiveEntry> iterator() {
        return new SevenZipIterator();
    }

    private class SevenZipIterator implements Iterator<SevenZArchiveEntry> {

        private final Logger log = LoggerFactory.getLogger(this.getClass());

        private SevenZArchiveEntry entry;


        @Override
        public boolean hasNext() {
            boolean retStatus = false;
            try {
                if (entry == null) {
                    entry = (SevenZArchiveEntry) archiveStream.getNextEntry();
                    retStatus =  entry != null;
                } else {
                    retStatus =  true;
                }
            } catch (IOException e) {
                log.error("exception thrown when walking 7zip archive", e);
                retStatus = false;
            }
            return retStatus;
        }

        @Override
        public SevenZArchiveEntry next() {
            if (entry == null) {
                boolean hasMore = hasNext();
                if (hasMore) {
                    return returnAndNullEntry();
                } else {
                    throw new NoSuchElementException("no mode elements");
                }
            } else {
                return returnAndNullEntry();
            }
        }

        private SevenZArchiveEntry returnAndNullEntry() {
            SevenZArchiveEntry entryToReturn = entry;
            entry = null;
            return entryToReturn;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not implemented");
        }
    }
}
