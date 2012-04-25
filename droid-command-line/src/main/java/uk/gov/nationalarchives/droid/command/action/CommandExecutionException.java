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
public class CommandExecutionException extends CommandLineException {

    private static final long serialVersionUID = -2068800893049061720L;

    /**
     * @param cause the cause 
     */
    public CommandExecutionException(Throwable cause) {
        super(cause);
    }
    
    /**
     * @param message error message 
     */
    public CommandExecutionException(String message) {
        super(message);
    }
    
    /**
     * @param message error message 
     * @param cause the cause 
     */
    public CommandExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
