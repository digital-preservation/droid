/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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
