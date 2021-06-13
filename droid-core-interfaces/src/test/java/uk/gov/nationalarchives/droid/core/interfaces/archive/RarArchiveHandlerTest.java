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

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.impl.FileVolumeManager;
import com.github.junrar.rarfile.FileHeader;
import org.junit.Test;
import uk.gov.nationalarchives.droid.core.interfaces.AsynchDroid;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.ResourceId;
import uk.gov.nationalarchives.droid.core.interfaces.ResultHandler;
import uk.gov.nationalarchives.droid.core.interfaces.resource.FileSystemIdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by rhubner on 3/21/17.
 */
public class RarArchiveHandlerTest {

    @Test
    public void simpleTest() throws IOException, RarException {

        FileVolumeManager manager = new FileVolumeManager(Paths.get("./src/test/resources/sample.rar").toFile());
        Archive archive = new Archive(manager);
        List<FileHeader> headers = archive.getFileHeaders();
        assertEquals(9, headers.size());
        InputStream a = archive.getInputStream(headers.get(0));
        archive.close();
    }


    @Test
    public void testIdentificationInRar() throws URISyntaxException, IOException {

        IdentificationRequestFactory<InputStream> factory = new RarEntryRequestFactory();

        AsynchDroid droid = mock(AsynchDroid.class);
        when(droid.passesIdentificationFilter(any(IdentificationRequest.class))).thenReturn(true);

        ResultHandler resultHandler = mock(ResultHandler.class);
        when(resultHandler.handleDirectory(any(IdentificationResult.class), any(ResourceId.class), anyBoolean())).thenReturn(mock(ResourceId.class));

        RarArchiveHandler rarHandler = new RarArchiveHandler();
        rarHandler.setDroid(droid);
        rarHandler.setIdentificationRequestFactory(factory);
        rarHandler.setResultHandler(resultHandler);

        RequestMetaData requestMetaData = new RequestMetaData(958L, 1L, "sample.rar");
        RequestIdentifier identifier = new RequestIdentifier(new URI("file://sample.rar"));
        identifier.setNodeId(1L);


        FileSystemIdentificationRequest req = new FileSystemIdentificationRequest(requestMetaData, identifier);
        req.open(Paths.get("./src/test/resources/sample.rar"));
        rarHandler.handle(req);

        verify(droid, times(6)).submit(any(IdentificationRequest.class));
        verify(resultHandler, times(3)).handleDirectory(any(IdentificationResult.class), any(ResourceId.class), anyBoolean());

    }
}
