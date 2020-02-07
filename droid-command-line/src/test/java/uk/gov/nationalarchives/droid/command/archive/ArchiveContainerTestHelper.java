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

import uk.gov.nationalarchives.droid.command.action.CommandExecutionException;
import uk.gov.nationalarchives.droid.container.ContainerSignatureDefinitions;
import uk.gov.nationalarchives.droid.container.ContainerSignatureSaxParser;
import uk.gov.nationalarchives.droid.core.BinarySignatureIdentifier;
import uk.gov.nationalarchives.droid.core.SignatureParseException;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ArchiveContainerTestHelper {

    private static final String STANDARD_SIGNATURES =
            "src/test/resources/signatures/DROID_SignatureFile_V96.xml";
    private static final Path CONTAINER_SIGNATURES =
            Paths.get("src/test/resources/signatures/container-signature-20200121.xml");

    public String getBinarySignatureFileLocation() {
        return STANDARD_SIGNATURES;
    }

    public BinarySignatureIdentifier getBinarySignatureIdentifier() throws CommandExecutionException {
        BinarySignatureIdentifier binarySignatureIdentifier = new BinarySignatureIdentifier();
        binarySignatureIdentifier.setSignatureFile(STANDARD_SIGNATURES);
        try {
            binarySignatureIdentifier.init();
        } catch (SignatureParseException e) {
            throw new CommandExecutionException("Can't parse signature file");
        }
        return binarySignatureIdentifier;
    }

    public ContainerSignatureDefinitions getContainerSignatureDefinitions() throws CommandExecutionException {
        ContainerSignatureDefinitions containerSignatureDefinitions;
        try (final InputStream in = Files.newInputStream(CONTAINER_SIGNATURES)) {
            ContainerSignatureSaxParser parser = new ContainerSignatureSaxParser();
            containerSignatureDefinitions = parser.parse(in);
        } catch (SignatureParseException e) {
            throw new CommandExecutionException ("Can't parse container signature file");
        } catch (Exception e) {
            throw new CommandExecutionException(e);
        }
        return containerSignatureDefinitions;
    }
}
