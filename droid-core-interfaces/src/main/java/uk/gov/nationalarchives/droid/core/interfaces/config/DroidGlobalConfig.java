/**
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gsi.gov.uk>
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
package uk.gov.nationalarchives.droid.core.interfaces.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

//BNO new libraries

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static uk.gov.nationalarchives.droid.core.interfaces.config.RuntimeConfig.DROID_USER;
import static uk.gov.nationalarchives.droid.core.interfaces.config.RuntimeConfig.DROID_TEMP_DIR;

/**
 * @author rflitcroft
 * 
 */
public class DroidGlobalConfig {

    /** The name of the DROID properties file. */
    public static final String DROID_PROPERTIES = "droid.properties";    
    
    /**
     * 
     */
    private static final String DEFAULT_DROID_PROPERTIES = "default_droid.properties";

    //FIXME: update to latest signature file before release.
    private static final String DROID_SIGNATURE_FILE = "DROID_SignatureFile_V84.xml";
    private static final String CONTAINER_SIGNATURE_FILE = "container-signature-20160121.xml";
    private static final String TEXT_SIGNATURE_FILE = "text-signature-20101101.xml";
    
    private static final String DATABASE_DURABILITY = "database.durability";
    private static final String AVAILABLE_HASH_ALGORITHMS = "availableHashAlgorithms";
    
    // UPDATE THIS SCHEMA VERSION IF THE DATABASE SCHEMA CHANGES.
    private static final String TEMPLATE_SCHEMA_VERSION = "schema 6.04";

    // BNO - to exclude availableHashAlgorithms (and possibly other settings in future) getting written to the
    // droid.properties file when settings are saved.  // See comments under update() method
    private static final List<String> NON_CONFIGURABLE_PROPERTIES = Arrays.asList(AVAILABLE_HASH_ALGORITHMS);
    
    private final Log log = LogFactory.getLog(getClass());
    
    private File droidWorkDir;
    private File signatureFilesDir;
    private File profileTemplateDir;
    private File containerSignatureDir;
    private File textSignatureFileDir;
    private File reportDefinitionDir;
    private File filterDir;
    
    private PropertiesConfiguration props;

    private File profilesDir;
    private File tempDir;

    /**
     * Default Constructor. Initialises the droid home directory.
     * @throws IOException if there was an error writing the signature file.
     */
    public DroidGlobalConfig() throws IOException {
        String droidHomePath = System.getProperty(DROID_USER);
        droidWorkDir = new File(droidHomePath);
        droidWorkDir.mkdirs();
        
        // always recreate the signature files if they don't exist:
        signatureFilesDir = new File(droidWorkDir, "signature_files");
        signatureFilesDir.mkdir();
        createResourceFile(signatureFilesDir, DROID_SIGNATURE_FILE, DROID_SIGNATURE_FILE);

        containerSignatureDir = new File(droidWorkDir, "container_sigs");
        containerSignatureDir.mkdir();
        createResourceFile(containerSignatureDir, CONTAINER_SIGNATURE_FILE, CONTAINER_SIGNATURE_FILE);
        
        /*
        signatureFilesDir = new File(droidWorkDir, "signature_files");
        if (signatureFilesDir.mkdir()) {
            createResourceFile(signatureFilesDir, DROID_SIGNATURE_FILE, DROID_SIGNATURE_FILE);
        }

        containerSignatureDir = new File(droidWorkDir, "container_sigs");
        if (containerSignatureDir.mkdir()) {
            createResourceFile(containerSignatureDir, CONTAINER_SIGNATURE_FILE, CONTAINER_SIGNATURE_FILE);
        }
        
        textSignatureFileDir = new File(droidWorkDir, "text_sigs");
        textSignatureFileDir.mkdir();
        //if (textSignatureFileDir.mkdir()) {
            //createResourceFile(textSignatureFileDir, TEXT_SIGNATURE_FILE);
        //}
        */

        reportDefinitionDir = new File(droidWorkDir, "report_definitions");
        reportDefinitionDir.mkdir();

        filterDir = new File(droidWorkDir, "filter_definitions");
        filterDir.mkdir();
        
        // Ensure base directory is created.
        profileTemplateDir = new File(droidWorkDir, "profile_templates");
        profileTemplateDir.mkdir();
        
        // Now create the schema version sub-directory:
        profileTemplateDir = new File(profileTemplateDir, TEMPLATE_SCHEMA_VERSION);
        profileTemplateDir.mkdir();
        
        // Get the default temporary area:
        String droidTempPath = System.getProperty(DROID_TEMP_DIR);
        
        profilesDir = new File(droidTempPath, "profiles");
        profilesDir.mkdirs();
        
        tempDir = new File(droidTempPath, "tmp");
        tempDir.mkdirs();
    }

