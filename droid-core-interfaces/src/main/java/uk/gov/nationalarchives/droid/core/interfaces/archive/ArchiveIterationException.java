/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.core.interfaces.archive;

/**
 * @author a-mpalmer
 *
 */
public class ArchiveIterationException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 5689202110721952261L;

    /**
     * 
     */
    public ArchiveIterationException() {
    }

    /**
     * @param message A message describing what happened.
     */
    public ArchiveIterationException(String message) {
        super(message);
    }

    /**
     * @param cause The cause of the exception.
     */
    public ArchiveIterationException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message A message describing what happened.
     * @param cause The cause of the exception.
     */
    public ArchiveIterationException(String message, Throwable cause) {
        super(message, cause);
    }

}
