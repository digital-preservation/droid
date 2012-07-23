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
package uk.gov.nationalarchives.droid.core.interfaces;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author rflitcroft
 * Executor service which which will block new tasks until a thread is available to service a task.
 */
public class BlockingThreadPoolExecutorFactory {

    private static final int DEFAULT_CORE_POOL_SIZE = 10;
    private static final int DEFAULT_MAX_CORE_POOL_SIZE = 10;
    
    //private static final int DEFAULT_CORE_POOL_SIZE = 4;
    //private static final int DEFAULT_MAX_CORE_POOL_SIZE = 4;
    
    private static final int DEFAULT_KEEP_ALIVE_TIME_MILLIS = 1000;
    
    private int corePoolSize = DEFAULT_CORE_POOL_SIZE;
    private int maxPoolSize = DEFAULT_MAX_CORE_POOL_SIZE;
    private long keepAliveTimeMillis = DEFAULT_KEEP_ALIVE_TIME_MILLIS;
    
    /**
     * Returns a new instance of a BlockingThreadPoolExecutor.
     * @return a new BlockingThreadPoolExecutor
     */
    public ThreadPoolExecutor newInstance() {
        
        final BlockingQueue<Runnable> workQueue = new SynchronousQueue<Runnable>();
        RejectedExecutionHandler rejectedExecutionHandler = new CallerRunsUnlessPoolShutdownPolicy();
        
        ThreadFactory tf = new MyThreadFactory();
        
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                corePoolSize, maxPoolSize, keepAliveTimeMillis, TimeUnit.MILLISECONDS, 
                workQueue, tf, rejectedExecutionHandler);
        return executor;
    }
    
    private static final class CallerRunsUnlessPoolShutdownPolicy implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            
            if (executor.isShutdown()) {
                throw new RejectedExecutionException("Executor has been shut down.");
            } 
            
            r.run();
        }
    }
    
    private static final class MyThreadFactory implements ThreadFactory {
        
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        private MyThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = "core-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                                  namePrefix + threadNumber.getAndIncrement(),
                                  0);
            
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }

    /**
     * @param corePoolSize the corePoolSize to set
     */
    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    /**
     * @param maxPoolSize the maxPoolSize to set
     */
    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    /**
     * @param keepAliveTimeMillis the keepAliveTimeMillis to set
     */
    public void setKeepAliveTimeMillis(long keepAliveTimeMillis) {
        this.keepAliveTimeMillis = keepAliveTimeMillis;
    }

    /**
     * @param workQueueSize the workQueueSize to set
     */
    public void setWorkQueueSize(int workQueueSize) {
    }
}
