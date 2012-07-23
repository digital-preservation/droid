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
package uk.gov.nationalarchives.droid.profile;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Semaphore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import uk.gov.nationalarchives.droid.core.interfaces.AsynchDroid;
import uk.gov.nationalarchives.droid.core.interfaces.NodeStatus;
import uk.gov.nationalarchives.droid.core.interfaces.ResourceType;
import uk.gov.nationalarchives.droid.core.interfaces.control.PauseAspect;
import uk.gov.nationalarchives.droid.core.interfaces.control.ThreadWaitingHandler;
import uk.gov.nationalarchives.droid.core.interfaces.filter.Filter;
import uk.gov.nationalarchives.droid.core.interfaces.filter.expressions.Criterion;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileException;
import uk.gov.nationalarchives.droid.export.interfaces.ItemReader;
import uk.gov.nationalarchives.droid.planet.xml.dao.PlanetsXMLDao;
import uk.gov.nationalarchives.droid.planet.xml.dao.PlanetsXMLData;
import uk.gov.nationalarchives.droid.profile.referencedata.Format;
import uk.gov.nationalarchives.droid.profile.referencedata.ReferenceData;
import uk.gov.nationalarchives.droid.profile.referencedata.ReferenceDataService;
import uk.gov.nationalarchives.droid.profile.throttle.SubmissionThrottle;
import uk.gov.nationalarchives.droid.report.dao.GroupByField;
import uk.gov.nationalarchives.droid.report.dao.ReportDao;
import uk.gov.nationalarchives.droid.report.dao.ReportFieldEnum;
import uk.gov.nationalarchives.droid.report.dao.ReportLineItem;
import uk.gov.nationalarchives.droid.results.handlers.ProgressMonitor;
import uk.gov.nationalarchives.droid.signature.FormatCallback;
import uk.gov.nationalarchives.droid.signature.SaxSignatureFileParser;
import uk.gov.nationalarchives.droid.signature.SignatureParser;
import uk.gov.nationalarchives.droid.submitter.ProfileSpecJobCounter;
import uk.gov.nationalarchives.droid.submitter.ProfileSpecWalker;
import uk.gov.nationalarchives.droid.submitter.ProfileWalkState;
import uk.gov.nationalarchives.droid.submitter.ProfileWalkerDao;

/**
 * @author rflitcroft
 * 
 */
public class ProfileInstanceManagerImpl implements ProfileInstanceManager {

    private final Log log = LogFactory.getLog(getClass());

    private ProfileDao profileDao;
    private ReportDao reportDao;
    private PlanetsXMLDao planetsDao;
    private ReferenceDataService referenceDataService;

    private ProfileInstance profileInstance;

    private ProfileSpecWalker specWalker;
    private Future<?> task;
    private AsynchDroid submissionGateway;
    private ProfileWalkerDao profileWalkerDao;

    private ProfileWalkState walkState;
    private boolean inError;

    private PauseAspect pauseControl;

