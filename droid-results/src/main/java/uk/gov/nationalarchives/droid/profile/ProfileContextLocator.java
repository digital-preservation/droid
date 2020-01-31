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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.map.LazyMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalConfig;
import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalProperty;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileException;
import uk.gov.nationalarchives.droid.results.handlers.JDBCBatchResultHandlerDao;
import uk.gov.nationalarchives.droid.results.handlers.ProgressObserver;
import uk.gov.nationalarchives.droid.util.FileUtil;


/**
 * @author rflitcroft
 * Maintains profile contexts.
 */
public class ProfileContextLocator {

    private static final String HIBERNATE_GENERATE_DDL = "hibernate.generateDdl";
    private static final String DATABASE_URL = "datasource.url";
    private static final String CREATE_URL = "datasource.createUrl";
    private static final String HIBERNATE_CREATE = "hibernate.hbm2ddl.auto";
    private static final String BLANK_PROFILE = "profile.template";
    private static final String SIG_PROFILE = "profile\\.\\d+\\.template";
    
    private final Logger log = LoggerFactory.getLogger(getClass());
    private DroidGlobalConfig globalConfig;
    
    private enum TemplateStatus { NO_TEMPLATE, BLANK_TEMPLATE, SIGNATURE_TEMPLATE };

    @SuppressWarnings("unchecked")
    private Map<String, ProfileInstance> profileInstances = 
        LazyMap.lazyMap(new HashMap<String, ProfileInstance>(), new ProfileTransformer());
    
    private ProfileInstanceLocator profileInstanceLocator;

    /**
     * Empty bean constructor.
     */
    public ProfileContextLocator() {
    }

    /**
     * Parameterized constructor.
     * @param config The global config to use.
     * @param instanceLocator The profile instance locator to use.
     */
    public ProfileContextLocator(DroidGlobalConfig config, ProfileInstanceLocator instanceLocator) {
        setGlobalConfig(config);
        setProfileInstanceLocator(instanceLocator);
    }

    /**
     * Transformer for creating profile instance objects.
     * @author rflitcroft
     */
    private final class ProfileTransformer implements Transformer {
        @Override
        public Object transform(Object id) {
            ProfileInstance profileInstance = new ProfileInstance(ProfileState.INITIALISING);
            profileInstance.setUuid((String) id);
            profileInstance.setThrottle(globalConfig.getProperties()
                    .getInt(DroidGlobalProperty.DEFAULT_THROTTLE.getName()));
            profileInstance.setHashAlgorithm(globalConfig.getProperties()
                    .getString(DroidGlobalProperty.HASH_ALGORITHM.getName()));
            profileInstance.setGenerateHash(globalConfig.getProperties()
                    .getBoolean(DroidGlobalProperty.GENERATE_HASH.getName()));

            profileInstance.setProcessTarFiles(globalConfig.getProperties()
                    .getBoolean(DroidGlobalProperty.PROCESS_TAR.getName()));
            profileInstance.setProcessZipFiles(globalConfig.getProperties()
                    .getBoolean(DroidGlobalProperty.PROCESS_ZIP.getName()));
            profileInstance.setProcessGzipFiles(globalConfig.getProperties().getBoolean(DroidGlobalProperty.PROCESS_GZIP.getName()));
            profileInstance.setProcessRarFiles(globalConfig.getProperties().getBoolean(DroidGlobalProperty.PROCESS_RAR.getName()));
            profileInstance.setProcess7zipFiles(globalConfig.getProperties().getBoolean(DroidGlobalProperty.PROCESS_7ZIP.getName()));
            profileInstance.setProcessIsoFiles(globalConfig.getProperties().getBoolean(DroidGlobalProperty.PROCESS_ISO.getName()));
            profileInstance.setProcessBzip2Files(globalConfig.getProperties().getBoolean(DroidGlobalProperty.PROCESS_BZIP2.getName()));

            profileInstance.setProcessArcFiles(globalConfig.getProperties().getBoolean(DroidGlobalProperty.PROCESS_ARC.getName()));
            profileInstance.setProcessWarcFiles(globalConfig.getProperties().getBoolean(DroidGlobalProperty.PROCESS_WARC.getName()));

            profileInstance.setMaxBytesToScan(globalConfig.getProperties()
                    .getLong(DroidGlobalProperty.MAX_BYTES_TO_SCAN.getName()));
            profileInstance.setMatchAllExtensions(globalConfig.getProperties()
                    .getBoolean(DroidGlobalProperty.EXTENSION_ALL.getName()));
            return profileInstance;
        }
    }
    
