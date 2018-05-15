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
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.stephenc.javaisotools.loopfs.iso9660.Iso9660FileEntry;
import com.github.stephenc.javaisotools.loopfs.iso9660.Iso9660FileSystem;

import uk.gov.nationalarchives.droid.core.interfaces.AsynchDroid;
import uk.gov.nationalarchives.droid.core.interfaces.ResultHandler;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.ResourceId;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultImpl;
import uk.gov.nationalarchives.droid.core.interfaces.resource.FileSystemIdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;


/**
 * Created by rhubner on 2/13/17.
 */
public class ISOImageArchiveHandler implements ArchiveHandler {

    private static final String PATH_SPLITTER = "!";
    private static final String UTF_8 = "UTF-8";

    private AsynchDroid droid;
    private IdentificationRequestFactory<InputStream> factory;
    private ResultHandler resultHandler;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void handle(IdentificationRequest request) throws IOException {

        if (request.getClass().isAssignableFrom(FileSystemIdentificationRequest.class)) {

            FileSystemIdentificationRequest req = (FileSystemIdentificationRequest) request;

            Iso9660FileSystem fileSystem = new Iso9660FileSystem(req.getFile().toFile(), true);

            ISOImageArchiveWalker walker = new ISOImageArchiveWalker(droid, factory, resultHandler,
                    fileSystem, request.getIdentifier());
            walker.walk(fileSystem);
        } else {
            log.info("Identification request for ISO image ignored due to limited support.");
        }
    }

    /**
     * Internal walker implementation.
     */
    public static class ISOImageArchiveWalker extends ArchiveFileWalker<Iso9660FileEntry> {

        private final AsynchDroid droid;
        private final IdentificationRequestFactory<InputStream> factory;
        private final ResultHandler resultHandler;


        private final Iso9660FileSystem fileSystem;
        private final ResourceId rootParentId;
        private final URI isoFileUri;
        private final long originatorNodeId;

        private final Map<String, ResourceId> directories = new HashMap<String, ResourceId>();
        private final Logger log = LoggerFactory.getLogger(this.getClass());


        /**
         * Create instance.
         * @param droid async droid.
         * @param factory factory for identification requests.
         * @param resultHandler result handler(used for directory handling).
         * @param fileSystem Original iso file system.
         * @param requestIdentifier ReqIdentifier.
         */
        public ISOImageArchiveWalker(AsynchDroid droid, IdentificationRequestFactory<InputStream> factory,
                                     ResultHandler resultHandler,
                                     Iso9660FileSystem fileSystem, RequestIdentifier requestIdentifier) {

            this.droid = droid;
            this.factory = factory;
            this.resultHandler = resultHandler;
            this.fileSystem = fileSystem;
            this.rootParentId = requestIdentifier.getResourceId();
            this.isoFileUri = requestIdentifier.getUri();
            this.originatorNodeId = requestIdentifier.getNodeId();
            directories.put("", rootParentId);  //Rood directory
        }


        private void submitFile(Iso9660FileEntry entry) throws IOException, URISyntaxException {
            String path = FilenameUtils.getPath(entry.getPath());
            String name = entry.getName();


            ResourceId correlationId = this.directories.get(path);
            if (correlationId == null) {
                correlationId = submitDirectory(path, entry.getLastModifiedTime());
            }


            InputStream entryInputStream = fileSystem.getInputStream(entry);
            try {
                RequestIdentifier identifier = new RequestIdentifier(ArchiveFileUtils.toIsoImageUri(isoFileUri, path + name));
                identifier.setAncestorId(originatorNodeId);
                identifier.setParentResourceId(correlationId);

                RequestMetaData metaData = new RequestMetaData(entry.getSize(),
                        entry.getLastModifiedTime(), name);

                IdentificationRequest<InputStream> request = factory.newRequest(metaData, identifier);
                request.open(entryInputStream);

                droid.submit(request);
            } finally {
                try {
                    if (entryInputStream != null) {
                        entryInputStream.close();
                    }
                } catch (IOException ex) {
                    log.warn("failed to close entryInputStream", ex);
                }
            }
        }


        private ResourceId submitDirectory(String path, long lastModifiedTime)
            throws URISyntaxException, UnsupportedEncodingException {

            String parentPath = FilenameUtils.getPath(path.substring(0, path.length() - 1));

            String name = FilenameUtils.getName(path.substring(0, path.length() - 1));

            log.debug("processing path: " + path + " name: " + name);

            ResourceId resourceId = directories.get(name);
            if (resourceId == null) {

                ResourceId parentID = directories.get(parentPath);
                if (parentID == null) {
                    parentID = submitDirectory(parentPath, lastModifiedTime);
                }

                RequestMetaData metaData = new RequestMetaData(null, lastModifiedTime, name);

                RequestIdentifier identifier = new RequestIdentifier(ArchiveFileUtils.toIsoImageUri(isoFileUri, path));

                IdentificationResultImpl result = new IdentificationResultImpl();
                result.setRequestMetaData(metaData);
                result.setIdentifier(identifier);

                resourceId = resultHandler.handleDirectory(result, parentID, false);
                this.directories.put(path, resourceId);

            }
            return resourceId;
        }

        @Override
        protected void handleEntry(Iso9660FileEntry entry) throws IOException {
            try {
                if (entry.isDirectory()) {
                    if (!"".equals(entry.getPath())) {   //NOT for root directory
                        submitDirectory(entry.getPath(), entry.getLastModifiedTime());
                    }
                } else {
                    submitFile(entry);
                }
            } catch (URISyntaxException e) {
                throw new IOException("Wrong URI syntax", e);
            }
        }

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
     * Set result set handler.
     * @param resultHandler h.
     */
    public void setResultHandler(ResultHandler resultHandler) {
        this.resultHandler = resultHandler;
    }
}
