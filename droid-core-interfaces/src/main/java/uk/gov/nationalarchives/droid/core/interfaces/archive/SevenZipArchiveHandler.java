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
import java.io.InputStream;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.io.FilenameUtils;

import uk.gov.nationalarchives.droid.core.interfaces.AsynchDroid;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.ResourceId;
import uk.gov.nationalarchives.droid.core.interfaces.ResultHandler;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultImpl;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;

/**
 *
 */
public class SevenZipArchiveHandler implements ArchiveHandler {

    private AsynchDroid droid;
    private IdentificationRequestFactory<InputStream> factory;
    private ResultHandler resultHandler;

    /**
     * Empty bean constructor.
     */
    public SevenZipArchiveHandler() {
    }

    /**
     * Constructor which sets internal parameters.
     * @param droidCore The droid core to use.
     * @param factory The IdentificationRequestFactory to use.
     * @param resultHandler The ResultHandler to use.
     */
    public SevenZipArchiveHandler(AsynchDroid droidCore,
                             IdentificationRequestFactory<InputStream> factory,
                             ResultHandler resultHandler) {
        this.droid = droidCore;
        this.factory = factory;
        this.resultHandler = resultHandler;
    }

    @Override
    public void handle(IdentificationRequest request) throws IOException {
        SeekableByteChannel     sevenZipReader = new SevenZipReader(request.getWindowReader());
        SevenZFile              sevenZFile     = new SevenZFile(sevenZipReader);
        SevenZipIteratorAdapter sevenZIterator = new SevenZipIteratorAdapter(sevenZFile);
        SevenZArchiveWalker     walker         = new SevenZArchiveWalker(droid, factory, request.getIdentifier(), resultHandler);
        walker.walk(sevenZIterator);
    }

    /**
     * A SevenZEntryInfo class which wraps entry information and a stream for that entry.
     */
    public static class SevenZEntryInfo {
        private final SevenZArchiveEntry entry;
        private final InputStream stream;

        /**
         * Constructs a SevenZEntryInfo class.
         * @param entry The entry metadata
         * @param stream The stream of bytes for this entry.
         */
        public SevenZEntryInfo(SevenZArchiveEntry entry, InputStream stream) {
            this.entry = entry;
            this.stream = stream;
        }

        /**
         * Returns the entry metadata.
         *
         * @return the entry metadata.
         */
        public SevenZArchiveEntry getEntry() { return entry; }

        /**
         * Returns the entry stream.
         *
         * @return the entry stream.
         */
        public InputStream getEntryStream() { return stream; }
    }

    /**
     *Construct a SevenZArchiveWalker which walks a SevenZip archive.
     */
    public static class SevenZArchiveWalker extends ArchiveFileWalker<SevenZEntryInfo> {

        private final AsynchDroid droid;
        private IdentificationRequestFactory<InputStream> factory;
        private final ResourceId parentId;
        private final URI parentName;
        private final long originatorNodeId;
        private final Map<String, ResourceId> directories = new HashMap<String, ResourceId>();
        private final ResultHandler resultHandler;


        /**
         *
         * @param droid d
         * @param factory f
         * @param parent p
         * @param resultHandler r
         */
        public SevenZArchiveWalker(AsynchDroid droid,
                                   IdentificationRequestFactory<InputStream> factory,
                                   RequestIdentifier parent,
                                   ResultHandler resultHandler) {
            this.droid = droid;
            this.factory = factory;
            this.parentId = parent.getResourceId();
            this.parentName = parent.getUri();
            this.originatorNodeId = parent.getAncestorId();
            this.resultHandler = resultHandler;
        }

        @Override
        protected void handleEntry(SevenZEntryInfo info) throws IOException {
            String entryName = info.entry.getName();
            final String prefixPath = FilenameUtils.getPath(entryName);
            ResourceId correlationId = parentId; // by default, files are correlated to the parent.

            // If there is a path, get the actual correlation id for its parent folder:
            if (!prefixPath.isEmpty()) {
                correlationId = directories.get(prefixPath);
                // If we haven't seen the path before, add the ancestor folders not yet seen:
                if (correlationId == null) {
                    correlationId = processAncestorFolders(prefixPath, info.getEntry().getLastModifiedDate());
                }
            }

            // If there is a file, submit the file:
            if (!info.entry.isDirectory()) {
                entryName = FilenameUtils.getName(entryName);
                if (!entryName.isEmpty()) {
                    submit(info, entryName, correlationId);
                }
            }
        }


