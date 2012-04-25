/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.core.interfaces.control;

/**
 * @author rflitcroft
 *
 */
public interface ThreadWaitingHandler {

    /**
     * Invoked by the thread which is being kept waiting by the pause control.
     */
    void onThreadWaiting();
}