    /**
     * Lazily instantiates (if necessary) and returns the profile instance with the name given.
     * @param id the id of the profile instance
     * @return the profile instance with the name given
     */
    public ProfileInstance getProfileInstance(String id) {
        return profileInstances.get(id);
    }
    
    /**
     * Adds a profile context.
     * @param profileInstance the profile instance to add
     */
    public void addProfileContext(ProfileInstance profileInstance) {
        profileInstances.put(profileInstance.getUuid(), profileInstance);
    }
    
    /**
     * Destroys a profile context.
     * @param id the id of the contex to destroy
     */
    public void removeProfileContext(String id) {
        profileInstances.remove(id);
        profileInstanceLocator.closeProfileInstance(id);
        
    }
    
    /**
     * Freeze the database for the specified profile, allowing a copy to be made.
     * @param profileId the id of a profile.
     */
    public void freezeDatabase(String profileId) {
        profileInstanceLocator.freezeDatabase(profileId);
    }
    
    /**
     *  reopen a frozen database database for the specified profile.
     * @param profileId the id of a profile.
     */
    public void thawDatabase(String profileId) {
        profileInstanceLocator.thawDatabase(profileId);
    }
    
    /**
     * Opens a profile instance manager for a pre-existing profile context.
     * @param profile the profile to obtain a profile manager for.
     * @return a profile instance manager for a pre-existing profile context
     */
    //CHECKSTYLE:OFF
    public ProfileInstanceManager openProfileInstanceManager(final ProfileInstance profile) {
        final Path profileHome = globalConfig.getProfilesDir().resolve(profile.getUuid());
        final Path databasePath = profileHome.resolve("db");
        final Path signatureFile = profileHome.resolve(profile.getSignatureFileName());
        final Path containerSignatureFile = profileHome.resolve(profile.getContainerSignatureFileName());
        final Path submissionQueueFile = profileHome.resolve("submissionQueue.xml");

        // Some global properties are needed to initialise the profile context.
        final Properties props = new Properties();
        props.setProperty("defaultThrottle", String.valueOf(profile.getThrottle()));
        props.setProperty("signatureFilePath", signatureFile.toAbsolutePath().toString());
        props.setProperty("submissionQueueFile", submissionQueueFile.toAbsolutePath().toString());
        props.setProperty("tempDirLocation", globalConfig.getTempDir().toAbsolutePath().toString());
        props.setProperty("profileHome", profileHome.toAbsolutePath().toString());
        
        props.setProperty("containerSigPath", containerSignatureFile.toAbsolutePath().toString());

        props.setProperty("processTar", String.valueOf(profile.getProcessTarFiles()));
        props.setProperty("processZip", String.valueOf(profile.getProcessZipFiles()));
        props.setProperty("processGzip", String.valueOf(profile.getProcessGzipFiles()));
        props.setProperty("processRar", String.valueOf(profile.getProcessRarFiles()));
        props.setProperty("process7zip", String.valueOf(profile.getProcess7zipFiles()));
        props.setProperty("processIso", String.valueOf(profile.getProcessIsoFiles()));
        props.setProperty("processBzip2", String.valueOf(profile.getProcessBzip2Files()));

        props.setProperty("processArc", String.valueOf(profile.getProcessArcFiles()));
        props.setProperty("processWarc", String.valueOf(profile.getProcessWarcFiles()));

        props.setProperty("generateHash", String.valueOf(profile.getGenerateHash()));
        props.setProperty("hashAlgorithm", String.valueOf(profile.getHashAlgorithm()));
        props.setProperty("maxBytesToScan", String.valueOf(profile.getMaxBytesToScan()));
        props.setProperty("matchAllExtensions", String.valueOf(profile.getMatchAllExtensions()));
 
        String createUrl = globalConfig.getProperties().getString("database.createUrl");
        if (createUrl == null || createUrl.isEmpty()) {
            createUrl = "{none}";
        }
        props.setProperty(CREATE_URL, createUrl);
        props.setProperty(DATABASE_URL, String.format("jdbc:derby:%s", databasePath.toAbsolutePath().toString()));
        TemplateStatus status = null;
        final boolean newDatabase = !Files.exists(databasePath);
        if (newDatabase) {
            final Path profileTemplate = getProfileTemplateFile(profile);
            status = getTemplateStatus(profileTemplate);
            status = setupDatabaseTemplate(status, profileTemplate, databasePath);
        }
        /*
        else {
            //BNO: No longer reqiured?
            //setCreateSchemaProperties(false, props);
        }
        */
        //BNO
        if (status == TemplateStatus.NO_TEMPLATE ) {
        //if (status != TemplateStatus.SIGNATURE_TEMPLATE ) {
            // If we're starting with a fresh DROID install, we'll get a SQL Exception if we try to connect to the
            // Derby database (the database will exist but the DROID_USER and schema objects will not be there yet.
            // TODO: Probably only want to do this if actually using this class - so query spring for whether
            // this is the case..
            JDBCBatchResultHandlerDao.setIsFreshTemplate(true);
        }

        ProfileInstanceManager profileManager = profileInstanceLocator.getProfileInstanceManager(profile, props);

        //if (status == TemplateStatus.NO_TEMPLATE ) {
            JDBCBatchResultHandlerDao.setIsFreshTemplate(false);
       // }
        
        if (newDatabase) {
            generateNewDatabaseAndTemplates(profile, profileManager, databasePath, signatureFile, status);
        }
        
        return profileManager;
    }
    //CHECKSTYLE:ON
    
