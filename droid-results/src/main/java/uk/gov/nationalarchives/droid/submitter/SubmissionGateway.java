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
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.nationalarchives.droid.core.interfaces.AsynchDroid;
import uk.gov.nationalarchives.droid.core.interfaces.DroidCore;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationErrorType;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationException;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.ResourceId;
import uk.gov.nationalarchives.droid.core.interfaces.ResultHandler;
import uk.gov.nationalarchives.droid.core.interfaces.archive.ArchiveFormatResolver;
import uk.gov.nationalarchives.droid.core.interfaces.archive.ArchiveHandler;
import uk.gov.nationalarchives.droid.core.interfaces.archive.ArchiveHandlerFactory;
import uk.gov.nationalarchives.droid.core.interfaces.archive.ContainerIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.archive.ContainerIdentifierFactory;
import uk.gov.nationalarchives.droid.core.interfaces.control.PauseAspect;
import uk.gov.nationalarchives.droid.core.interfaces.hash.HashGenerator;

/**
 * Acts as a DroidCore proxy by keeping track of in-flight identification
 * requests. Requests are removed from the queue when the droid ID task finishes
 * All requests should come through this pipeline.
 * @author rflitcroft
 *
 *
 */
//CHECKSTYLE:OFF - fan out complexity too high.
public class SubmissionGateway implements AsynchDroid {
    /**
     *
     */
    private static final String CONTAINER_ERROR = "Could not process the potential container format (%s): %s\t%s\t%s";

    /**
     *
     */
    private static final String ARCHIVE_ERROR = "Could not process the archival format(%s): %s\t%s\t%s";

    //CHECKSTYLE:ON    
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final JobCounter jobCounter = new JobCounter();
    private DroidCore droidCore;
    private ResultHandler resultHandler;
    private ExecutorService executorService;
    private boolean processZip;
    private boolean processTar;
    private boolean processGzip;
    private boolean processRar;
    private boolean process7zip;
    private boolean processIso;
    private boolean processBzip2;
    private boolean processArc;
    private boolean processWarc;
    private ArchiveFormatResolver archiveFormatResolver;
    private ArchiveFormatResolver containerFormatResolver;
    private ArchiveHandlerFactory archiveHandlerFactory;
    private ContainerIdentifierFactory containerIdentifierFactory;
    private HashGenerator hashGenerator;
    private String hashAlgorithm;
    private boolean generateHash;
    private boolean matchAllExtensions;
    private long maxBytesToScan = -1;
    private SubmissionQueue submissionQueue;
    private ReplaySubmitter replaySubmitter;
    private PauseAspect pauseControl;

    private Set<IdentificationRequest> requests = Collections.synchronizedSet(new HashSet<IdentificationRequest>());

    /**
     * Empty bean constructor.
     */
    public SubmissionGateway() {
    }

    //CHECKSTYLE:OFF - too many parameters - but this is how many you need to instantiate safely.
    /**
     * Parameterized constructor.
     *
     * @param droidCore The droid core to use.
     * @param resultHandler The result handler.
     * @param executorService The executor service.
     * @param archiveFormatResolver The archive format resolver.
     * @param containerFormatResolver The container format resolver.
     * @param archiveHandlerFactory The archive handler factory.
     * @param containerFactory The container identifier factory.
     * @param pauseControl the PauseAspect to use.
     * @param replaySubmitter the ReplaySubmitter to use.
     * @param maxBytesToScan the bytes to scan at the start and end of a file, or negative if unlimited.
     */
    public SubmissionGateway(DroidCore droidCore, ResultHandler resultHandler, ExecutorService executorService,
                             ArchiveFormatResolver archiveFormatResolver, ArchiveFormatResolver containerFormatResolver,
                             ArchiveHandlerFactory archiveHandlerFactory, ContainerIdentifierFactory containerFactory,
                             PauseAspect pauseControl, ReplaySubmitter replaySubmitter, long maxBytesToScan) {
        setDroidCore(droidCore);
        setResultHandler(resultHandler);
        setExecutorService(executorService);
        setArchiveFormatResolver(archiveFormatResolver);
        setContainerFormatResolver(containerFormatResolver);
        setArchiveHandlerFactory(archiveHandlerFactory);
        setContainerIdentifierFactory(containerFactory);
        setPauseAspect(pauseControl);
        setReplaySubmitter(replaySubmitter);
        setMaxBytesToScan(maxBytesToScan);

        setProcess7zip(processZip);
        setProcessTar(processTar);
        setProcessGzip(processGzip);
        setProcessRar(processRar);
        setProcess7zip(process7zip);
        setProcessIso(processIso);
        setProcessBzip2(processBzip2);
        setProcessArc(processArc);
        setProcessWarc(processWarc);

        setMatchAllExtensions(matchAllExtensions);
        setGenerateHash(generateHash);
    }
    //CHECKSTYLE:ON

