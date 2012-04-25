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
 * Handles identification results.
 * @author rflitcroft
 *
 */
public interface ResultHandler {

    /**
     * Invoked whenever DROID completes an identification.
     * @param result the result of the identification.
     * @return the alloacted ID of the handled result.
     */
    ResourceId handle(IdentificationResultCollection result);

    /**
     * Invoked whenever DROID completes an identification.
     * @param result the result of the identification.
     * @param parentId the dirtectory's parent's ID
     * @param restricted if access to directory was restricted
     * @return the alloacted ID of the handled result.
     */
    ResourceId handleDirectory(IdentificationResult result, ResourceId parentId, boolean restricted);

    /**
     * Invoked whenever DROID cannot complete an identification.
     * @param e the error which prevented the job from completing
     */
    void handleError(IdentificationException e);


    /**
     * Deletes a node an all its children.
     * @param nodeId the node to delete.
     */
    void deleteCascade(Long nodeId);
    
    
    /**
     * Commits any uncommitted results.
     */
    void commit();
    
    
    /**
     * Ensures that the result handler is initialised properly.
     */
    void init();

}
