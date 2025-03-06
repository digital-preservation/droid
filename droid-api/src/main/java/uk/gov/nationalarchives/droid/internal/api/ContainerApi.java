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

import java.io.InputStream;
import java.nio.file.Path;

import uk.gov.nationalarchives.droid.container.*;
import uk.gov.nationalarchives.droid.container.gz.GzIdentifier;
import uk.gov.nationalarchives.droid.container.gz.GzIdentifierEngine;
import uk.gov.nationalarchives.droid.container.ole2.Ole2Identifier;
import uk.gov.nationalarchives.droid.container.ole2.Ole2IdentifierEngine;
import uk.gov.nationalarchives.droid.container.zip.ZipIdentifier;
import uk.gov.nationalarchives.droid.container.zip.ZipIdentifierEngine;
import uk.gov.nationalarchives.droid.core.interfaces.DroidCore;
import uk.gov.nationalarchives.droid.core.interfaces.archive.ArchiveFormatResolver;
import uk.gov.nationalarchives.droid.core.interfaces.archive.ArchiveFormatResolverImpl;
import uk.gov.nationalarchives.droid.core.interfaces.archive.ContainerIdentifierFactory;
import uk.gov.nationalarchives.droid.core.interfaces.archive.IdentificationRequestFactory;
import uk.gov.nationalarchives.droid.core.interfaces.archive.ContainerIdentifierFactoryImpl;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileException;

public final class ContainerApi {

    private final DroidCore droid;
    private final Path containerSignature;

    public ContainerApi(DroidCore droid, Path containerSignature) {
        this.droid = droid;
        this.containerSignature = containerSignature;
    }

    public ContainerSignatureFileReader signatureReader() {
        ContainerSignatureFileReader reader = new ContainerSignatureFileReader();
        reader.setFilePath(containerSignature.toAbsolutePath().toString());

        return reader;
    }

    private IdentificationRequestFactory<InputStream> requestFactory() {
        return new ContainerFileIdentificationRequestFactory();
    }

    private IdentifierEngine zipIdentifierEngine() {
        ZipIdentifierEngine engine = new ZipIdentifierEngine();
        engine.setRequestFactory(requestFactory());
        return engine;
    }

    private IdentifierEngine gzIdentifierEngine() {
        GzIdentifierEngine engine = new GzIdentifierEngine();
        engine.setRequestFactory(requestFactory());
        return engine;
    }

    private Ole2IdentifierEngine ole2IdentifierEngine() {
        Ole2IdentifierEngine engine = new Ole2IdentifierEngine();
        engine.setRequestFactory(requestFactory());
        return engine;
    }

    private ArchiveFormatResolver archiveFormatResolver() {
        return new ArchiveFormatResolverImpl();
    }

    private ContainerIdentifierFactory identifierFactory() {
        return new ContainerIdentifierFactoryImpl();
    }

    public GzIdentifier gzIdentifier() {
        return initialiseContainerIdentifier(new GzIdentifier(), gzIdentifierEngine());
    }

    public ZipIdentifier zipIdentifier() {
        return initialiseContainerIdentifier(new ZipIdentifier(), zipIdentifierEngine());
    }

    public Ole2Identifier ole2Identifier() {
        return initialiseContainerIdentifier(new Ole2Identifier(), ole2IdentifierEngine());
    }

    private <T extends AbstractContainerIdentifier, U extends IdentifierEngine> T initialiseContainerIdentifier(T containerIdentifier, U identifierEngine) {
        containerIdentifier.setContainerIdentifierFactory(identifierFactory());
        containerIdentifier.setContainerFormatResolver(archiveFormatResolver());
        containerIdentifier.setDroidCore(droid);
        containerIdentifier.setIdentifierEngine(identifierEngine);
        containerIdentifier.setSignatureReader(signatureReader());
        try {
            containerIdentifier.init();
        } catch (SignatureFileException ex) {
            throw new RuntimeException("Unable to init " + containerIdentifier.getClass().getSimpleName(), ex);
        }
        return containerIdentifier;
    }
}
