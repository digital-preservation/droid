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
