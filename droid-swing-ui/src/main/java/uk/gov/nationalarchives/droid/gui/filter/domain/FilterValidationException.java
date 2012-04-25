/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui.filter.domain;

/**
 * Checked exception to be thrown when a validation fails.
 */
public class FilterValidationException extends Exception {

    private static final long serialVersionUID = -2393786077320407901L;

    /**
     * 
     * @param message the message.
     */
    public FilterValidationException(String message) {
        super(message);
    }

}
