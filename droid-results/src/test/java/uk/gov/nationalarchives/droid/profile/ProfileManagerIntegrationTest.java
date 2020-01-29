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
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import uk.gov.nationalarchives.droid.core.interfaces.config.RuntimeConfig;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileInfo;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureType;
import uk.gov.nationalarchives.droid.results.handlers.ProgressObserver;
import uk.gov.nationalarchives.droid.util.FileUtil;

/**
 * @author rflitcroft
 *
 */

/* 
 * TODO this test case has been disabled (@Ignore} because it has side effects.
 * If you run it once on a clean machine, i.e. delete droid's settings folder
 * ~/.droid6 and run the test it fails, if you then run the test again it suceeds
 * the test should not read/write ~/.droid6. It needs to be cleaned up. Also
 * any state it creates should be destroyed at the end of the test and the test
 * should run repetitively.
 */
@Ignore
//BNO: Commented out as causes compilation failure with Java 8 build...
//@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:META-INF/spring-profile.xml"})
public class ProfileManagerIntegrationTest {

   private SignatureFileInfo binarySignatureFileInfo;
   private SignatureFileInfo containerSignatureFileInfo;
   @Autowired
   private ProfileManager profileManager;

   @BeforeClass
   public static void setupFiles() throws IOException {
      RuntimeConfig.configureRuntimeEnvironment();
      Files.createDirectories(Paths.get("integration-test-files"));
      Files.createFile(Paths.get("integration-test-files/file1"));
   }

   @AfterClass
   public static void tearDown() throws IOException {
      FileUtil.deleteQuietly(Paths.get("integration-test-files"));
   }
   
   @Before
   public void setup() {
      binarySignatureFileInfo = new SignatureFileInfo(26, false, SignatureType.BINARY);
      binarySignatureFileInfo.setFile(Paths.get("test_sig_files/DROID_SignatureFile_V26.xml"));

      containerSignatureFileInfo = new SignatureFileInfo(26, false, SignatureType.CONTAINER);
      containerSignatureFileInfo.setFile(Paths.get("test_sig_files/container-signature.xml"));
   }

   @Test
   public void testStartProfileSpecPersistsAJobForEachFileResourceNode() throws Exception {
      FileUtil.deleteQuietly(Paths.get("profiles/integration-test"));

      final Path testFile = Paths.get("test_sig_files/sample.pdf");
      final FileProfileResource fileResource = new FileProfileResource(testFile);
      assertNotNull(profileManager);
      final ProfileSpec profileSpec = new ProfileSpec();
      profileSpec.addResource(fileResource);

      final ProfileResultObserver myObserver = mock(ProfileResultObserver.class);

      final Map<SignatureType, SignatureFileInfo> signatureFiles = new HashMap<>();
      signatureFiles.put(SignatureType.BINARY, binarySignatureFileInfo);
      signatureFiles.put(SignatureType.CONTAINER, containerSignatureFileInfo);

      final ProfileInstance profile = profileManager.createProfile(signatureFiles);
      profileManager.updateProfileSpec(profile.getUuid(), profileSpec);
      profileManager.setResultsObserver(profile.getUuid(), myObserver);

      final Collection<ProfileResourceNode> rootNodes = profileManager.findRootNodes(profile.getUuid());
      assertEquals(1, rootNodes.size());
      assertEquals(testFile.toUri(), rootNodes.iterator().next().getUri());

      final ProgressObserver progressObserver = mock(ProgressObserver.class);
      profileManager.setProgressObserver(profile.getUuid(), progressObserver);
      profile.changeState(ProfileState.VIRGIN);

      final Future<?> submission = profileManager.start(profile.getUuid());
      submission.get();

      // Assert we got our result
      final ArgumentCaptor<ProfileResourceNode> nodeCaptor = ArgumentCaptor.forClass(ProfileResourceNode.class);
      verify(myObserver).onResult(nodeCaptor.capture());
      final URI capturedUri = nodeCaptor.getValue().getUri();

      assertEquals(testFile.toUri(), capturedUri);

      // Now assert that file/1 is in the database
      final List<ProfileResourceNode> nodes = profileManager.findProfileResourceNodeAndImmediateChildren(
              profile.getUuid(), null);

      assertEquals(1, nodes.size());
      final ProfileResourceNode node = nodes.get(0);
      assertEquals(testFile.toUri(), node.getUri());
//        assertEquals(JobStatus.COMPLETE, node.getJob().getStatus());
      assertEquals(FileUtil.sizeQuietly(testFile), node.getMetaData().getSize().longValue());
      assertEquals(new Date(FileUtil.lastModifiedQuietly(testFile).toMillis()), node.getMetaData().getLastModifiedDate());

      // check the progress listener was invoked properly.
      verify(progressObserver, times(1)).onProgress(0);
      //verify(progressObserver).onProgress(100);

   }

