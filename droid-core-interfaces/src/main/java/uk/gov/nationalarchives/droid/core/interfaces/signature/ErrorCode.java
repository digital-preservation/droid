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
public enum ErrorCode {
    /** The file specified does not exist. */
    FILE_NOT_FOUND,
    
    /** THe file specified was not valid. */
    INVALID_SIGNATURE_FILE;
    
}
