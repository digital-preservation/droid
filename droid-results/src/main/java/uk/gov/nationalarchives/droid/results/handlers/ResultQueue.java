/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.results.handlers;

import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;


/**
 * @author rflitcroft
 *
 */
public interface ResultQueue {

    /**
     * Receives a result form the queue. Will block until the timeout is exceeded.
     * Will return null if the thread was interrupted.
     * @param timeout timeout in milliseconds.
     * @return the next queued result.
     */
    IdentificationResultCollection receive(long timeout);
}
