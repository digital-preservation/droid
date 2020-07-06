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
package uk.gov.nationalarchives.droid.profile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

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
import uk.gov.nationalarchives.droid.util.FileUtil;


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
        globalConfig = new DroidGlobalConfig();

        profileManager = new ProfileManagerImpl();
//        profileManager.setUuidGenerator(uuidGenerator);
        profileSpecDao = mock(ProfileSpecDao.class);
        profileContextLocator = mock(ProfileContextLocator.class);
        profileManager.setProfileSpecDao(profileSpecDao);
        profileManager.setConfig(globalConfig);
        
        profileManager.setProfileContextLocator(profileContextLocator);
        profileContextLocator.setGlobalConfig(globalConfig);
    }
    
    @Test
    @Ignore("This should really be integration tests")
    public void testCreateProfileFromProfileSpecAndOpenProfile() throws Exception {
        
        final Path derbyDatabase = Paths.get("profiles/myLocation");
        if (Files.exists(derbyDatabase)) {
            FileUtil.deleteQuietly(derbyDatabase);
        }
        
        profileSpecDao = new JaxbProfileSpecDao();
        profileManager.setProfileSpecDao(profileSpecDao);
        
        SignatureFileInfo signatureFileInfo = new SignatureFileInfo(26, false, SignatureType.BINARY);
        signatureFileInfo.setFile(Paths.get("test_sig_files/DROID_SignatureFile_V26.xml"));
        
        Map<SignatureType, SignatureFileInfo> signatureFiles = new HashMap<SignatureType, SignatureFileInfo>();
        signatureFiles.put(SignatureType.BINARY, signatureFileInfo);
        ProfileInstance instance = profileManager.createProfile(signatureFiles);
        String profileId = instance.getUuid();
        
        profileManager.closeProfile("myLocation");
        
       
        ProfileInstance instance2 = profileManager.openProfile(profileId);
        assertEquals(instance.getUuid(), instance2.getUuid());

        // check we have a database.
        Path databaseLocation = Paths.get("profiles", profileId, "db");
        Class.forName(DERBY_DRIVER_CLASSNAME);
        Connection conn = DriverManager.getConnection("jdbc:derby:" + databaseLocation);
        
        Statement stmt = conn.createStatement();
        ResultSet resultSet = stmt.executeQuery("SELECT COUNT (*) FROM droid_user.PROFILE_RESOURCE_NODE");
        assertTrue(resultSet.next());
        assertEquals(0, resultSet.getInt(1));
        conn.close();
        
        assertTrue(Files.exists(Paths.get("profiles", profileId, "profile.xml")));
        
        
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
        when(config.getProfilesDir()).thenReturn(Paths.get("profiles"));
        
        profileManager.setConfig(config);

        Path destination = Paths.get("tmp/myProfile.drd");
        
        profileManager.save("profileName", destination, callback);
        
        verify(profileSpecDao).saveProfile(profileInstance, Paths.get("profiles/profileName"));
        verify(profileDiskAction).saveProfile(
                Paths.get("profiles/profileName"), destination, callback);
        
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

        Path destination = Paths.get("tmp/myProfile.drd");
        
        try {
            profileManager.save("profileName", destination, callback);
            fail("Expected IllegalStateException.");
        } catch (IllegalStateException e) {
            assertEquals("Illegal attempt to transition state from [RUNNING] to [SAVING]", e.getMessage());
        }
        
        verify(profileSpecDao, never()).saveProfile(any(ProfileInstance.class), any(Path.class));
        verify(profileDiskAction, never()).saveProfile(any(Path.class), any(Path.class), any(ProgressObserver.class));
        
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

    @Test
    public void testOverrideProperties() throws ProfileManagerException, ConfigurationException, InterruptedException {
        globalConfig.init();
        ProfileContextLocator realContextLocator = new ProfileContextLocator();
        realContextLocator.setGlobalConfig(globalConfig);
        profileManager.setProfileContextLocator(realContextLocator);

        ProfileInstance mainInstance = createProfile();

        // Test process zip flag:
        Thread.sleep(10);
        Boolean propertyFlag = globalConfig.getProperties().getBoolean("profile.processZip");
        ProfileInstance overridden = createOverriddenProfile("profile.processZip", propertyFlag);
        assertEquals(propertyFlag, mainInstance.getProcessZipFiles());
        assertNotEquals(propertyFlag, overridden.getProcessZipFiles());

        // Test process tar flag:
        Thread.sleep(10);
        propertyFlag = globalConfig.getProperties().getBoolean("profile.processTar");
        overridden = createOverriddenProfile("profile.processTar", propertyFlag);
        assertEquals(propertyFlag, mainInstance.getProcessTarFiles());
        assertNotEquals(propertyFlag, overridden.getProcessTarFiles());

        // Test match all extensions:
        Thread.sleep(10);
        propertyFlag = globalConfig.getProperties().getBoolean("profile.matchAllExtensions");
        overridden = createOverriddenProfile("profile.matchAllExtensions", propertyFlag);
        assertEquals(propertyFlag, mainInstance.getMatchAllExtensions());
        assertNotEquals(propertyFlag, overridden.getMatchAllExtensions());

        // Test max bytes to scan:
        Thread.sleep(10);
        Long maxBytes = globalConfig.getProperties().getLong("profile.maxBytesToScan");
        overridden = createOverriddenProfile("profile.maxBytesToScan", maxBytes);
        assertEquals(maxBytes, mainInstance.getMaxBytesToScan());
        assertEquals((Long) (maxBytes + 1000L), (Long) overridden.getMaxBytesToScan());
    }


    private ProfileInstance createProfile() throws ProfileManagerException {
        Map<SignatureType, SignatureFileInfo> sigInfo = new HashMap<>();
        return profileManager.createProfile(sigInfo);
    }

    private ProfileInstance createOverriddenProfile(String propertyName, Boolean value) throws ProfileManagerException {
        Map<SignatureType, SignatureFileInfo> sigInfo = new HashMap<>();
        PropertiesConfiguration overrides = new PropertiesConfiguration();
        if (value) {
            overrides.setProperty(propertyName, "false");
        } else {
            overrides.setProperty(propertyName, "true");
        }
        return profileManager.createProfile(sigInfo, overrides);
    }

    private ProfileInstance createOverriddenProfile(String propertyName, Long value) throws ProfileManagerException {
        Map<SignatureType, SignatureFileInfo> sigInfo = new HashMap<>();
        PropertiesConfiguration overrides = new PropertiesConfiguration();
        overrides.setProperty(propertyName, value + 1000);
        return profileManager.createProfile(sigInfo, overrides);
    }
}
