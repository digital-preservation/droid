/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.command.action;

/**
 * @author rflitcroft
 *
 */
public abstract class CommandLineException extends Exception {

    private static final long serialVersionUID = -2870251728877769886L;

    /**
     * @param cause the cause
     */
    public CommandLineException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message the message
     */
    public CommandLineException(String message) {
        super(message);
    }

    /**
     * @param message the message
     * @param cause the cause
     */
    public CommandLineException(String message, Throwable cause) {
        super(message, cause);
    }


}
