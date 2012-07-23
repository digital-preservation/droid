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
