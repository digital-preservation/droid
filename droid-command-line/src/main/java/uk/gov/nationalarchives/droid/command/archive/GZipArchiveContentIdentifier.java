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
import java.util.zip.GZIPInputStream;

//import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipUtils;

import uk.gov.nationalarchives.droid.command.ResultPrinter;
import uk.gov.nationalarchives.droid.command.action.CommandExecutionException;
import uk.gov.nationalarchives.droid.container.ContainerSignatureDefinitions;
import uk.gov.nationalarchives.droid.core.BinarySignatureIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.resource.GZipIdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;

/**
 * Identifier for files held in a GZIP archive.
 * 
 * @author rbrennan
 */
public class GZipArchiveContentIdentifier extends ArchiveContentIdentifier {

    private static final long SIZE = 12L;
    private static final long TIME = 13L;


    /**
     *
     * @param binarySignatureIdentifier     binary signature identifier
     * @param containerSignatureDefinitions container signatures
     * @param path                          current archive path
     * @param slash                         local path element delimiter
     * @param slash1                        local first container prefix delimiter
     * @param archiveConfiguration          configuration to expand archives and web archives
     */
    public GZipArchiveContentIdentifier(final BinarySignatureIdentifier binarySignatureIdentifier,
                                        final ContainerSignatureDefinitions containerSignatureDefinitions,
                                        final String path, final String slash, final String slash1, final ArchiveConfiguration archiveConfiguration) {

       super(binarySignatureIdentifier, containerSignatureDefinitions, path,
            slash, slash1, archiveConfiguration);

    }

    /**
     * @param uri The URI of the file to identify
     * @param request The Identification Request
     * @throws CommandExecutionException When an exception happens during execution
     * @throws CommandExecutionException When an exception happens during archive file input/output
     */

    public final void identify(final URI uri, final IdentificationRequest request)
        throws CommandExecutionException {

        final String newPath = "gzip:" + slash1 + path + request.getFileName() + "!" + slash;
        slash1 = "";
        final URI newUri = URI.create(GzipUtils.getUncompressedFilename(uri.toString()));

        final RequestIdentifier identifier = new RequestIdentifier(newUri);
        final RequestMetaData metaData = new RequestMetaData(SIZE, TIME, uri.getPath());
        final GZipIdentificationRequest gzRequest = new GZipIdentificationRequest(
                metaData, identifier, tmpDir);

        GZIPInputStream gzin = null;
        try {
            gzin = new GZIPInputStream(request.getSourceInputStream());
            gzRequest.open(gzin);
            final IdentificationResultCollection gzResults =
                    binarySignatureIdentifier.matchBinarySignatures(gzRequest);

            final ResultPrinter resultPrinter = new ResultPrinter(binarySignatureIdentifier,
                    containerSignatureDefinitions, newPath, slash, slash1, super.getArchiveConfiguration());
            resultPrinter.print(gzResults, gzRequest);
        } catch (IOException ioe) {
            System.err.println(ioe + " (" + newPath + ")"); // continue after corrupt archive
        } finally {
            if (gzin != null) {
                try {
                    gzin.close();
                } catch (IOException ioe) {
                    throw new CommandExecutionException(ioe.getMessage(), ioe);
                }
            }
            if (gzRequest != null) {
                try {
                    gzRequest.close();
                } catch (IOException ioe) {
                    throw new CommandExecutionException(ioe.getMessage(), ioe);
                }
            }
        }
    }

}
