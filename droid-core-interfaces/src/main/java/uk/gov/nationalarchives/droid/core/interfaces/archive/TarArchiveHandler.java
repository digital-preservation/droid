/**
 * Copyright (c) 2012, The National Archives <pronom@nationalarchives.gsi.gov.uk>
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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.FilenameUtils;

import uk.gov.nationalarchives.droid.core.interfaces.AsynchDroid;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultImpl;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.ResourceId;
import uk.gov.nationalarchives.droid.core.interfaces.ResultHandler;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;

/**
 * @author rflitcroft
 *
 */
public class TarArchiveHandler implements ArchiveHandler {

    private AsynchDroid droidCore;
    private IdentificationRequestFactory factory;
    private ResultHandler resultHandler;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final void handle(IdentificationRequest request) throws IOException {

        InputStream tarIn = request.getSourceInputStream(); 
        try {
            final TarArchiveInputStream in = new TarArchiveInputStream(tarIn);
            try {                
                Iterable<TarArchiveEntry> iterable = new Iterable<TarArchiveEntry>() {
                    @Override
                    public final Iterator<TarArchiveEntry> iterator() {
                        return new TarArchiveEntryIterator(in);
                    }
                };
                
                TarArchiveWalker walker = new TarArchiveWalker(request.getIdentifier(), in);
                walker.walk(iterable);
            } finally {
                if (in != null) {
                    in.close();
                }
            }
        } finally {
            if (tarIn != null) {
                tarIn.close();
            }
        }
    }
    
    /**
     * Adapts a TarArchiveInputStream to an iterator.
     * @author rflitcroft
     *
     */
    private static final class TarArchiveEntryIterator 
        extends ArchiveInputStreamIterator<TarArchiveEntry, TarArchiveInputStream> {
        
        TarArchiveEntryIterator(TarArchiveInputStream in) {
            super(in);
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        protected TarArchiveEntry getNextEntry(TarArchiveInputStream stream) throws IOException {
            return getInputStream().getNextTarEntry();
        }
        
    }
    
    /**
     * Submits a request to droid.
     * @param entry the tar entry to submit
     * @param entryName the name of the entry
     * @param parentName the name of the parent file
     * @param in the archive input stream
     * @param correlationId the correlation iod for the request
     * @param originatorNodeId the ID of the originator node
     * @throws IOException if the input stream could not be read
     */
    final void submit(TarArchiveEntry entry, String entryName, URI parentName, 
            ArchiveInputStream in, ResourceId correlationId, long originatorNodeId) throws IOException {
        long size = entry.getSize();
        Date time = entry.getModTime();

        RequestMetaData metaData = new RequestMetaData(
                size == -1 ? null : size,
                time == null ? null : time.getTime(),
                entryName);
        
        RequestIdentifier identifier = 
            new RequestIdentifier(ArchiveFileUtils.toTarUri(parentName, entry.getName()));
        identifier.setAncestorId(originatorNodeId);
        identifier.setParentResourceId(correlationId);
        IdentificationRequest request = factory.newRequest(metaData, identifier);
        request.open(in);
        droidCore.submit(request);
    }
    
    /**
     * @param parentName
     * @param entry
     * @param entryName
     * @param correlationId
     * @return
     */
    private ResourceId submitDirectory(final URI parentName,
            TarArchiveEntry entry, String entryName, ResourceId correlationId) {
        IdentificationResultImpl result = new IdentificationResultImpl();
        
        long size = entry.getSize();
        Date date = entry.getModTime();
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
     * @param factory the factory to set
     */
    public final void setFactory(IdentificationRequestFactory factory) {
        this.factory = factory;
    }

    /**
     * @param droidCore the droidCore to set
     */
    public final void setDroidCore(AsynchDroid droidCore) {
        this.droidCore = droidCore;
    }

    /**
     * @param resultHandler the resultHandler to set
     */
    public final void setResultHandler(ResultHandler resultHandler) {
        this.resultHandler = resultHandler;
    }
    
    /**
     * Archive walker for TAR archives.
     * @author rflitcroft
     *
     */
    private final class TarArchiveWalker extends ArchiveFileWalker<TarArchiveEntry> {
        
        private final ResourceId parentId;
        private final long originatorNodeId;
        private final URI parentName;
        private final ArchiveInputStream in;
        private final Map<String, ResourceId> directories = new HashMap<String, ResourceId>();
        
        TarArchiveWalker(RequestIdentifier parent, ArchiveInputStream in) {
            this.in = in;
            this.parentId = parent.getResourceId();
            this.parentName = parent.getUri();
            this.originatorNodeId = parent.getAncestorId();
        }
        
        @Override
        protected void handleEntry(TarArchiveEntry entry) throws IOException {
            String entryName = entry.getName();
            final String prefixPath = FilenameUtils.getPath(entryName);
            ResourceId correlationId = parentId; // by default, files are correlated to the parent.

            // If there is a path, get the actual correlation id for its parent folder:
            if (!prefixPath.isEmpty()) {
                correlationId = directories.get(prefixPath);
                // If we haven't seen the path before, add the ancestor folders not yet seen:
                if (correlationId == null) {
                    correlationId = processAncestorFolders(prefixPath);
                }
            }
            
            // If there is a file, submit the file:
            entryName = FilenameUtils.getName(entryName);
            if (!entryName.isEmpty()) {
                submit(entry, entryName, parentName, in, correlationId, originatorNodeId);
            }
        }
        

        
        /*
        @Override
        protected void handleEntry(TarArchiveEntry entry) throws IOException {
            // strip trailing slash
            String entryName = StringUtils.stripEnd(entry.getName(), "\\/");
            final String prefixPath = FilenameUtils.getPath(entryName);
            // fish the correlation ID out of the directories encountered
            
            String name = FilenameUtils.getName(entryName);
            Long correlationId = directories.get(prefixPath);
            if (correlationId == null) {
                correlationId = parentId;
                name = entryName;
            }
            
            if (entry.isDirectory()) {
                IdentificationResultImpl result = new IdentificationResultImpl();
                RequestMetaData metaData = new RequestMetaData(null, null, entryName);
                
                RequestIdentifier identifier = new RequestIdentifier(
                        ArchiveFileUtils.toTarUri(parentName, entry.getName()));
                identifier.setParentId(correlationId);
                identifier.setAncestorId(originatorNodeId);
                
                result.setRequestMetaData(metaData);
                result.setIdentifier(identifier);
                long dirId = resultHandler.handleDirectory(result, correlationId, false);
                directories.put(entry.getName(), dirId);
            } else {
                submit(entry, name, parentName, in, correlationId, originatorNodeId);
            }
        }
        */

        /**
         * Finds the longest path which has been seen before (if any),
         * and adds all the subsequent folders which haven't been seen.
         * @param prefixPath the path of 
         */
        private ResourceId processAncestorFolders(String path) {
            // Split the path string into a list of ancestor paths:
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
                TarArchiveEntry entry = new TarArchiveEntry(pathName);
                String dirName = FilenameUtils.getName(pathName.substring(0, pathName.length() - 1));
                longestParentId = submitDirectory(parentName, entry, dirName, longestParentId);
                directories.put(pathName, longestParentId);
            }
            
            return longestParentId;
        }        
        
    }

}
