/**
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gsi.gov.uk>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following
 * conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of the The National Archives nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
