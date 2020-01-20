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

import java.net.URI;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.stephenc.javaisotools.loopfs.iso9660.Iso9660FileEntry;
import com.github.stephenc.javaisotools.loopfs.iso9660.Iso9660FileSystem;

import uk.gov.nationalarchives.droid.command.action.CommandExecutionException;
import uk.gov.nationalarchives.droid.container.ContainerSignatureDefinitions;
import uk.gov.nationalarchives.droid.core.BinarySignatureIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.resource.FileSystemIdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.ISOImageIdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;



/**
 * Created by rhubner on 5/18/17.
 */
public class IsoArchiveContainerIdentifier extends ArchiveContentIdentifier {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Initialization of instance values must be explicitly called by all children.
     *
     * @param binarySignatureIdentifier     binary signature identifier
     * @param containerSignatureDefinitions container signatures
     * @param path                          current archive path
     * @param slash                         local path element delimiter
     * @param slash1                        local first container prefix delimiter
     */
    public IsoArchiveContainerIdentifier(final BinarySignatureIdentifier binarySignatureIdentifier,
                                         final ContainerSignatureDefinitions containerSignatureDefinitions,
                                         final String path, final String slash, final String slash1) {

        super(binarySignatureIdentifier, containerSignatureDefinitions, path, slash, slash1, false);
    }

    /**
     * @param uri to identify
     * @param request identification request. Only FileSystemIdentificationRequest is supported.
     * @throws CommandExecutionException ex.
     */
    public void identify(final URI uri, final IdentificationRequest request) throws CommandExecutionException {

        final String newPath = makeContainerURI("zip", request.getFileName());
        setSlash1("");

        if (request.getClass().isAssignableFrom(FileSystemIdentificationRequest.class)) {

            FileSystemIdentificationRequest req = (FileSystemIdentificationRequest) request;

            try {
                try (Iso9660FileSystem fileSystem = new Iso9660FileSystem(req.getFile().toFile(), true)) {
                    for (Iso9660FileEntry entry : fileSystem) {
                        processEntry(entry, uri, fileSystem, newPath);
                    }
                }
            } catch (IOException e) {
                throw new CommandExecutionException("IO error in ISOFileSystem", e);
            }
        } else {
            log.info("Identification request for ISO image ignored due to limited support.");
        }

    }

    private void processEntry(Iso9660FileEntry entry, URI uri, Iso9660FileSystem fileSystem, String newPath) throws CommandExecutionException {
        String name = entry.getPath();
        if (!entry.isDirectory()) {
            final RequestMetaData metaData = new RequestMetaData(entry.getSize(), 2L, name);
            final RequestIdentifier identifier = new RequestIdentifier(uri);
            ISOImageIdentificationRequest req = new ISOImageIdentificationRequest(metaData, identifier, getTmpDir());
            expandContainer(req, fileSystem.getInputStream(entry), newPath);
        } else {
            log.trace("processing directory : " + entry.getPath());
        }
    }
}
