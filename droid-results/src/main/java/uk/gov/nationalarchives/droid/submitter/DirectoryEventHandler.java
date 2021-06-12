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

import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Date;

import uk.gov.nationalarchives.droid.core.interfaces.IdentificationMethod;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultImpl;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.ResourceId;
import uk.gov.nationalarchives.droid.core.interfaces.ResultHandler;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;
import uk.gov.nationalarchives.droid.util.FileUtil;

/**
 * @author rflitcroft
 *
 */
public class DirectoryEventHandler {

    private static final int URI_STRING_BUILDER_CAPACITY = 1024;

    private ResultHandler resultHandler;
    private StringBuilder uriStringBuilder = new StringBuilder(URI_STRING_BUILDER_CAPACITY);

    /**
     * Empty bean constructor.
     */
    public DirectoryEventHandler() {
    }

    /**
     * Paramaterized constructor.
     * @param resultHandler The result handler to use.
     */
    public DirectoryEventHandler(ResultHandler resultHandler) {
        setResultHandler(resultHandler);
    }

    /**
     * Handles a directory.
     * @param dir the directory to handle
     * @param parentId the directory's parent id
     * @param depth the depth of the directory in the tree
     * @param restricted true if access to the directory was restricted, false otherwise
     * @return the id of the directory
     */
    public ResourceId onEvent(final Path dir, ResourceId parentId, int depth, boolean restricted) {
        IdentificationResultImpl result = new IdentificationResultImpl();
        result.setMethod(IdentificationMethod.NULL);

        final FileTime lastModified = FileUtil.lastModifiedQuietly(dir);
        RequestMetaData metaData = new RequestMetaData(
                -1L, //recursing causes performance hit and the size is never used for directories return -1L
                lastModified == null ? new Date(0).getTime() : new Date(lastModified.toMillis()).getTime(),
                depth == 0 ? dir.toAbsolutePath().toString() : FileUtil.fileName(dir));
        
        RequestIdentifier identifier = new RequestIdentifier(dir.toUri());
        identifier.setParentResourceId(parentId);
        result.setRequestMetaData(metaData);
        result.setIdentifier(identifier);
        return resultHandler.handleDirectory(result, parentId, restricted);
    }

    /**
     * @param resultHandler the resultHandler to set
     */
    public void setResultHandler(ResultHandler resultHandler) {
        this.resultHandler = resultHandler;
    }

}
