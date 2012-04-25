/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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
