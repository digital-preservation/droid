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
