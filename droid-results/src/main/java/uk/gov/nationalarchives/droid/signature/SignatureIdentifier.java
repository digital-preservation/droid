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
package uk.gov.nationalarchives.droid.signature;

import java.io.IOException;
import java.net.URI;

import uk.gov.nationalarchives.droid.core.BinarySignatureIdentifier;
import uk.gov.nationalarchives.droid.core.SignatureParseException;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.core.interfaces.archive.ArchiveFormatResolver;
import uk.gov.nationalarchives.droid.core.interfaces.archive.ContainerIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.archive.ContainerIdentifierFactory;

/**
 * A signature identifier that can match binary signatures, container signatures and file extensions.
 */
public class SignatureIdentifier extends BinarySignatureIdentifier {

    /**
     * A class which can return the correct container signature identifier given its container type (e.g. ZIP or OLE2).
     */
    private ContainerIdentifierFactory containerIdentifierFactory;

    /**
     * Empty bean constructor.
     */
    public SignatureIdentifier() {
        super();
    }

    //TODO: the signature identifier should not be parsing the signature file.
    //      This should be the responsibility of another class which parses signature files and caches the result.

    /**
     * A parameterized constructor.
     *
     * @param signatureFileURI the URI of the binary signature file to parse.
     * @param containerFormatResolver The class which examines a set of results to see if there is a container type registered for them.
     * @param containerIdentifierFactory The class which can provide a container identifier given the container type.
     * @throws SignatureParseException if the binary signature file could not be parsed.
     */
    public SignatureIdentifier(URI signatureFileURI,
                               ArchiveFormatResolver containerFormatResolver,
                               ContainerIdentifierFactory containerIdentifierFactory) throws SignatureParseException {
        super(signatureFileURI, containerFormatResolver);
        setContainerIdentifierFactory(containerIdentifierFactory);
    }

    /**
     * Sets the container identifier factory.
     * @param containerIdentifierFactory The container identifier factory to set.
     */
    public void setContainerIdentifierFactory(ContainerIdentifierFactory containerIdentifierFactory) {
        this.containerIdentifierFactory = containerIdentifierFactory;
    }

    @Override
    public IdentificationResultCollection matchContainerSignatures(IdentificationRequest request, String containerType) throws IOException {
        if (containerType != null) {
            ContainerIdentifier containerIdentifier = containerIdentifierFactory.getIdentifier(containerType);
            //containerIdentifier.setMaxBytesToScan(maxBytesToScan);
            IdentificationResultCollection containerResults = containerIdentifier.submit(request);
            containerResults.setFileLength(request.size());
            containerResults.setRequestMetaData(request.getRequestMetaData());
            return containerResults;
        }
        return null;
    }



}
