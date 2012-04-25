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
public class CommandLineSyntaxException extends CommandLineException {

    private static final long serialVersionUID = 1976287275805163230L;

    /**
     * @param message the message
     */
    public CommandLineSyntaxException(String message) {
        super(message);
    }
    
    /**
     * @param cause the cause
     */
    public CommandLineSyntaxException(Throwable cause) {
        super(cause);
    }
}
