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
 * @author rflitcroft
 *
 */
public class RequestMetaData {

    private final Long size;
    private final Long time;
    private final String name;
    private String hash;

    /**
     * @param size - the size in bytes of the request data
     * @param time - the time associated with data (e.g. last modified)
     * @param entryName the name of the request data
     */
    public RequestMetaData(Long size, Long time, String entryName) {
        this.size = size;
        this.time = time;
        this.name = entryName;
    }

    /**
     * @return the size
     */
    public final Long getSize() {
        return size;
    }

    /**
     * @return the time
     */
    public final Long getTime() {
        return time;
    }

    /**
     * @return the name
     */
    public final String getName() {
        return name;
    }

    /**
     * @param hash the hash to set
     */
    public final void setHash(String hash) {
        this.hash = hash;
    }
    
    /**
     * @return the hash
     */
    public final String getHash() {
        return hash;
    }
}
