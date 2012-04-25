/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.report.interfaces;

import uk.gov.nationalarchives.droid.results.handlers.ProgressObserver;

/**
 * @author rflitcroft
 *
 */
public interface CancellableProgressObserver extends ProgressObserver {

    /**
     * @return true if cancel was invoked; false otherwise; 
     */
    boolean isCancelled();
    
    /**
     * Invoke this to signal a cancellation.
     */
    void cancel();

}
