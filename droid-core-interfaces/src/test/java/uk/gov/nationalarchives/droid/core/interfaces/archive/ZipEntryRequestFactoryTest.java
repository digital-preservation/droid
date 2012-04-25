/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.core.interfaces.archive;

import java.io.InputStream;
import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;
import uk.gov.nationalarchives.droid.core.interfaces.resource.ZipEntryIdentificationRequest;
/**
 * @author rflitcroft
 *
 */
public class ZipEntryRequestFactoryTest {

    private ZipEntryRequestFactory factory;
    
    @Before
    public void setup() {
        factory = new ZipEntryRequestFactory();
    }
    
    @Test
    public void testNewZipRequest() throws Exception {

        URI zipUri = new URI("zip:file:/foo.bar!/fu.bah");
        RequestMetaData metaData = new RequestMetaData(12345L, 67890L, "fu.bah");
        InputStream in = mock(InputStream.class);
        when(in.read()).thenReturn(-1);
        when(in.read(any(byte[].class))).thenReturn(-1);
        when(in.read(any(byte[].class), anyInt(), anyInt())).thenReturn(-1);
        when(in.skip(anyLong())).thenReturn(-1L);
        
        RequestIdentifier identifier = new RequestIdentifier(zipUri);
        
        IdentificationRequest request = factory.newRequest(metaData, identifier);
        assertTrue(request instanceof ZipEntryIdentificationRequest);
        
        assertEquals("fu.bah", request.getFileName());
        assertEquals("bah", request.getExtension());
        assertEquals(metaData, request.getRequestMetaData());
        assertEquals(12345, request.size());
        assertEquals(identifier, request.getIdentifier());
        
    }

}
