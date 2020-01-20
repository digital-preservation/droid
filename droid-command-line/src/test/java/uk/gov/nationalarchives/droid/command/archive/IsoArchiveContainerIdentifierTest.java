/**
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
package uk.gov.nationalarchives.droid.command.archive;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import uk.gov.nationalarchives.droid.command.action.CommandExecutionException;

import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.resource.FileSystemIdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;


/**
 * Created by rhubner on 5/18/17.
 */
public class IsoArchiveContainerIdentifierTest {

    private ArchiveContainerTestHelper testHelper = new ArchiveContainerTestHelper();
    private Path filePath = Paths.get("src/test/resources/testfiles/testiso.iso");

    @Test
    public void identifyIsoFile()throws CommandExecutionException, IOException {
        RequestIdentifier identifier = new RequestIdentifier(filePath.toUri());
        identifier.setParentId(1L);

        RequestMetaData metaData = new RequestMetaData(Files.size(filePath),
                Files.getLastModifiedTime(filePath).toMillis(), filePath.toAbsolutePath().toString());
        FileSystemIdentificationRequest request = new FileSystemIdentificationRequest(metaData, identifier);
        request.open(filePath);


        IsoArchiveContainerIdentifier isoArchiveContainerIdentifier =
                new IsoArchiveContainerIdentifier(testHelper.getBinarySignatureIdentifier(),
                        testHelper.getContainerSignatureDefinitions(), "", "/", "/");

        isoArchiveContainerIdentifier.identify(filePath.toUri(), request);
    }
}
