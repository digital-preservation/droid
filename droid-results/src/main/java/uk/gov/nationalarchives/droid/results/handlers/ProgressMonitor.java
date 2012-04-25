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

import uk.gov.nationalarchives.droid.profile.ProfileResourceNode;
import uk.gov.nationalarchives.droid.profile.ProfileResultObserver;

/**
 * @author rflitcroft
 * 
 */
public interface ProgressMonitor {

    /** value for indeterminate answer. */
    int INDETERMINATE_PROGRESS = -1;

    /**
     * Sets the maximum count.
     * 
     * @param count
     *            the maximum count to set
     */
    void setTargetCount(long count);

    /**
     * Gets the progress percentage: -1 indicates indeterminate progress.
     * 
     * @return the progress as a percentage.
     * 
     */
    int getProgressPercentage();

    /**
     * @return the maximum count.
     */
    long getProfileSize();

    /**
     * 
     * @return the number of identifications made.
     */
    long getIdentificationCount();

    /**
     * Sets an observer to fired whenever the progress percentage increments.
     * 
     * @param observer
     *            the observer to set
     */
    void setPercentIncrementObserver(ProgressObserver observer);

    /**
     * @param uri
     *            the URI of the job.
     */
    void startJob(URI uri);

    /**
     * @param uri
     *            the URI of the job
     */
    void stopJob(ProfileResourceNode uri);

    /**
     * @param resultObserver ResultObserver.
     */
    void setResultObserver(ProfileResultObserver resultObserver);

    /**
     * Initialised the progress monitor to some initial state.
     * @param targetCount the target count
     * @param currentCount the actual count
     */
    void initialise(long targetCount, long currentCount);

    /**
     * @return the target number of identifications (100%)
     */
    long getTargetCount();
    
}
