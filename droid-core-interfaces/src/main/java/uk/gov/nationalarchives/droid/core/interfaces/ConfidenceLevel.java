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
 *
 */
public enum ConfidenceLevel {

    /** A single, positive format identification was made. **/
    POSITIVE,

    /** More than one positive format was identified. */
    UNCLEAR,

    /** No format identification was made. */
    NEGATIVE,

}
