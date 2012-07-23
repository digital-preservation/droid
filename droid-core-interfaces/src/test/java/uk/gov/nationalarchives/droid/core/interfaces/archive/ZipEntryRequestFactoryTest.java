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
