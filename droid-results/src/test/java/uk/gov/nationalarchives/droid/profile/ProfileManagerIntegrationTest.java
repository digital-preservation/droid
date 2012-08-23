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
import java.net.URI;
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

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.gov.nationalarchives.droid.core.interfaces.config.RuntimeConfig;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileInfo;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureType;
import uk.gov.nationalarchives.droid.results.handlers.ProgressObserver;

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
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:META-INF/spring-profile.xml"})
public class ProfileManagerIntegrationTest {

   private SignatureFileInfo binarySignatureFileInfo;
   private SignatureFileInfo containerSignatureFileInfo;
   @Autowired
   private ProfileManager profileManager;

   @BeforeClass
   public static void setupFiles() throws IOException {
      RuntimeConfig.configureRuntimeEnvironment();
      new File("integration-test-files").mkdir();
      new File("integration-test-files/file1").createNewFile();
   }

   @AfterClass
   public static void tearDown() throws IOException {
      FileUtils.forceDelete(new File("integration-test-files"));
   }
   
   @Before
   public void setup() {
      binarySignatureFileInfo = new SignatureFileInfo(26, false, SignatureType.BINARY);
      binarySignatureFileInfo.setFile(new File("test_sig_files/DROID_SignatureFile_V26.xml"));

      containerSignatureFileInfo = new SignatureFileInfo(26, false, SignatureType.CONTAINER);
      containerSignatureFileInfo.setFile(new File("test_sig_files/container-signature.xml"));
   }

   @Test
   public void testStartProfileSpecPersistsAJobForEachFileResourceNode() throws Exception {
      try {
         FileUtils.forceDelete(new File("profiles/integration-test"));
      } catch (IOException e) {
      }

      File testFile = new File("test_sig_files/sample.pdf");
      FileProfileResource fileResource = new FileProfileResource(testFile);
      assertNotNull(profileManager);
      ProfileSpec profileSpec = new ProfileSpec();
      profileSpec.addResource(fileResource);

      ProfileResultObserver myObserver = mock(ProfileResultObserver.class);

      Map<SignatureType, SignatureFileInfo> signatureFiles = new HashMap<SignatureType, SignatureFileInfo>();
      signatureFiles.put(SignatureType.BINARY, binarySignatureFileInfo);
      signatureFiles.put(SignatureType.CONTAINER, containerSignatureFileInfo);

      ProfileInstance profile = profileManager.createProfile(signatureFiles);
      profileManager.updateProfileSpec(profile.getUuid(), profileSpec);
      profileManager.setResultsObserver(profile.getUuid(), myObserver);

      Collection<ProfileResourceNode> rootNodes = profileManager.findRootNodes(profile.getUuid());
      assertEquals(1, rootNodes.size());
      assertEquals(testFile.toURI(), rootNodes.iterator().next().getUri());

      ProgressObserver progressObserver = mock(ProgressObserver.class);
      profileManager.setProgressObserver(profile.getUuid(), progressObserver);
      profile.changeState(ProfileState.VIRGIN);

      Future<?> submission = profileManager.start(profile.getUuid());
      submission.get();

      // Assert we got our result
      ArgumentCaptor<ProfileResourceNode> nodeCaptor = ArgumentCaptor.forClass(ProfileResourceNode.class);
      verify(myObserver).onResult(nodeCaptor.capture());
      URI capturedUri = nodeCaptor.getValue().getUri();

      assertEquals(testFile.toURI(), capturedUri);

      // Now assert that file/1 is in the database
      List<ProfileResourceNode> nodes = profileManager.findProfileResourceNodeAndImmediateChildren(
              profile.getUuid(), null);

      assertEquals(1, nodes.size());
      ProfileResourceNode node = nodes.get(0);
      assertEquals(testFile.toURI(), node.getUri());
//        assertEquals(JobStatus.COMPLETE, node.getJob().getStatus());
      assertEquals(testFile.length(), node.getMetaData().getSize().longValue());
      assertEquals(new Date(testFile.lastModified()), node.getMetaData().getLastModifiedDate());

      // check the progress listener was invoked properly.
      verify(progressObserver, times(1)).onProgress(0);
      //verify(progressObserver).onProgress(100);

   }

