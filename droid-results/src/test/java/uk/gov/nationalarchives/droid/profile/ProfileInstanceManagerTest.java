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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileException;
import uk.gov.nationalarchives.droid.profile.referencedata.ReferenceDataServiceImpl;
import uk.gov.nationalarchives.droid.profile.throttle.SubmissionThrottle;
import uk.gov.nationalarchives.droid.submitter.FileEventHandler;
import uk.gov.nationalarchives.droid.submitter.ProfileSpecWalker;
import uk.gov.nationalarchives.droid.submitter.ProfileSpecWalkerImpl;
import uk.gov.nationalarchives.droid.submitter.ProfileWalkState;
import uk.gov.nationalarchives.droid.submitter.ProfileWalkerDao;
import uk.gov.nationalarchives.droid.submitter.SubmissionGateway;

/**
 * @author rflitcroft
 *
 */
public class ProfileInstanceManagerTest {

    private ProfileInstanceManagerImpl profileInstanceManager;
    
    @Before
    public void setup() {
        profileInstanceManager = new ProfileInstanceManagerImpl();
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testFindAllProfileResourceNodesBuildsNonPersistentRootNodesFromSpec() throws SignatureFileException {
        
        ProfileDao profileDao = mock(ProfileDao.class);
        profileInstanceManager.setProfileDao(profileDao);

        ProfileSpec profileSpec = new ProfileSpec();
        profileSpec.addResource(new FileProfileResource(new File("file/1")));
        profileSpec.addResource(new FileProfileResource(new File("file/2")));
        profileSpec.addResource(new FileProfileResource(new File("file/3")));
        profileSpec.addResource(new FileProfileResource(new File("file/4")));
        
        ProfileInstance profile = new ProfileInstance();
        profile.setSignatureFileVersion(26);
        profile.setProfileSpec(profileSpec);
        
        when(profileDao.findProfileResourceNodes(null)).thenReturn(Collections.EMPTY_LIST);
        profileInstanceManager.setProfile(profile);
        ReferenceDataServiceImpl referenceDaoImplMock = mock(ReferenceDataServiceImpl.class);
        profileInstanceManager.setReferenceDataService(referenceDaoImplMock);

        profileInstanceManager.initProfile(new File("test_sig_files/DROID_SignatureFile_V26.xml").toURI());
        Collection<ProfileResourceNode> nodes = profileInstanceManager.findRootProfileResourceNodes();
        
        assertEquals(profileSpec.getResources().size(), nodes.size());
    }

    @Test
    public void testFindAllProfileResourceNodesOverridesPrimordialNodesWithPersistentOnes() 
        throws SignatureFileException {
        
        List<ProfileResourceNode> persistentNodes = new ArrayList<ProfileResourceNode>();
        persistentNodes.add(buildProfileResourceNodeAtUriWithJob("file/1"));
        persistentNodes.add(buildProfileResourceNodeAtUriWithJob("file/4"));
        
        ProfileDao profileDao = mock(ProfileDao.class);
        
        ReferenceDataServiceImpl referenceDaoImplMock = mock(ReferenceDataServiceImpl.class);
        when(profileDao.findProfileResourceNodes(null)).thenReturn(persistentNodes);
        
        profileInstanceManager.setProfileDao(profileDao);

        profileInstanceManager.setReferenceDataService(referenceDaoImplMock);

        ProfileSpec profileSpec = new ProfileSpec();
        profileSpec.addResource(new FileProfileResource(new File("file/1")));
        profileSpec.addResource(new FileProfileResource(new File("file/2")));
        profileSpec.addResource(new FileProfileResource(new File("file/3")));
        profileSpec.addResource(new FileProfileResource(new File("file/4")));
        
        ProfileInstance profile = new ProfileInstance();
        profile.setSignatureFileVersion(26);
        profile.setProfileSpec(profileSpec);
        
        profileInstanceManager.setProfile(profile);
        profileInstanceManager.initProfile(new File("test_sig_files/DROID_SignatureFile_V26.xml").toURI());
        Collection<ProfileResourceNode> nodes = profileInstanceManager.findRootProfileResourceNodes();
        
        Iterator<ProfileResourceNode> nodeIterator = nodes.iterator();
        assertEquals(profileSpec.getResources().size(), nodes.size());
//        assertEquals(JobStatus.COMPLETE, nodeIterator.next().getJob().getStatus());
//        assertNotNull(nodeIterator.next().getJob());
//        assertNotNull(nodeIterator.next().getJob());
//        assertEquals(JobStatus.COMPLETE, nodeIterator.next().getJob().getStatus());
    }
    
    private ProfileResourceNode buildProfileResourceNodeAtUriWithJob(String filePath) {
//        IdentificationJob identificationJob = mock(IdentificationJob.class);
//        when(identificationJob.getStatus()).thenReturn(JobStatus.COMPLETE);
        
        ProfileResourceNode node = new ProfileResourceNode(new File(filePath).toURI());
//       node.setJob(identificationJob);
        return node;
        
    }
    
    @Test
    public void testCancelSubmission() throws Exception {
        
        ProfileSpec profileSpec = new ProfileSpec();

        DirectoryProfileResource directoryResource = mock(DirectoryProfileResource.class);
        when(directoryResource.isRecursive()).thenReturn(true);
        when(directoryResource.getUri()).thenReturn(new File(".").toURI());
        when(directoryResource.isDirectory()).thenReturn(true);
        profileSpec.addResource(directoryResource);

        ProfileDao profileDao = mock(ProfileDao.class);
        profileInstanceManager.setProfileDao(profileDao);
        ReferenceDataServiceImpl referenceDaoImplMock = mock(ReferenceDataServiceImpl.class);
        profileInstanceManager.setReferenceDataService(referenceDaoImplMock);
        
        SubmissionGateway submissionGateway = mock(SubmissionGateway.class);
        profileInstanceManager.setSubmissionGateway(submissionGateway);

        ProfileInstance profile = new ProfileInstance(ProfileState.STOPPED);
        profile.setProfileSpec(profileSpec);
        profile.setSignatureFileVersion(26);
        
        ProfileWalkerDao profileWalkerDao = mock(ProfileWalkerDao.class);
        profileInstanceManager.setProfileWalkerDao(profileWalkerDao);
        
        ProfileSpecWalker specWalker = mock(ProfileSpecWalker.class);
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Thread.sleep(200);
                return null;
            }
            
        }).when(specWalker).walk(eq(profileSpec), any(ProfileWalkState.class));
        
        profileInstanceManager.setSpecWalker(specWalker);
        
        
        profileInstanceManager.setProfile(profile);
        profileInstanceManager.initProfile(new File("test_sig_files/DROID_SignatureFile_V26.xml").toURI());
        Future<?> profileTask = profileInstanceManager.start();
        
        Thread.sleep(100);
        assertTrue(profileTask.cancel(false));
        assertTrue(profileTask.isCancelled());
    }
    
    @Test
    public void testSetThrottle() {
        
        final int throttleValue = 1234;
        
        SubmissionThrottle throttle = mock(SubmissionThrottle.class);
        ProfileInstance profile = new ProfileInstance();
        
        FileEventHandler fileEventHandler = mock(FileEventHandler.class);
        when(fileEventHandler.getSubmissionThrottle()).thenReturn(throttle);
        profileInstanceManager.setProfile(profile);

        ProfileSpecWalkerImpl profileSpecWalker = new ProfileSpecWalkerImpl();
        profileSpecWalker.setFileEventHandler(fileEventHandler);
        profileInstanceManager.setSpecWalker(profileSpecWalker);

        profileInstanceManager.setThrottleValue(throttleValue);
        verify(throttle).setWaitMilliseconds(throttleValue);
        assertEquals(throttleValue, profile.getThrottle());
        
    }
}
