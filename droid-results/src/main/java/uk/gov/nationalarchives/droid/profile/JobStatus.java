/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.profile;

/**
 * Enumerates all job states.
 * @author rflitcroft
 *
 */
public enum JobStatus {

    /** Job finished successfully. */
    COMPLETE("FIN"),
    
    /** Job did not complete due to error. */
    ERROR("ERR");
    
    private String name;
    
    private JobStatus(String name) {
        this.name = name;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return name;
    }
}
