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
package uk.gov.nationalarchives.droid.core.interfaces.archive;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import uk.gov.nationalarchives.droid.core.interfaces.AsynchDroid;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.ResourceId;
import uk.gov.nationalarchives.droid.core.interfaces.ResultHandler;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;

/**
 * @author rflitcroft
 *
 */
public class TarArchiveHandlerTest {

    @Test
    public void testHandleTarFile() throws Exception {

        final Path file = Paths.get(getClass().getResource("/saved.tar").toURI());

        IdentificationRequestFactory factory = mock(IdentificationRequestFactory.class);

        // count the tar entries
        List<IdentificationRequest> mockRequests = new ArrayList<>();
        try(final InputStream in = Files.newInputStream(file);
            final TarArchiveInputStream tarIn = new TarArchiveInputStream(in)) {
            ArchiveEntry entry;
            ResourceId expectedParentId = new ResourceId(99L, "");
            int count = 0;
            while ((entry = tarIn.getNextEntry()) != null) {
                URI expectedUri = ArchiveFileUtils.toTarUri(file.toUri(), entry.getName());
                IdentificationRequest mockRequest = mock(IdentificationRequest.class);

                RequestIdentifier expectedIdentifier = new RequestIdentifier(expectedUri);
                expectedIdentifier.setParentResourceId(expectedParentId);
                expectedIdentifier.setAncestorId(10L);

                when(mockRequest.getIdentifier()).thenReturn(expectedIdentifier);
                mockRequests.add(mockRequest);
                when(factory.newRequest(any(RequestMetaData.class), eq(expectedIdentifier)))
                        .thenReturn(mockRequests.get(count));
                count++;
            }

            AsynchDroid droidCore = mock(AsynchDroid.class);

            TarArchiveHandler handler = new TarArchiveHandler();
            handler.setFactory(factory);
            handler.setDroidCore(droidCore);

            ResultHandler resultHandler = mock(ResultHandler.class);
            when(resultHandler.handleDirectory(any(IdentificationResult.class),
                    any(ResourceId.class), anyBoolean())).thenReturn(expectedParentId);
            handler.setResultHandler(resultHandler);

            IdentificationRequest originalRequest = mock(IdentificationRequest.class);
            RequestIdentifier originalIdentifier = new RequestIdentifier(file.toUri());
            originalIdentifier.setAncestorId(10L);
            originalIdentifier.setParentId(20L);
            originalIdentifier.setNodeId(30L);

            when(originalRequest.getIdentifier()).thenReturn(originalIdentifier);
            when(originalRequest.getSourceInputStream()).thenReturn(Files.newInputStream(file));
            handler.handle(originalRequest);

            //TODO:MP: mocking this fails... fix these tests.
            //BNO: These tests now working (didn't do anything specific to fix them!)
            verify(droidCore).submit(mockRequests.get(2));
            verify(droidCore).submit(mockRequests.get(3));
            verify(droidCore).submit(mockRequests.get(4));
            verify(droidCore).submit(mockRequests.get(5));
            verify(droidCore).submit(mockRequests.get(6));
        }
    }
    
    @Test
    public void testZipEntryRequestHandlerGeneratesCorrectRequestMetaData() throws IOException, URISyntaxException {
        
        URI jarFileName = getClass().getResource("/persistence.tar").toURI();
        
        Path jarFile = Paths.get(jarFileName);
        assertTrue(Files.exists(jarFile));
        
        TarArchiveHandler handler = new TarArchiveHandler();
        handler.setFactory(new TarEntryRequestFactory());
        
        AsynchDroid droidCore = mock(AsynchDroid.class); 
        handler.setDroidCore(droidCore);
        
        IdentificationRequest originalRequest = mock(IdentificationRequest.class);
        RequestIdentifier identifier = new RequestIdentifier(jarFile.toUri());
        identifier.setAncestorId(10L);
        //identifier.setParentId(20L);
        identifier.setNodeId(20L);
        
        when(originalRequest.getIdentifier()).thenReturn(identifier);
        when(originalRequest.getSourceInputStream()).thenReturn(Files.newInputStream(jarFile));
        handler.handle(originalRequest);
        
        ArgumentCaptor<IdentificationRequest> captor = ArgumentCaptor.forClass(IdentificationRequest.class);
        verify(droidCore, times(1)).submit(captor.capture());
        
        List<IdentificationRequest> requests = captor.getAllValues();
        assertEquals(1, requests.size());
        assertEquals(52445, requests.get(0).size());
        requests.get(0).getByte(52000);
    }

}
