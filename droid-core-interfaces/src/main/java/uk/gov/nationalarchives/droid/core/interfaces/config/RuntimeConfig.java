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
package uk.gov.nationalarchives.droid.core.interfaces.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


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
     * The name of the default log4j2 property file.
     * Will be created from a resource if it doesn't exist under the droid work system.
     */
    private static final String LOG4J2_PROPERTIES = "log4j2.properties";

    
    /**
     * The default logging threshold to the console.
     */
    private static final String CONSOLE_LOG_THRESHOLD = "consoleLogThreshold";
    
    
    /**
     * The name of the system property to set the log4j configuration file. 
     */
    private static final String LOG4J2_CONFIGURATION_FILE = "log4j.configurationFile";
    
    
    /**
     * Error message if you cannot create a default logging file.
     */
    private static final String ERROR_CREATING_LOG4J2_FILE =
        "Could not create the default log4j2 property file [%s] at: %s";
    
    
    private RuntimeConfig() { }
    
    /**
     * Sets the "droidWorkDir" system property and the "droidLogDir" system property.
     * 
     */
    public static void configureRuntimeEnvironment() {

        final Path defaultDroidWorkingFolder = Paths.get(System.getProperty("user.home"), DEFAULT_DROIDFOLDER_NAME);
        final Path droidWorkDir;
        final Path droidTempDir;
        final Path logDir;
        try {
            // Configure the droid user area
            droidWorkDir = createFolderAndSystemProperty(DROID_USER, defaultDroidWorkingFolder);

            // Configure the droid temporary file and profile area:
            droidTempDir = createFolderAndSystemProperty(DROID_TEMP_DIR, defaultDroidWorkingFolder);

            // Configure the droid log folder:
            final Path defaultDroidLogFolder = droidWorkDir.resolve(DEFAULT_LOGFOLDER_NAME);
            logDir = createFolderAndSystemProperty(LOG_DIR, defaultDroidLogFolder);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        
        // Specify the default droid log file.
        final Path logFile = logDir.resolve("droid.log");
        System.setProperty("logFile", logFile.toAbsolutePath().toString());
        
        // Configure default logging configuration.
        // If the user does not specify the log4j.configurationFile property
        final String logConfig = getSystemOrEnvironmentProperty(LOG4J2_CONFIGURATION_FILE);
        if (logConfig == null) {
            // No log4j configuration specified by system property or command line. 
            // Create a default log4j file here if it doesn't already exist under
            // the droid work area, and set log4j to use that.
            // Had too many issues where DROID was not picking up the default 
            // log4j2.properties file.  Or it would work in the Eclipse environment, but
            // not in the final build produced by the build server.
            try {
                final Path logConfigFile = createResourceFile(droidWorkDir, LOG4J2_PROPERTIES, LOG4J2_PROPERTIES);
                String logFileURI = logConfigFile.toUri().toString();
                System.setProperty(LOG4J2_CONFIGURATION_FILE, logFileURI);
            //CHECKSTYLE:OFF
            } catch (Exception e) {
            //CHECKSTYLE:ON
                final String message = String.format(ERROR_CREATING_LOG4J2_FILE, LOG4J2_PROPERTIES,
                        droidWorkDir.toAbsolutePath().toString());
                throw new RuntimeException(message, e);
            }
        } else {
            final Path logConfigFile = Paths.get(URI.create(logConfig));
            final String logFileURI = logConfigFile.toUri().toString();
            System.setProperty(LOG4J2_CONFIGURATION_FILE, logFileURI);
        }
        
        // Set a default console logging level of INFO:
        final String consoleLogThreshold = getSystemOrEnvironmentProperty(CONSOLE_LOG_THRESHOLD);
        if (consoleLogThreshold == null || consoleLogThreshold.isEmpty()) {
            System.setProperty(CONSOLE_LOG_THRESHOLD, "INFO");
        }

    }

    private static Path createFolderAndSystemProperty(final String property, final Path defaultFolder) throws IOException {
        final String folderPath = getSystemOrEnvironmentProperty(property);
        final Path folder;
        if (folderPath == null || folderPath.isEmpty()) {
            folder = defaultFolder;
        } else {
            folder = Paths.get(folderPath);
        }
        Files.createDirectories(folder);
        System.setProperty(property, folder.toAbsolutePath().toString());
        return folder;
    }
    
    private static String getSystemOrEnvironmentProperty(final String property) {
        String value = System.getProperty(property);
        if (value == null) {
            value = System.getenv(property);
        }
        return value;
    }

    private static Path createResourceFile(final Path resourceDir, final String fileName, final String resourceName) throws IOException {
        try (final InputStream in = RuntimeConfig.class.getClassLoader().getResourceAsStream(resourceName)) {
            final Path resourceFile = resourceDir.resolve(fileName);
            if (!Files.exists(resourceFile)) {
                Files.copy(in, resourceFile);
            }
            return resourceFile;
        }
    }    
    
}
