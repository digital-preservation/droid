/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import uk.gov.nationalarchives.droid.core.interfaces.config.RuntimeConfig;

/**
 * @author rflitcroft
 *
 */
public class RuntimeConfigTest {

    @AfterClass
    public static void tearDown() {
        System.clearProperty(RuntimeConfig.DROID_USER);
    }
    
    @Before
    public void setup() {
        System.clearProperty(RuntimeConfig.DROID_USER);
    }
    
    @Test
    public void testConfigureRuntimeEnvironmentUsingSystemProperty() {
        System.clearProperty(RuntimeConfig.DROID_USER);
        System.clearProperty(RuntimeConfig.LOG_DIR);
        assertNull(System.getProperty(RuntimeConfig.DROID_USER));
        System.setProperty(RuntimeConfig.DROID_USER, "/tmp/droid");
        RuntimeConfig.configureRuntimeEnvironment();
        
        assertEquals(new File("/tmp/droid").getPath(), System.getProperty(RuntimeConfig.DROID_USER));
        assertEquals(new File("/tmp/droid/logs/droid.log").getPath(), System.getProperty("logFile"));

    }

    @Test
    public void testConfigureDefaultRuntimeEnvironment() {
        System.clearProperty(RuntimeConfig.DROID_USER);
        System.clearProperty(RuntimeConfig.LOG_DIR);
        assertNull(System.getProperty(RuntimeConfig.DROID_USER));
        RuntimeConfig.configureRuntimeEnvironment();
        
        File userHome = new File(System.getProperty("user.home"));
        
        assertEquals(new File(userHome, ".droid6").getPath(), System.getProperty(RuntimeConfig.DROID_USER));
        assertEquals(new File(userHome, ".droid6/logs/droid.log").getPath(), System.getProperty("logFile"));
    }
}
