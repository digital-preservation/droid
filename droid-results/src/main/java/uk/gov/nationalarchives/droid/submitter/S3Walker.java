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
package uk.gov.nationalarchives.droid.submitter;

import software.amazon.awssdk.services.s3.model.S3Object;
import uk.gov.nationalarchives.droid.core.interfaces.*;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;
import uk.gov.nationalarchives.droid.core.interfaces.resource.S3Utils;
import uk.gov.nationalarchives.droid.profile.AbstractProfileResource;
import uk.gov.nationalarchives.droid.profile.S3ProfileResource;
import uk.gov.nationalarchives.droid.results.handlers.ProgressMonitor;
import uk.gov.nationalarchives.droid.util.FileUtil;

import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;

public class S3Walker {

    private static final String FORWARD_SLASH = "/";
    private static final String S3_SCHEME = "s3://";

    private final ProgressMonitor progressMonitor;

    private final ResultHandler resultHandler;

    private final S3EventHandler s3EventHandler;

    public S3Walker(final ProgressMonitor progressMonitor, final ResultHandler resultHandler, final S3EventHandler s3EventHandler) {
        this.progressMonitor = progressMonitor;
        this.resultHandler = resultHandler;
        this.s3EventHandler = s3EventHandler;
    }

    public void walk(AbstractProfileResource resource) {
        S3Result s3Result = getS3Result(resource);
        progressMonitor.setTargetCount(s3Result.totalCount());
        ArrayList<String> keysList = new ArrayList<>(s3Result.dirToObjectInfo().keySet());
        Map<String, ResourceId> pathToResourceId = new HashMap<>();
        keysList.sort(Comparator.comparingInt(String::length));
        for (int i=0; i < keysList.size(); i++) {
            URI dirUri = URI.create(keysList.get(i));
            Path dirPath = getPath(dirUri);
            ResourceId fileParentNode = null;
            if (dirPath.getParent() != null && s3Result.totalCount() > 2) {
                progressMonitor.startJob(dirUri);
                ResourceId parent = dirPath.getParent() == null ? null : pathToResourceId.get(dirPath.getParent().toUri().toString());
                fileParentNode = handleS3Directory(dirPath, parent, i+1);
                pathToResourceId.put(dirUri + FORWARD_SLASH, fileParentNode);
            }
            for (S3ObjectInfo objectInfo: s3Result.dirToObjectInfo().get(keysList.get(i))) {
                progressMonitor.startJob(URI.create(objectInfo.key().replaceAll(" ", "%20")));
                S3ProfileResource fileProfileResource = new S3ProfileResource(objectInfo.key);
                fileProfileResource.setLastModifiedDate(objectInfo.lastModified);
                fileProfileResource.setSize(objectInfo.size);
                s3EventHandler.onS3Event(fileProfileResource, fileParentNode);
            }
        }
    }

    private ResourceId handleS3Directory(final Path dir, ResourceId parentId, int depth) {
        IdentificationResultImpl result = new IdentificationResultImpl();
        result.setMethod(IdentificationMethod.NULL);

        RequestMetaData metaData = new RequestMetaData(-1L, new Date(0).getTime(), depth == 0 ? dir.toAbsolutePath().toString() : FileUtil.fileName(dir));

        RequestIdentifier identifier = new RequestIdentifier(dir.toUri());
        identifier.setParentResourceId(parentId);
        result.setRequestMetaData(metaData);
        result.setIdentifier(identifier);
        return resultHandler.handleDirectory(result, parentId, false);
    }

    private S3Result getS3Result(AbstractProfileResource resource) {
        URI uri = resource.getUri();
        S3Utils s3Utils = new S3Utils(this.s3EventHandler.getS3Client(resource));

        S3Utils.S3ObjectList objectList = s3Utils.listObjects(uri);
        Iterable<S3Object> contents = objectList.contents();
        String bucket = objectList.bucket();

        Map<String, List<S3ObjectInfo>> dirToFileMap = new HashMap<>();
        int totalCount = 0;
        String uriWithBucket = S3_SCHEME + bucket + FORWARD_SLASH;

        for (S3Object s3Object: contents) {
            int lastSlashIndex = (FORWARD_SLASH + s3Object.key()).lastIndexOf(FORWARD_SLASH);
            String keyUri = uriWithBucket + s3Object.key();
            String parent;
            if (lastSlashIndex == 0) {
                parent = uriWithBucket;
            } else {
                parent = uriWithBucket + s3Object.key().substring(0, lastSlashIndex -1);
            }

            if (!dirToFileMap.containsKey(parent)) {
                List<S3ObjectInfo> existingKeys = new ArrayList<>();
                existingKeys.add(new S3ObjectInfo(keyUri, new Date(s3Object.lastModified().toEpochMilli()), s3Object.size()));
                dirToFileMap.put(parent, existingKeys);
                if (FORWARD_SLASH.equals(URI.create(parent).getPath())) {
                    totalCount = totalCount + 1;
                } else {
                    totalCount = totalCount + 2;
                }

            } else {
                List<S3ObjectInfo> existingKeys = dirToFileMap.get(parent);
                existingKeys.add(new S3ObjectInfo(keyUri, new Date(s3Object.lastModified().toEpochMilli()), s3Object.size()));
                dirToFileMap.put(parent, existingKeys);
                totalCount++;
            }
        }
        return new S3Result(dirToFileMap, totalCount);
    }
    private record S3ObjectInfo(String key, Date lastModified, long size) {}
    private record S3Result(Map<String, List<S3ObjectInfo>> dirToObjectInfo, int totalCount) {
    }

    private Path getPath(URI uri) {
        return FileSystems.getFileSystem(uri).getPath(uri.getPath());
    }
}
