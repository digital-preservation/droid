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
package uk.gov.nationalarchives.droid.profile.config;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalConfig;
import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalProperty;
import uk.gov.nationalarchives.droid.core.interfaces.config.RuntimeConfig;

/**
 * @author rflitcroft
 *
 */
public class DroidGlobalConfigTest {

    private File droidProperties;

    @BeforeClass
    public static void clearSystemProperties() {
        System.clearProperty(RuntimeConfig.DROID_USER);
    }
    
    @Before
    public void setup() {
        
        assertNull(System.getenv().get(RuntimeConfig.DROID_USER));
        assertNull(System.getProperty(RuntimeConfig.DROID_USER));
        RuntimeConfig.configureRuntimeEnvironment();
        
        String userHome = System.getProperty("user.home");
        File droidHome = new File(userHome, ".droid6");
        droidProperties = new File(droidHome, DroidGlobalConfig.DROID_PROPERTIES);
        droidProperties.delete();
    }
    
    @After
    public void tearDown() {
        System.clearProperty(RuntimeConfig.DROID_USER);
    }
    
    @Test
    public void testDefaultHome() throws IOException {
        DroidGlobalConfig config = new DroidGlobalConfig();
        File expectedHome = new File(System.getProperty("user.home"), ".droid6");
        assertEquals(expectedHome, config.getDroidWorkDir());
    }
    
    @Test
    public void testSystemPropertyHome() throws IOException {
        System.setProperty(RuntimeConfig.DROID_USER, "custom_home");
        try {
            DroidGlobalConfig config = new DroidGlobalConfig();
            File expectedHome = new File("custom_home");
            assertEquals(expectedHome, config.getDroidWorkDir());
        } finally {
            System.clearProperty(RuntimeConfig.DROID_USER);
        }
    }

    @Test
    public void testInitialisationWritesDefaultConfigInDefaultLocation() throws Exception {
        
        DroidGlobalConfig config = new DroidGlobalConfig();
        config.init();
        
        PropertiesConfiguration props = new PropertiesConfiguration(droidProperties);
        assertEquals(0, props.getInt(DroidGlobalProperty.DEFAULT_THROTTLE.getName()));
    }

    @Test
    public void testGetProfileProperties() throws Exception {
        
        DroidGlobalConfig config = new DroidGlobalConfig();
        config.init();
        
        Properties profileProperties = config.getProfileProperties();
        assertEquals("0", profileProperties.getProperty("defaultThrottle"));
    }
    
    @Test
    public void testUpdate() throws Exception {
        DroidGlobalConfig config = new DroidGlobalConfig();
        config.init();
        
        Map<String, Object> changedProperties = new HashMap<String, Object>();
        changedProperties.put(DroidGlobalProperty.DEFAULT_THROTTLE.getName(), "123");

        config.update(changedProperties);
        
        assertEquals(123, config.getProperties().getInt(DroidGlobalProperty.DEFAULT_THROTTLE.getName()));
        
        PropertiesConfiguration props = new PropertiesConfiguration(droidProperties);
        assertEquals(123, props.getInt(DroidGlobalProperty.DEFAULT_THROTTLE.getName()));
    }
}
