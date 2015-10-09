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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Iterator;

import org.jwat.common.ByteCountingPushBackInputStream;
import org.jwat.warc.WarcReader;
import org.jwat.warc.WarcReaderFactory;
import org.jwat.warc.WarcRecord;

import uk.gov.nationalarchives.droid.command.action.CommandExecutionException;
import uk.gov.nationalarchives.droid.container.ContainerSignatureDefinitions;
import uk.gov.nationalarchives.droid.core.BinarySignatureIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.archive.IdentificationRequestFactory;
import uk.gov.nationalarchives.droid.core.interfaces.archive.WebArchiveEntryRequestFactory;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;

/**
 * Identifier for files held in a WARC archive.
 * 
 * @author G.Seaman
 */
public class WarcArchiveContentIdentifier extends ArchiveContentIdentifier {

    /**
     * 
     * @param binarySignatureIdentifier     binary signature identifier
     * @param containerSignatureDefinitions container signatures
     * @param path                          current archive path 
     * @param slash                         local path element delimiter
     * @param slash1                        local first container prefix delimiter
     */
    public WarcArchiveContentIdentifier(final BinarySignatureIdentifier binarySignatureIdentifier,
            final ContainerSignatureDefinitions containerSignatureDefinitions,
            final String path, final String slash, final String slash1) {
    
        super(binarySignatureIdentifier, containerSignatureDefinitions, path, slash, slash1, false);
    }
    
    /**
     * @param uri The URI of the file to identify
     * @param request The Identification Request
     * @throws CommandExecutionException When an exception happens during execution
     * @throws CommandExecutionException When an exception happens during archive access
     */
    public void identify(final URI uri, final IdentificationRequest request)
        throws CommandExecutionException {
        /**
         * Save importing all the http codes
         */
        final int httpACCEPTED = 200;

        final String newPath = makeContainerURI("warc", request.getFileName());
        setSlash1("");
        final IdentificationRequestFactory factory  = new WebArchiveEntryRequestFactory();
        try {
            InputStream warcIn = request.getSourceInputStream();
            WarcReader warcReader = WarcReaderFactory.getReader(warcIn);
            Iterator<WarcRecord> iterator = warcReader.iterator();
            try {
                WarcRecord record = null;
                while (iterator.hasNext()) {
                    record = iterator.next();
                    // skip all but responses, and only accept HTTP 200s
                    // This means .wat and .wet files will report as empty
                    if ("response".equals(record.header.warcTypeStr)
                        && record.getHttpHeader() != null
                        && httpACCEPTED == record.getHttpHeader().statusCode) {
                        // no directory structure, so we use the full url as name
                        String name = record.header.warcTargetUriStr;

                        RequestMetaData metaData = new RequestMetaData(
                            record.header.contentLength,
                            record.header.warcDate.getTime(),
                            name);

                        final RequestIdentifier identifier = new RequestIdentifier(uri);
                        IdentificationRequest warcRequest = factory.newRequest(metaData, identifier);
                        ByteCountingPushBackInputStream in =
                                (ByteCountingPushBackInputStream) record.getPayloadContent();

                        expandContainer(warcRequest, in, newPath);
                    }
                }
            } finally {
                if (warcIn != null) {
                    warcIn.close();
                }
            }
        } catch (IOException ioe) {
            System.err.println(ioe + ": " + newPath); // continue after corrupt archive
        }
    }

}
