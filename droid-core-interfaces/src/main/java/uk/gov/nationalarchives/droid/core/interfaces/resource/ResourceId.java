/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.core.interfaces.resource;

/**
 * @author a-mpalmer
 *
 */
public class ResourceId {

    private final long id;
    private final String prefix;
    
    /**
     * 
     * @param id The id of the resource.
     * @param prefix The prefix of the resource.
     */
    public ResourceId(long id, String prefix) {
        this.id = id;
        this.prefix = prefix;
    }

    /**
     * 
     * @return The id of the resource.
     */
    public long getId() {
        return id;
    }

    /**
     * 
     * @return The prefix of the resource.
     */
    public String getPrefix() {
        return prefix;
    }
    
}
