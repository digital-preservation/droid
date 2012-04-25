/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.profile;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlElementWrapper;

/**
 * @author rflitcroft
 *
 */
@XmlAccessorType(XmlAccessType.NONE)
public class ProfileSpec {

    /** Max length of name field. */
    static final int NAME_MAX_LENGTH = 255;

    /** Max length of description field. */
    static final int DESC_MAX_LENGTH = 255;

    @XmlElementWrapper(name = "Resources", required = true)
    @XmlElementRefs({
        @XmlElementRef(name = "File", type = FileProfileResource.class), 
        @XmlElementRef(name = "Dir", type = DirectoryProfileResource.class) })
    private List<AbstractProfileResource> resources = new ArrayList<AbstractProfileResource>();

    private Map<URI, AbstractProfileResource> resourceMap = new HashMap<URI, AbstractProfileResource>();
    
   /**
     * Adds a resource to this profile spec.
     * @param resource the resource to add.
     * @return true if the resource was added; false otherwise
     */
    boolean addResource(AbstractProfileResource resource) {
        if (resources.contains(resource)) {
            return false;
        }
        
        resources.add(resource);
        resourceMap.put(resource.getUri(), resource);
        return true;
    }

    /**
     * @return resources.
     */
    public List<AbstractProfileResource> getResources() {
        return Collections.unmodifiableList(resources);
    }
    
    /**
     * Removes a resource witheth given URI.
     * @param uri the URI of the resource to remove
     * @return true if the resource was removed; false otherwise
     * 
     */
    boolean removeResource(URI uri) {
        return resources.remove(resourceMap.remove(uri));
    }

}
