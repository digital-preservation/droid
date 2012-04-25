/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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
