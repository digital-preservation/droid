/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.profile.throttle;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author rflitcroft
 *
 */
public class SimpleSubmissionThrottle implements SubmissionThrottle {

    private AtomicInteger waitMilliseconds = new AtomicInteger();
    private boolean enabled;
    
    /**
     * @param waitMilliseconds the time to wait.
     */
    @Override
    public void setWaitMilliseconds(int waitMilliseconds) {
        this.waitMilliseconds.set(waitMilliseconds);
        enabled = waitMilliseconds > 0;
    }
    
    @Override
    public void apply() throws InterruptedException {
        if (enabled) {
            Thread.sleep(waitMilliseconds.get());
        }
    }

}
