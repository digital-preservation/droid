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
import java.io.InputStream;
import java.net.URI;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;

import uk.gov.nationalarchives.droid.core.interfaces.AsynchDroid;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;

/**
 * @author rflitcroft
 *
 */
public class GZipArchiveHandlerTest {

    @Test
    public void testHandleGZipFile() throws Exception {

        File file = new File(getClass().getResource("/testXmlFile.xml.gz").getFile());

        IdentificationRequest request = mock(IdentificationRequest.class);
        
        IdentificationRequestFactory factory = mock(IdentificationRequestFactory.class);
        
        
        URI expectedUri = ArchiveFileUtils.toGZipUri(file.toURI());
        
        RequestIdentifier identifier = new RequestIdentifier(file.toURI());
        identifier.setAncestorId(10L);
        identifier.setParentId(20L);
        identifier.setNodeId(30L);

        RequestIdentifier expectedIdentifier = new RequestIdentifier(expectedUri);
        expectedIdentifier.setAncestorId(10L);
        expectedIdentifier.setParentId(30L);

        when(factory.newRequest(any(RequestMetaData.class), eq(expectedIdentifier)))
            .thenReturn(request);

        AsynchDroid droidCore = mock(AsynchDroid.class);

        GZipArchiveHandler handler = new GZipArchiveHandler();
        handler.setFactory(factory);
        handler.setDroidCore(droidCore);
        
        IdentificationRequest originalRequest = mock(IdentificationRequest.class);
        when(originalRequest.getIdentifier()).thenReturn(identifier);
        when(originalRequest.getSourceInputStream()).thenReturn(new FileInputStream(file));
        
        handler.handle(originalRequest);
        
        verify(request).open(any(InputStream.class));
        verify(droidCore).submit(request);
    }
    
//    private static Matcher<RequestMetaData> uriMatcher(final URI uri) {
//        
//        return new TypeSafeMatcher<RequestMetaData>() {
//            @Override
//            public void describeTo(Description arg0) {
//                arg0.appendText("Matches " + uri);
//                
//            }
//            @Override
//            public boolean matchesSafely(RequestMetaData item) {
//                return item.getUri().equals(uri);
//            }
//        };
//    }
}
