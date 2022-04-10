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
package uk.gov.nationalarchives.droid.internal.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import uk.gov.nationalarchives.droid.core.SignatureParseException;
import uk.gov.nationalarchives.droid.core.interfaces.DroidCore;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.archive.ContainerIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.resource.FileSystemIdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;
import uk.gov.nationalarchives.droid.core.BinarySignatureIdentifier;

/**
 *
 */
public final class DroidAPI {

    private static final String ZIP_PUID = "x-fmt/263";
    private static final String OLE2_PUID = "fmt/111";

    private static AtomicLong idGenerator = new AtomicLong();

    private final DroidCore droidCore;

    private final ContainerIdentifier zipIdendifier;

    private final ContainerIdentifier ole2Idendifier;

    private DroidAPI(DroidCore droidCore, ContainerIdentifier zipIdendifier, ContainerIdentifier ole2Idendifier) {
        this.droidCore = droidCore;
        this.zipIdendifier = zipIdendifier;
        this.ole2Idendifier = ole2Idendifier;
    }

    public static DroidAPI getInstance(final Path binarySignature, final Path containerSignature) throws SignatureParseException {
        BinarySignatureIdentifier droidCore = new BinarySignatureIdentifier();
        droidCore.setSignatureFile(binarySignature.toAbsolutePath().toString());
        droidCore.init();
        droidCore.setMaxBytesToScan(Long.MAX_VALUE);
        droidCore.getSigFile().prepareForUse();

        ContainerApi containerApi = new ContainerApi(droidCore, containerSignature);

        return new DroidAPI(droidCore, containerApi.zipIdentifier(), containerApi.ole2Identifier());
    }

    public IdentificationResultCollection submit(final Path file) throws IOException {
        final RequestMetaData metaData = new RequestMetaData(
                Files.size(file),
                Files.getLastModifiedTime(file).toMillis(),
                file.toAbsolutePath().toString()
        );

        final RequestIdentifier id = new RequestIdentifier(file.toAbsolutePath().toUri());
        id.setParentId(idGenerator.getAndIncrement());
        id.setNodeId(idGenerator.getAndIncrement());

        try (final FileSystemIdentificationRequest request = new FileSystemIdentificationRequest(metaData, id)) {
            request.open(file);

            IdentificationResultCollection binaryResult = droidCore.matchBinarySignatures(request);
            Optional<String> containerPuid = getContainerPuid(binaryResult);

            if (containerPuid.isPresent()) {
                return handleContainer(binaryResult, request, containerPuid.get());
            } else {
                droidCore.removeLowerPriorityHits(binaryResult);
                droidCore.checkForExtensionsMismatches(binaryResult, request.getExtension());
                return binaryResult;
            }
        }
    }


    private Optional<String> getContainerPuid(final IdentificationResultCollection binaryResult) {
        return binaryResult.getResults().stream().filter(x ->
                ZIP_PUID.equals(x.getPuid()) || OLE2_PUID.equals(x.getPuid())
        ).map(x -> x.getPuid()).findFirst();
    }

    private IdentificationResultCollection handleContainer(final IdentificationResultCollection binaryResult,
                                                           final IdentificationRequest identificationRequest, final String containerPuid) throws IOException {
        ContainerIdentifier identifier = null;

        switch (containerPuid) {
            case ZIP_PUID:
                identifier = zipIdendifier;
                break;
            case OLE2_PUID:
                identifier = ole2Idendifier;
                break;
            default:
                throw new RuntimeException("Unknown container PUID : " + containerPuid);
        }

        IdentificationResultCollection containerResults = identifier.submit(identificationRequest);
        droidCore.removeLowerPriorityHits(containerResults);
        droidCore.checkForExtensionsMismatches(containerResults, identificationRequest.getExtension());
        containerResults.setFileLength(identificationRequest.size());
        containerResults.setRequestMetaData(identificationRequest.getRequestMetaData());

        return containerResults.getResults().isEmpty() ? binaryResult : containerResults;
    }
}
