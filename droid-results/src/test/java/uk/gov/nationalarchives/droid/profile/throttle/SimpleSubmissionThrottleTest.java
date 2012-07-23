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
