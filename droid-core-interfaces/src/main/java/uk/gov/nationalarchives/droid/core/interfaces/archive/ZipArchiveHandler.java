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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
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
public class ZipArchiveHandler implements ArchiveHandler {

    private AsynchDroid droidCore;
    private IdentificationRequestFactory factory;
    private ResultHandler resultHandler;
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final void handle(IdentificationRequest request) throws IOException {

        InputStream in = request.getSourceInputStream(); 
        try {
            final ZipArchiveInputStream zin = new ZipArchiveInputStream(in);
            
            try {
                Iterable<ZipArchiveEntry> iterable = new Iterable<ZipArchiveEntry>() {
                    @Override
                    public final Iterator<ZipArchiveEntry> iterator() {
                        return new ZipInputStreamIterator(zin);
                    }
                };
                 
        
                ZipArchiveWalker walker = new ZipArchiveWalker(zin, request.getIdentifier());  
                walker.walk(iterable);
            } finally {
                if (zin != null) {
                    zin.close();
                }
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * @param parentName
     * @param entry
     * @param entryName
     * @param correlationId
     * @return
     */
    private ResourceId submitDirectory(final URI parentName,
            ZipArchiveEntry entry, String entryName, ResourceId correlationId) {
        IdentificationResultImpl result = new IdentificationResultImpl();
        
        long size = entry.getSize();
        long time = entry.getTime();
        
        RequestMetaData metaData = new RequestMetaData(
                size != -1 ? size : null, 
                time != -1 ? time : null,
                entryName);
        
        RequestIdentifier identifier = new RequestIdentifier(
                ArchiveFileUtils.toZipUri(parentName, entry.getName()));
        
        result.setRequestMetaData(metaData);
        result.setIdentifier(identifier);
        return resultHandler.handleDirectory(result, correlationId, false);
    }
    
    /**
     * Submits a request to droid.
     * @param entry the zip entry to submit
     * @param parentName the name of the parent file
     * @param entryName the name of the Zip entry
     * @param in the zip input stream
     * @param correlationId an ID to correlate this submission to
     * @param originatorNodeId the ID of the originator node
     * @throws IOException if there was an error accessing the input stream 'in'
     */
    final void submit(ZipArchiveEntry entry, String entryName, URI parentName, 
            ZipArchiveInputStream in, ResourceId correlationId, long originatorNodeId) 
        throws IOException {
        
        long size = entry.getSize();
        long time = entry.getTime();
        
        RequestMetaData metaData = new RequestMetaData(
                size != -1 ? size : null, 
                time != -1 ? time : null,
                entryName);
        
        RequestIdentifier identifier = new RequestIdentifier(ArchiveFileUtils.toZipUri(parentName, entry.getName()));
        identifier.setAncestorId(originatorNodeId);
        identifier.setParentResourceId(correlationId);
        IdentificationRequest request = factory.newRequest(metaData, identifier);
        request.open(in);
        droidCore.submit(request);
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
     * Adapts an enumeration to the Iterator interface.
     * @author rflitcroft
     *
     */
    private static final class ZipInputStreamIterator 
        extends ArchiveInputStreamIterator<ZipArchiveEntry, ZipArchiveInputStream> { 
        
        /**
         * @param in
         */
        public ZipInputStreamIterator(ZipArchiveInputStream in) {
            super(in);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected ZipArchiveEntry getNextEntry(ZipArchiveInputStream stream) throws IOException {
            return getInputStream().getNextZipEntry();
        }
    }
    
    /**
     * Archive walker for zip files.
     * @author rflitcroft
     *
     */
    private final class ZipArchiveWalker extends ArchiveFileWalker<ZipArchiveEntry> {
        
        private final ResourceId parentId;
        private final long originatorNodeId;
        private final URI parentName;
        private final ZipArchiveInputStream in;
        private final Map<String, ResourceId> directories = new HashMap<String, ResourceId>();
        
        ZipArchiveWalker(ZipArchiveInputStream in, RequestIdentifier identifier) {
            this.in = in;
            this.parentId = identifier.getResourceId();
            this.parentName = identifier.getUri();
            this.originatorNodeId = identifier.getAncestorId();
        }

        
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
                ZipArchiveEntry entry = new ZipArchiveEntry(pathName);
                String dirName = FilenameUtils.getName(pathName.substring(0, pathName.length() - 1));
                longestParentId = submitDirectory(parentName, entry, dirName, longestParentId);
                directories.put(pathName, longestParentId);
            }
            
            return longestParentId;
        }
        
        
        @Override
        protected void handleEntry(ZipArchiveEntry entry) throws IOException {
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
    }

    /**
     * @param resultHandler the resultHandler to set
     */
    public final void setResultHandler(ResultHandler resultHandler) {
        this.resultHandler = resultHandler;
    }
    
}
