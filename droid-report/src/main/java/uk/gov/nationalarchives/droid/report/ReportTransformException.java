/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.report;

/**
 * Thrown when the report transformer API fails.
 * @author rflitcroft
 *
 */
public class ReportTransformException extends Exception {

    private static final long serialVersionUID = -4109467992618831911L;

    /**
     * @param cause the cause of this exception
     */
    public ReportTransformException(Throwable cause) {
        super(cause);
    }
}
