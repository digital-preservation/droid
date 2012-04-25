/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.profile.throttle;

/**
 * @author rflitcroft
 *
 */
public interface SubmissionThrottle {

    /**
     * Applies the throttle, possibly causing the calling thread to wait. 
     * @throws InterruptedException if the calling thread was interrupted
     */
    void apply() throws InterruptedException;
    
    /**
     * @param waitMilliseconds the time to wait.
     */
    void setWaitMilliseconds(int waitMilliseconds);

}
