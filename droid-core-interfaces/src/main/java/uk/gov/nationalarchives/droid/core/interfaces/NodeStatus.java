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

public enum NodeStatus {
    
    /** All those which are not processed.  */
    NOT_DONE("Not done", "This resource is not processed yet."),

    /** Nodes which have been processed. */
    DONE("Done", "The resource has been processed."),
    
    /** The resource could not be found. */
    NOT_FOUND("Not found", "The resource could not be found."),
    
    /** Read access was denied to the resource. */
    ACCESS_DENIED("Access denied", "Read access was denied to the resource."),

    /** Nodes with identification error.   */
    ERROR("Error", "An unexpected error has occurred while processing the resource.");
    
    
    private String status;
    private String statusDescription;
    
    
    /**
     * Constructor for NodeStatusInMemory  
     * @param status node status 
     * @param statusDescription node status description.
     */
    NodeStatus(String status, String statusDescription) {
        this.status = status;
        this.statusDescription = statusDescription;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @return the statusDescription
     */
    public String getStatusDescription() {
        return statusDescription;
    }
    

    
}
