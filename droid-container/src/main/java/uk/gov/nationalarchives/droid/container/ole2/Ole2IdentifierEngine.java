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
package uk.gov.nationalarchives.droid.container.ole2;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import uk.gov.nationalarchives.droid.container.AbstractIdentifierEngine;
import uk.gov.nationalarchives.droid.container.ContainerSignatureMatch;
import uk.gov.nationalarchives.droid.container.ContainerSignatureMatchCollection;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.FileSystemIdentificationRequest;
import uk.gov.nationalarchives.droid.core.signature.ByteReader;

/**
 *
 * @author rbrennan, boreilly
 */
public class Ole2IdentifierEngine extends AbstractIdentifierEngine {

    private static final String NO_READER_ERROR =
            "No reader was obtained for %s. This may be due to low memory conditions. "
                    + "Try running with a larger heap size!";
    private final Logger log = LoggerFactory.getLogger(Ole2IdentifierEngine.class);

    //CHECKSTYLE:OFF - cyclomatic complexity too high.
    @Override
    public void process(IdentificationRequest request, ContainerSignatureMatchCollection matches) throws IOException {
        //CHECKSTYLE:ON
        final InputStream in = request.getSourceInputStream();
        POIFSFileSystem reader = null;
        try {
            try {
                if (FileSystemIdentificationRequest.class.isAssignableFrom(request.getClass())) {
                    FileSystemIdentificationRequest req = FileSystemIdentificationRequest.class.cast(request);
                    reader = new POIFSFileSystem(req.getFile().toFile());
                } else {
                    reader = new POIFSFileSystem(in);
                }
            } finally {
                // We can get Out Of Memory errors when attempting to instantiate the POIFSFileSystem.  However, these
                // are handled internally by POIFS and not propogated to the calling code.  Therefore we check here
                // whether the reader has been assigned - if not, this is probably due to an Out Of Memory error,
                // possibly caused by a low heap size.
                if (reader == null) {
                    //request.getIdentifier() is null when running in command line 'no profile' mode.
                    String identifier = request.getIdentifier() != null
                            ? request.getIdentifier().getUri().toString() : "the current container file";
                    throw new IOException(String.format(NO_READER_ERROR, identifier));
                }
            }

            DirectoryEntry root = reader.getRoot();
            Iterator<EntryInfo> iterator = new OLE2Walker((DirectoryNode) root, true);
            while (iterator.hasNext()) {
                EntryInfo info = iterator.next();
                Entry entry = info.getEntry();
                String entryName = info.getPath();
                DirectoryNode parent = info.getParent();

                boolean needsBinaryMatch = false;

                for (ContainerSignatureMatch match : matches.getContainerSignatureMatches()) {
                    match.matchFileEntry(entryName);
                    if (match.needsBinaryMatch(entryName)) {
                        needsBinaryMatch = true;
                    }
                }

                if (needsBinaryMatch && entry instanceof DocumentNode) {
                    DocumentInputStream docIn = null;
                    ByteReader byteReader = null;
                    try {
                        docIn = parent.createDocumentInputStream(entry.getName());
                        byteReader = newByteReader(docIn);
                        for (ContainerSignatureMatch match : matches.getContainerSignatureMatches()) {
                            match.matchBinaryContent(entryName, byteReader);
                        }
                    } finally {
                        if (byteReader != null) {
                            byteReader.close();
                        }
                        if (docIn != null) {
                            docIn.close();
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        } finally {
            if (reader != null) {
                reader.close();
            }

            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * A class which iterates over all the file entries in an OLE2 file.
     * It can optionally process children in sub folders, or just do the immediate children of the root.
     * It returns an EntryInfo on next(), which wraps the Entry, and the path to the entry.
     */
    private static class OLE2Walker implements Iterator<EntryInfo> {

        private final List<PathIterator> entries = new ArrayList<>();
        private final boolean processSubFolders;
        private EntryInfo nextEntry;

        public OLE2Walker(DirectoryNode rootEntry, boolean processSubFolders) {
            entries.add(new PathIterator(rootEntry, "", rootEntry.getEntries()));
            this.processSubFolders = processSubFolders;
        }

        @Override
        public boolean hasNext() {
            if (nextEntry == null) {
                while (!entries.isEmpty()) {
                    // We get the last entry so that removing it after processing doesn't cause the whole array to shift.
                    // They all have to be processed, so the order doesn't matter.
                    int posToProcess = entries.size() - 1;
                    PathIterator currentEntries = entries.get(posToProcess);
                    if (currentEntries.getIterator().hasNext()) {
                        Entry entry = currentEntries.getIterator().next();
                        String entryName = entry.getName().trim();
                        String entryPath = currentEntries.getPath() + entryName;
                        if (processSubFolders && entry.isDirectoryEntry()) {
                            entries.add(new PathIterator((DirectoryNode) entry,
                                    entryPath + '/', ((DirectoryEntry) entry).getEntries()));
                        }
                        nextEntry = new EntryInfo(currentEntries.getParent(), entryPath, entry);
                        return true;
                    }
                    entries.remove(posToProcess); // remove the current entry iterator from the list of entries.
                }
            }
            return nextEntry != null;
        }

        @Override
        public EntryInfo next() {
            if (hasNext()) {
                EntryInfo toReturn = nextEntry;
                nextEntry = null;
                return toReturn;
            }
            throw new NoSuchElementException("No more OLE2 entries");
        }

        /**
         * A record of an Entry iterator to process and the path for the files in that iterator.
         */
        private static class PathIterator {
            private final DirectoryNode parent;
            private final String path;
            private final Iterator<Entry> iterator;
            PathIterator(DirectoryNode parent, String path, Iterator<Entry> iterator) {
                this.parent = parent;
                this.path = path;
                this.iterator = iterator;
            }

            public DirectoryNode getParent() {
                return parent;
            }

            public String getPath() {
                return path;
            }

            public Iterator<Entry> getIterator() {
                return iterator;
            }
        }
    }

    /**
     * A wrapper for an OLE2 file entry object and its path and directory entry parent.
     */
    private static class EntryInfo {
        private final DirectoryNode parent;
        private final String path;
        private final Entry entry;
        public EntryInfo(DirectoryNode parent, String path, Entry entry) {
            this.parent = parent;
            this.path = path;
            this.entry = entry;
        }

        public DirectoryNode getParent() {
            return parent;
        }

        public String getPath() {
            return path;
        }

        public Entry getEntry() {
            return entry;
        }
    }

}
