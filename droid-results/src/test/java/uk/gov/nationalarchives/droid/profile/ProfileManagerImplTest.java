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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalConfig;
import uk.gov.nationalarchives.droid.core.interfaces.config.RuntimeConfig;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileInfo;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureType;
import uk.gov.nationalarchives.droid.results.handlers.ProgressObserver;


/**
 * @author rflitcroft
 *
 */
public class ProfileManagerImplTest {

    private static final String DERBY_DRIVER_CLASSNAME = "org.apache.derby.jdbc.EmbeddedDriver";
    
    private ProfileManagerImpl profileManager;
    private ProfileSpecDao profileSpecDao;

    private ProfileContextLocator profileContextLocator;

    private DroidGlobalConfig globalConfig;
    
    @BeforeClass
    public static void setupEnv() {
        RuntimeConfig.configureRuntimeEnvironment();
    }
    
    @AfterClass
    public static void tearDownEnv() {
        System.clearProperty(RuntimeConfig.DROID_USER);
    }
    
    @Before
    public void setup() throws IOException {
        ProfileUuidGenerator uuidGenerator = mock(ProfileUuidGenerator.class);
        when(uuidGenerator.generateUuid()).thenReturn("abc");
        
        profileManager = new ProfileManagerImpl();
//        profileManager.setUuidGenerator(uuidGenerator);
        profileSpecDao = mock(ProfileSpecDao.class);
        profileContextLocator = mock(ProfileContextLocator.class);
        profileManager.setProfileSpecDao(profileSpecDao);
        
        profileManager.setProfileContextLocator(profileContextLocator);
        globalConfig = new DroidGlobalConfig();
        profileContextLocator.setGlobalConfig(globalConfig);
    }
    
    @Test
    @Ignore("This should really be integration tests")
    public void testCreateProfileFromProfileSpecAndOpenProfile() throws Exception {
        
        File derbyDatabase = new File("profiles/myLocation");
        if (derbyDatabase.exists()) {
            FileUtils.deleteDirectory(derbyDatabase);
        }
        
        profileSpecDao = new JaxbProfileSpecDao();
        profileManager.setProfileSpecDao(profileSpecDao);
        
        SignatureFileInfo signatureFileInfo = new SignatureFileInfo(26, false, SignatureType.BINARY);
        signatureFileInfo.setFile(new File("test_sig_files/DROID_SignatureFile_V26.xml"));
        
        Map<SignatureType, SignatureFileInfo> signatureFiles = new HashMap<SignatureType, SignatureFileInfo>();
        signatureFiles.put(SignatureType.BINARY, signatureFileInfo);
        ProfileInstance instance = profileManager.createProfile(signatureFiles);
        String profileId = instance.getUuid();
        
        profileManager.closeProfile("myLocation");
        
       
        ProfileInstance instance2 = profileManager.openProfile(profileId);
        assertEquals(instance.getUuid(), instance2.getUuid());

        // check we have a database.
        String databaseLocation = new File("profiles/" + profileId + "/db").getPath();
        Class.forName(DERBY_DRIVER_CLASSNAME);
        Connection conn = DriverManager.getConnection("jdbc:derby:" + databaseLocation);
        
        Statement stmt = conn.createStatement();
        ResultSet resultSet = stmt.executeQuery("SELECT COUNT (*) FROM droid_user.PROFILE_RESOURCE_NODE");
        assertTrue(resultSet.next());
        assertEquals(0, resultSet.getInt(1));
        conn.close();
        
        assertTrue(new File("profiles/" + profileId + "/profile.xml").exists());
        
        
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void openNonExistentProfileThrowsException() {
        profileManager.openProfile("I_DO_NOT_EXIST");
    }
    
    @Test
    public void testSaveProfileToFile() throws Exception {
        
        ProfileDiskAction profileDiskAction = mock(ProfileDiskAction.class);
        ProgressObserver callback = mock(ProgressObserver.class);
        
        ProfileInstance profileInstance = new ProfileInstance(ProfileState.STOPPED);
        profileInstance.setUuid("profileName");
        when(profileContextLocator.getProfileInstance("profileName")).thenReturn(profileInstance);
        
        profileManager.setProfileDiskAction(profileDiskAction);
        
        DroidGlobalConfig config = mock(DroidGlobalConfig.class);
        when(config.getProfilesDir()).thenReturn(new File("profiles"));
        
        profileManager.setConfig(config);

        File destination = new File("tmp/myProfile.drd");
        
        profileManager.save("profileName", destination, callback);
        
        verify(profileSpecDao).saveProfile(profileInstance, new File("profiles/profileName"));
        verify(profileDiskAction).saveProfile(
                new File("profiles/profileName").getPath(), destination, callback);
        
    }
    
    @Test
    public void testSaveProfileToFileWhenProfileIsRunning() throws Exception {
        
        ProfileDiskAction profileDiskAction = mock(ProfileDiskAction.class);
        ProgressObserver callback = mock(ProgressObserver.class);
        
        profileManager.setProfileDiskAction(profileDiskAction);

        ProfileInstance profileInstance = new ProfileInstance(ProfileState.STOPPED);
        profileInstance.start();
        assertEquals(ProfileState.RUNNING, profileInstance.getState());
        
        when(profileContextLocator.getProfileInstance("profileName")).thenReturn(profileInstance);

        File destination = new File("tmp/myProfile.drd");
        
        try {
            profileManager.save("profileName", destination, callback);
            fail("Expected IllegalStateException.");
        } catch (IllegalStateException e) {
            assertEquals("Illegal attempt to transition state from [RUNNING] to [SAVING]", e.getMessage());
        }
        
        verify(profileSpecDao, never()).saveProfile(any(ProfileInstance.class), any(File.class));
        verify(profileDiskAction, never()).saveProfile(anyString(), any(File.class), any(ProgressObserver.class));
        
    }
    
    @Test
    public void testSetThrottleValue() {
        final String profileId = "abc";
        
        ProfileInstanceManager profileInstanceManager = mock(ProfileInstanceManager.class);
        ProfileInstance profileInstance = mock(ProfileInstance.class);
        
        when(profileInstance.getUuid()).thenReturn(profileId);
        when(profileContextLocator.getProfileInstance(profileId)).thenReturn(profileInstance);
        when(profileContextLocator.openProfileInstanceManager(profileInstance)).thenReturn(profileInstanceManager);
        
        profileManager.setThrottleValue(profileId, 12345);
        
        verify(profileInstanceManager).setThrottleValue(12345);
    }
}
