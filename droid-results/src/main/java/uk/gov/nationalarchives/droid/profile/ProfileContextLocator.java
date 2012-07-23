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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalConfig;
import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalProperty;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileException;
import uk.gov.nationalarchives.droid.results.handlers.ProgressObserver;


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
    
    private final Log log = LogFactory.getLog(getClass());
    private DroidGlobalConfig globalConfig;
    
    private enum TemplateStatus { NO_TEMPLATE, BLANK_TEMPLATE, SIGNATURE_TEMPLATE };

    @SuppressWarnings("unchecked")
    private Map<String, ProfileInstance> profileInstances = 
        LazyMap.decorate(new HashMap<String, ProfileInstance>(), new ProfileTransformer());
    
    private ProfileInstanceLocator profileInstanceLocator;
    
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
            profileInstance.setGenerateHash(globalConfig.getProperties()
                    .getBoolean(DroidGlobalProperty.GENERATE_HASH.getName()));
            profileInstance.setProcessArchiveFiles(globalConfig.getProperties()
                    .getBoolean(DroidGlobalProperty.PROCESS_ARCHIVES.getName()));
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
     * Shuts down the database for the specified profile, releasing all connections
     * and resources.
     * @param profileId the id of a profile.
     */
    public void shutdownDatabase(String profileId) {
        profileInstanceLocator.shutdownDatabase(profileId);
    }
    
    /**
     * Opens a profile instance manager for a pre-existing profile context.
     * @param profile the profile to obtain a profile manager for.
     * @return a profile instance manager for a pre-existing profile context
     */
    public ProfileInstanceManager openProfileInstanceManager(ProfileInstance profile) {
        
        File profileHome = new File(globalConfig.getProfilesDir(), profile.getUuid());
        File databasePath = new File(profileHome, "/db");
        File signatureFile = new File(profileHome, profile.getSignatureFileName());
        File containerSignatureFile = new File(profileHome, profile.getContainerSignatureFileName());
        File submissionQueueFile = new File(profileHome, "submissionQueue.xml");

        // Some global properties are needed to initialise the profile context.
        Properties props = new Properties();
        props.setProperty("defaultThrottle", String.valueOf(profile.getThrottle()));
        props.setProperty("signatureFilePath", signatureFile.getPath());
        props.setProperty("submissionQueueFile", submissionQueueFile.getPath());
        props.setProperty("tempDirLocation", globalConfig.getTempDir().getPath());
        props.setProperty("profileHome", profileHome.getPath());
        
        props.setProperty("containerSigPath", containerSignatureFile.getPath()); 
        props.setProperty("processArchives", String.valueOf(profile.getProcessArchiveFiles()));
        props.setProperty("generateHash", String.valueOf(profile.getGenerateHash()));
        props.setProperty("maxBytesToScan", String.valueOf(profile.getMaxBytesToScan()));
        props.setProperty("matchAllExtensions", String.valueOf(profile.getMatchAllExtensions()));

        String createUrl = globalConfig.getProperties().getString("database.createUrl");
        if (createUrl == null || createUrl.isEmpty()) {
            createUrl = "{none}";
        }
        props.setProperty(CREATE_URL, createUrl);
        props.setProperty(DATABASE_URL, String.format("jdbc:derby:%s", databasePath.getPath()));
        
        TemplateStatus status = null;
        final boolean newDatabase = !databasePath.exists();        
        if (newDatabase) {
            File profileTemplate = getProfileTemplateFile(profile);
            status = getTemplateStatus(profileTemplate);
            status = setupDatabaseTemplate(status, profileTemplate, databasePath, props);
        } else {
            setCreateSchemaProperties(false, props);
        }
        
        ProfileInstanceManager profileManager = profileInstanceLocator.getProfileInstanceManager(profile, props);
        
        if (newDatabase) {
            generateNewDatabaseAndTemplates(profile, profileManager, databasePath, signatureFile, status);
        }
        
        return profileManager;
    }

    private void setCreateSchemaProperties(boolean create, Properties props) {
        if (create) {
            props.setProperty(HIBERNATE_GENERATE_DDL, "true");
            props.setProperty(HIBERNATE_CREATE, "create");
        } else {
            props.setProperty(HIBERNATE_CREATE, "none");
            props.setProperty(HIBERNATE_GENERATE_DDL, "false");
        }
    }
    
    private TemplateStatus setupDatabaseTemplate(TemplateStatus status, 
            File profileTemplate,
            File databasePath,
            Properties props) {
        TemplateStatus result = status;
        
        // if no profile template exists, generate a fresh profile database:
        if (status == TemplateStatus.NO_TEMPLATE) {
            setCreateSchemaProperties(true, props);
        } else { // we either have a blank profile, or a signature profile: unpack the profile template:
            setCreateSchemaProperties(false, props);
            try {
                unpackProfileTemplate(profileTemplate, databasePath);
            } catch (IOException e) {
                log.error(e);
                // could not load profile template - fall back on creating one from scratch.
                setCreateSchemaProperties(true, props);
                result = TemplateStatus.NO_TEMPLATE;
            }
        }

        return result;
    }
    
    private void generateNewDatabaseAndTemplates(final ProfileInstance profile, 
            final ProfileInstanceManager profileManager, 
            final File databasePath,
            final File signatureFile,
            final TemplateStatus status) {
        
        // If we were starting with no template at all, we now have a blank profile we can use as a blank template:
        if (status == TemplateStatus.NO_TEMPLATE) {
            shutdownDatabase(profile.getUuid());
            packProfileTemplate(databasePath, getTemplateFile(BLANK_PROFILE));
            bootDatabase(profile.getUuid());
        }
        // If we don't have a signature template, then we need to 
        // populate the database with signature file metadata.
        if (status != TemplateStatus.SIGNATURE_TEMPLATE) {
            try {
                profileManager.initProfile(signatureFile.toURI());
                // store this database as a profile template for this 
                // signature file version:
                final String name = getTemplateNameForSignatureVersion(profile.getSignatureFileVersion());
                File templateFile = getTemplateFile(name);
                shutdownDatabase(profile.getUuid());
                packProfileTemplate(databasePath, templateFile);
                bootDatabase(profile.getUuid());
            } catch (SignatureFileException e) {
                String message = "Error reading signature file";
                log.error(message, e);
                throw new ProfileException(message, e);
            }
        } 
    }
    
    private TemplateStatus getTemplateStatus(File profileTemplateFile) {
        TemplateStatus status = TemplateStatus.NO_TEMPLATE;
        if (profileTemplateFile != null) {
            if (BLANK_PROFILE.equals(profileTemplateFile.getName())) {
                status = TemplateStatus.BLANK_TEMPLATE;
            } else if (profileTemplateFile.getName().matches(SIG_PROFILE)) {
                status = TemplateStatus.SIGNATURE_TEMPLATE;
            }
        }
        return status;
    }

    
    private File getProfileTemplateFile(ProfileInstance profile) {
        final String sigTemplateName = getTemplateNameForSignatureVersion(profile.getSignatureFileVersion());
        File profileTemplate = getTemplateFile(sigTemplateName);
        if (!profileTemplate.exists()) {
            profileTemplate = getTemplateFile(BLANK_PROFILE);
            if (!profileTemplate.exists()) {
                profileTemplate = null;
            }
        }
        return profileTemplate;
    }

    
    private String getTemplateNameForSignatureVersion(Integer signatureVersion) {
        return String.format("profile.%d.template", signatureVersion);
    }
    
    private File getTemplateFile(String templateFileName) {
        File templateDir = globalConfig.getProfileTemplateDir();
        return new File(templateDir, templateFileName);
    }
    
    
    private void unpackProfileTemplate(File profileTemplate, File copyToDirectory) throws IOException {
        copyToDirectory.mkdir();
        ProfileDiskAction unpacker = new ProfileDiskAction();
        ProgressObserver observe = new ProgressObserver() {
            @Override
            public void onProgress(Integer progress) {
            }
        };
        unpacker.load(profileTemplate, copyToDirectory, observe);
    }
    
    
    private void packProfileTemplate(File databaseDir, File profileTemplate)  {
        ProfileDiskAction packer = new ProfileDiskAction();
        ProgressObserver observe = new ProgressObserver() {
            @Override
            public void onProgress(Integer progress) {
            }
        };
        try {
            packer.saveProfile(databaseDir.getPath(), profileTemplate, observe);
        } catch (IOException e) {
            log.error(e);
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
     * Boots the database for the specified profile.
     * @param profileId the id of a profile.
     */
    public void bootDatabase(String profileId) {
        profileInstanceLocator.bootDatabase(profileId);
    }
    
    /**
     * @param globalConfig the globalConfig to set
     */
    public void setGlobalConfig(DroidGlobalConfig globalConfig) {
        this.globalConfig = globalConfig;
    }
    
}
