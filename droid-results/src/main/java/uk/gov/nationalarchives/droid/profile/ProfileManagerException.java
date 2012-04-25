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
public class ProfileManagerException extends Exception {

    private static final long serialVersionUID = 231783107598686983L;

    /**
     * @param message the message
     */
    public ProfileManagerException(String message) {
        super(message);
    }

    /**
     * @param cause the cause of this exception
     */
    public ProfileManagerException(Throwable cause) {
        super(cause);
    }
}
