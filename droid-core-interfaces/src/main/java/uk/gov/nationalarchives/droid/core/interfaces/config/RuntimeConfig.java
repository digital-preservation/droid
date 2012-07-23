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
package uk.gov.nationalarchives.droid.core.interfaces.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;


/**
 * Global runtime configuration utility.
 * @author rflitcroft
 *
 */
public final class RuntimeConfig {

    
    /**
     * The droid user system / environment property name.
     */
    public static final String DROID_USER = "droidUserDir";
    
    /**
     * The droid temporary folder to store temporary and active profiles in.
     */
    public static final String DROID_TEMP_DIR = "droidTempDir";
    
    /**
     * The droid log directory system / environment property name.
     */
    public static final String LOG_DIR = "droidLogDir";
    
    /**
     * The name of the default log folder.
     */
    public static final String DEFAULT_LOGFOLDER_NAME = "logs";

    
    /**
     *  The name of the default working area folder.
     */
    public static final String DEFAULT_DROIDFOLDER_NAME = ".droid6";
    
    
    /**
     * The name of the default log4j property file.
     * Will be created from a resource if it doesn't exist under the droid work system.
     */
    private static final String LOG4J_PROPERTIES = "log4j.properties";

    
    /**
     * The default logging threshold to the console.
     */
    private static final String CONSOLE_LOG_THRESHOLD = "consoleLogThreshold";
    
    
    /**
     * The name of the system property to set the log4j configuration file. 
     */
    private static final String LOG4J_CONFIGURATION = "log4j.configuration";
    
    
    /**
     * Error message if you cannot create a default logging file.
     */
    private static final String ERROR_CREATING_LOG4J_FILE = 
        "Could not create the default log4j property file [%s] at: %s";
    
    
    private RuntimeConfig() { }
    
    /**
     * Sets the "droidWorkDir" system property and the "droidLogDir" system property.
     * 
     */
    public static void configureRuntimeEnvironment() {
        
        // Configure the droid user area
        File defaultDroidWorkingFolder = new File(System.getProperty("user.home"), DEFAULT_DROIDFOLDER_NAME);
        File droidWorkDir = createFolderAndSystemProperty(DROID_USER, defaultDroidWorkingFolder);

        // Configure the droid temporary file and profile area:
        File droidTempDir = createFolderAndSystemProperty(DROID_TEMP_DIR, defaultDroidWorkingFolder);
        
        // Configure the droid log folder:
        File defaultDroidLogFolder = new File(droidWorkDir, DEFAULT_LOGFOLDER_NAME);
        File logDir = createFolderAndSystemProperty(LOG_DIR, defaultDroidLogFolder);
        
        // Specify the default droid log file.
        File logFile = new File(logDir, "droid.log");
        System.setProperty("logFile", logFile.getPath());
        
        // Configure default logging configuration.
        // If the user does not specify the log4j.configuration property
        String logConfig = getSystemOrEnvironmentProperty(LOG4J_CONFIGURATION);
        if (logConfig == null) {
            // No log4j configuration specified by system property or command line. 
            // Create a default log4j file here if it doesn't already exist under
            // the droid work area, and set log4j to use that.
            // Had too many issues where DROID was not picking up the default 
            // log4j.properties file.  Or it would work in the Eclipse environment, but
            // not in the final build produced by the build server.
            try {
                File logConfigFile = createResourceFile(droidWorkDir, LOG4J_PROPERTIES, LOG4J_PROPERTIES);
                String logFileURI = logConfigFile.toURI().toString();
                System.setProperty(LOG4J_CONFIGURATION, logFileURI);
            //CHECKSTYLE:OFF
            } catch (Exception e) {
            //CHECKSTYLE:ON
                String message = String.format(ERROR_CREATING_LOG4J_FILE, LOG4J_PROPERTIES, 
                        droidWorkDir.getAbsolutePath());
                throw new RuntimeException(message, e);
            }
        } else {
            File logConfigFile = new File(logConfig);
            String logFileURI = logConfigFile.toURI().toString();
            System.setProperty(LOG4J_CONFIGURATION, logFileURI);
        }
        
        // Set a default console logging level of INFO:
        String consoleLogThreshold = getSystemOrEnvironmentProperty(CONSOLE_LOG_THRESHOLD);
        if (consoleLogThreshold == null || consoleLogThreshold.isEmpty()) {
            System.setProperty(CONSOLE_LOG_THRESHOLD, "INFO");
        }
        
    }
    
    
    private static File createFolderAndSystemProperty(String property, File defaultFolder) {
        File folder;
        String folderPath = getSystemOrEnvironmentProperty(property);
        if (folderPath == null || folderPath.isEmpty()) {
            folder = defaultFolder;
        } else {
            folder = new File(folderPath);
        }
        folder.mkdirs();
        System.setProperty(property, folder.getPath());
        return folder;
    }
    
    
    private static String getSystemOrEnvironmentProperty(String property) {
        String value = System.getProperty(property);
        if (value == null) {
            value = System.getenv(property);
        }
        return value;
    }
    
    
    private static File createResourceFile(File resourceDir, String fileName, String resourceName) throws IOException {
        File resourceFile = null;
        InputStream in = RuntimeConfig.class.getClassLoader().getResourceAsStream(resourceName);
        if (in != null) {
            resourceFile = new File(resourceDir, fileName);
            if (resourceFile.createNewFile()) {
                OutputStream out = new FileOutputStream(resourceFile);
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
        return resourceFile;
    }    
    
}
