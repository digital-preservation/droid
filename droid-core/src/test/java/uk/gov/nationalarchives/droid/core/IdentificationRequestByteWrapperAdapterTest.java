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
package uk.gov.nationalarchives.droid.core;


import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.signature.FileFormatHit;

/**
 * @author rflitcroft
 *
 */
public class IdentificationRequestByteWrapperAdapterTest {

    private IdentificationRequestByteReaderAdapter adapter;
    private IdentificationRequest request;
    
    @Before
    public void setup() {
        request = mock(IdentificationRequest.class);
        adapter = new IdentificationRequestByteReaderAdapter(request);
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void testGetBufferIsNotSupported() {
        adapter.getbuffer();
    }
    
    @Test
    public void testGetByte() {
        long index = 12134567;
        
        adapter.getByte(index);
        byte myByte = (byte) 255;
        when(request.getByte(index)).thenReturn(myByte);
        
        assertEquals(myByte, adapter.getByte(index));
    }
    
    @Test
    public void testGetFilename() {
        final String expectedFilename = "my/file.name";
        when(request.getFileName()).thenReturn(expectedFilename);
        
        String fileName = adapter.getFileName();
        assertEquals(expectedFilename, fileName);
    }
    
    @Test
    public void testSetGetFileMarker() {
        
        final long markerPosition = 1234;
        adapter.setFileMarker(markerPosition);
        assertEquals(markerPosition, adapter.getFileMarker());
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void testGetFilePathIsNotSupported() {
        adapter.getFilePath();
    }
    
    @Test
    public void testGetNumBytes() {
        final long mySize = 123456;
        
        when(request.size()).thenReturn(mySize);
        assertEquals(mySize, adapter.getNumBytes());
    }
    
    @Test
    public void testAddAndRemoveHits() {
        FileFormatHit hit0 = mock(FileFormatHit.class);
        FileFormatHit hit1 = mock(FileFormatHit.class);
        FileFormatHit hit2 = mock(FileFormatHit.class);
        assertEquals(0, adapter.getNumHits());

        adapter.addHit(hit0);
        adapter.addHit(hit1);
        adapter.addHit(hit2);
        assertEquals(3, adapter.getNumHits());
        
        assertEquals(hit0, adapter.getHit(0));
        assertEquals(hit1, adapter.getHit(1));
        assertEquals(hit2, adapter.getHit(2));
        
        adapter.removeHit(1);
        
        assertEquals(2, adapter.getNumHits());
        assertEquals(hit0, adapter.getHit(0));
        assertEquals(hit2, adapter.getHit(1));
    }
    

    @Test
    public void testSetPositiveIdent() {
        adapter.setPositiveIdent();
    }

    @Test
    public void setTentativeIdent() {
        adapter.setPositiveIdent();
    }

    @Test
    public void setNoIdent() {
        adapter.setPositiveIdent();
    }

    @Test
    public void setErrorIdent() {
        adapter.setPositiveIdent();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testIsClassifiedIsUnsupported() {
        adapter.isClassified();
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void testGetIdentificationWarningIsUnsupported() {
        adapter.getIdentificationWarning();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetIdentificationWarningIsUnsupported() {
        adapter.setIdentificationWarning("");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetClassificationIsUnsupported() {
        adapter.getClassification();
    }
}
