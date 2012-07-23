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
import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;
import uk.gov.nationalarchives.droid.core.interfaces.ResourceId;
import uk.gov.nationalarchives.droid.core.interfaces.ResultHandler;

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
        
        File file = new File("my/directory");
        URI uri = file.toURI();
        
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
        final File file = new File("tmp/fictional");
        URI uri = file.toURI();
        
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
    public void testDirectoryResultMetaDataWhenDepthOne() {
        
        final long length = 120000;
        
        File dir = mock(File.class);
        when(dir.lastModified()).thenReturn(123456789L);
        final URI uri = URI.create("file:/c:/my-dir");
        when(dir.toURI()).thenReturn(uri);
        when(dir.length()).thenReturn(length);
        when(dir.getName()).thenReturn("my-dir");
        when(dir.getAbsolutePath()).thenReturn("c:/my-dir");
        
        ResourceId id = new ResourceId(1L, "");
        
        directoryEventHandler.setResultHandler(resultHandler);
        directoryEventHandler.onEvent(dir, id, 1, false);
        
        ArgumentCaptor<IdentificationResult> resultCaptor = ArgumentCaptor.forClass(IdentificationResult.class); 
        verify(resultHandler).handleDirectory(resultCaptor.capture(), eq(id), eq(false));
        
        IdentificationResult result = resultCaptor.getValue();
        assertEquals("my-dir", result.getMetaData().getName());
        assertEquals(length, result.getMetaData().getSize().longValue());
        assertEquals(123456789L, result.getMetaData().getTime().longValue());
        assertEquals(uri, result.getIdentifier().getUri());
        assertEquals(1L, result.getIdentifier().getParentId().longValue());
        
        
    }

    @Test
    public void testDirectoryResultMetaDataWhenDepthZero() {
        
        final long length = 120000;
        
        File dir = mock(File.class);
        when(dir.lastModified()).thenReturn(123456789L);
        final URI uri = URI.create("file:/c:/my-dir");
        when(dir.toURI()).thenReturn(uri);
        when(dir.length()).thenReturn(length);
        when(dir.getName()).thenReturn("my-dir");
        when(dir.getAbsolutePath()).thenReturn("c:/my-dir");
        
        ResourceId id = new ResourceId(1L, "");
        
        directoryEventHandler.setResultHandler(resultHandler);
        directoryEventHandler.onEvent(dir, id, 0, false);
        
        ArgumentCaptor<IdentificationResult> resultCaptor = ArgumentCaptor.forClass(IdentificationResult.class); 
        verify(resultHandler).handleDirectory(resultCaptor.capture(), eq(id), eq(false));
        
        IdentificationResult result = resultCaptor.getValue();
        assertEquals("c:/my-dir", result.getMetaData().getName());
        assertEquals(length, result.getMetaData().getSize().longValue());
        assertEquals(123456789L, result.getMetaData().getTime().longValue());
        assertEquals(uri, result.getIdentifier().getUri());
        assertEquals(1L, result.getIdentifier().getParentId().longValue());

        
    }
}
