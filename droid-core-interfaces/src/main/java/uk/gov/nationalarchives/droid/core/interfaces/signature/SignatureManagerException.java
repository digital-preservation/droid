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
public class SignatureManagerException extends Exception {

    private static final long serialVersionUID = -8303591441756265037L;

    /**
     * @param cause the cause
     */
    public SignatureManagerException(Throwable cause) {
        super(cause.getMessage(), cause);
    }
    
    /**
     * @return The cause message.
     */
    public String getCauseMessage() {
        return getCause().getCause().getMessage();
    }
    
    /**
     * The cause type.
     * @return The cause type
     */
    public String getCauseType() {
        return getCause().getCause().getClass().getName();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getLocalizedMessage() {
        return String.format("Error updating signature file:\n%s\nReason: %s - %s", 
               getMessage(),
               getCauseType(),
               getCauseMessage());
    }
}
