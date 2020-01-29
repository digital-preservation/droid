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
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import static org.junit.Assert.fail;
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

/**
 *
 * @author rbrennan
 */
public class TarArchiveContentIdentifierTest {
    
    private BinarySignatureIdentifier binarySignatureIdentifier;
    private TarArchiveContentIdentifier tarArchiveContentIdentifier;
    private ContainerSignatureDefinitions containerSignatureDefinitions;
    private String standardSignatures =
            "src/test/resources/signatures/DROID_SignatureFile_V91.xml";
    private Path containerSignatures =
            Paths.get("src/test/resources/signatures/container-signature-20170330.xml");
    private String tarFile =
            "src/test/resources/testfiles/test.tar";

    @Before
    public void setUp() throws CommandExecutionException {
        binarySignatureIdentifier = new BinarySignatureIdentifier();
        binarySignatureIdentifier.setSignatureFile(standardSignatures);
        try {
            binarySignatureIdentifier.init();
        } catch (SignatureParseException e) {
            throw new CommandExecutionException("Can't parse signature file");
        }
        try (final InputStream in = Files.newInputStream(containerSignatures);) {
            ContainerSignatureSaxParser parser = new ContainerSignatureSaxParser();
            containerSignatureDefinitions = parser.parse(in);
        } catch (SignatureParseException e) {
            throw new CommandExecutionException ("Can't parse container signature file");
        } catch (Exception e) {
            throw new CommandExecutionException(e);
        }
        tarArchiveContentIdentifier =
                new TarArchiveContentIdentifier(binarySignatureIdentifier,
                    containerSignatureDefinitions, "", "/", "/", new ArchiveConfiguration(true, null, true, null));
    }
    
    @After
    public void tearDown() {
        tarArchiveContentIdentifier = null;
        containerSignatureDefinitions = null;
        binarySignatureIdentifier = null;
    }
    
    @Test
    public void identifyGZipArchiveTest() throws CommandExecutionException {

        String fileName;
        final Path file = Paths.get(tarFile);
        if (!Files.exists(file)) {
            fail("TAR test file not found");
        }
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
            tarArchiveContentIdentifier.identify(uri, request);

        } catch (IOException e) {
            throw new CommandExecutionException(e);
        }
    }
}
