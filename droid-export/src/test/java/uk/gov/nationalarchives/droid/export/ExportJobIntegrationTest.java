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
package uk.gov.nationalarchives.droid.export;

import java.io.File;
import java.util.Arrays;

import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalConfig;
import uk.gov.nationalarchives.droid.export.interfaces.ExportOptions;
import uk.gov.nationalarchives.droid.profile.DirectoryProfileResource;
import uk.gov.nationalarchives.droid.profile.ProfileContextLocator;
import uk.gov.nationalarchives.droid.profile.ProfileInstance;
import uk.gov.nationalarchives.droid.profile.ProfileInstanceManager;
import uk.gov.nationalarchives.droid.profile.ProfileSpec;

/**
 * @author rflitcroft
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { 
        "classpath*:META-INF/export-spring.xml"
        })
@Ignore
public class ExportJobIntegrationTest {

    @Resource(name = "exportManager")
    private ExportManagerImpl exportManager;
    
    @Autowired
    private ProfileContextLocator profileContextLocator;
    
    @Autowired
    private DroidGlobalConfig config;
    
    @Test
    public void testEndToEndExportOfOneProfile() throws Exception {
        
        
        // create a new profile and run it
        ProfileInstance testProfile = profileContextLocator.getProfileInstance("test");
        testProfile.setProfileSpec(new ProfileSpec());
        
        File profileHomeDir = new File(config.getProfilesDir(), "test");
        // Delete any renmants...
        FileUtils.deleteDirectory(profileHomeDir);
        
        final File sigFile = new File("sig_files/DROID_SignatureFile_V26.xml");
        FileUtils.copyFileToDirectory(sigFile, profileHomeDir);
        testProfile.setSignatureFileName("DROID_SignatureFile_V26.xml");
        String path = "C:/Documents and Settings/rflitcroft/My Documents/matts_disk";
        //String path = "src/test/resources";
        testProfile.addResource(new DirectoryProfileResource(new File(path), true));
        
        ProfileInstanceManager profileInstance = profileContextLocator.openProfileInstanceManager(testProfile);
        //profileInstance.initProfile(sigFile.toURI());
        profileInstance.start().get();
        
        
        String[] profileIds = new String[] {
            "test", 
        };
        
        exportManager.exportProfiles(Arrays.asList(profileIds), "exports/export.csv", null,
            ExportOptions.ONE_ROW_PER_FILE);
        
        profileContextLocator.removeProfileContext("test");
        
    }
    
}
