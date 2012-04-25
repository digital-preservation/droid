/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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
