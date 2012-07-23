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
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalConfig;
import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalProperty;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileException;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileInfo;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureManager;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureType;
import uk.gov.nationalarchives.droid.profile.referencedata.Format;
import uk.gov.nationalarchives.droid.profile.referencedata.ReferenceData;
import uk.gov.nationalarchives.droid.results.handlers.ProgressObserver;


/**
 * @author rflitcroft
 * 
 */
public class ProfileManagerImpl implements ProfileManager {

    private final Log log = LogFactory.getLog(getClass());

    private ProfileContextLocator profileContextLocator;
    private ProfileSpecDao profileSpecDao;

    private ProfileDiskAction profileSaver;
    private SignatureManager signatureManager;
    private DroidGlobalConfig config;

    /**
     * Gets a profile instance manager.
     * 
     * @param sigFileInfos
     *            the path to the signature file to be used for this profile.
     * @return the profile instance created.
     * @throws ProfileManagerException if the profile could not be created
     */
    @Override
    public ProfileInstance createProfile(Map<SignatureType, SignatureFileInfo> sigFileInfos) 
        throws ProfileManagerException {
        Map<SignatureType, SignatureFileInfo> signatures = sigFileInfos;
        if (sigFileInfos == null) {
            // get the default sig file config
            try {
                signatures = signatureManager.getDefaultSignatures();
            } catch (SignatureFileException e) {
                throw new ProfileManagerException(e.getMessage());
            }
        }
        
        String profileId = String.valueOf(System.currentTimeMillis());
        log.info("Creating profile: " + profileId);
        ProfileInstance profile = profileContextLocator.getProfileInstance(profileId);
        
        File profileHomeDir = new File(config.getProfilesDir(), profile.getUuid()); 
        profileHomeDir.mkdir();

        SignatureFileInfo binarySigFile = signatures.get(SignatureType.BINARY);
        if (binarySigFile != null) {
            profile.setSignatureFileVersion(binarySigFile.getVersion());
            profile.setSignatureFileName(binarySigFile.getFile().getName());
            copySignatureFile(binarySigFile.getFile(), profileHomeDir);
        }
        
        SignatureFileInfo containerSigFile = signatures.get(SignatureType.CONTAINER);
        if (containerSigFile != null) {
            profile.setContainerSignatureFileName(containerSigFile.getFile().getName());
            profile.setContainerSignatureFileVersion(containerSigFile.getVersion());
            copySignatureFile(containerSigFile.getFile(), profileHomeDir);
        }
        
        SignatureFileInfo textSigFile = signatures.get(SignatureType.TEXT);
        if (textSigFile != null) {
            profile.setTextSignatureFileName(textSigFile.getFile().getName());
            profile.setTextSignatureFileVersion(textSigFile.getVersion());
            copySignatureFile(textSigFile.getFile(), profileHomeDir);
        }

        profile.setUuid(profileId);
        profile.setProfileSpec(new ProfileSpec());

        

        // Persist the profile.
        profileSpecDao.saveProfile(profile, profileHomeDir);

        // Copy the signature file to the profile area
        
        // Ensure a newly created profile is not in a "dirty" state:
        profile.setDirty(false);
            
        profileContextLocator.addProfileContext(profile);
        return profile;
    }
    
