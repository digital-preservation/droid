/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.export.interfaces;

/**
 * @author rflitcroft
 *
 */
public class JobCancellationException extends Exception {

    private static final long serialVersionUID = 1782092384719974757L;

    /**
     * 
     * @param message the message
     */
    public JobCancellationException(String message) {
        super(message);
    }
}
