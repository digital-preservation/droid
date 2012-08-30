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
package uk.gov.nationalarchives.droid.submitter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import static org.mockito.Matchers.any;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import uk.gov.nationalarchives.droid.core.interfaces.AsynchDroid;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationErrorType;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationException;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.ResourceId;
import uk.gov.nationalarchives.droid.core.interfaces.ResultHandler;
import uk.gov.nationalarchives.droid.core.interfaces.archive.IdentificationRequestFactory;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;
import uk.gov.nationalarchives.droid.profile.AbstractProfileResource;
import uk.gov.nationalarchives.droid.profile.ProfileResourceNode;
import uk.gov.nationalarchives.droid.profile.throttle.SubmissionThrottle;

/**
 * @author rflitcroft
 *
 */
public class FileEventHandlerTest {

    private IdentificationRequestFactory requestFactory;
    private IdentificationRequest request;
    private FileEventHandler fileEventHandler;
    private AsynchDroid identificationEngine;
    private File tmpDir;
    
    @Before
    public void setup() {
        identificationEngine = mock(AsynchDroid.class);
        requestFactory = mock(IdentificationRequestFactory.class);
        request = mock(IdentificationRequest.class);
        fileEventHandler = new FileEventHandler(identificationEngine);
        when(requestFactory.newRequest(any(RequestMetaData.class), any(RequestIdentifier.class))).thenReturn(request);
        fileEventHandler.setRequestFactory(requestFactory);
        
        tmpDir = new File("tmp/");
        tmpDir.mkdir();
    }
    
    @After
    public void tearDown() {
        
        tmpDir.delete();
    
    }
    
    @Test
    public void testFileEventHandlerSubmitsAJobToDroid() {
        
        SubmissionThrottle throttle = mock(SubmissionThrottle.class);
        fileEventHandler.setSubmissionThrottle(throttle);
        
        File file = new File("test_sig_files/DROID 5  Architecture.doc");
        URI uri = file.toURI();
        AbstractProfileResource resource = mock(AbstractProfileResource.class);
        when(resource.getUri()).thenReturn(uri);
        
        ProfileResourceNode node = mock(ProfileResourceNode.class);
        when(node.getUri()).thenReturn(uri);
        
        ArgumentCaptor<IdentificationRequest> requestCaptor = ArgumentCaptor.forClass(IdentificationRequest.class);

        fileEventHandler.onEvent(file, new ResourceId(1L, ""), null);
        
        verify(identificationEngine).submit(requestCaptor.capture());
    }
    
    @Test
    public void testFileEventHandlerAppliesThrottleAfterSubmittingAFile() throws Exception {
        
        SubmissionThrottle throttle = mock(SubmissionThrottle.class);
        fileEventHandler.setSubmissionThrottle(throttle);
        
        File file = new File("test_sig_files/DROID 5  Architecture.doc");
        fileEventHandler.onEvent(file, new ResourceId(1L, ""), null);
        
        verify(throttle).apply();
    }

    @Test
    public void testNonexistentFileSubmitsErrorToResultHandler() throws IOException {
        
        final File file = new File("non-existent");
        assertFalse(file.exists());
        
        when(requestFactory.newRequest(any(RequestMetaData.class), any(RequestIdentifier.class)))
            .thenReturn(request);
        
        fileEventHandler.setRequestFactory(requestFactory);
        
        ResultHandler resultHandler = mock(ResultHandler.class);
        
        fileEventHandler.setResultHandler(resultHandler);
        
        SubmissionThrottle throttle = mock(SubmissionThrottle.class);
        fileEventHandler.setSubmissionThrottle(throttle);

        fileEventHandler.onEvent(file, new ResourceId(1L, ""), null);
        
        ArgumentCaptor<IdentificationException> exCaptor = ArgumentCaptor.forClass(IdentificationException.class);
        verify(resultHandler).handleError(exCaptor.capture());
        
        final IdentificationException thrown = exCaptor.getValue();
        assertTrue(thrown.getCause() instanceof FileNotFoundException);
        assertEquals(request, thrown.getRequest());
        assertEquals(IdentificationErrorType.FILE_NOT_FOUND, thrown.getErrorType());
    }

    @Test
    public void testUnreadableFileSubmitsErrorToResultHandler() throws IOException {
        
        final File file = new File("tmp/unreadable.file");
        file.createNewFile();
        assertTrue(file.exists());
        
        when(requestFactory.newRequest(any(RequestMetaData.class), any(RequestIdentifier.class)))
            .thenReturn(request);
        
        final IOException ioException = new IOException("Can't read me!");
        doThrow(ioException).when(request).open(any(InputStream.class));
        
        fileEventHandler.setRequestFactory(requestFactory);
        
        ResultHandler resultHandler = mock(ResultHandler.class);
        
        fileEventHandler.setResultHandler(resultHandler);
        
        SubmissionThrottle throttle = mock(SubmissionThrottle.class);
        fileEventHandler.setSubmissionThrottle(throttle);

        fileEventHandler.onEvent(file, new ResourceId(1L, ""), null);
        
        ArgumentCaptor<IdentificationException> exCaptor = ArgumentCaptor.forClass(IdentificationException.class);
        verify(resultHandler).handleError(exCaptor.capture());
        
        final IdentificationException thrown = exCaptor.getValue();
        assertEquals(ioException, thrown.getCause());
        assertEquals("Can't read me!", thrown.getMessage());
        assertEquals(request, thrown.getRequest());
        assertEquals(IdentificationErrorType.ACCESS_DENIED, thrown.getErrorType());
        
        file.delete();
    }
}
