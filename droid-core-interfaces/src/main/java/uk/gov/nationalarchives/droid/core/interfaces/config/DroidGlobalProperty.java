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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.Configuration;

/**
 * All DROID global properties.
 * @author rflitcroft
 *
 */
public enum DroidGlobalProperty {
    

    /** Default throttle. */
    DEFAULT_THROTTLE("profile.defaultThrottle", PropertyType.INTEGER, true),
    
    /** THe default version for signature files. */
    DEFAULT_BINARY_SIG_FILE_VERSION("profile.defaultBinarySigFileVersion", PropertyType.TEXT, true),
    
    /** THe default version for signature files. */
    DEFAULT_CONTAINER_SIG_FILE_VERSION("profile.defaultContainerSigFileVersion", PropertyType.TEXT, true),

    /** THe default version for signature files. */
    DEFAULT_TEXT_SIG_FILE_VERSION("profile.defaultTextSigFileVersion", PropertyType.TEXT, true),

    /** PRONOM Update URL. */
    BINARY_UPDATE_URL("pronom.update.url", PropertyType.TEXT, true),

    /** PRONOM Update URL. */
    CONTAINER_UPDATE_URL("container.update.url", PropertyType.TEXT, true),

    /** PRONOM Update URL. */
    TEXT_UPDATE_URL("text.update.url", PropertyType.TEXT, true),

    /** Update auto check. */
    UPDATE_AUTO_CHECK("update.autoCheck", PropertyType.BOOLEAN, true),
    
    /** Update frequency of days to check for updates. */
    UPDATE_FREQUENCY_DAYS("update.frequency.days", PropertyType.INTEGER, true),
    
    /** Check for updates on startup. */
    UPDATE_ON_STARTUP("update.frequency.startup", PropertyType.BOOLEAN, true),
    
    /** Update proxy server used. */
    UPDATE_USE_PROXY("update.proxy", PropertyType.BOOLEAN, true),
    
    /** Update proxy host. */
    UPDATE_PROXY_HOST("update.proxy.host", PropertyType.TEXT, true),

    /** Update proxy port. */
    UPDATE_PROXY_PORT("update.proxy.port", PropertyType.INTEGER, true),
    
    /** Autoset the default signature to latest downloaded. */
    UPDATE_AUTOSET_DEFAULT("update.autoSetDefault", PropertyType.BOOLEAN, true),
    
    /** Autoset the default signature to latest downloaded. */
    UPDATE_DOWNLOAD_PROMPT("update.downloadPrompt", PropertyType.BOOLEAN, true),

    /** Autoset the default signature to latest downloaded. */
    LAST_UPDATE_CHECK("update.lastCheck", PropertyType.LONG, false), 
    
    /** Development mode. */
    DEV_MODE("development_mode", PropertyType.BOOLEAN, false),
    
    /** Whether to process files in archives. */
    PROCESS_ARCHIVES("profile.processArchives", PropertyType.BOOLEAN, true),

    /** Whether to process files in archives. */
    PUID_URL_PATTERN("puid.urlPattern", PropertyType.TEXT, true), 
    
    /** Generate hashes for each file analysed?. */
    GENERATE_HASH("profile.generateHash", PropertyType.BOOLEAN, true),
    
    /** CSV Export one row per format. */
    CSV_EXPORT_ROW_PER_FORMAT("export.rowPerFormat", PropertyType.BOOLEAN, true),
    
    /** The max number of bytes to scan from the beginning or
     * end of a file, or negative, meaning unlimited scanning.
     */
    MAX_BYTES_TO_SCAN("profile.maxBytesToScan", PropertyType.LONG, true),
    
    
    /** Whether to match all extensions, or just ones without another signature attached. */
    EXTENSION_ALL("profile.matchAllExtensions", PropertyType.BOOLEAN, true),
    
    /** Whether the database plays safe (=true), or gains performance
     * but loses resilience in the face of failures (=false).
     */
    DATABASE_DURABILITY("database.durability", PropertyType.BOOLEAN, true); 
    
    private static Map<String, DroidGlobalProperty> allValues = new HashMap<String, DroidGlobalProperty>(); 
    
    static {
        for (DroidGlobalProperty prop : DroidGlobalProperty.values()) {
            allValues.put(prop.getName(), prop);
        }
    }
    
    private String name;
    private PropertyType type;
    private boolean userConfigurable;
    
    private DroidGlobalProperty(String name, PropertyType type, boolean userConfigurable) {
        this.name = name;
        this.type = type;
        this.userConfigurable = userConfigurable;
    }
    
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * @return the numeric
     */
    public PropertyType getType() {
        return type;
    }
    
    /**
     * @return the userConfigurable
     */
    boolean isUserConfigurable() {
        return userConfigurable;
    }
    
    /**
     * @author rflitcroft
     *
     */
    public static enum PropertyType {
        /** Text. */
        TEXT { 
            @Override
            public Object getTypeSafeValue(Configuration config, String key) {
                return config.getString(key);
            }
        },
        
        /** Numeric. */
        INTEGER {
            @Override
            public Object getTypeSafeValue(Configuration config, String key) {
                return config.getInt(key);
            }
        },
        
        /** Boolean. */
        BOOLEAN {
            @Override
            public Object getTypeSafeValue(Configuration config, String key) {
                return config.getBoolean(key);
            }
        }, 
        
        /** Long Integer. */
        LONG {
            @Override
            public Object getTypeSafeValue(Configuration config, String key) {
                return config.getBigInteger(key);
            }
        };

        /**
         * Converts a String property to a type-safe value.
         * @param config the configuration
         * @param key the key
         * @return a type-safe object
         */
        public abstract Object getTypeSafeValue(Configuration config, String key);
        
    }

    /**
     * @param key the name
     * @return a DroidGlobalProperty
     */
    public static DroidGlobalProperty forName(String key) {
        DroidGlobalProperty property = allValues.get(key);
        return property != null && property.isUserConfigurable() ? property : null;
    }


}