    private static void copySignatureFile(File file, File destDir) {
        try {
            FileUtils.copyFileToDirectory(file, destDir);
        } catch (IOException e) {
            throw new ProfileException(e.getMessage(), e);
        }    
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void closeProfile(String profileName) {
        log.info("Closing profile: " + profileName);
        profileContextLocator.removeProfileContext(profileName);
        if (!config.getProperties().getBoolean(DroidGlobalProperty.DEV_MODE.getName())) {
            File profileHome = new File(config.getProfilesDir(), profileName);
            FileUtils.deleteQuietly(profileHome);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @throws SignatureFileException
     */
    @Override
    public ProfileInstance openProfile(String profileId) {
        log.info("Opening profile: " + profileId);
        if (!profileContextLocator.hasProfileContext(profileId)) {
            throw new IllegalArgumentException(String.format(
                    "No such profile id [%s]", profileId));
        }
        ProfileInstance profile = profileContextLocator
                .getProfileInstance(profileId);
        profileContextLocator.openProfileInstanceManager(profile);
        profile.fireListeners();
        return profile;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws SignatureFileException
     */
    @Override
    public void stop(String profileInstance) {
        log.info("Stopping profile: " + profileInstance);
        ProfileInstanceManager profileInstanceManager = getProfileInstanceManager(profileInstance);
        profileInstanceManager.pause();
    }

    /**
     * @param profileInstance
     * @return
     * @throws SignatureFileException
     */
    private ProfileInstanceManager getProfileInstanceManager(
            String profileInstance) {
        if (profileInstance == null) {
            String message = "Profile instance id was null";
            log.error(message);
            throw new ProfileException(message);
        }
        ProfileInstanceManager profileInstanceManager = profileContextLocator
                .openProfileInstanceManager(profileContextLocator
                        .getProfileInstance(profileInstance));
        return profileInstanceManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Future<?> start(String profileId) throws IOException {
        log.info("Starting profile: " + profileId);
        ProfileInstanceManager profileInstanceManager = getProfileInstanceManager(profileId);
        return profileInstanceManager.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ProfileResourceNode> findProfileResourceNodeAndImmediateChildren(
            String profileId, Long parentId) {
        log.debug(String.format(
            " **** Called findProfileResourceNodeAndImmediateChildren [%s] ****",
            profileId));

        ProfileInstanceManager profileInstanceManager = getProfileInstanceManager(profileId);

        return profileInstanceManager.findAllProfileResourceNodes(parentId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ProfileResourceNode> findRootNodes(String profileId) {
        ProfileInstanceManager profileInstanceManager = getProfileInstanceManager(profileId);
        return profileInstanceManager.findRootProfileResourceNodes();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProgressObserver(String profileId,
            ProgressObserver progressObserver) {
        ProfileInstanceManager profileInstanceManager = getProfileInstanceManager(profileId);
        profileInstanceManager.getProgressMonitor()
                .setPercentIncrementObserver(progressObserver);
    }

    /**
     * @param profileSpecDao
     *            the profileSpecDao to set
     */
    public void setProfileSpecDao(ProfileSpecDao profileSpecDao) {
        this.profileSpecDao = profileSpecDao;
    }

    /**
     * @param profileContextLocator
     *            the profileContextLocator to set
     */
    public void setProfileContextLocator(
            ProfileContextLocator profileContextLocator) {
        this.profileContextLocator = profileContextLocator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setResultsObserver(String profileUuid,
            ProfileResultObserver observer) {

        getProfileInstanceManager(profileUuid).setResultsObserver(observer);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateProfileSpec(String profileId, ProfileSpec profileSpec) {
        ProfileInstance profile = profileContextLocator
                .getProfileInstance(profileId);
        profile.setProfileSpec(profileSpec);
        profileSpecDao.saveProfile(profile, getProfileHomeDir(profile));
    }

    /**
     * Saves the specified profile to the file specified. The file will be
     * created if it does not already exist or overwritten if it exists.
     * 
     * @param profileId
     *            the ID of the profile.
     * @param destination
     *            the file to be created or overwitten
     * @param callback
     *            an object to be notified when progress is made.
     * @return the saved profile instance
     * @throws IOException
     *             if the file IO failed
     */
    @Override
    public ProfileInstance save(String profileId, File destination,
            ProgressObserver callback) throws IOException {

        log.info("Saving profile: " + profileId + " to " + destination.getPath());
        // shutdown the database so that we can safely zip it up.
        profileContextLocator.shutdownDatabase(profileId);
        ProfileInstance profile = profileContextLocator.getProfileInstance(profileId);
        ProfileState oldState = profile.getState();
        profile.changeState(ProfileState.SAVING);

        try {
            File output = destination != null ? destination : profile.getLoadedFrom();

            profileSpecDao.saveProfile(profile, getProfileHomeDir(profile));

            profileSaver.saveProfile(getProfileHomeDir(profile).getPath(), output, callback);
            profile.setLoadedFrom(output);
            profile.setName(FilenameUtils.getBaseName(output.getName()));
            profile.onSave();
        } finally {
            profileContextLocator.bootDatabase(profileId);
            profile.changeState(oldState);
        }
        return profile;
    }

    /**
     * @param profileDiskAction
     *            the profile diskaction to set.
     */
    public void setProfileDiskAction(ProfileDiskAction profileDiskAction) {
        this.profileSaver = profileDiskAction;
    }

    /**
     * Loads a profile from disk, unless that profile is already open.
     * 
     * @param source
     *            the source file (zipped).
     * @param observer
     *            an object to be notified when progress is made.
     * @return the name of the profile.
     * @throws IOException
     *             - if the file could not be opened, was corrupt, or invalid.
     */
    @Override
    public ProfileInstance open(File source, ProgressObserver observer)
        throws IOException {
        log.info("Loading profile from: " + source.getPath());
        ZipFile sourceZip = new ZipFile(source);
        InputStream in = ProfileFileHelper.getProfileXmlInputStream(sourceZip);
        ProfileInstance profile;
        try {
            profile = profileSpecDao.loadProfile(in);
        } finally {
            if (in != null) {
                in.close();
            }
            sourceZip.close();
        }

        profile.setLoadedFrom(source);
        profile.setName(FilenameUtils.getBaseName(source.getName()));
        profile.setUuid(String.valueOf(System.currentTimeMillis()));
        profile.onLoad();

        String profileId = profile.getUuid();

        if (!profileContextLocator.hasProfileContext(profileId)) {
            profileContextLocator.addProfileContext(profile);
            File destination = getProfileHomeDir(profile);
            profileSaver.load(source, destination, observer);
            profileSpecDao.saveProfile(profile, getProfileHomeDir(profile));
            profileContextLocator.openProfileInstanceManager(profile);
        }
        return profile;

    }

    /**
     * Retrieves all the formats.
     * @param profileId Profile Id of the profile 
     *        for which format is requested.
     * @return list of formats
     */
    public List<Format> getAllFormats(String profileId) {
        return getProfileInstanceManager(profileId).getAllFormats();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReferenceData getReferenceData(String profileId) {
        return getProfileInstanceManager(profileId).getReferenceData();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setThrottleValue(String uuid, int value) {
        getProfileInstanceManager(uuid).setThrottleValue(value);
    }
    
    /**
     * @param signatureManager the signatureManager to set
     */
    public void setSignatureManager(SignatureManager signatureManager) {
        this.signatureManager = signatureManager;
    }
    
    /**
     * @param config the config to set
     */
    public void setConfig(DroidGlobalConfig config) {
        this.config = config;
    }
    
    private File getProfileHomeDir(ProfileInstance profile) {
        return new File(config.getProfilesDir(), profile.getUuid());
    }
    
}
