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


import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import uk.gov.nationalarchives.droid.core.interfaces.*;
import uk.gov.nationalarchives.droid.core.interfaces.resource.FatFileIdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.FileSystemIdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;



import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

public class FatArchiveHandlerTest {


    @Test
    public void testWithFatFile() throws Exception {

        Path tmpDir = Files.createTempDirectory("fat-test");

        FatEntryRequestFactory factory = mock(FatEntryRequestFactory.class);

        AsynchDroid droid = mock(AsynchDroid.class);

        ResultHandler resultHandler = mock(ResultHandler.class);
        when(resultHandler.handleDirectory(any(IdentificationResult.class), any(ResourceId.class), anyBoolean())).thenReturn(mock(ResourceId.class));

        FatArchiveHandler fatArchiveHandler = new FatArchiveHandler();
        fatArchiveHandler.setDroid(droid);
        fatArchiveHandler.setFactory(factory);
        when(factory.newRequest(any(RequestMetaData.class),any(RequestIdentifier.class))).thenAnswer(new Answer<FatFileIdentificationRequest>() {
            @Override
            public FatFileIdentificationRequest answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                Object mock = invocationOnMock.getMock();
                return new FatFileIdentificationRequest((RequestMetaData)args[0],(RequestIdentifier)args[1],tmpDir);
            }
        });


        fatArchiveHandler.setResultHandler(resultHandler);


        RequestMetaData requestMetaData = new RequestMetaData(1474560L, 1L, "fat12.img");
        RequestIdentifier identifier = new RequestIdentifier(new URI("file://fat12.img"));
        identifier.setNodeId(1L);


        FileSystemIdentificationRequest req = new FileSystemIdentificationRequest(requestMetaData, identifier);


        req.open(Paths.get("./src/test/resources/fat12.img"));
        fatArchiveHandler.handle(req);

        verify(droid, times(7)).submit(any(IdentificationRequest.class));

        verify(resultHandler, times(1)).handleDirectory(any(IdentificationResult.class), any(ResourceId.class), anyBoolean());

        FileUtils.deleteDirectory(tmpDir.toFile());

    }

}