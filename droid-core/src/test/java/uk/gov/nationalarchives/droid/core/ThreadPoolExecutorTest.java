/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.core;


import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import uk.gov.nationalarchives.droid.core.interfaces.BlockingThreadPoolExecutorFactory;

/**
 * @author rflitcroft
 *
 */
public class ThreadPoolExecutorTest {

    private CountDownLatch startLatch;
    private CountDownLatch stopLatch;
    private BlockingThreadPoolExecutorFactory factory;

    @Before
    public void setup() {
        startLatch = new CountDownLatch(11);
        stopLatch = new CountDownLatch(1);
        factory = new BlockingThreadPoolExecutorFactory();
    }
    
    @Test
    public void testPoolBlocksWhenMoreThanTenTasksAreQueued() throws Exception {
        factory.setCorePoolSize(10);
        factory.setMaxPoolSize(10);
        factory.setWorkQueueSize(5);
        factory.setKeepAliveTimeMillis(1000);
        
        final ThreadPoolExecutor executor = factory.newInstance();
        
        // Saturate the thread pool
        final AtomicInteger count = new AtomicInteger();
        Thread t = new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < 22; i++) {
                    Callable<Object> task = new TestTask();
                    executor.submit(task);
                    count.incrementAndGet();
                }
            }
        };
        t.start();
        
        startLatch.await();
        assertEquals(0, executor.getQueue().size());
        
        // We expect 10 tasks running
        assertEquals(10, count.intValue());
        
        stopLatch.countDown();
        executor.awaitTermination(100, TimeUnit.MILLISECONDS);
        assertEquals(0, executor.getQueue().size());
        assertEquals(22, count.intValue());
        
    }
    
    @Test
    public void testPoolRejectsTasksAfterShutdown() throws Exception {
        factory.setCorePoolSize(10);
        factory.setMaxPoolSize(10);
        factory.setWorkQueueSize(5);
        factory.setKeepAliveTimeMillis(1000);
        
        final ThreadPoolExecutor executor = factory.newInstance();
        
        // Saturate the thread pool
        final AtomicInteger count = new AtomicInteger();
        Thread t = new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < 22; i++) {
                    Callable<Object> task = new TestTask();
                    executor.submit(task);
                    count.incrementAndGet();
                }
            }
        };
        t.start();
        
        startLatch.await();
        assertEquals(0, executor.getQueue().size());
        
        // We expect 10 tasks running
        assertEquals(10, count.intValue());
        
        executor.shutdown();
        try {
            executor.submit(new TestTask());
            fail("Expected RejectedExecutionException.");
        } catch (RejectedExecutionException e) {
            
        }
        
        stopLatch.countDown();
        executor.awaitTermination(100, TimeUnit.MILLISECONDS);
        assertEquals(0, executor.getQueue().size());
        // We expect the 10 tasks which have already been queued to have run.
        assertEquals(11, count.intValue());
        
    }
    
    @Test
    public void testExceptionHandling() throws Exception {
        
        factory.setCorePoolSize(10);
        factory.setMaxPoolSize(10);
        factory.setWorkQueueSize(5);
        factory.setKeepAliveTimeMillis(1000);
        
        final ThreadPoolExecutor executor = factory.newInstance();
        
        Callable<Object> task = new ExceptionTask();
        Future<?> result = executor.submit(task);
        
        try {
            result.get();
            fail("Expected ExecutionException");
        } catch (ExecutionException e) {
            assertEquals("An error...", e.getCause().getMessage());
        }
    }

    private final class TestTask implements Callable<Object> {
        
        @Override
        public Object call() {
            try {
                startLatch.countDown();
                stopLatch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            
            return null;
        }
    }

    private final class ExceptionTask implements Callable<Object> {
        
        @Override
        public Object call() {
            throw new RuntimeException("An error...");
        }
    }
}
