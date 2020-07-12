/**
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gov.uk>
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.zip.ZipFile;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalConfig;
import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalProperty;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileException;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileInfo;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureManager;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureType;
import uk.gov.nationalarchives.droid.profile.referencedata.Format;
import uk.gov.nationalarchives.droid.profile.referencedata.ReferenceData;
import uk.gov.nationalarchives.droid.results.handlers.ProgressObserver;
import uk.gov.nationalarchives.droid.util.FileUtil;


/**
 * @author rflitcroft
 * 
 */
public class ProfileManagerImpl implements ProfileManager {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private ProfileContextLocator profileContextLocator;
    private ProfileSpecDao profileSpecDao;

    private ProfileDiskAction profileSaver;
    private SignatureManager signatureManager;
    private DroidGlobalConfig config;

    /**
     * Empty bean constructor.
     */
    public ProfileManagerImpl() {
    }

    /**
     * Paramterized constructor.
     * @param contextLocator The profile context locator.
     * @param profileSpecDao The profile spec dao.
     * @param diskAction The profile disk action.
     * @param signatureManager The signature manager.
     * @param config The global config.
     */
    public ProfileManagerImpl(ProfileContextLocator contextLocator, ProfileSpecDao profileSpecDao,
                              ProfileDiskAction diskAction, SignatureManager signatureManager,
                              DroidGlobalConfig config) {
        setProfileContextLocator(contextLocator);
        setProfileSpecDao(profileSpecDao);
        setProfileDiskAction(diskAction);
        setSignatureManager(signatureManager);
        setConfig(config);
    }

    @Override
    public ProfileInstance createProfile(Map<SignatureType, SignatureFileInfo> sigFileInfos) throws ProfileManagerException {
        return createProfile(sigFileInfos, null);
    }

    @Override
    public ProfileInstance createProfile(Map<SignatureType, SignatureFileInfo> sigFileInfos,
                                         PropertiesConfiguration propertyOverrides)
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
        ProfileInstance profile = profileContextLocator.getProfileInstance(profileId, propertyOverrides);
        
        final Path profileHomeDir = config.getProfilesDir().resolve(profile.getUuid());
        FileUtil.mkdirsQuietly(profileHomeDir);

        createProfileBinarySigFile(signatures.get(SignatureType.BINARY), profile, profileHomeDir);
        createProfileContainerSigFile(signatures.get(SignatureType.CONTAINER), profile, profileHomeDir);
        createProfileTextSigFile(signatures.get(SignatureType.TEXT), profile, profileHomeDir);

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

    private void createProfileBinarySigFile(final SignatureFileInfo binarySigFile, final ProfileInstance profile, final Path profileHomeDir) {
        if (binarySigFile != null) {
            profile.setSignatureFileVersion(binarySigFile.getVersion());
            profile.setSignatureFileName(FileUtil.fileName(binarySigFile.getFile()));
            copySignatureFile(binarySigFile.getFile(), profileHomeDir);
        }
    }

    private void createProfileContainerSigFile(final SignatureFileInfo containerSigFile, final ProfileInstance profile, final Path profileHomeDir) {
        if (containerSigFile != null) {
            profile.setContainerSignatureFileName(FileUtil.fileName(containerSigFile.getFile()));
            profile.setContainerSignatureFileVersion(containerSigFile.getVersion());
            copySignatureFile(containerSigFile.getFile(), profileHomeDir);
        }
    }

    private void createProfileTextSigFile(final SignatureFileInfo textSigFile, final ProfileInstance profile, final Path profileHomeDir) {
        if (textSigFile != null) {
            profile.setTextSignatureFileName(FileUtil.fileName(textSigFile.getFile()));
            profile.setTextSignatureFileVersion(textSigFile.getVersion());
            copySignatureFile(textSigFile.getFile(), profileHomeDir);
        }
    }
    
    private static void copySignatureFile(final Path file, final Path destDir) {
        try {
            Files.createDirectories(destDir);
            Path destFile = destDir.resolve(file.getFileName());
            Files.copy(file, destFile);
        } catch (final IOException e) {
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
            final Path profileHome = config.getProfilesDir().resolve(profileName);
            FileUtil.deleteQuietly(profileHome);
        }
    }

    /**
     * {@inheritDoc}
     * @param profileId String
     * @throws IllegalArgumentException if profileId nonexistent
     * @return Profile
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
     * @param profileInstance The profile to stop
     */
    @Override
    public void stop(String profileInstance) {
        log.info("Stopping profile: " + profileInstance);
        ProfileInstanceManager profileInstanceManager = getProfileInstanceManager(profileInstance);
        profileInstanceManager.pause();
    }

    /**
     * @param profileInstance String
     * @return ProfileInstanceManager
     * @throws ProfileException if null profileInstance
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
    public ProfileInstance save(final String profileId, final Path destination,
            final ProgressObserver callback) throws IOException {

        log.info("Saving profile: " + profileId + " to " + destination.toAbsolutePath().toString());
        // freeze the database so that we can safely zip it up.
        profileContextLocator.freezeDatabase(profileId);
        ProfileInstance profile = profileContextLocator.getProfileInstance(profileId);
        ProfileState oldState = profile.getState();
        profile.changeState(ProfileState.SAVING);

        try {
            final Path output = destination != null ? destination : profile.getLoadedFrom();

            profileSpecDao.saveProfile(profile, getProfileHomeDir(profile));

            profileSaver.saveProfile(getProfileHomeDir(profile), output, callback);
            profile.setLoadedFrom(output);
            profile.setName(FilenameUtils.getBaseName(FileUtil.fileName(output)));
            profile.onSave();
        } finally {
            profileContextLocator.thawDatabase(profileId);
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
    public ProfileInstance open(final Path source, final ProgressObserver observer)
            throws IOException {
        log.info("Loading profile from: " + source.toAbsolutePath().toString());
        try (final ZipFile sourceZip = new ZipFile(source.toFile());
                final InputStream in = ProfileFileHelper.getProfileXmlInputStream(sourceZip)) {
            final ProfileInstance profile = profileSpecDao.loadProfile(in);

            profile.setLoadedFrom(source);
            profile.setName(FilenameUtils.getBaseName(FileUtil.fileName(source)));
            profile.setUuid(String.valueOf(System.currentTimeMillis()));
            profile.onLoad();

            String profileId = profile.getUuid();

            if (!profileContextLocator.hasProfileContext(profileId)) {
                profileContextLocator.addProfileContext(profile);
                final Path destination = getProfileHomeDir(profile);
                profileSaver.load(source, destination, observer);
                profileSpecDao.saveProfile(profile, getProfileHomeDir(profile));
                profileContextLocator.openProfileInstanceManager(profile);
            }
            return profile;
        }

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
    
    private Path getProfileHomeDir(ProfileInstance profile) {
        return config.getProfilesDir().resolve(profile.getUuid());
    }
    
}
