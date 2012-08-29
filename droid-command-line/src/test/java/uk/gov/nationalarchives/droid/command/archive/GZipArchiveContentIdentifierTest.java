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
package uk.gov.nationalarchives.droid.command.archive;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
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
public class GZipArchiveContentIdentifierTest {
    
    private BinarySignatureIdentifier binarySignatureIdentifier;
    private GZipArchiveContentIdentifier gZipArchiveContentIdentifier;
    private ContainerSignatureDefinitions containerSignatureDefinitions;
    private String standardSignatures =
            "src/test/resources/signatures/DROID_SignatureFile_V63.xml";
    private String containerSignatures =
            "src/test/resources/signatures/container-signature-20120828.xml";
    private String gZipFile =
            "src/test/resources/testfiles/test.gz";
    
    @Before
    public void setUp() throws CommandExecutionException {
        binarySignatureIdentifier = new BinarySignatureIdentifier();
        binarySignatureIdentifier.setSignatureFile(standardSignatures);
        try {
            binarySignatureIdentifier.init();
        } catch (SignatureParseException e) {
            throw new CommandExecutionException("Can't parse signature file");
        }
        try {
            InputStream in = new FileInputStream(containerSignatures);
            ContainerSignatureSaxParser parser = new ContainerSignatureSaxParser();
            containerSignatureDefinitions = parser.parse(in);
        } catch (SignatureParseException e) {
            throw new CommandExecutionException ("Can't parse container signature file");
        } catch (Exception e) {
            throw new CommandExecutionException(e);
        }
        gZipArchiveContentIdentifier =
                new GZipArchiveContentIdentifier(binarySignatureIdentifier,
                    containerSignatureDefinitions, "", "/", "/");
    }
    
    @After
    public void tearDown() {
        gZipArchiveContentIdentifier = null;
        containerSignatureDefinitions = null;
        binarySignatureIdentifier = null;
    }
    
    @Test
    public void identifyGZipArchiveTest() throws CommandExecutionException {

        String fileName;
        File file = new File(gZipFile);
        if (!file.exists()) {
            fail("GZIP test file not found");
        }
        URI uri = file.toURI();
        RequestIdentifier identifier = new RequestIdentifier(uri);
        identifier.setParentId(1L);
        try {
            fileName = file.getCanonicalPath();
            RequestMetaData metaData =
                new RequestMetaData(file.length(), file.lastModified(), fileName);
            FileSystemIdentificationRequest request =
                new FileSystemIdentificationRequest(metaData, identifier);

            InputStream gZipStream = new FileInputStream(file);
            request.open(gZipStream);
            gZipArchiveContentIdentifier.identify(uri, request);

        } catch (IOException e) {
            throw new CommandExecutionException(e);
        }
    }
}