    /**
     * Initialises the droid config bean.
     * 
     * @throws ConfigurationException
     *             if the config could not be intialised
     */
    public void init() throws ConfigurationException {

        File droidProperties = new File(droidWorkDir, DROID_PROPERTIES);
        // Read the properties form the configuration file
        props = new PropertiesConfiguration(droidProperties);

        URL defaultPropsUrl = getClass().getClassLoader().getResource(
                DEFAULT_DROID_PROPERTIES);
        PropertiesConfiguration defaultProps = new PropertiesConfiguration(
                defaultPropsUrl);

        /**
        if (!droidProperties.exists()) {
            try {
                createResourceFile(droidProperties, DROID_PROPERTIES, DEFAULT_DROID_PROPERTIES);
            } catch (IOException e) {
                final String message = String.format("Could not create default property file at: %s",
                        droidProperties.getAbsolutePath());
                log.error(message, e);
            }
        }
        */
        
        // Adds any new properties from the defaults into the existing
        // properties file, or creates it if it was not there to begin 
        // with.
        boolean saveProperties = false;
        for (Iterator<String> it = defaultProps.getKeys(); it.hasNext();) {
            String key = it.next();
            if (!props.containsKey(key)) {
                props.addProperty(key, defaultProps.getProperty(key));
                saveProperties = true;
            }
        }

        if (saveProperties) {
            props.save();
        }
        
        if (props.containsKey(DATABASE_DURABILITY)) {
            boolean durability = props.getBoolean(DATABASE_DURABILITY);
            if (!durability) {
                System.setProperty("derby.system.durability", "test");
            }
        }
    }

    /**
     * @return the droidHomeDir
     */
    public File getDroidWorkDir() {
        return droidWorkDir;
    }

    /**
     * @return all profile-realted properties
     */
    public Properties getProfileProperties() {
        Properties profileProperties = new Properties();
        
        final Configuration profilePropsConfig = props.subset("profile");
        for (Iterator<String> it = profilePropsConfig.getKeys(); it.hasNext();) {
            String key = it.next();
            profileProperties.setProperty(key, profilePropsConfig.getProperty(key).toString());
        }
        
        return profileProperties;
    }

    /**
     * @return the property configuration;
     */
    public PropertiesConfiguration getProperties() {
        return props;
    }

    /**
     * Updates the config with the properties given and persists. 
     * @param properties the changed properties
     * @throws ConfigurationException if the config could not be saved.
     */
    public void update(Map<String, Object> properties) throws ConfigurationException {
        for (Entry<String, Object> entry : properties.entrySet()) {
            //BNO to stop us updating droid.properties with values that aren't user configurable
            // See comments under getPropertiesMap below
            if (!NON_CONFIGURABLE_PROPERTIES.contains(entry.getKey())) {
                props.setProperty(entry.getKey(), entry.getValue());
            }
        }
        
        props.save();
    }

    /**
     * @return all settings in a map
     */
    public Map<String, Object> getPropertiesMap() {
        final Map<String, Object> allSettings = new HashMap<String, Object>();
        for (Iterator<String> it = props.getKeys(); it.hasNext();) {
            String key = it.next();
            DroidGlobalProperty property = DroidGlobalProperty.forName(key);
            if (property != null) {
                allSettings.put(key, property.getType().getTypeSafeValue(props, key));
            }
        }

        // BNO: To note - The available hash algorithms are better hard coded here than in the
        // droid.properties file since the list is not user configurable.  However, the existing droid.properties
        // profile.hashAlgorithm can still be used to indicate the default selection.
        List<String> availableHashAlgorithms = new ArrayList<String>();

        availableHashAlgorithms.add("md5");
        availableHashAlgorithms.add("sha1");
        availableHashAlgorithms.add("sha256");

        allSettings.put(AVAILABLE_HASH_ALGORITHMS, availableHashAlgorithms);

        return allSettings;
    }
    
    /**
     * 
     * @return the directory where droid signature files reside.
     */
    public File getSignatureFileDir() {
        return signatureFilesDir;
    }
    
    /**
     * 
     * @return the directory where droid profile templates reside.
     */
    public File getProfileTemplateDir() {
        return profileTemplateDir;
    }
    
    /**
     * @return the containerSignatureDir
     */
    public File getContainerSignatureDir() {
        return containerSignatureDir;
    }
    
    /**
     * @return the textSignatureFileDir
     */
    public File getTextSignatureFileDir() {
        return textSignatureFileDir;
    }
    
    /**
     * 
     * @return the reportDefinitionDir
     */
    public File getReportDefinitionDir() {
        return reportDefinitionDir;
    }
    
    /**
     * @return the profilesDir
     */
    public File getProfilesDir() {
        return profilesDir;
    }

    /**
     * 
     * @return the filterDir.
     */
    public File getFilterDir() {
        return filterDir;
    }
    
    /**
     * @return the directory for droid temporary files
     */
    public File getTempDir() {
        return tempDir;
    }
    
    private void createResourceFile(File resourceDir, String fileName, String resourceName) throws IOException {
        InputStream in = getClass().getClassLoader().getResourceAsStream(resourceName);
        if (in == null) {
            log.warn("Resource not found: " + resourceName);
        } else {
            File resourcefile = new File(resourceDir, fileName);
            if (resourcefile.createNewFile()) {
                OutputStream out = new FileOutputStream(resourcefile);
                try {
                    IOUtils.copy(in, out);
                } finally {
                    if (out != null) {
                        out.close();
                    }
                    if (in != null) {
                        in.close();
                    }
                }
            }
        }
    }
    
    /**
     * Returns the value of the boolean property specified.
     * The runtime exception will be thrown if the specified property has no boolean representation.
     * @param propertyKey the property
     * @return the boolean value
     */
    public boolean getBooleanProperty(DroidGlobalProperty propertyKey) {
        return props.getBoolean(propertyKey.getName());
    }
    
}