    /**
     * {@inheritDoc}
     */
    @Override
    public Future<IdentificationResultCollection> submit(final IdentificationRequest request) {
        pauseControl.awaitUnpaused();
        jobCounter.increment();
        requests.add(request);

        // old code blocking identification:
        Callable<IdentificationResultCollection> callable = new Callable<IdentificationResultCollection>() {
            @Override
            public IdentificationResultCollection call() throws IOException {
                droidCore.setMaxBytesToScan(maxBytesToScan);
                IdentificationResultCollection results = droidCore.matchBinarySignatures(request);
                return results;
            }
        };

        FutureTask<IdentificationResultCollection> task = new SubmissionFutureTask(callable, request);
        executorService.submit(task);
        return task;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void replay() {
        replaySubmitter.replay();
    }

    private void generateHash(IdentificationRequest request) throws IOException {
        if (generateHash) {
            try {
                InputStream in = request.getSourceInputStream();
                try {
                    String hash = hashGenerator.hash(in);
                    request.getRequestMetaData().setHash(hash);
                } finally {
                    if (in != null) {
                        in.close();
                    }
                }
                //CHECKSTYLE:OFF - generating a hash can't prejudice any other results
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            //CHECKSTYLE:ON
        }
    }

    private IdentificationResultCollection handleExtensions(IdentificationRequest request,
                                                            IdentificationResultCollection results) {
        IdentificationResultCollection extensionResults = results;
        try {
            List<IdentificationResult> resultList = results.getResults();
            if (resultList != null && resultList.isEmpty()) {
                // If we call matchExtensions with "true", it will match
                // ALL files formats which have a given extension.
                // If "false", it will only match file formats for which
                // there is no other signature defined.
                IdentificationResultCollection checkExtensionResults =
                        droidCore.matchExtensions(request, matchAllExtensions);
                if (checkExtensionResults != null) {
                    extensionResults = checkExtensionResults;
                }
            } else {
                droidCore.checkForExtensionsMismatches(extensionResults,
                        request.getExtension());
            }
            //CHECKSTYLE:OFF - do not allow any errors in other code to
            //    prevent results so far from being recorded.
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        //CHECKSTYLE:ON
        return extensionResults;
    }

    /**
     * @param request  The archive request to handle.
     * @param results The previous identification results for the archive format.
     * @return
     */
    private void handleArchive(IdentificationRequest request,
                               IdentificationResultCollection results, String archiveFormat) {
        results.setArchive(true);
        ResourceId id = resultHandler.handle(results);
        jobCounter.incrementPostProcess();
        RequestIdentifier identifier = request.getIdentifier();
        identifier.setResourceId(id);
        if (identifier.getAncestorId() == null) {
            identifier.setAncestorId(id.getId());
        }
        submissionQueue.add(request.getIdentifier());
        jobCounter.decrement();
        try {
            //BNO: Does this always return the same archive handler for any given container format?
            //And will it end up using the same submission gateway, or a new one with a different thread pool?
            ArchiveHandler handler = archiveHandlerFactory.getHandler(archiveFormat);
            handler.handle(request);
            // CHECKSTYLE:OFF
        } catch (Exception e) {
            // CHECKSTYLE:ON
            String causeMessage = "";
            if (e.getCause() != null) {
                causeMessage = e.getCause().getMessage();
            }
            final String message = String.format(ARCHIVE_ERROR,
                    archiveFormat, request.getIdentifier().getUri().toString(), e.getMessage(), causeMessage);
            if (log.isDebugEnabled()) {
                log.debug(message, e); // exception details included in debug.
            } else {
                log.warn(message); // Just the message in normal operation.
            }
            resultHandler.handleError(new IdentificationException(
                    request, IdentificationErrorType.OTHER, e));
        } finally {
            submissionQueue.remove(request.getIdentifier());
            jobCounter.decrementPostProcess();
        }
    }

    private IdentificationResultCollection handleContainer(IdentificationRequest request,
                                                           IdentificationResultCollection results)
            throws IOException {
        // process a container format (ole2, odf, ooxml etc)
        String containerFormat = getContainerFormat(results);
        try {
            if (containerFormatResolver != null && containerFormat != null) {
                ContainerIdentifier containerIdentifier = containerIdentifierFactory.getIdentifier(containerFormat);
                containerIdentifier.setMaxBytesToScan(maxBytesToScan);
                IdentificationResultCollection containerResults = containerIdentifier.submit(request);
                droidCore.removeLowerPriorityHits(containerResults);
                droidCore.checkForExtensionsMismatches(containerResults, request.getExtension());
                containerResults.setFileLength(request.size());
                containerResults.setRequestMetaData(request.getRequestMetaData());
                return containerResults.getResults().isEmpty() ? null : containerResults;
            }
            //CHECKSTYLE:OFF - rules say don't catch this, but other code keeps on throwing them.
            // Don't prejudice any results so far because other code isn't following 'the rules'.
        } catch (Exception e) {
            //CHECKSTYLE:ON
            String causeMessage = "";
            if (e.getCause() != null) {
                causeMessage = e.getCause().getMessage();
            }
            final String message = String.format(CONTAINER_ERROR,
                    containerFormat, request.getIdentifier().getUri().toString(), e.getMessage(), causeMessage);
            log.warn(message);
        }
        return null;
    }

    /**
     * @param results A previous identification of an archival format.
     * @return format or null
     */
    private String getArchiveFormat(IdentificationResultCollection results) {
        final List<IdentificationResult> theResults = results.getResults();
        final int numResults = theResults.size(); // use an indexed loop to reduce garbage, don't allocate an iterator.
        for (int i = 0; i < numResults; i++) {
            final IdentificationResult result = theResults.get(i);
            String format = archiveFormatResolver.forPuid(result.getPuid());
            if (format != null) { // exit on the first non-null format met
                if (isProcessedArchiveOrWebArchiveFormat(format)) {
                    format = null;
                }
                return format;
            }
        }

        return null;
    }

    //CHECKSTYLE:OFF - cyclomatic complexity too high.
    private boolean isProcessedArchiveOrWebArchiveFormat(String format) {
        return "ZIP".equals(format) && !processZip
                || "TAR".equals(format) && !processTar
                || "GZ".equals(format) && !processGzip
                || "RAR".equals(format) && !processRar
                || "7Z".equals(format) && !process7zip
                || "ISO".equals(format) && !processIso
                || "BZ".equals(format) && !processBzip2
                || "ARC".equals(format) && !processArc
                || "WARC".equals(format) && !processWarc;
    }
    //CHECKSTYLE:ON


    /**
     *
     * @param format
     * @return true if a Web Archive format
     */
    private Boolean isWebArchiveFormat(String format) {
        return "ARC".equals(format) || "WARC".equals(format);
    }

    private String getContainerFormat(IdentificationResultCollection results) {
        final List<IdentificationResult> theResults = results.getResults();
        final int numResults = theResults.size(); // use an indexed loop to reduce garbage, don't allocate an iterator.
        for (int i = 0; i < numResults; i++) {
            final IdentificationResult result = theResults.get(i);
            final String format = containerFormatResolver.forPuid(result.getPuid());
            if (format != null) {
                return format;
            }
        }

        return null;
    }

    /**
     * Waits until all in-process jobs have completed.
     * @throws InterruptedException if the calling thread was interrupted.
     */
    @Override
    public void awaitIdle() throws InterruptedException {
        jobCounter.awaitIdle();
    }

    /**
     * Waits until the job queue is empty AND all sub-tasks (archives etc.) have finished.
     * @throws InterruptedException if the calling thread was interrupted.
     */
    @Override
    public void awaitFinished() throws InterruptedException {
        jobCounter.awaitFinished();
    }

    /**
     * @param archiveFormatResolver the archiveFormatResolver to set
     */
    public void setArchiveFormatResolver(ArchiveFormatResolver archiveFormatResolver) {
        this.archiveFormatResolver = archiveFormatResolver;
    }

    /**
     * @param archiveHandlerFactory the archiveHandlerFactory to set
     */
    public void setArchiveHandlerFactory(ArchiveHandlerFactory archiveHandlerFactory) {
        this.archiveHandlerFactory = archiveHandlerFactory;
    }

    /**
     * @param containerFormatResolver the containerFormatResolver to set
     */
    public void setContainerFormatResolver(ArchiveFormatResolver containerFormatResolver) {
        this.containerFormatResolver = containerFormatResolver;
    }

    /**
     * @param containerIdentifierFactory the containerIdentifierFactory to set
     */
    public void setContainerIdentifierFactory(ContainerIdentifierFactory containerIdentifierFactory) {
        this.containerIdentifierFactory = containerIdentifierFactory;
    }

    /**
     * @param droidCore the droidCore to set
     */
    public void setDroidCore(DroidCore droidCore) {
        this.droidCore = droidCore;
    }

    /**
     * @param executorService the executorService to set
     */
    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    /**
     * @param processZip set whether to process Zip files
     */
    public void setProcessZip(boolean processZip) {
        this.processZip = processZip;
    }

    /**
     * @param processTar set whether to process Tar files
     */
    public void setProcessTar(boolean processTar) {
        this.processTar = processTar;
    }

    /**
     * @param processGzip set whether to process Gzip files
     */
    public void setProcessGzip(boolean processGzip) {
        this.processGzip = processGzip;
    }

    /**
     * @param processRar set whether to process Rar files
     */
    public void setProcessRar(boolean processRar) {
        this.processRar = processRar;
    }

    /**
     * @param process7zip set whether to process 7zip files
     */
    public void setProcess7zip(boolean process7zip) {
        this.process7zip = process7zip;
    }

    /**
     * @param processIso set whether to process Iso files
     */
    public void setProcessIso(boolean processIso) {
        this.processIso = processIso;
    }

    /**
     * @param processBzip2 set whether to process Bzip2 files
     */
    public void setProcessBzip2(boolean processBzip2) {
        this.processBzip2 = processBzip2;
    }


    /**
     * @param processArc set whether to process Arc files
     */
    public void setProcessArc(boolean processArc) {
        this.processArc = processArc;
    }

    /**
     * @param processWarc set whether to process Warc files
     */
    public void setProcessWarc(boolean processWarc) {
        this.processWarc = processWarc;
    }

    /**
     * @param resultHandler the resultHandler to set
     */
    public void setResultHandler(ResultHandler resultHandler) {
        this.resultHandler = resultHandler;
    }

    /**
     * @param submissionQueue the submissionQueue to set
     */
    public void setSubmissionQueue(SubmissionQueue submissionQueue) {
        this.submissionQueue = submissionQueue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void save() {
        resultHandler.commit(); // flush any remaining entities out to the database.
        submissionQueue.save();
    }

    /**
     * @param replaySubmitter the replaySubmitter to set
     */
    public void setReplaySubmitter(ReplaySubmitter replaySubmitter) {
        this.replaySubmitter = replaySubmitter;
    }

    /**
     * @param pauseAspect The pauseAspect to use.
     */
    public void setPauseAspect(PauseAspect pauseAspect) {
        this.pauseControl = pauseAspect;
    }

    /**
     * @param hashGenerator the hashGenerator to set
     */
    public void setHashGenerator(HashGenerator hashGenerator) {
        this.hashGenerator = hashGenerator;
    }

    /**
     * @param generateHash the generateHash to set
     */
    public void setGenerateHash(boolean generateHash) {
        this.generateHash = generateHash;
    }

    /**
     * @param hashAlgorithm the algorithm to set
     */
    public void setHashAlgorithm(String hashAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
    }

    /**
     * Shuts down the executor service and closes any in-flight requests.
     * @throws IOException if temp files could not be deleted.
     */
    public void close() throws IOException {
        executorService.shutdownNow();
        for (IdentificationRequest request : requests) {
            request.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMaxBytesToScan(long maxBytesToScan) {
        this.maxBytesToScan = maxBytesToScan;
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public void setMatchAllExtensions(boolean matchAllExtensions) {
        this.matchAllExtensions = matchAllExtensions;
    }

    private final class SubmissionFutureTask extends FutureTask<IdentificationResultCollection> {

        private IdentificationRequest request;

        SubmissionFutureTask(Callable<IdentificationResultCollection> callable, IdentificationRequest request) {
            super(callable);
            this.request = request;
        }

        @Override
        protected void done() {
            boolean jobCountDecremented = false;
            try {
                generateHash(request);
                IdentificationResultCollection results = get();
                IdentificationResultCollection containerResults = handleContainer(request, results);
                if (containerResults == null) {
                    // no container results - process the normal results.
                    droidCore.removeLowerPriorityHits(results);
                    results = handleExtensions(request, results);

                    // Are we processing archive formats?
                    String archiveFormat = null;
                    if (archiveFormatResolver != null) {
                        archiveFormat = getArchiveFormat(results);
                    }
                    if (archiveFormat != null) {
                        handleArchive(request, results, archiveFormat);
                        jobCountDecremented = true;
                    } else { // just process the results so far:
                        results.setArchive(getArchiveFormat(results) != null);
                        ResourceId id = resultHandler.handle(results);
                        request.getIdentifier().setResourceId(id);
                    }
                } else { // we have possible container formats:
                    droidCore.removeLowerPriorityHits(containerResults);
                    containerResults = handleExtensions(request, containerResults);
                    ResourceId id = resultHandler.handle(containerResults);
                    request.getIdentifier().setResourceId(id);
                }
            } catch (ExecutionException e) {
                final Throwable cause = e.getCause();
                log.error(cause.getStackTrace().toString(), cause);
                resultHandler.handleError(new IdentificationException(
                        request, IdentificationErrorType.OTHER, cause));
            } catch (InterruptedException e) {
                log.debug(e.getMessage(), e);
            } catch (IOException e) {
                resultHandler.handleError(new IdentificationException(
                        request, IdentificationErrorType.OTHER, e));
            } finally {
                closeRequest();
                if (!jobCountDecremented) {
                    jobCounter.decrement();
                }
            }
        }

        private void closeRequest() {
            requests.remove(request);
            try {
                request.close();
            } catch (IOException e) {
                log.error(String.format("Error closing request [%s]", request.getIdentifier().getUri()), e);
            }
        }
    }

}
