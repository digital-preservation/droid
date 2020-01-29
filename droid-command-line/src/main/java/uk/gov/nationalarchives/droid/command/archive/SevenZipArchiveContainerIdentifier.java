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
import java.net.URI;

import org.apache.ant.compress.util.SevenZStreamFactory;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.io.input.BoundedInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.nationalarchives.droid.command.action.CommandExecutionException;
import uk.gov.nationalarchives.droid.container.ContainerSignatureDefinitions;
import uk.gov.nationalarchives.droid.core.BinarySignatureIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.resource.FileSystemIdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;
import uk.gov.nationalarchives.droid.core.interfaces.resource.SevenZipEntryIdentificationRequest;


/**
 * Created by rhubner on 5/18/17.
 */
public class SevenZipArchiveContainerIdentifier extends ArchiveContentIdentifier {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Initialization of instance values must be explicitly called by all children.
     *  @param binarySignatureIdentifier     binary signature identifier
     * @param containerSignatureDefinitions container signatures
     * @param path                          current archive path
     * @param slash                         local path element delimiter
     * @param slash1                        local first container prefix delimiter
     * @param archiveConfiguration          configuration to expand archives and web archives
     */
    public SevenZipArchiveContainerIdentifier(final BinarySignatureIdentifier binarySignatureIdentifier,
                                              final ContainerSignatureDefinitions containerSignatureDefinitions,
                                              final String path, final String slash, final String slash1, ArchiveConfiguration archiveConfiguration) {

        super(binarySignatureIdentifier, containerSignatureDefinitions, path, slash, slash1, archiveConfiguration);
    }

    /**
     * @param uri to identify
     * @param request identification request. Only FileSystemIdentificationRequest is supported.
     * @throws CommandExecutionException ex.
     */
    public void identify(final URI uri, final IdentificationRequest request) throws CommandExecutionException {

        final String newPath = makeContainerURI("7z", request.getFileName());
        setSlash1("");

        if (request.getClass().isAssignableFrom(FileSystemIdentificationRequest.class)) {

            FileSystemIdentificationRequest req = (FileSystemIdentificationRequest) request;

            try {
                SevenZStreamFactory sevenZStreamFactory = new SevenZStreamFactory();
                try (ArchiveInputStream archiveStream = sevenZStreamFactory.getArchiveInputStream(req.getFile().toFile(), null)) {
                    SevenZArchiveEntry entry;
                    while ((entry = (SevenZArchiveEntry) archiveStream.getNextEntry()) != null) {
                        processEntry(entry, archiveStream, uri, newPath);
                    }
                }
            } catch (IOException e) {
                throw new CommandExecutionException("IO error in 7z FileSystem", e);
            }
        } else {
            log.info("Identification request for 7z archive ignored due to limited support.");
        }

    }

    private void processEntry(SevenZArchiveEntry entry, ArchiveInputStream archiveStream, URI uri, String newPath) throws CommandExecutionException {
        String name = entry.getName();
        if (!entry.isDirectory()) {
            final RequestMetaData metaData = new RequestMetaData(entry.getSize(), 2L, name);
            final RequestIdentifier identifier = new RequestIdentifier(uri);
            SevenZipEntryIdentificationRequest req = new SevenZipEntryIdentificationRequest(metaData, identifier, getTmpDir());
            BoundedInputStream entryStream = new BoundedInputStream(archiveStream, entry.getSize());
            entryStream.setPropagateClose(false);   //Don't close ArchiveStream!!!!
            expandContainer(req, entryStream, newPath);
        } else {
            log.debug("processing directory : " + entry.getName());
        }
    }

}