    private void setCreateSchemaProperties(boolean create, Properties props) {
        if (create) {
            props.setProperty(HIBERNATE_GENERATE_DDL, "true");
            props.setProperty(HIBERNATE_CREATE, "create");
        } else {
            props.setProperty(HIBERNATE_CREATE, "none");
            props.setProperty(HIBERNATE_GENERATE_DDL, "false");
        }
    }
    
    private TemplateStatus setupDatabaseTemplate(final TemplateStatus status,
            final Path profileTemplate,
            final Path databasePath) {
        TemplateStatus result = status;
        
        // if no profile template exists, generate a fresh profile database:
        //if (status == TemplateStatus.NO_TEMPLATE) {
            //BNO - no longer required:
            //setCreateSchemaProperties(true, props);

        //} else { // we either have a blank profile, or a signature profile: unpack the profile template:
        if (status != TemplateStatus.NO_TEMPLATE) {
            //BNO - no longer required:
            //setCreateSchemaProperties(false, props);
            try {
                unpackProfileTemplate(profileTemplate, databasePath);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                // could not load profile template - fall back on creating one from scratch.
                //BNO. No Longer required?
                //setCreateSchemaProperties(true, props);
                result = TemplateStatus.NO_TEMPLATE;
            }
        }

        return result;
    }
   
