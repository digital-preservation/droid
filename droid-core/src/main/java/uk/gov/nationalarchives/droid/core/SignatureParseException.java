/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.core;

/**
 * @author Matt Palmer
 *
 */
public class SignatureParseException extends Exception {

    private static final long serialVersionUID = -6562570951231636750L;

    /**
     * Construct a SignatureParseException.
     * @param message The message for the exception.
     */
    public SignatureParseException(String message) {
        super(message);
    }

    
    /**
     * Construct a SignatureParseException.
     * @param message The message for the exception.
     * @param cause The cause of throwing this exception. 
     */
    public SignatureParseException(String message, Throwable cause) {
        super(message, cause);
    }
    

    /**
     * Construct a SignatureParseException.
     * @param cause The cause of throwing this exception. 
     */
    public SignatureParseException(Throwable cause) {
        super(cause);
    }
}
