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
package uk.gov.nationalarchives.droid.submitter;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;
import uk.gov.nationalarchives.droid.core.interfaces.ResourceId;
import uk.gov.nationalarchives.droid.core.interfaces.ResultHandler;
import uk.gov.nationalarchives.droid.util.FileUtil;

/**
 * @author rflitcroft
 *
 */
public class DirectoryEventHandlerTest {

    private DirectoryEventHandler directoryEventHandler;
    private ResultHandler resultHandler;

    @Before
    public void setup() {
        directoryEventHandler = new DirectoryEventHandler();
        resultHandler = mock(ResultHandler.class);
        directoryEventHandler.setResultHandler(resultHandler);
    }
    
    @Test
    public void testDirectoryEventHandlerSavesANewNodeButDoesNotSubmitToDroid() {
        
        final Path file = Paths.get("my/directory");
        URI uri = file.toUri();
        
        ResourceId id = new ResourceId(1L, "");
        
        directoryEventHandler.onEvent(file, id, 1, false);
        
        ArgumentCaptor<IdentificationResult> captor = ArgumentCaptor.forClass(IdentificationResult.class);
        verify(resultHandler).handleDirectory(captor.capture(), eq(id), eq(false));
        
        IdentificationResult captured = captor.getValue();
        assertEquals(uri, captured.getIdentifier().getUri());
        assertEquals("directory", captured.getMetaData().getName());
    }
    
    @Test
    public void testResultHandlerHandlesErrorWhenDirectoryAccessIsRestricted() {
        final Path file = Paths.get("tmp/fictional");
        URI uri = file.toUri();
        
        directoryEventHandler.setResultHandler(resultHandler);
        
        ResourceId id = new ResourceId(123L, "");
        
        directoryEventHandler.onEvent(file, id, 1, true);
        
        ArgumentCaptor<IdentificationResult> captor = ArgumentCaptor.forClass(IdentificationResult.class);
        verify(resultHandler).handleDirectory(captor.capture(), eq(id), eq(true));
        
        IdentificationResult captured = captor.getValue();
        assertEquals(uri, captured.getIdentifier().getUri());
        assertEquals("fictional", captured.getMetaData().getName());
        
    }

    @Test
    public void testDirectoryResultMetaDataWhenDepthOne() throws IOException {
        final Path dir = Paths.get(".");

        ResourceId id = new ResourceId(1L, "");
        
        directoryEventHandler.setResultHandler(resultHandler);
        directoryEventHandler.onEvent(dir, id, 1, false);
        
        ArgumentCaptor<IdentificationResult> resultCaptor = ArgumentCaptor.forClass(IdentificationResult.class); 
        verify(resultHandler).handleDirectory(resultCaptor.capture(), eq(id), eq(false));
        
        IdentificationResult result = resultCaptor.getValue();
        assertEquals(dir.getFileName().toString(), result.getMetaData().getName());
        assertEquals("Directory Size is invalid", -1L, result.getMetaData().getSize().longValue());
        assertEquals("Last Modified Time is invalid", Files.getLastModifiedTime(dir).toMillis(), result.getMetaData().getTime().longValue());
        assertEquals(dir.toUri(), result.getIdentifier().getUri());
        assertEquals(1L, result.getIdentifier().getParentId().longValue());
    }

    @Test
    public void testDirectoryResultMetaDataWhenDepthZero() throws IOException {
        final Path dir = Paths.get(".");
        ResourceId id = new ResourceId(1L, "");
        
        directoryEventHandler.setResultHandler(resultHandler);
        directoryEventHandler.onEvent(dir, id, 0, false);
        
        ArgumentCaptor<IdentificationResult> resultCaptor = ArgumentCaptor.forClass(IdentificationResult.class); 
        verify(resultHandler).handleDirectory(resultCaptor.capture(), eq(id), eq(false));
        
        IdentificationResult result = resultCaptor.getValue();
        assertEquals(dir.toAbsolutePath().toString(), result.getMetaData().getName());
        assertEquals("Directory Size is invalid", -1L, result.getMetaData().getSize().longValue());
        assertEquals("Last Modified Time is invalid", Files.getLastModifiedTime(dir).toMillis(), result.getMetaData().getTime().longValue());
        assertEquals(dir.toUri(), result.getIdentifier().getUri());
        assertEquals(1L, result.getIdentifier().getParentId().longValue());

        
    }
}
