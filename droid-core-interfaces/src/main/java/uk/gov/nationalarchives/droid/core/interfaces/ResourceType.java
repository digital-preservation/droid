/**
 * Copyright (c) 2012, The National Archives <pronom@nationalarchives.gsi.gov.uk>
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
package uk.gov.nationalarchives.droid.core.interfaces;

/**
 * @author Alok Kumar Dash
 */

public enum ResourceType {
    
    /** Resource Type is a Folder. */
    FOLDER("Folder", "Resource type is a folder", true),

    /** Resource type is a Container. */
    CONTAINER("Container", "Resource is a container.", true),

    /** Resource type is a File.  */
    FILE("File", "Resource type is a file.", false);
    
    private boolean allowsChildren;
    
    private String resourceType;
    private String resourceTypeDescription;
    
    /**
     * Constructor for ResourceTypeInMemory  
     * @param allowsChildren Allows Children. 
     * @param resourceType Resource Type.
     * @param resourceTypeDescription ResourceType description.  
     */
    ResourceType(String resourceType, String resourceTypeDescription, boolean allowsChildren) {
        this.resourceType = resourceType;
        this.resourceTypeDescription = resourceTypeDescription;
        this.allowsChildren = allowsChildren;

    }

//    /**
//     * @return the id
//     */
//    public long getId() {
//        return ordinal();
//    }

    /**
     * @return the resourceType
     */
    public String getResourceType() {
        return resourceType;
    }

    /**
     * @return the resourceTypeDescription
     */
    public String getResourceTypeDescription() {
        return resourceTypeDescription;
    }

    /**
     * @return true if the type allows children, false otherwise
     */
    public boolean allowsChildren() {
        return allowsChildren;
    }


}
