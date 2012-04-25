/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.command.filter;

/**
 * @author rflitcroft
 *
 */
public class DqlParseException extends RuntimeException {

    private static final long serialVersionUID = -4081239885659052145L;

    /**
     * 
     * @param message the error message
     */
    public DqlParseException(final String message) {
        super(message);
    }
    
    /**
     * @param cause the cause of the Dql exception
     */
    public DqlParseException(final Throwable cause) {
        super(cause);
    }
}
