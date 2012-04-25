/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.submitter;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Utility class for DroidCore to count in-flight jobs.
 * @author rflitcroft
 *
 */
class JobCounter {

    private final Lock lock = new ReentrantLock();
    private final Condition idle  = lock.newCondition();

    private final AtomicInteger jobCount = new AtomicInteger(0);
    private final AtomicInteger postProcessCount = new AtomicInteger(0);
    
    /**
     * Increments the job counter.
     */
    void increment() {
        lock.lock();
        try {
            jobCount.incrementAndGet();
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Increments the post-process job counter.
     */
    void incrementPostProcess() {
        lock.lock();
        try {
            postProcessCount.incrementAndGet();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Decrements the job counter.
     */
    void decrement() {
        lock.lock();
        try {
            int cnt = jobCount.decrementAndGet();
            if (cnt == 0) {
                idle.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Decrements the post-process job counter.
     */
    void decrementPostProcess() {
        lock.lock();
        try {
            int cnt = postProcessCount.decrementAndGet();
            if (cnt == 0) {
                idle.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Waits for all job counters to become zero.
     * @throws InterruptedException if the thread waiting was interrupted
     */
    void awaitFinished() throws InterruptedException {
        lock.lock();
        try {
            while (jobCount.get() > 0 || postProcessCount.get() > 0) {
                idle.await();
            }
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Waits for the jobCount to become zero.
     * @throws InterruptedException if the thread waiting was interrupted
     */
    void awaitIdle() throws InterruptedException {
        lock.lock();
        try {
            while (jobCount.get() > 0) {
                idle.await();
            }
        } finally {
            lock.unlock();
        }
    }
}
