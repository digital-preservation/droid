/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.core.interfaces.control;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

/**
 * @author rflitcroft
 *
 */
@Aspect
public class PauseAspect {

    private boolean isPaused;
    private ReentrantLock pauseLock = new ReentrantLock();
    private Condition unpaused = pauseLock.newCondition();
    
    private ThreadWaitingHandler threadWaitingHandler;

    /**
     * Blocks until the executor is not paused.
     */
    @Before("@annotation(uk.gov.nationalarchives.droid.core.interfaces.control.PauseBefore)")
    @After("@annotation(uk.gov.nationalarchives.droid.core.interfaces.control.PauseAfter)")
    public void awaitUnpaused() {
        pauseLock.lock();
        try {
            while (isPaused) {
                threadWaitingHandler.onThreadWaiting();
                unpaused.awaitUninterruptibly();
            }
        } finally {
            pauseLock.unlock();
        }
    }
    
    /**
     * Pauses the executor.
     */
    public void pause() {
        pauseLock.lock();
        try {
            isPaused = true;
        } finally {
            pauseLock.unlock();
        }
    }
    
    /**
     * Resumes the executor.
     */
    public void resume() {
        pauseLock.lock();
        try {
            isPaused = false;
            unpaused.signalAll();
        } finally {
            pauseLock.unlock();
        }
    }
    
    /**
     * @param threadWaitingHandler the threadWaitingHandler to set
     */
    public void setThreadWaitingHandler(ThreadWaitingHandler threadWaitingHandler) {
        this.threadWaitingHandler = threadWaitingHandler;
    }
    
    

}
