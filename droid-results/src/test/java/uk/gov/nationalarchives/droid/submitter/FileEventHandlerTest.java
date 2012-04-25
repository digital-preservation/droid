/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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
    
    @Before
    public void setup() {
        identificationEngine = mock(AsynchDroid.class);
        requestFactory = mock(IdentificationRequestFactory.class);
        request = mock(IdentificationRequest.class);
        fileEventHandler = new FileEventHandler(identificationEngine);
        when(requestFactory.newRequest(any(RequestMetaData.class), any(RequestIdentifier.class))).thenReturn(request);
        fileEventHandler.setRequestFactory(requestFactory);
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
        
        final File file = new File("tmp/unreadable");
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
    }
}
