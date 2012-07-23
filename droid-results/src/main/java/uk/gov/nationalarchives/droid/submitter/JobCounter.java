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
