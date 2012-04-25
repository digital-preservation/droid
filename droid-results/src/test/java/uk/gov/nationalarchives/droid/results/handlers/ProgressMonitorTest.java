/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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
