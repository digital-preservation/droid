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
public class SignatureFileException extends Exception {

    private static final long serialVersionUID = 5878068551833875L;

    private final ErrorCode errorCode;

    /**
     * Constructs a new SignatureFileException with a message.
     * 
     * @param message
     *            the message
     * @param filePath
     * @param errorCode
     *            the error code
     */
    public SignatureFileException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Constructs a new SignatureFileException with a message.
     * 
     * @param message
     *            the message
     * @param filePath
     * @param cause
     *            the cause of the exception
     * @param errorCode
     *            the error code
     */
    public SignatureFileException(String message, Throwable cause,
            ErrorCode errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * @return the error code
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

}
