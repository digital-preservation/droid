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
public enum IdentificationErrorType {

    /** Access to the resource was denied. */
    ACCESS_DENIED(true),
    
    /** The resource was not found. */
    FILE_NOT_FOUND(true),
    
    /** Unexpected error. */
    OTHER(false);
    
    private boolean unreadable;
    
    private IdentificationErrorType(boolean unreadable) {
        this.unreadable = unreadable;
    }
    
    /**
     * @return the unreadable
     */
    public boolean isUnreadable() {
        return unreadable;
    }
}
