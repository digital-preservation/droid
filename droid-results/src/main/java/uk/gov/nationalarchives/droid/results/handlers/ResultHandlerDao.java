/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.results.handlers;

import uk.gov.nationalarchives.droid.core.interfaces.ResourceId;
import uk.gov.nationalarchives.droid.profile.ProfileResourceNode;
import uk.gov.nationalarchives.droid.profile.referencedata.Format;

/**
 * Operations for persistence.
 * @author rflitcroft
 *
 */
public interface ResultHandlerDao {

    /**
     * Saves a new identification to the database.
     * @param node the result to save
     * @param parentId the node's parent ID
     */
    void save(ProfileResourceNode node, ResourceId parentId);


    /**.
     * Loads a Format. 
     * @param puid - the unique id of the format
     * @return the format.
     */
    Format loadFormat(String puid);

    /**
     * @param nodeId the Id of the node to load
     * @return the refernce to the node
     */
    ProfileResourceNode loadNode(Long nodeId);

    /**
     * Deletes a node and all its children.
     * @param nodeId the noe to remove
     */
    void deleteNode(Long nodeId);

}