    private void generateNewDatabaseAndTemplates(final ProfileInstance profile, 
            final ProfileInstanceManager profileManager, 
            final Path databasePath,
            final Path signatureFile,
            final TemplateStatus status) {
        
        // If we were starting with no template at all, we now have a blank profile we can use as a blank template:
        if (status == TemplateStatus.NO_TEMPLATE) {
            // freeze the database to allow safe copying
            freezeDatabase(profile.getUuid());
            packProfileTemplate(databasePath, getTemplateFile(BLANK_PROFILE));
            thawDatabase(profile.getUuid());
        }
        // If we don't have a signature template, then we need to 
        // populate the database with signature file metadata.
        if (status != TemplateStatus.SIGNATURE_TEMPLATE) {
            try {
                profileManager.initProfile(signatureFile.toUri());
                // store this database as a profile template for this 
                // signature file version:
                final String name = getTemplateNameForSignatureVersion(profile.getSignatureFileVersion());
                Path templateFile = getTemplateFile(name);
                freezeDatabase(profile.getUuid());
                packProfileTemplate(databasePath, templateFile);
                thawDatabase(profile.getUuid());
            } catch (SignatureFileException e) {
                String message = "Error reading signature file";
                log.error(message, e);
                throw new ProfileException(message, e);
            }
        } 
    }
    
    private TemplateStatus getTemplateStatus(final Path profileTemplateFile) {
        TemplateStatus status = TemplateStatus.NO_TEMPLATE;
        if (profileTemplateFile != null) {
            if (BLANK_PROFILE.equals(FileUtil.fileName(profileTemplateFile))) {
                status = TemplateStatus.BLANK_TEMPLATE;
            } else if (FileUtil.fileName(profileTemplateFile).matches(SIG_PROFILE)) {
                status = TemplateStatus.SIGNATURE_TEMPLATE;
            }
        }
        return status;
    }

    
    private Path getProfileTemplateFile(ProfileInstance profile) {
        final String sigTemplateName = getTemplateNameForSignatureVersion(profile.getSignatureFileVersion());
        Path profileTemplate = getTemplateFile(sigTemplateName);
        if (!Files.exists(profileTemplate)) {
            profileTemplate = getTemplateFile(BLANK_PROFILE);
            if (!Files.exists(profileTemplate)) {
                profileTemplate = null;
            }
        }
        return profileTemplate;
    }

    
    private String getTemplateNameForSignatureVersion(Integer signatureVersion) {
        return String.format("profile.%d.template", signatureVersion);
    }
    
    private Path getTemplateFile(String templateFileName) {
        final Path templateDir = globalConfig.getProfileTemplateDir();
        return templateDir.resolve(templateFileName);
    }
    
    
    private void unpackProfileTemplate(final Path profileTemplate, final Path copyToDirectory) throws IOException {
        Files.createDirectories(copyToDirectory);
        final ProfileDiskAction unpacker = new ProfileDiskAction();
        final ProgressObserver observe = new ProgressObserver() {
            @Override
            public void onProgress(Integer progress) {
            }
        };
        unpacker.load(profileTemplate, copyToDirectory, observe);
    }
    
    
    private void packProfileTemplate(final Path databaseDir, final Path profileTemplate)  {
        ProfileDiskAction packer = new ProfileDiskAction();
        ProgressObserver observe = new ProgressObserver() {
            @Override
            public void onProgress(Integer progress) {
            }
        };
        try {
            packer.saveProfile(databaseDir, profileTemplate, observe);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
    
    
    /**
     * @param profileInstanceLocator the profileInstanceLocator to set
     */
    public void setProfileInstanceLocator(
            ProfileInstanceLocator profileInstanceLocator) {
        this.profileInstanceLocator = profileInstanceLocator;
    }

    /**
     * @param profileName the profile ID
     * @return true if the context has this profile; false otherwise
     */
    public boolean hasProfileContext(String profileName) {
        return profileInstances.containsKey(profileName);
    }
    
    /**
     * @param globalConfig the globalConfig to set
     */
    public void setGlobalConfig(DroidGlobalConfig globalConfig) {
        this.globalConfig = globalConfig;
    }
    
}
