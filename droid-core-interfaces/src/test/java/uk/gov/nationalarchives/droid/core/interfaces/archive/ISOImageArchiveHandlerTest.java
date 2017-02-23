/**
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gsi.gov.uk>
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


import com.github.stephenc.javaisotools.loopfs.iso9660.Iso9660FileEntry;
import com.github.stephenc.javaisotools.loopfs.iso9660.Iso9660FileSystem;
import com.github.stephenc.javaisotools.vfs.provider.iso.IsoFileSystem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.nationalarchives.droid.core.interfaces.*;
import uk.gov.nationalarchives.droid.core.interfaces.archive.ISOImageArchiveHandler.ISOImageArchiveWalker;
import uk.gov.nationalarchives.droid.core.interfaces.resource.ISOImageIdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by rhubner on 2/15/17.
 */

@RunWith(MockitoJUnitRunner.class)
public class ISOImageArchiveHandlerTest {



    @Test
    public void testImageWalker() throws Exception {
        AsynchDroid droid = mock(AsynchDroid.class);
        IdentificationRequestFactory<InputStream> factory = mock(IdentificationRequestFactory.class);

        ResultHandler resultHandler = mock(ResultHandler.class);
        Iso9660FileSystem filesystem = mock(Iso9660FileSystem.class);
        RequestIdentifier requestIdentifier = new RequestIdentifier(new URI("mock://some/path/to/iso"));
        requestIdentifier.setNodeId(10L);

        ISOImageArchiveWalker walker = new ISOImageArchiveWalker(droid, factory, resultHandler, filesystem,  requestIdentifier);


        Iso9660FileEntry rootEntry = mock(Iso9660FileEntry.class);
        when(rootEntry.getName()).thenReturn(".");
        when(rootEntry.getPath()).thenReturn("");
        when(rootEntry.isDirectory()).thenReturn(true);

        Iso9660FileEntry contentDir = mock(Iso9660FileEntry.class);
        when(contentDir.getName()).thenReturn("content");
        when(contentDir.getPath()).thenReturn("content/");
        when(contentDir.isDirectory()).thenReturn(true);

        Iso9660FileEntry testFile = mock(Iso9660FileEntry.class);
        when(contentDir.getName()).thenReturn("test.txt");
        when(contentDir.getPath()).thenReturn("content/text.txt");
        when(contentDir.isDirectory()).thenReturn(true);


        List<Iso9660FileEntry> entryList = new ArrayList<>();
        entryList.add(rootEntry);
        entryList.add(contentDir);

        walker.walk(entryList);

        verify(resultHandler, atLeastOnce()).handleDirectory(any(IdentificationResult.class), any(ResourceId.class), anyBoolean());

        assertTrue(true);

    }

    @Test
    public void testWithOneFile() throws Exception {
        AsynchDroid droid = mock(AsynchDroid.class);
        IdentificationRequestFactory<InputStream> factory = mock(IdentificationRequestFactory.class);
        when(factory.newRequest(any(RequestMetaData.class), any(RequestIdentifier.class))).thenReturn(mock(IdentificationRequest.class));


        ResultHandler resultHandler = mock(ResultHandler.class);
        when(resultHandler.handleDirectory(any(IdentificationResult.class), any(ResourceId.class), anyBoolean())).thenReturn(mock(ResourceId.class));

        Iso9660FileSystem filesystem = mock(Iso9660FileSystem.class);
        RequestIdentifier requestIdentifier = new RequestIdentifier(new URI("mock://some/path/to/iso"));
        requestIdentifier.setNodeId(10L);

        ISOImageArchiveWalker walker = new ISOImageArchiveWalker(droid, factory, resultHandler, filesystem,  requestIdentifier);


        Iso9660FileEntry rootEntry = mock(Iso9660FileEntry.class);
        when(rootEntry.getName()).thenReturn(".");
        when(rootEntry.getPath()).thenReturn("");
        when(rootEntry.isDirectory()).thenReturn(true);

        Iso9660FileEntry contentDir = mock(Iso9660FileEntry.class);
        when(contentDir.getName()).thenReturn("content");
        when(contentDir.getPath()).thenReturn("content/");
        when(contentDir.isDirectory()).thenReturn(true);

        Iso9660FileEntry testFile = mock(Iso9660FileEntry.class);
        when(contentDir.getName()).thenReturn("test.txt");
        when(contentDir.getPath()).thenReturn("content/text.txt");
        when(contentDir.isDirectory()).thenReturn(true);


        List<Iso9660FileEntry> entryList = new ArrayList<>();
        entryList.add(rootEntry);
        entryList.add(contentDir);
        entryList.add(testFile);

        walker.walk(entryList);

        verify(resultHandler, atLeastOnce()).handleDirectory(any(IdentificationResult.class), any(ResourceId.class), anyBoolean());
        verify(droid, atLeastOnce()).submit(any(IdentificationRequest.class));

        assertTrue(true);

    }

    @Test
    public void testWithOneFileWithWrongOrder() throws Exception {
        AsynchDroid droid = mock(AsynchDroid.class);
        IdentificationRequestFactory<InputStream> factory = mock(IdentificationRequestFactory.class);
        when(factory.newRequest(any(RequestMetaData.class), any(RequestIdentifier.class))).thenReturn(mock(IdentificationRequest.class));


        ResultHandler resultHandler = mock(ResultHandler.class);
        when(resultHandler.handleDirectory(any(IdentificationResult.class), any(ResourceId.class), anyBoolean())).thenReturn(mock(ResourceId.class));
        Iso9660FileSystem filesystem = mock(Iso9660FileSystem.class);
        RequestIdentifier requestIdentifier = new RequestIdentifier(new URI("mock://some/path/to/iso"));
        requestIdentifier.setNodeId(10L);

        ISOImageArchiveWalker walker = new ISOImageArchiveWalker(droid, factory, resultHandler, filesystem,  requestIdentifier);


        Iso9660FileEntry rootEntry = mock(Iso9660FileEntry.class);
        when(rootEntry.getName()).thenReturn(".");
        when(rootEntry.getPath()).thenReturn("");
        when(rootEntry.isDirectory()).thenReturn(true);

        Iso9660FileEntry contentDir = mock(Iso9660FileEntry.class);
        when(contentDir.getName()).thenReturn("content");
        when(contentDir.getPath()).thenReturn("content/");
        when(contentDir.isDirectory()).thenReturn(true);

        Iso9660FileEntry testFile = mock(Iso9660FileEntry.class);
        when(contentDir.getName()).thenReturn("test.txt");
        when(contentDir.getPath()).thenReturn("content/text.txt");
        when(contentDir.isDirectory()).thenReturn(false);


        List<Iso9660FileEntry> entryList = new ArrayList<>();
        entryList.add(rootEntry);
        entryList.add(testFile);
        entryList.add(contentDir);

        walker.walk(entryList);

        InOrder inOrder = inOrder(resultHandler, droid);

        inOrder.verify(resultHandler, atLeastOnce()).handleDirectory(any(IdentificationResult.class), any(ResourceId.class), anyBoolean());
        inOrder.verify(droid, atLeastOnce()).submit(any(IdentificationRequest.class));

        assertTrue(true);
    }

}
