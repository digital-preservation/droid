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
package uk.gov.nationalarchives.droid.results.handlers;

import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * @author rflitcroft
 *
 */
public class ProgressMonitorTest {

    private ProgressMonitorImpl progressMonitor;
    
    @Before
    public void setup() {
        progressMonitor = new ProgressMonitorImpl();
    }
    
    @Test
    public void testProgressMonitorIsInitialisedCorrectly() {
        
        assertEquals(ProgressMonitor.INDETERMINATE_PROGRESS, progressMonitor.getProfileSize());
        assertEquals(ProgressMonitor.INDETERMINATE_PROGRESS, progressMonitor.getProgressPercentage());
        assertEquals(0, progressMonitor.getIdentificationCount());
        
    }
    
    @Test
    public void testIncrementUsingMultipleThreadsWhenMaxCountUnset() throws InterruptedException {
        
        final int threadCount = 20;
        
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch stopLatch = new CountDownLatch(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            Thread t = new LatchedThread(startLatch, stopLatch, 100);
            t.start();
        }
        assertEquals(ProgressMonitor.INDETERMINATE_PROGRESS, progressMonitor.getProfileSize());

        startLatch.countDown();
        stopLatch.await();
        
        assertEquals(2000, progressMonitor.getIdentificationCount());
        assertEquals(ProgressMonitor.INDETERMINATE_PROGRESS, progressMonitor.getProgressPercentage());
        
    }

    @Test
    public void testIncrementUsingMultipleThreadsWhenMaxCountIsSet() throws InterruptedException {
        
        final int threadCount = 20;
        
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch stopLatch = new CountDownLatch(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            Thread t = new LatchedThread(startLatch, stopLatch, 100);
            t.start();
        }
        
        progressMonitor.setTargetCount(2000);
        assertEquals(2000, progressMonitor.getProfileSize());
        assertEquals(0, progressMonitor.getProgressPercentage());
        
        startLatch.countDown();
        stopLatch.await();
        
        assertEquals(2000, progressMonitor.getIdentificationCount());
        assertEquals(100, progressMonitor.getProgressPercentage());
        
    }
    
    @Test
    public void testEventFiredForEveryOnePercentageIncrease() throws InterruptedException {
        
        final int threadCount = 20;
        progressMonitor.setTargetCount(2000);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch stopLatch = new CountDownLatch(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            Thread t = new LatchedThread(startLatch, stopLatch, 100);
            t.start();
        }
        
        ProgressObserver observer = mock(ProgressObserver.class);
        progressMonitor.setPercentIncrementObserver(observer);

        startLatch.countDown();
        stopLatch.await();

        assertEquals(100, progressMonitor.getProgressPercentage());
        
        ArgumentCaptor<Integer> progressCaptor = ArgumentCaptor.forClass(Integer.class);
        
        verify(observer, atMost(100)).onProgress(progressCaptor.capture());
        verify(observer).onProgress(100);
        
        Integer[] progresses = progressCaptor.getAllValues().toArray(new Integer[0]);
        for (int i = 1; i < progresses.length; i++) {
            Integer current = progresses[i];
            Integer previous = progresses[i - 1];
            assertTrue(String.format("Progress wrong: this [%d]; previous [%d]", current, previous),
                    current > previous);
        }
    }
    
    private final class LatchedThread extends Thread {
        
        private CountDownLatch start;
        private CountDownLatch stop;
        private int count;
        
        public LatchedThread(CountDownLatch start, CountDownLatch stop, int count) {
            this.start = start;
            this.stop = stop;
            this.count = count;
        }
        
        @Override
        public void run() {
            try {
                start.await();
                for (int i = 0; i < count; i++) {
                    progressMonitor.increment();
                }
            } catch (InterruptedException e) {
            }
            stop.countDown();
        }
    }

}
