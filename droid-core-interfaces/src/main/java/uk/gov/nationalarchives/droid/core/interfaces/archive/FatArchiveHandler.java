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


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import de.waldheinz.fs.BlockDevice;
import de.waldheinz.fs.FileSystem;
import de.waldheinz.fs.FsDirectory;
import de.waldheinz.fs.FsDirectoryEntry;
import de.waldheinz.fs.FsFile;
import de.waldheinz.fs.fat.FatFileSystem;

import uk.gov.nationalarchives.droid.core.interfaces.AsynchDroid;
import uk.gov.nationalarchives.droid.core.interfaces.ResultHandler;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.ResourceId;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultImpl;
import uk.gov.nationalarchives.droid.core.interfaces.resource.FatFileIdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;


/**
 * FatArchiveHandler.
 */
public final class FatArchiveHandler implements ArchiveHandler {

    private static final boolean OPEN_READ_ONLY = true;

    private AsynchDroid droid;
    private ResultHandler resultHandler;
    private IdentificationRequestFactory<InputStream> factory;

    /**
     * Empty bean constructor.
     */
    public FatArchiveHandler() {
    }

    /**
     * Constructor which sets internal parameters.
     * @param droidCore The droid core to use.
     * @param factory The IdentificationRequestFactory to use.
     * @param resultHandler The ResultHandler to use.
     */
    public FatArchiveHandler(AsynchDroid droidCore,
                              IdentificationRequestFactory factory,
                              ResultHandler resultHandler) {
        this.droid = droidCore;
        this.factory = factory;
        this.resultHandler = resultHandler;
    }

    @Override
    public void handle(IdentificationRequest request) throws IOException {
        BlockDevice device    = new FatReader(request.getWindowReader());
        FileSystem fatSystem = FatFileSystem.read(device, OPEN_READ_ONLY);
        FsDirectory root      = fatSystem.getRoot();
        FatArchiveWalker walker    = new FatArchiveWalker(droid, resultHandler, request.getIdentifier());
        walker.walk(root);
    }


    private final class FatArchiveWalker extends ArchiveFileWalker<FsDirectoryEntry> {
        private final AsynchDroid droid;
        private final ResultHandler resultHandler;

        private final ResourceId rootParentId;
        private final URI fatFileUri;
        private final long originatorNodeId;
        private final Logger log = LoggerFactory.getLogger(this.getClass());
        private final Map<FsDirectoryEntry, ResourceId> directories = new HashMap<>();
        private final Map<FsDirectoryEntry, FsDirectoryEntry> parentMap = new HashMap<>();

        /**
         * Create instance.
         *
         * @param droid             async droid.
         * @param resultHandler     result handler(used for directory handling).
         * @param requestIdentifier ReqIdentifier.
         */
        private FatArchiveWalker(AsynchDroid droid, ResultHandler resultHandler,
                                 RequestIdentifier requestIdentifier) {

            this.droid = droid;
            this.resultHandler = resultHandler;
            this.rootParentId = requestIdentifier.getResourceId();
            this.fatFileUri = requestIdentifier.getUri();
            this.originatorNodeId = requestIdentifier.getNodeId();

        }

        private void submitFile(FsFile file, FsDirectoryEntry entry) throws IOException {

            FsDirectoryEntry parent = parentMap.get(entry);
            ResourceId correlationId = rootParentId;
            if (parent != null) {
                correlationId = directories.get(parent);
            }

            RequestIdentifier identifier =
                    new RequestIdentifier(ArchiveFileUtils.toFatImageUri(fatFileUri, expand(entry, parentMap)));
            identifier.setAncestorId(originatorNodeId);
            identifier.setParentResourceId(correlationId);

            RequestMetaData requestMetaData =
                    new RequestMetaData(file.getLength(), entry.getCreated(), entry.getName());

            IdentificationRequest<InputStream> req = factory.newRequest(requestMetaData, identifier);
            if (droid.passesSubmitFilter(req)) {
                Path tempFile = ArchiveFileUtils.writeFsFileToTemp(entry, ((FatFileIdentificationRequest) req).getTempDir());
                InputStream is = new FileInputStream(tempFile.toFile()); //TODO: why not new a file reader rather than in inputstream reader here?
                req.open(is);
                droid.submit(req);
            }
        }

        private void submitDirectory(FsDirectoryEntry directoryEntry) throws IOException {

            ResourceId resourceId = directories.get(directoryEntry);
            if (resourceId == null) {
                FsDirectoryEntry parent = parentMap.get(directoryEntry);
                ResourceId parentID;
                if (parent != null) {
                    parentID = directories.get(parent);
                } else {
                    parentID = rootParentId;
                }

                RequestMetaData metaData =
                        new RequestMetaData(null, directoryEntry.getLastModified(), directoryEntry.getName());

                RequestIdentifier identifier =
                        new RequestIdentifier(ArchiveFileUtils.toFatImageUri(fatFileUri, expand(directoryEntry, parentMap)));

                IdentificationResultImpl result = new IdentificationResultImpl();
                result.setRequestMetaData(metaData);
                result.setIdentifier(identifier);

                resourceId = resultHandler.handleDirectory(result, parentID, false);
                this.directories.put(directoryEntry, resourceId);

            }
        }


        @Override
        protected void handleEntry(FsDirectoryEntry directoryEntry) throws IOException {
            if (directoryEntry.isFile()) {
                if (directoryEntry.getFile().getLength() != 0) {
                   submitFile(directoryEntry.getFile(), directoryEntry);
                }
            } else if (directoryEntry.isDirectory()) {
                if (notCurrentOrParentDirectory(directoryEntry)) {
                    for (FsDirectoryEntry e : directoryEntry.getDirectory()) {
                        if (notCurrentOrParentDirectory(e)) {
                            parentMap.put(e, directoryEntry);
                        }
                    }
                    if (!"".equals(directoryEntry.getDirectory().toString())) {
                        try {
                            submitDirectory(directoryEntry);
                            walk(toIterable(directoryEntry.getDirectory().iterator()));
                        } catch (IOException ex) {
                            log.error("Failed on directory submission", ex);
                        }
                    }
                }

            } else {
                log.error("unknown entry : " + directoryEntry);
            }
        }

        private boolean notCurrentOrParentDirectory(FsDirectoryEntry directoryEntry) {
            return !(".".equals(directoryEntry.getName()) || "..".equals(directoryEntry.getName()));
        }
    }

    private <T> Iterable<T> toIterable(Iterator<T> it) {
        return () -> it;
    }

    /**
     * Evaluates Fat file path.
     * @param entry FsDirectoryEntry.
     * @param map FsDirectoryEntry parent map.
     * @return String file path.
     */
    private String expand(FsDirectoryEntry entry, Map<FsDirectoryEntry, FsDirectoryEntry> map) {
        String filePath = entry.getName();
        FsDirectoryEntry parent = map.get(entry);

        while (parent != null) {
            filePath = parent.getName() + File.separatorChar + filePath;
            parent = map.get(parent);
        }

        return filePath;
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
     * @param droid droid.
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