   @Test
   public void testStartProfileSpecPersistsJobsForEachFileInANonRecursiveDirResourceNode() throws Exception {
      FileUtil.deleteQuietly(Paths.get("profiles/integration-test2"));

      final Path testFile = Paths.get("test_sig_files");
      int folderSize = FileUtil.listFiles(testFile, false, (DirectoryStream.Filter)null).size();

      // Test affected because hard coded folder may be in version control
      // If in older versions of subversion .svn will exist. This is the most
      // pragmatic check to ensure this test works. Hard coded folder should
      // only ever be eight files or nine if including .svn
      assertTrue((folderSize == 8 || folderSize == 9));
      
      final int EXPECTED_RESOURCES = folderSize;
      final int CHECK_VALUE = 8;
      final int PROGRESS_CHECK = 100;
      
      final FileProfileResource fileResource = new DirectoryProfileResource(testFile, false);
      assertNotNull(profileManager);
      final ProfileSpec profileSpec = new ProfileSpec();
      profileSpec.addResource(fileResource);

      final ProfileResultObserver myObserver = mock(ProfileResultObserver.class);
      final Map<SignatureType, SignatureFileInfo> signatureFiles = new HashMap<SignatureType, SignatureFileInfo>();
      signatureFiles.put(SignatureType.BINARY, binarySignatureFileInfo);
      signatureFiles.put(SignatureType.CONTAINER, containerSignatureFileInfo);
      ProfileInstance profile = profileManager.createProfile(signatureFiles);

      profile.changeState(ProfileState.VIRGIN);
      profile.setProcessTarFiles(false);
      profile.setProcessZipFiles(false);
      profile.setProcessGzipFiles(false);
      profile.setProcessRarFiles(false);
      profile.setProcess7zipFiles(false);
      profile.setProcessIsoFiles(false);
      profile.setProcessBzip2Files(false);

      profile.setProcessArcFiles(false);
      profile.setProcessWarcFiles(false);

      final String profileId = profile.getUuid();
      profileManager.updateProfileSpec(profileId, profileSpec);
      profileManager.setResultsObserver(profileId, myObserver);

      final ProgressObserver progressObserver = mock(ProgressObserver.class);
      profileManager.setProgressObserver(profileId, progressObserver);

      final Future<?> submission = profileManager.start(profileId);
      submission.get();

      // Assert we got our result notifications
      final ArgumentCaptor<ProfileResourceNode> nodeCaptor = ArgumentCaptor.forClass(ProfileResourceNode.class);
      final List<ProfileResourceNode> capturedUris = nodeCaptor.getAllValues();

      verify(myObserver, atLeast(CHECK_VALUE)).onResult(nodeCaptor.capture());

      final ProfileResourceNode testFileNode = capturedUris.get(0);

      Collections.sort(capturedUris, new Comparator<ProfileResourceNode>() {

         @Override
         public int compare(ProfileResourceNode o1, ProfileResourceNode o2) {
            return o1.getUri().compareTo(o2.getUri());
         }
      });

      // Now assert that file/1 is in the database
      final List<ProfileResourceNode> nodes = profileManager.findProfileResourceNodeAndImmediateChildren(
              profile.getUuid(), testFileNode.getId());

      // There are n expectedResources in node list
      assertEquals(EXPECTED_RESOURCES, nodes.size());

      ProfileResourceNode samplePdf = null;
      final Path samplePdfFile = Paths.get("test_sig_files/sample.pdf");
      for (final ProfileResourceNode childNode : nodes) {
         if (childNode.getUri().equals(samplePdfFile.toUri())) {
            samplePdf = childNode;
            break;
         }
      }

      assertEquals(samplePdfFile.toUri(), samplePdf.getUri());
      assertEquals(FileUtil.sizeQuietly(samplePdfFile), samplePdf.getMetaData().getSize().longValue());
      assertEquals(new Date(FileUtil.lastModifiedQuietly(samplePdfFile).toMillis()), samplePdf.getMetaData().getLastModifiedDate());

      // check the progress listener was invoked properly.
      verify(progressObserver, atLeast(CHECK_VALUE)).onProgress(anyInt());
      verify(progressObserver).onProgress(PROGRESS_CHECK);
   }
}
