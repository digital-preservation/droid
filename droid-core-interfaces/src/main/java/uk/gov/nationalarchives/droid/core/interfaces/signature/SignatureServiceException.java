/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.core.interfaces.signature;

/**
 * @author rflitcroft
 *
 */
public class SignatureServiceException extends Exception {

    private static final long serialVersionUID = 7493374863824873527L;

    /**
     * @param cause - the cause
     */
    public SignatureServiceException(Throwable cause) {
        super(cause);
    }
    
    /**
     * 
     * @param message - a message explaining the cause.
     */
    public SignatureServiceException(String message) {
        super(message);
    }
    
    @Override
    public String getLocalizedMessage() {
        return String.format("Error updating signature file:\n%s", 
               getMessage());
    }

}