        /**
         * Submits a request to droid.
         * @param info the sevenzip entry and stream to submit
         * @param entryName the name of the entry
         * @param correlationId the correlation iod for the request
         * @throws IOException if the input stream could not be read
         */
        final void submit(SevenZEntryInfo info, String entryName,
                          ResourceId correlationId) throws IOException {
            Long size = info.entry != null ? info.entry.getSize() : null;
            Long lastModfied = info.entry != null && info.entry.getHasLastModifiedDate()
                    ? info.entry.getLastModifiedDate().getTime() : new Date().getTime();

            RequestMetaData metaData = new RequestMetaData(
                    size,
                    lastModfied,
                    entryName);

            RequestIdentifier identifier =
                    new RequestIdentifier(ArchiveFileUtils.toSevenZUri(parentName, info.entry.getName()));
            identifier.setAncestorId(originatorNodeId);
            identifier.setParentResourceId(correlationId);
            if (identifier.getParentPrefix() != null && identifier.getParentPrefix().isEmpty()) {
                identifier.setParentPrefix(null);
            }
            IdentificationRequest<InputStream> request = factory.newRequest(metaData, identifier);
            if (droid.passesIdentificationFilter(request)) {
                request.open(info.stream);
                droid.submit(request);
            } // Seven zip entry stream should not be closed whether opened or not - see SevenZipIteratorAdapter class.
        }

        private ResourceId processAncestorFolders(String path, Date lastModified) {
            List<String> paths = ArchiveFileUtils.getAncestorPaths(path);
            ResourceId longestParentId = parentId;
            // Find the longest path we *have* seen before (if any):
            // (ancestor paths are ordered longest first)
            int longestSeenBefore = paths.size();
            ResourceId correlationId = null;
            for (int pathIndex = 0; pathIndex < paths.size(); pathIndex++) {
                correlationId = directories.get(paths.get(pathIndex));
                if (correlationId != null) {
                    longestSeenBefore = pathIndex;
                    longestParentId = correlationId;
                    break;
                }
            }

            // Add any that haven't yet been processed (from shortest to longest)
            for (int pathIndex = longestSeenBefore - 1; pathIndex >= 0; pathIndex--) {
                final String pathName = paths.get(pathIndex);
                SevenZArchiveEntry entry = new SevenZArchiveEntry();
                entry.setLastModifiedDate(lastModified);
                entry.setName(pathName);
                String dirName = FilenameUtils.getName(pathName.substring(0, pathName.length() - 1));
                longestParentId = submitDirectory(parentName, entry, dirName, longestParentId, resultHandler);
                directories.put(pathName, longestParentId);
            }

            return longestParentId;
        }

    }




    /**
     * @param parentName
     * @param entry
     * @param entryName
     * @param correlationId
     * @return
     */
    private static ResourceId submitDirectory(final URI parentName,
                                              SevenZArchiveEntry entry, String entryName, ResourceId correlationId, ResultHandler resultHandler) {
        IdentificationResultImpl result = new IdentificationResultImpl();

        long size = entry.getSize();
        Date date = entry.getLastModifiedDate();
        long time = date == null ? -1 : date.getTime();

        RequestMetaData metaData = new RequestMetaData(
                size != -1 ? size : null,
                time != -1 ? time : null,
                entryName);

        RequestIdentifier identifier = new RequestIdentifier(
                ArchiveFileUtils.toTarUri(parentName, entry.getName()));

        result.setRequestMetaData(metaData);
        result.setIdentifier(identifier);
        return resultHandler.handleDirectory(result, correlationId, false);
    }


    /**
     * Set factory.
     * @param factory f.
     */
    public void setFactory(IdentificationRequestFactory<InputStream> factory) {
        this.factory = factory;
    }

    /**
     * Sed droid.
     * @param droid d.
     */
    public void setDroid(AsynchDroid droid) {
        this.droid = droid;
    }

    /**
     *
     * @param resultHandler r
     */
    public void setResultHandler(ResultHandler resultHandler) {
        this.resultHandler = resultHandler;
    }
}
