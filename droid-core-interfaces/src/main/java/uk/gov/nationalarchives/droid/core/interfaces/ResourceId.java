/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.core.interfaces;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Class which holds information to identify a resource node.
 * @author a-mpalmer
 *
 */
public class ResourceId {

    private final long id;
    private final String path;
    
    /**
     * 
     * @param id The id of the resource.
     * @param path A representation of the path of the resource.
     */
    public ResourceId(long id, String path) {
        this.id = id;
        this.path = path;
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
     * @return The path of the resource.
     */
    public String getPath() {
        return path;
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(id)
            .append(path)
            .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ResourceId other = (ResourceId) obj;
        
        return new EqualsBuilder()
            .append(id, other.id)
            .append(path, other.path)
            .isEquals();
    }    
    
}
