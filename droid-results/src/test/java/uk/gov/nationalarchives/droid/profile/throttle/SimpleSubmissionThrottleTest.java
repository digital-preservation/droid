/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.profile.throttle;

import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertThat;

import org.apache.commons.lang.time.StopWatch;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.gov.nationalarchives.droid.core.interfaces.config.RuntimeConfig;

/**
 * @author rflitcroft
 *
 */
public class SimpleSubmissionThrottleTest {

    private SimpleSubmissionThrottle throttle;
    
    @BeforeClass
    public static void setupEnv() {
        RuntimeConfig.configureRuntimeEnvironment();
    }
    
    @AfterClass
    public static void tearDownEnv() {
        System.clearProperty(RuntimeConfig.DROID_USER);
    }

    @Before
    public void setup() {
        throttle = new SimpleSubmissionThrottle();
    }
    
    @Test
    public void testThrottlingWith100msWait() throws Exception {
        throttle.setWaitMilliseconds(100);
        
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        throttle.apply();
        stopWatch.stop();
        assertThat((double) stopWatch.getTime(), closeTo(100L, 60L));
        
    }

    @Test
    public void testThrottlingWithNoWait() throws Exception {
        throttle.setWaitMilliseconds(0);
        
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        throttle.apply();
        stopWatch.stop();
        assertThat((double) stopWatch.getTime(), closeTo(0L, 5L));
    }

    
    /* This test is not reliable.  Sometimes it really does take longer than 100
     * milliseconds to return, even if the calling thread is interupted.
     * Could adjust the time value range expected as valid... but then, what
     * is this test testing?
     * 
    @Test
    public void testThrottlingWhenCallingThreadIsInterrupted() throws InterruptedException {
        throttle.setWaitMilliseconds(100);
        
        final StopWatch stopWatch = new StopWatch();
        
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    throttle.apply();
                } catch (InterruptedException e) {
                }
            }
        };
        stopWatch.start();
        t.start();
        
        Thread.sleep(50);
        t.interrupt();
        stopWatch.stop();
        assertThat(stopWatch.getTime(), lessThan(100L));
        
    }
    */
}
