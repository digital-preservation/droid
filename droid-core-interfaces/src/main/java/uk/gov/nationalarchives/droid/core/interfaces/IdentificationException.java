/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.core.interfaces;


/**
 * @author rflitcroft
 * Exception indicationg that an Idenfification failed.
 */
public class IdentificationException extends Exception {
    
    private static final long serialVersionUID = -345663642425488552L;

    private final IdentificationRequest request;
    private final IdentificationErrorType errorType;
    
    /**
     * @param request the request that failed
     * @param errorType the type of error
     * @param cause the cause of the failure
     */
    public IdentificationException(IdentificationRequest request, IdentificationErrorType errorType, Throwable cause) {
        super(cause.getMessage(), cause);
        this.request = request;
        this.errorType = errorType;
    }
    
    /**
     * @return the request
     */
    public IdentificationRequest getRequest() {
        return request;
    }
    
    /**
     * @return the errorType
     */
    public IdentificationErrorType getErrorType() {
        return errorType;
    }
    
}
