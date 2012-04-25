/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.submitter;

import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;



/**
 * @author rflitcroft
 *
 */
public interface SubmissionQueue {

    /**
     * Adds a request to the queue.
     * @param request the request to add
     */
    void add(RequestIdentifier request);
    
    /**
     * Removes a request from the queue.
     * @param request the request to remove
     */
    void remove(RequestIdentifier request);
    
    /**
     * Flushes the queue to persistent storage.
     */
    void save();
    
    /**
     * @return lists all replay data.
     */
    SubmissionQueueData list();
    
}
