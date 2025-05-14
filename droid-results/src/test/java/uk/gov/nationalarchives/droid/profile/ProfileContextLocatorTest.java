/*
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

import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalConfig;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class ProfileContextLocatorTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testOpenProfileInstanceManagerCreatesNewTemplateIfNewProfile() throws IOException, SignatureFileException, ConfigurationException {
        String profileId = UUID.randomUUID().toString();
        File droidHome = temporaryFolder.newFolder();
        Path profileHome = Files.createDirectory(Path.of(droidHome.getPath(), profileId));
        Path templatesDirectory = Files.createDirectory(profileHome.resolve("profile_templates"));
        DroidGlobalConfig globalConfig = createTestConfig(profileHome);
        Path signatureFilePath = profileHome.resolve("signature_files").resolve("testsigfile.xml");

        ProfileInstanceLocator profileInstanceLocator = getProfileInstanceLocator(profileHome, profileId, globalConfig);

        verify(profileInstanceLocator, times(2)).freezeDatabase(eq(profileId));
        verify(profileInstanceLocator, times(2)).thawDatabase(eq(profileId));
        verify(globalConfig).update(eq(Map.of("profile.binarySignatureLastUpdated.testsigfile.xml", Files.getLastModifiedTime(signatureFilePath).toMillis())));

        List<String> templateFiles = Files.list(templatesDirectory)
                .map(Path::getFileName)
                .map(Path::toString)
                .sorted()
                .toList();
        Assert.assertArrayEquals(templateFiles.toArray(), new String[]{"profile.1.template", "profile.template"});
    }

    @Test
    public void testOpenProfileInstanceManagerSetsLastUpdatedInProperties() throws IOException, ConfigurationException, SignatureFileException {
        String profileId = UUID.randomUUID().toString();
        File droidHome = temporaryFolder.newFolder();
        Path profileHome = Files.createDirectory(Path.of(droidHome.getPath(), profileId));
        DroidGlobalConfig globalConfig = createTestConfig(profileHome);
        Path signatureFilePath = profileHome.resolve("signature_files").resolve("testsigfile.xml");

        getProfileInstanceLocator(profileHome, profileId, globalConfig);

        verify(globalConfig).update(eq(Map.of("profile.binarySignatureLastUpdated.testsigfile.xml", Files.getLastModifiedTime(signatureFilePath).toMillis())));

    }

    @Test
    public void testOpenProfileInstanceManagerUsesExistingTemplateIfExists() throws IOException, SignatureFileException {
        String profileId = UUID.randomUUID().toString();
        File droidHome = temporaryFolder.newFolder();
        Path profileHome = Files.createDirectory(Path.of(droidHome.getPath(), profileId));
        Path templatesDirectory = Files.createDirectory(profileHome.resolve("profile_templates"));
        DroidGlobalConfig globalConfig = createTestConfig(profileHome);

        createTestProfile(templatesDirectory.resolve("profile.1.template"));

        ProfileInstanceLocator profileInstanceLocator = getProfileInstanceLocator(profileHome, profileId, globalConfig);

        verify(profileInstanceLocator, times(0)).freezeDatabase(eq(profileId));
        verify(profileInstanceLocator, times(0)).thawDatabase(eq(profileId));
        List<String> templateFiles = Files.list(templatesDirectory)
                .map(Path::getFileName)
                .map(Path::toString)
                .sorted()
                .toList();
        Assert.assertArrayEquals(templateFiles.toArray(), new String[]{"profile.1.template"});
    }

    @Test
    public void testOpenProfileInstanceManagerUsesNewTemplateIfSigFileLastModifiedHasChanged() throws IOException, SignatureFileException, ConfigurationException {
        String profileId = UUID.randomUUID().toString();
        File droidHome = temporaryFolder.newFolder();
        Path profileHome = Files.createDirectory(Path.of(droidHome.getPath(), profileId));
        Path templatesDirectory = Files.createDirectory(profileHome.resolve("profile_templates"));
        DroidGlobalConfig globalConfig = createTestConfig(profileHome);
        String sigFileLastUpdatedKey = "profile.binarySignatureLastUpdated.testsigfile.xml";
        Path signatureFilePath = profileHome.resolve("signature_files").resolve("testsigfile.xml");

        when(globalConfig.getProperties().getLong(eq(sigFileLastUpdatedKey))).thenReturn(1L);
        when(globalConfig.getProperties().containsKey(sigFileLastUpdatedKey)).thenReturn(true);
        createTestProfile(templatesDirectory.resolve("profile.1.template"));

        ProfileInstanceLocator profileInstanceLocator = getProfileInstanceLocator(profileHome, profileId, globalConfig);

        verify(profileInstanceLocator, times(2)).freezeDatabase(eq(profileId));
        verify(profileInstanceLocator, times(2)).thawDatabase(eq(profileId));
        verify(globalConfig).update(eq(Map.of("profile.binarySignatureLastUpdated.testsigfile.xml", Files.getLastModifiedTime(signatureFilePath).toMillis())));
        List<String> templateFiles = Files.list(templatesDirectory)
                .map(Path::getFileName)
                .map(Path::toString)
                .sorted()
                .toList();
        Assert.assertArrayEquals(templateFiles.toArray(), new String[]{"profile.1.template", "profile.template"});
    }

    public static void createTestProfile(Path profilePath) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zipOutput = new ZipArchiveOutputStream(bos);
        String content = "test";
        ZipArchiveEntry entry = new ZipArchiveEntry(content);
        entry.setSize(content.length());
        zipOutput.putArchiveEntry(entry);
        zipOutput.write(content.getBytes());
        zipOutput.closeArchiveEntry();
        Files.write(profilePath, bos.toByteArray());
    }
    
    private static DroidGlobalConfig createTestConfig(Path profileHome) {
        Path templatesDirectory = profileHome.resolve("profile_templates");
        Path signatureFileDir = profileHome.resolve("signature_files");
        PropertiesConfiguration propertiesConfiguration = mock(PropertiesConfiguration.class);
        when(propertiesConfiguration.getString(eq("database.createUrl"))).thenReturn(null);
        DroidGlobalConfig config = mock(DroidGlobalConfig.class);
        when(config.getProfilesDir()).thenReturn(profileHome);
        when(config.getTempDir()).thenReturn(profileHome.getParent());
        when(config.getProperties()).thenReturn(propertiesConfiguration);
        when(config.getProfileTemplateDir()).thenReturn(templatesDirectory);
        when(config.getSignatureFileDir()).thenReturn(signatureFileDir);
        return config;
    }

    private static ProfileInstanceLocator getProfileInstanceLocator(Path profileHome, String profileId, DroidGlobalConfig config) throws IOException, SignatureFileException {

        ProfileInstanceLocator profileInstanceLocator = mock(ProfileInstanceLocator.class);

        ProfileInstanceManager profileInstanceManager = mock(ProfileInstanceManager.class);

        Files.createDirectory(profileHome.resolve("db"));
        Path signatureFileDir = Files.createDirectory(profileHome.resolve("signature_files"));
        String signatureFileName = "testsigfile.xml";
        String containerFileName = "testcontainerfile.xml";
        Files.createFile(signatureFileDir.resolve(signatureFileName));
        Files.createFile(profileHome.resolve(containerFileName));


        when(profileInstanceLocator.getProfileInstanceManager(any(), any())).thenReturn(profileInstanceManager);
        doNothing().when(profileInstanceManager).initProfile(any());

        
        ProfileContextLocator profileContextLocator = new ProfileContextLocator(config, profileInstanceLocator);
        ProfileInstance profileInstance = new ProfileInstance();
        profileInstance.setUuid(profileId);
        profileInstance.setSignatureFileName(signatureFileName);
        profileInstance.setSignatureFileVersion(1);
        profileInstance.setContainerSignatureFileName(containerFileName);

        profileContextLocator.openProfileInstanceManager(profileInstance);
        return profileInstanceLocator;
    }
}
