/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.results.handlers;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import uk.gov.nationalarchives.droid.profile.ProfileResourceNode;
import uk.gov.nationalarchives.droid.profile.ProfileResultObserver;

/**
 * @author rflitcroft
 * 
 */
public class ProgressMonitorImpl implements ProgressMonitor {

    private static final int UNITY_PERCENT = 100;

    private volatile long count;
    private volatile int progressPercentage = INDETERMINATE_PROGRESS;
    private long target = INDETERMINATE_PROGRESS;

    private Set<URI> jobsInProgress = Collections.synchronizedSet(new HashSet<URI>());
    private ProgressObserver observer;
    private ProfileResultObserver resultObserver;

    /**
     * {@inheritDoc}
     */
    @Override
    public long getIdentificationCount() {
        return count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getTargetCount() {
        return target;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getProfileSize() {
        return target;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getProgressPercentage() {
        return progressPercentage;
    }

  /**
   * Increments the internal count.
   */
 
    synchronized void increment() {
        count++;
        if (target == ProgressMonitor.INDETERMINATE_PROGRESS) {
            progressPercentage = ProgressMonitor.INDETERMINATE_PROGRESS;
        } else {
            calcProgress();
        }
    }

    private void calcProgress() {
        if (target > 0) {
            // Avoid any / by zero problems...
            int oldProg = progressPercentage;
            int newProg = (int) (UNITY_PERCENT * count / target);
            if (newProg > UNITY_PERCENT) {
                newProg = UNITY_PERCENT;
            }
            progressPercentage = newProg;
            if (observer != null && newProg != oldProg) {
                observer.onProgress(progressPercentage);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void setTargetCount(long targetCount) {
        this.target = targetCount;
        calcProgress();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPercentIncrementObserver(ProgressObserver progressObserver) {
        this.observer = progressObserver;
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public void startJob(URI uri) {
        jobsInProgress.add(uri);
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public void stopJob(ProfileResourceNode node) {
        if (jobsInProgress.remove(node.getUri())) {
            increment();
        }
        
        if (resultObserver != null) {
            resultObserver.onResult(node);
        }
    }

    /**
     * @param resultObserver
     *            the resultObserver to set
     */
    @Override
    public void setResultObserver(ProfileResultObserver resultObserver) {
        this.resultObserver = resultObserver;
    }
    
    /**
     * Initialised the progress monitor to some initial state.
     * @param targetCount the target count
     * @param currentCount the actual count
     */
    public void initialise(long targetCount, long currentCount) {
        this.target = targetCount;
        this.count = currentCount;
        calcProgress();
    }
    
}