   @Test
   public void testStartProfileSpecPersistsJobsForEachFileInANonRecursiveDirResourceNode() throws Exception {
      try {
         FileUtils.forceDelete(new File("profiles/integration-test2"));
      } catch (IOException e) {
      }

      File testFile = new File("test_sig_files");
      
      int folderSize = testFile.listFiles().length;

      // Test affected because hard coded folder may be in version control
      // If in older versions of subversion .svn will exist. This is the most
      // pragmatic check to ensure this test works. Hard coded folder should
      // only ever be eight files or nine if including .svn
      assertTrue((folderSize == 8 || folderSize == 9));
      
      final int EXPECTED_RESOURCES = folderSize;
      final int CHECK_VALUE = 8;
      final int PROGRESS_CHECK = 100;
      
      FileProfileResource fileResource = new DirectoryProfileResource(testFile, false);
      assertNotNull(profileManager);
      ProfileSpec profileSpec = new ProfileSpec();
      profileSpec.addResource(fileResource);

      ProfileResultObserver myObserver = mock(ProfileResultObserver.class);
      Map<SignatureType, SignatureFileInfo> signatureFiles = new HashMap<SignatureType, SignatureFileInfo>();
      signatureFiles.put(SignatureType.BINARY, binarySignatureFileInfo);
      signatureFiles.put(SignatureType.CONTAINER, containerSignatureFileInfo);
      ProfileInstance profile = profileManager.createProfile(signatureFiles);

      profile.changeState(ProfileState.VIRGIN);
      profile.setProcessArchiveFiles(false);
      
      String profileId = profile.getUuid();
      profileManager.updateProfileSpec(profileId, profileSpec);
      profileManager.setResultsObserver(profileId, myObserver);

      ProgressObserver progressObserver = mock(ProgressObserver.class);
      profileManager.setProgressObserver(profileId, progressObserver);

      Future<?> submission = profileManager.start(profileId);
      submission.get();

      // Assert we got our result notifications
      ArgumentCaptor<ProfileResourceNode> nodeCaptor = ArgumentCaptor.forClass(ProfileResourceNode.class);
      List<ProfileResourceNode> capturedUris = nodeCaptor.getAllValues();

      verify(myObserver, atLeast(CHECK_VALUE)).onResult(nodeCaptor.capture());

      ProfileResourceNode testFileNode = capturedUris.get(0);

      Collections.sort(capturedUris, new Comparator<ProfileResourceNode>() {

         @Override
         public int compare(ProfileResourceNode o1, ProfileResourceNode o2) {
            return o1.getUri().compareTo(o2.getUri());
         }
      });

      // Now assert that file/1 is in the database
      List<ProfileResourceNode> nodes = profileManager.findProfileResourceNodeAndImmediateChildren(
              profile.getUuid(), testFileNode.getId());

      // There are n expectedResources in node list
      assertEquals(EXPECTED_RESOURCES, nodes.size());

      ProfileResourceNode samplePdf = null;
      File samplePdfFile = new File("test_sig_files/sample.pdf");
      for (ProfileResourceNode childNode : nodes) {
         if (childNode.getUri().equals(samplePdfFile.toURI())) {
            samplePdf = childNode;
            break;
         }
      }

      assertEquals(samplePdfFile.toURI(), samplePdf.getUri());
      assertEquals(samplePdfFile.length(), samplePdf.getMetaData().getSize().longValue());
      assertEquals(new Date(samplePdfFile.lastModified()), samplePdf.getMetaData().getLastModifiedDate());

      // check the progress listener was invoked properly.
      verify(progressObserver, atLeast(CHECK_VALUE)).onProgress(anyInt());
      verify(progressObserver).onProgress(PROGRESS_CHECK);
   }
}