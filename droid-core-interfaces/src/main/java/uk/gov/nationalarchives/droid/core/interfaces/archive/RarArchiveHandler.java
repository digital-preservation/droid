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

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.impl.FileVolumeManager;
import com.github.junrar.rarfile.FileHeader;

import uk.gov.nationalarchives.droid.core.interfaces.AsynchDroid;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultImpl;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.ResourceId;
import uk.gov.nationalarchives.droid.core.interfaces.ResultHandler;
import uk.gov.nationalarchives.droid.core.interfaces.resource.FileSystemIdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;




/**
 * Created by rhubner on 3/21/17.
 */
public final class RarArchiveHandler implements ArchiveHandler {

    private static final String UNIX_PATH_SPLITTER = "/";
    private static final String WINDOWS_PATH_SPLITTER = "\\";


    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private ResultHandler resultHandler;
    private AsynchDroid droid;
    private IdentificationRequestFactory<InputStream> identificationRequestFactory;


    @Override
    public void handle(IdentificationRequest request) throws IOException {

        if (request.getClass().isAssignableFrom(FileSystemIdentificationRequest.class)) {

            FileSystemIdentificationRequest req = (FileSystemIdentificationRequest) request;

            FileVolumeManager fileVolumeManager = new FileVolumeManager(req.getFile().toFile());
            try {
                try (Archive archive = new Archive(fileVolumeManager)) {
                    if (archive.isEncrypted()) {
                        throw new RuntimeException("Encrypted archive");
                    }
                    RarWalker walker = new RarWalker(archive, req.getIdentifier());

                    walker.walk(archive.getFileHeaders());
                }
            } catch (RarException ex) {
                throw new RuntimeException("Rar procesing failed :", ex);
            }
        } else {
            log.info("Identification request for RAR archive ignored due to limited support.");
        }
    }

    /**
     * Result Handler.
     * @param resultHandler R.
     */
    public void setResultHandler(ResultHandler resultHandler) {
        this.resultHandler = resultHandler;
    }

    /**
     * Droid identification engine.
     * @param droid D.
     */
    public void setDroid(AsynchDroid droid) {
        this.droid = droid;
    }

    /**
     * identificationRequestFactory.
     * @param identificationRequestFactory f.
     */
    public void setIdentificationRequestFactory(IdentificationRequestFactory<InputStream> identificationRequestFactory) {
        this.identificationRequestFactory = identificationRequestFactory;
    }

    private final class RarWalker extends ArchiveFileWalker<FileHeader> {

        private final Map<String, ResourceId> directories = new HashMap<String, ResourceId>();
        private final Logger log = LoggerFactory.getLogger(this.getClass());
        private final Archive archive;
        private final ResourceId rootParentId;

        private final URI parentURI;
        private final long originatorNodeId;


        private RarWalker(Archive archive, RequestIdentifier requestIdentifier) {
            this.archive = archive;
            this.parentURI = requestIdentifier.getUri();
            originatorNodeId = requestIdentifier.getNodeId();
            this.rootParentId = requestIdentifier.getResourceId();
            directories.put("", this.rootParentId); //ROOT directory in archive
        }

        private ResourceId submitDirectory(String fullPath, Date mTime) throws URISyntaxException, UnsupportedEncodingException {
            String parentPath = FilenameUtils.getPath(fullPath.substring(0, fullPath.length() - 1));
            String name = FilenameUtils.getName(fullPath.substring(0, fullPath.length() - 1));

            //CHECKSTYLE:OFF
            log.debug("submitDirectory, processing path: " + parentPath + " name: " + name);
            //CHECKSTYLE:ON

            ResourceId resourceId = getFromDirCache(fullPath);
            if (resourceId == null) {

                ResourceId parentID = getFromDirCache(parentPath);
                if (parentID == null) {
                    parentID = submitDirectory(parentPath, mTime);
                }

                RequestMetaData metaData = new RequestMetaData(null, mTime.getTime(), name);

                RequestIdentifier identifier = new RequestIdentifier(ArchiveFileUtils.toRarUri(parentURI, fullPath));

                IdentificationResultImpl result = new IdentificationResultImpl();
                result.setRequestMetaData(metaData);
                result.setIdentifier(identifier);

                resourceId = resultHandler.handleDirectory(result, parentID, false);
                saveDirToCache(fullPath, resourceId);

            }
            return resourceId;
        }

        private void submitFile(FileHeader entry) throws IOException, URISyntaxException, RarException {
            String fullpath = entry.getFileNameString();
            String path = FilenameUtils.getPath(fullpath);
            String name = FilenameUtils.getName(fullpath);

            //CHECKSTYLE:OFF
            log.debug("submitFile, processing path: " + path + " name: " + name);
            //CHECKSTYLE:ON

            ResourceId correlationId = getFromDirCache(path);
            if (correlationId == null) {
                correlationId = submitDirectory(path, entry.getMTime());
            }

            InputStream entryInputStream = archive.getInputStream(entry);

            RequestIdentifier identifier = new RequestIdentifier(ArchiveFileUtils.toRarUri(parentURI, path + name));
            identifier.setAncestorId(originatorNodeId);
            identifier.setParentResourceId(correlationId);

            RequestMetaData metaData = new RequestMetaData(entry.getUnpSize(),
                    entry.getMTime().getTime(), name);

            IdentificationRequest<InputStream> request = identificationRequestFactory.newRequest(metaData, identifier);
            request.open(entryInputStream);

            droid.submit(request);
        }


        private void saveDirToCache(String path, ResourceId resourceId) {
            if (path.endsWith(UNIX_PATH_SPLITTER) || path.endsWith(WINDOWS_PATH_SPLITTER)) {
                directories.put(path.substring(0, path.length() - 1), resourceId);
            } else {
                directories.put(path, resourceId);
            }
        }

        private ResourceId getFromDirCache(String path) {
            if (path.endsWith(UNIX_PATH_SPLITTER) || path.endsWith(WINDOWS_PATH_SPLITTER)) {
                return directories.get(path.substring(0, path.length() - 1));
            } else {
                return directories.get(path);
            }
        }


        @Override
        protected void handleEntry(FileHeader entry) throws IOException {
            try {
                if (entry.isDirectory()) {
                    String path = entry.getFileNameString();
                    if (!(path.endsWith(UNIX_PATH_SPLITTER) || path.endsWith(WINDOWS_PATH_SPLITTER))) {
                        path += UNIX_PATH_SPLITTER;
                    }
                    submitDirectory(path, entry.getMTime());
                } else if (entry.isEncrypted()) {
                    throw new RuntimeException("Encrypted entry : " + entry.getFileNameString());
                } else {
                    submitFile(entry);
                }

            } catch (URISyntaxException ex) {
                throw new RuntimeException("Malformed uri for entry : " + entry.getFileNameString(), ex);
            } catch (RarException rarEx) {
                throw new RuntimeException("Probem with RAR extraction : " + entry.getFileNameString(), rarEx);

            }

        }
    }
}
