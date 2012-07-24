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
package uk.gov.nationalarchives.droid.core.interfaces.archive;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.matchers.TypeSafeMatcher;
import org.mockito.ArgumentCaptor;

import uk.gov.nationalarchives.droid.core.interfaces.AsynchDroid;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.ResourceId;
import uk.gov.nationalarchives.droid.core.interfaces.ResultHandler;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;

/**
 * Handles files identified as ZIP archives.
 * @author rflitcroft
 *
 */
public class ZipArchiveHandlerTest {

    @Test
    public void testHandleZipFile() throws Exception {

        File file = new File(getClass().getResource("/saved.zip").getFile());

        List<IdentificationRequest> mockRequests = new ArrayList<IdentificationRequest>();
        for (int i = 0;  i < 7; i++) {
            mockRequests.add(mock(IdentificationRequest.class));
        }
        
        List<URI> expectedDirectories = new ArrayList<URI>();
        IdentificationRequestFactory factory = mock(IdentificationRequestFactory.class);
        
        ZipArchiveInputStream zin = new ZipArchiveInputStream(new FileInputStream(file));
        int entryCount = 0;
        ZipArchiveEntry entry;
        ResourceId parentId = new ResourceId(20L, "X");
        ResourceId nodeId = new ResourceId(20L, "X");
        while ((entry = zin.getNextZipEntry()) != null) {
            URI expectedUri = ArchiveFileUtils.toZipUri(file.toURI(), entry.getName());
            
            RequestIdentifier identifer = new RequestIdentifier(expectedUri);
            identifer.setAncestorId(10L);
            identifer.setParentResourceId(parentId);
            
            if (!entry.isDirectory()) {
                when(factory.newRequest(any(RequestMetaData.class), eq(identifer)))
                        .thenReturn(mockRequests.get(entryCount));
                entryCount++;
            } else {
                expectedDirectories.add(expectedUri);
            }
        }

        AsynchDroid droidCore = mock(AsynchDroid.class);

        
        
        ZipArchiveHandler handler = new ZipArchiveHandler();
        ResultHandler resultHandler = mock(ResultHandler.class);
        when(resultHandler.handleDirectory(any(IdentificationResult.class), eq(parentId), 
                 eq(false))).thenReturn(nodeId);
        handler.setResultHandler(resultHandler);

        handler.setFactory(factory);
        handler.setDroidCore(droidCore);
        
        IdentificationRequest originalRequest = mock(IdentificationRequest.class);
        RequestIdentifier identifier = new RequestIdentifier(file.toURI());
        identifier.setAncestorId(10L);
        identifier.setResourceId(nodeId);
        identifier.setParentResourceId(parentId);
        
        when(originalRequest.getIdentifier()).thenReturn(identifier);
        
        when(originalRequest.getSourceInputStream()).thenReturn(new FileInputStream(file));
        handler.handle(originalRequest);

        verify(mockRequests.get(0)).open(any(InputStream.class));
        verify(mockRequests.get(1)).open(any(InputStream.class));
        verify(mockRequests.get(2)).open(any(InputStream.class));
        verify(mockRequests.get(3)).open(any(InputStream.class));
        verify(mockRequests.get(4)).open(any(InputStream.class));
        verify(mockRequests.get(5)).open(any(InputStream.class));
        verify(mockRequests.get(6)).open(any(InputStream.class));

        // verify that file files got submitted
        assertEquals(7, entryCount);
        verify(droidCore).submit(mockRequests.get(0));
        verify(droidCore).submit(mockRequests.get(1));
        verify(droidCore).submit(mockRequests.get(2));
        verify(droidCore).submit(mockRequests.get(3));
        verify(droidCore).submit(mockRequests.get(4));
        verify(droidCore).submit(mockRequests.get(5));
        verify(droidCore).submit(mockRequests.get(6));
        
        //verify that the directories got persisted
        verify(resultHandler).handleDirectory(argThat(resultMatcher(expectedDirectories.get(0))),
                eq(parentId), eq(false));
        verify(resultHandler).handleDirectory(argThat(resultMatcher(expectedDirectories.get(1))),
                eq(parentId), eq(false));
    }
    
    @Test
    public void testZipEntryRequestHandlerGeneratesCorrectRequestMetaData() throws IOException {
        
        String jarFileName = getClass().getResource("/persistence.zip").getFile();
        
        File jarFile = new File(jarFileName);
        assertTrue(jarFile.exists());
        
        ZipArchiveHandler handler = new ZipArchiveHandler();
        handler.setFactory(new ZipEntryRequestFactory());
        
        AsynchDroid droidCore = mock(AsynchDroid.class); 
        handler.setDroidCore(droidCore);
        
        IdentificationRequest originalRequest = mock(IdentificationRequest.class);
        RequestIdentifier identifier = new RequestIdentifier(jarFile.toURI());
        identifier.setAncestorId(10L);
        identifier.setParentId(20L);
        identifier.setNodeId(30L);
        
        when(originalRequest.getIdentifier()).thenReturn(identifier);
        when(originalRequest.getSourceInputStream()).thenReturn(new FileInputStream(jarFile));
        handler.handle(originalRequest);
        
        ArgumentCaptor<IdentificationRequest> captor = ArgumentCaptor.forClass(IdentificationRequest.class);
        verify(droidCore, times(1)).submit(captor.capture());
        
        List<IdentificationRequest> requests = captor.getAllValues();
        assertEquals(1, requests.size());
        assertEquals(52445, requests.get(0).size());
        requests.get(0).getByte(52000);
    }

    private static Matcher<IdentificationResult> resultMatcher(final URI uri) {
        
        return new TypeSafeMatcher<IdentificationResult>() {
            @Override
            public void describeTo(Description arg0) {
                arg0.appendText("Matches " + uri);
                
            }
            @Override
            public boolean matchesSafely(IdentificationResult item) {
                return item.getIdentifier().getUri().equals(uri);
            }
        };
    }
    
    @Test
    public void testHandleMultipartZip() throws Exception {
        String multipartZip = getClass().getResource("/problem_zip/package2.zip").getFile();
        File multipartFile = new File(multipartZip);
        
        
        ZipArchiveHandler handler = new ZipArchiveHandler();
        IdentificationRequest originalRequest = mock(IdentificationRequest.class);
        RequestIdentifier identifier = new RequestIdentifier(multipartFile.toURI());
        identifier.setAncestorId(1L);
        identifier.setParentId(10L);
        identifier.setNodeId(30L);
        
        when(originalRequest.getIdentifier()).thenReturn(identifier);
        
        
//        when(originalRequest.getOriginatorNodeId()).thenReturn(10L);
//        when(originalRequest.getCorrelationId()).thenReturn(20L);
//        when(originalRequest.getUri()).thenReturn(multipartFile.toURI());
        when(originalRequest.getSourceInputStream()).thenReturn(new FileInputStream(multipartFile));
        handler.handle(originalRequest);

        
    }
}
