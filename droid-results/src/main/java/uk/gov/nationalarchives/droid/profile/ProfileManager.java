/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.profile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileInfo;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureType;
import uk.gov.nationalarchives.droid.profile.referencedata.Format;
import uk.gov.nationalarchives.droid.profile.referencedata.ReferenceData;
import uk.gov.nationalarchives.droid.results.handlers.ProgressObserver;

/**
 * Interface for managing profile instances. Clients should keep references to
 * each ProfileManager they
 * 
 * @author rflitcroft
 * 
 */
public interface ProfileManager {

    /**
     * Creates a profile using the signature file specified.
     * 
     * @param sigFiles
     *            the signature files.
     * @return the new Profile Instance.
     * @throws ProfileManagerException if the profile could not be created
     */
    ProfileInstance createProfile(Map<SignatureType, SignatureFileInfo> sigFiles) throws ProfileManagerException;

    /**
     * Loads the reference data for the selected profile.
     * 
     * @param profileId
     *            the profile to start
     * @return the new Profile Instance.
     */
    ReferenceData getReferenceData(String profileId);

    /**
     * Starts a profile asynchronously.
     * 
     * @param profileId
     *            the profile to start
     * @return future wihich is done when the profile finishes.
     * @throws IOException if there was a problem with the profile. 
     */
    Future<?> start(String profileId) throws IOException;

    /**
     * Stops a profile.
     * 
     * @param profileId
     *            the profile to stop
     */
    void stop(String profileId);

    /**
     * Closes a profile and releases all resources.
     * 
     * @param profileId
     *            the profile to close
     */
    void closeProfile(String profileId);

    /**
     * Opens a profile.
     * 
     * @param profileId
     *            the profile to open
     * @return the profile.
     */
    ProfileInstance openProfile(String profileId);

    /**
     * Updates the profile spec.
     * 
     * @param profileId
     *            the profile ID
     * @param profileSpec
     *            the updated spec
     */
    void updateProfileSpec(String profileId, ProfileSpec profileSpec);

    /**
     * Sets the results observer for the profile given.
     * 
     * @param profileUuid
     *            the profile ID
     * @param observer
     *            the observer to set
     */
    void setResultsObserver(String profileUuid, ProfileResultObserver observer);

    /**
     * @param profileUuid
     *            the profile ID
     * @param parentId
     *            the ID of the parent of the node
     * @return the profile resource node at the URL with its immediate children,
     */
    List<ProfileResourceNode> findProfileResourceNodeAndImmediateChildren(
            String profileUuid, Long parentId);

    /**
     * @param profileUuid
     *            the profile ID
     * @return All root nodes for the profile given
     */
    List<ProfileResourceNode> findRootNodes(String profileUuid);

    /**
     * Sets the progress monitor for the specified profile.
     * 
     * @param profileId
     *            the profile ID
     * @param observer
     *            the progress observer to set.
     */
    void setProgressObserver(String profileId, ProgressObserver observer);

    /**
     * Saves the specified profile to the file specified. The file will be
     * created if it does not already exist or overwritten if it exists.
     * 
     * @param profileId
     *            the ID of the profile.
     * @param destination
     *            the file to be created or overwitten
     * @param progressCallback
     *            a progress call back object
     * @return the saved profile instance
     * @throws IOException
     *             - if the file IO failed.
     */
    ProfileInstance save(String profileId, File destination,
            ProgressObserver progressCallback) throws IOException;

    /**
     * Opens a profile from the file specified.
     * 
     * @param source
     *            the source file
     * @param progressCallback
     *            a progress call back object
     * @return the profile's UUID
     * @throws IOException
     *             if the File could not be read.
     */
    ProfileInstance open(File source, ProgressObserver progressCallback)
        throws IOException;

    /**
     * Retrieves all the formats.
     * @param profileId Profile Id of the profile.
     *        for which format is required.
     * @return list of formats
     */
    List<Format> getAllFormats(String profileId);

    /**
     * Sets the throttle value for a profile.
     * @param uuid the profile ID
     * @param value the new throttle value
     */
    void setThrottleValue(String uuid, int value);
    
}
