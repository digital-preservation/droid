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
package uk.gov.nationalarchives.droid.core.interfaces.archive;

import org.apache.ant.compress.util.SevenZStreamFactory;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.junit.Test;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.ResourceId;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.AsynchDroid;
import uk.gov.nationalarchives.droid.core.interfaces.ResultHandler;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SevenZArchiveHandlerTest {

    @Test
    public void testHandleSevenZFile() throws Exception {

        final Path file = Paths.get(getClass().getResource("/saved.7z").toURI());

        IdentificationRequestFactory factory = mock(IdentificationRequestFactory.class);

        List<IdentificationRequest> mockRequests = new ArrayList<>();
        SevenZStreamFactory sevenZStreamFactory = new SevenZStreamFactory();
        ArchiveInputStream archiveInputStream = sevenZStreamFactory.getArchiveInputStream(file.toFile(), null);

        ArchiveEntry entry;
        ResourceId expectedParentId = new ResourceId(30L, "");

        int count = 0;
        while ((entry = archiveInputStream.getNextEntry()) != null) {
            URI expectedUri = ArchiveFileUtils.toSevenZUri(file.toUri(), entry.getName());
            IdentificationRequest mockRequest = mock(IdentificationRequest.class);
            
            RequestIdentifier expectedIdentifier = new RequestIdentifier(expectedUri);
            expectedIdentifier.setParentResourceId(expectedParentId);
            expectedIdentifier.setParentPrefix(null);
            expectedIdentifier.setAncestorId(10L);

            when(mockRequest.getIdentifier()).thenReturn(expectedIdentifier);
            mockRequests.add(mockRequest);
            when(factory.newRequest(any(RequestMetaData.class), eq(expectedIdentifier))).thenReturn(mockRequests.get(count));
            count++;
        }
        
        AsynchDroid droidCore = mock(AsynchDroid.class);

        SevenZipArchiveHandler handler = new SevenZipArchiveHandler();
        handler.setFactory(factory);
        handler.setDroid(droidCore);
        
        ResultHandler resultHandler = mock(ResultHandler.class);
        when(resultHandler.handleDirectory(any(IdentificationResult.class), 
                any(ResourceId.class), anyBoolean())).thenReturn(new ResourceId(30L, ""));
        handler.setResultHandler(resultHandler);
        
        IdentificationRequest originalRequest = mock(IdentificationRequest.class);
        RequestIdentifier originalIdentifier = new RequestIdentifier(file.toUri());
        originalIdentifier.setAncestorId(10L);
        originalIdentifier.setParentId(20L);
        originalIdentifier.setNodeId(30L);
        
        when(originalRequest.getIdentifier()).thenReturn(originalIdentifier);
        when(originalRequest.getSourceInputStream()).thenReturn(Files.newInputStream(file));
        when(originalRequest.getWindowReader()).thenReturn(new net.byteseek.io.reader.FileReader(file.toFile()));
        handler.handle(originalRequest);

        verify(droidCore).submit(mockRequests.get(2));
        verify(droidCore).submit(mockRequests.get(3));
        verify(droidCore).submit(mockRequests.get(4));
        verify(droidCore).submit(mockRequests.get(5));
        verify(droidCore).submit(mockRequests.get(6));
    }

}