    private Semaphore submitterPermits = new Semaphore(1);
    private ThreadLocal<String> submitterThreadId = new ThreadLocal<String>();
    private volatile String pausedThreadId;

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancel() {
        log.info("**** Profile Cancelled ****");
        task.cancel(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void initProfile(URI signatureFileUri) throws SignatureFileException {
        // pre-populate with available file formats

        SignatureParser sigParser = new SaxSignatureFileParser(signatureFileUri);

        // Dummy format for 'no id'
        profileDao.saveFormat(Format.NULL);

        FormatCallback callback = new FormatCallback() {
            @Override
            public void onFormat(Format format) {
                profileDao.saveFormat(format);
            }
        };
        sigParser.formats(callback);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProfile(ProfileInstance profile) {
        profileInstance = profile;
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public void pause() {
        pauseControl.setThreadWaitingHandler(new ThreadWaitingHandler() {
            @Override
            public void onThreadWaiting() {
                pausedThreadId = submitterThreadId.get();
                if (profileInstance.getUuid().equals(pausedThreadId)) {
                    pausedThreadId = submitterThreadId.get();
                    submitterPermits.release();
                }
            }
        });

        pauseControl.pause();
        try {
            submitterPermits.acquire();
            submissionGateway.awaitIdle();
            profileInstance.stop();
            submissionGateway.save();

            ProgressState progressState = new ProgressState(getProgressMonitor().getTargetCount(), getProgressMonitor()
                    .getIdentificationCount());
            profileInstance.setProgress(progressState);
            profileWalkerDao.save(walkState);
        } catch (InterruptedException e) {
            log.debug(e);
        } finally {
            submitterPermits.release();
        }
    }

    /**
     * Resumes a paused profile.
     */
    private void resume() {
        if (pausedThreadId != null) {
            submitterPermits.acquireUninterruptibly();
        }
        pauseControl.resume();
        profileInstance.start();
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public Future<?> start() throws IOException {

        if (!inError && walkState != null) {
            resume();
        } else {
            if (inError) {
                if (pausedThreadId != null) {
                    submitterPermits.acquireUninterruptibly();
                }
                pauseControl.resume();
            }
            
            inError = false;
            walkState = profileWalkerDao.load();

            // replay any queued requests
            submissionGateway.replay();

            // start walking the profile spec
            profileInstance.start();
            // start a thread to estimate the number of jobs, and
            // update the progress monitor when it's done.
            final ProfileSpecJobCounter counter = new ProfileSpecJobCounter(profileInstance.getProfileSpec());
            final FutureTask<Long> countFuture = new FutureTask<Long>(counter) {
                @Override
                protected void done() {
                    if (!isCancelled()) {
                        try {
                            specWalker.getProgressMonitor().setTargetCount(get());
                        } catch (InterruptedException e) {
                            log.debug(e);
                        } catch (ExecutionException e) {
                            log.error(e);
                        }
                    }
                }
            };

            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(countFuture);

            ExecutorService mainSubmitter = Executors.newSingleThreadExecutor();

            Runnable walk = new WalkerTask(countFuture, counter);
            task = mainSubmitter.submit(walk);
            mainSubmitter.shutdown();
        }
        return task;

    }
    
    private final class WalkerTask implements Runnable {
        
        private ProfileSpecJobCounter counter;
        private Future<Long> countFuture;
        
        WalkerTask(Future<Long> countFuture, ProfileSpecJobCounter counter) {
            this.countFuture = countFuture;
            this.counter = counter;
        }
        
        @Override
        public void run() {
            try {
                preWalk();
                specWalker.walk(profileInstance.getProfileSpec(), walkState);
            } catch (InterruptedException e) {
                log.debug(e);
            } catch (IOException e) {
                inError = true;
                log.error(e);
                throw new ProfileException(e);
            } finally {
                postWalk();
                counter.cancel();
                countFuture.cancel(false);
                if (!inError) {
                    profileInstance.finish();
                }
                submissionGateway.save();
                profileWalkerDao.delete();
            }
        }
        
        private void preWalk() throws InterruptedException {
            submitterThreadId.set(profileInstance.getUuid());
            submitterPermits.acquire();
            ProgressMonitor progressMonitor = specWalker.getProgressMonitor();
            final ProgressState progress = profileInstance.getProgress();
            if (progress != null) {
                progressMonitor.initialise(progress.getTarget(), progress.getCount());
            }
        }

        private void postWalk() {
            submitterPermits.release();
            try {
                submissionGateway.awaitFinished();
            } catch (InterruptedException e) {
                log.debug(e);
            }
        }
    }

    /**
     * @param profileDao
     *            the profileDao to set
     */
    public void setProfileDao(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ProfileResourceNode> findRootProfileResourceNodes() {

        final Filter filter = profileInstance.getFilter();
        if (filter.isEnabled() && filter.hasCriteria()) {
            return findRootProfileResourceNodes(filter);
        }

        // Handle the root uri i.e. all root level resource nodes
        Map<URI, ProfileResourceNode> primordialNodes = new LinkedHashMap<URI, ProfileResourceNode>();

        for (AbstractProfileResource resource : profileInstance.getProfileSpec().getResources()) {
            ProfileResourceNode primordialNode = new ProfileResourceNode(resource.getUri());
            primordialNode.getMetaData().setResourceType(
                    resource.isDirectory() ? ResourceType.FOLDER : ResourceType.FILE);
            final NodeMetaData metaData = primordialNode.getMetaData();
            metaData.setNodeStatus(NodeStatus.NOT_DONE);
            metaData.setName(resource.getUri().getPath());

            primordialNodes.put(resource.getUri(), primordialNode);
        }

        List<ProfileResourceNode> processedNodes = profileDao.findProfileResourceNodes(null);
        for (ProfileResourceNode node : processedNodes) {
            primordialNodes.put(node.getUri(), node);
        }

        return new ArrayList<ProfileResourceNode>(primordialNodes.values());
    }

    /**
     */
    private List<ProfileResourceNode> findRootProfileResourceNodes(Filter filter) {

        // // Handle the root uri i.e. all root level resource nodes
        // Map<URI, ProfileResourceNode> primordialNodesBeforeFilter = new
        // LinkedHashMap<URI, ProfileResourceNode>();
        Map<URI, ProfileResourceNode> primordialNodesAfterFilter = new LinkedHashMap<URI, ProfileResourceNode>();
        //        
        // for (AbstractProfileResource resource :
        // profileInstance.getProfileSpec().getResources()) {
        // ProfileResourceNode primordialNode = new
        // ProfileResourceNode(resource.getUri());
        // primordialNode.setContainer(resource.isDirectory());
        // primordialNode.getMetaData().setLastModified(resource.getLastModifiedDate());
        // primordialNode.getMetaData().setName(resource.getName());
        // primordialNode.getMetaData().setSize(resource.getSize());
        // primordialNode.getMetaData().setExtension(resource.getExtension());
        // primordialNodesBeforeFilter.put(resource.getUri(), primordialNode);
        // }
        //        
        // FilterIterator filterIterator = new FilterIterator(
        // primordialNodesBeforeFilter.values().iterator(), new
        // FilterPredicate(filter));
        //        
        // while (filterIterator.hasNext()) {
        // ProfileResourceNode node = (ProfileResourceNode)
        // filterIterator.next();
        // primordialNodesAfterFilter.put(node.getUri(), node);
        // }

        List<ProfileResourceNode> processedNodes = profileDao.findProfileResourceNodes(null, filter);
        for (ProfileResourceNode node : processedNodes) {
            primordialNodesAfterFilter.put(node.getUri(), node);
        }

        return new ArrayList<ProfileResourceNode>(primordialNodesAfterFilter.values());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public List<ProfileResourceNode> findAllProfileResourceNodes(Long parentId) {
        final Filter filter = profileInstance.getFilter();
        if (filter.isEnabled() && filter.hasCriteria()) {
            return profileDao.findProfileResourceNodes(parentId, filter);
        }
        return profileDao.findProfileResourceNodes(parentId);
    }

    /**
     * @return the profileInstance
     */
    ProfileInstance getProfileInstance() {
        return profileInstance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setResultsObserver(ProfileResultObserver observer) {
        specWalker.getProgressMonitor().setResultObserver(observer);
    }

    @Override
    public List<Format> getAllFormats() {
        return profileDao.getAllFormats();
    }

    /**
     * @param referenceDataService
     *            the referenceDataService to set
     */
    public void setReferenceDataService(ReferenceDataService referenceDataService) {
        this.referenceDataService = referenceDataService;
    }

    /**
     * @return the referenceData
     */
    public ReferenceData getReferenceData() {
        return referenceDataService.getReferenceData();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setThrottleValue(int throttleValue) {
        final SubmissionThrottle submissionThrottle = specWalker.getFileEventHandler().getSubmissionThrottle();
        submissionThrottle.setWaitMilliseconds(throttleValue);
        profileInstance.setThrottle(throttleValue);
    }

    /**
     * Gets a resource node item reader. Spring will provide the implementation
     * via a method lookup. 
     * 
     * @return a new ItemReader
     */
    public ItemReader<ProfileResourceNode> getNodeItemReader() {
        return null;
    }

    /**
     * Gets data required for planets.
     * 
     * @return Planet xml data.
     */

    public PlanetsXMLData getPlanetsData() {
        return planetsDao.getDataForPlanetsXML(profileInstance.getFilter());
    }

    /**
     * Gets reports data for a profile.
     * 
     * @param filter
     *            Filter to be applied to the report data.
     * @param reportField
     *            reported field.
     * @param groupByFields
     *            A list of fields to group by, with associated grouping functions.
     * @return report data.
     */
    public List<ReportLineItem> getReportData(Criterion filter, ReportFieldEnum reportField,
            List<GroupByField> groupByFields) {
        return reportDao.getReportData(filter, reportField, groupByFields);
    }

    /**
     * @param planetsDao
     *            the planetsDao to set
     */
    public void setPlanetsDao(PlanetsXMLDao planetsDao) {
        this.planetsDao = planetsDao;
    }

    /**
     * @param reportDao
     *            the reportDao to set
     */
    public void setReportDao(ReportDao reportDao) {
        this.reportDao = reportDao;
    }

    /**
     * @param pauseControl
     *            the pauseControl to set
     */
    public void setPauseControl(PauseAspect pauseControl) {
        this.pauseControl = pauseControl;
    }

    /**
     * @param submissionGateway
     *            the submissionGateway to set
     */
    public void setSubmissionGateway(AsynchDroid submissionGateway) {
        this.submissionGateway = submissionGateway;
    }

    /**
     * @param profileWalkerDao
     *            the profileWalkerDao to set
     */
    public void setProfileWalkerDao(ProfileWalkerDao profileWalkerDao) {
        this.profileWalkerDao = profileWalkerDao;
    }

    /**
     * @param specWalker
     *            the specWalker to set
     */
    public void setSpecWalker(ProfileSpecWalker specWalker) {
        this.specWalker = specWalker;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProgressMonitor getProgressMonitor() {
        return specWalker.getProgressMonitor();
    }

}
