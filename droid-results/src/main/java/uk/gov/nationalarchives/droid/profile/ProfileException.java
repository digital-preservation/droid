/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.profile;

/**
 * @author rflitcroft
 *
 */
public class ProfileException extends RuntimeException {

    private static final long serialVersionUID = -2367129935290512358L;

    /**
     * @param message the exception message.
     */
    public ProfileException(String message) {
        super(message);
    }

    /**
     * @param message the exception message.
     * @param cause the cause of the exception.
     */
    public ProfileException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * @param cause the cause of the exception.
     */
    public ProfileException(Throwable cause) {
        super(cause);
    }
    

}
