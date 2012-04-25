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
