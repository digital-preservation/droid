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
package uk.gov.nationalarchives.droid.submitter;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.nationalarchives.droid.core.interfaces.AsynchDroid;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationErrorType;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationException;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.ResourceId;
import uk.gov.nationalarchives.droid.core.interfaces.ResultHandler;
import uk.gov.nationalarchives.droid.core.interfaces.archive.IdentificationRequestFactory;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;
import uk.gov.nationalarchives.droid.profile.throttle.SubmissionThrottle;
import uk.gov.nationalarchives.droid.util.FileUtil;

/**
 * @author rflitcroft
 * 
 */
public class FileEventHandler {

    private static final int URI_STRING_BUILDER_CAPACITY = 1024;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private AsynchDroid droidCore;
    private ResultHandler resultHandler;
    private IdentificationRequestFactory<Path> requestFactory;

    private SubmissionThrottle submissionThrottle;

    private StringBuilder uriStringBuilder = new StringBuilder(URI_STRING_BUILDER_CAPACITY);

    /**
     * Default Constructor.
     */
    public FileEventHandler() { }
    
    
    /**
     * @param droidCore
     *            an identification engine for this event handler to submit to
     */
    public FileEventHandler(AsynchDroid droidCore) {
        this.droidCore = droidCore;
    }

    /**
     * Creates a job in the database and submits the job to the identification
     * engine.
     * 
     * @param file
     *            the node file to handle
     * @param parentId
     *            the ID of the node's parent
     * @param nodeId
     *            an optional node ID for the request.
     */
    public void onEvent(final Path file, ResourceId parentId, ResourceId nodeId) {

        URI uri = SubmitterUtils.toURI(file.toFile(), uriStringBuilder);
        final FileTime lastModified = FileUtil.lastModifiedQuietly(file);
        RequestMetaData metaData = new RequestMetaData(
                FileUtil.sizeQuietly(file),
                lastModified == null ? new Date(0).getTime() : new Date(lastModified.toMillis()).getTime(),
                FileUtil.fileName(file));

        RequestIdentifier identifier = new RequestIdentifier(uri);
        identifier.setParentResourceId(parentId);
        identifier.setResourceId(nodeId);
        IdentificationRequest<Path> request = requestFactory.newRequest(metaData, identifier);
        try {
            request.open(file);
            droidCore.submit(request);
            submissionThrottle.apply();
        } catch (IOException e) {
            IdentificationErrorType error = Files.exists(file) ? IdentificationErrorType.ACCESS_DENIED
                    : IdentificationErrorType.FILE_NOT_FOUND;
            if (error.equals(IdentificationErrorType.ACCESS_DENIED)) {
                log.warn(String.format("Access was denied to the file: [%s]", file.toAbsolutePath().toString()));
            } else {
                log.warn(String.format("File not found: [%s]", file.toAbsolutePath().toString()));
            }
            resultHandler.handleError(new IdentificationException(request, error, e));
        } catch (InterruptedException e) {
            log.debug("Interrupted while throttle active.", e);
        }
    }

    /**
     * @return the submission throttle
     */
    public SubmissionThrottle getSubmissionThrottle() {
        return submissionThrottle;
    }
    
    /**
     * @param submissionThrottle the submissionThrottle to set
     */
    public void setSubmissionThrottle(SubmissionThrottle submissionThrottle) {
        this.submissionThrottle = submissionThrottle;
    }
    
    /**
     * @param droidCore the droidCore to set
     */
    public void setDroidCore(AsynchDroid droidCore) {
        this.droidCore = droidCore;
    }

    /**
     * @param resultHandler the resultHandler to set
     */
    public void setResultHandler(ResultHandler resultHandler) {
        this.resultHandler = resultHandler;
    }
    
    /**
     * @param requestFactory the requestFactory to set
     */
    public void setRequestFactory(IdentificationRequestFactory requestFactory) {
        this.requestFactory = requestFactory;
    }

}
