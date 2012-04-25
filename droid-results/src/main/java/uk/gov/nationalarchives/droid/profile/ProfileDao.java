/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.profile;

import java.util.List;

import uk.gov.nationalarchives.droid.core.interfaces.filter.Filter;
import uk.gov.nationalarchives.droid.profile.referencedata.Format;

/**
 * Interface for profile database operations.
 * 
 * @author rflitcroft
 * 
 */
public interface ProfileDao {

    /**
     * Inserts a row into the format table.
     * 
     * @param format
     *            the format to insert
     */
    void saveFormat(Format format);

    /**
     * Retrieves all the formats.
     * 
     * @return list of formats
     */
    List<Format> getAllFormats();

    /**
     * @param parentId
     *            the parentId of the nodes sought
     * @return the profile resource node identified by this URI.
     */
    List<ProfileResourceNode> findProfileResourceNodes(Long parentId);

    /**
     * @param parentId
     *            the parentId of the nodes sought
     * @param filter
     *            to filter the results.
     * @return the profile resource node identified by this URI and filter.
     */
    List<ProfileResourceNode> findProfileResourceNodes(Long parentId, Filter filter);

//    /**
//     * @return the root node ID.
//     */
//    long createRootNode();

}
