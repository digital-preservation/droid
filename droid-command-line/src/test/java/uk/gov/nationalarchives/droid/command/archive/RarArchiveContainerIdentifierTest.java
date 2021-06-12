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
package uk.gov.nationalarchives.droid.command.archive;

import org.junit.Before;
import org.junit.Test;
import uk.gov.nationalarchives.droid.command.action.CommandExecutionException;
import uk.gov.nationalarchives.droid.container.ContainerSignatureDefinitions;
import uk.gov.nationalarchives.droid.container.ContainerSignatureSaxParser;
import uk.gov.nationalarchives.droid.core.BinarySignatureIdentifier;
import uk.gov.nationalarchives.droid.core.SignatureParseException;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.resource.FileSystemIdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by rhubner on 5/18/17.
 */
public class RarArchiveContainerIdentifierTest {


    private BinarySignatureIdentifier binarySignatureIdentifier;
    private RarArchiveContainerIdentifier rarArchiveContainerIdentifier;
    private ContainerSignatureDefinitions containerSignatureDefinitions;
    private String rarFile =
            "src/test/resources/testfiles/sample.rar";

    @Before
    public void setUp() throws CommandExecutionException {
        ArchiveContainerTestHelper helper = new ArchiveContainerTestHelper();
        binarySignatureIdentifier = helper.getBinarySignatureIdentifier();
        containerSignatureDefinitions = helper.getContainerSignatureDefinitions();
        rarArchiveContainerIdentifier =
                new RarArchiveContainerIdentifier(binarySignatureIdentifier,
                        containerSignatureDefinitions, "", "/", "/", new ArchiveConfiguration(true, null, true, null));
    }

    @Test
    public void identifyRarFile() throws CommandExecutionException {

        String fileName;
        final Path file = Paths.get(rarFile);
        URI uri = file.toUri();
        RequestIdentifier identifier = new RequestIdentifier(uri);
        identifier.setParentId(1L);
        try {
            fileName = file.toAbsolutePath().toString();
            RequestMetaData metaData = new RequestMetaData(
                    Files.size(file), Files.getLastModifiedTime(file).toMillis(), fileName);
            FileSystemIdentificationRequest request =
                    new FileSystemIdentificationRequest(metaData, identifier);
            request.open(file);

            // we run test, bu how we get result????
            rarArchiveContainerIdentifier.identify(uri, request);


        } catch (IOException e) {
            throw new CommandExecutionException(e);
        }
    }
}
