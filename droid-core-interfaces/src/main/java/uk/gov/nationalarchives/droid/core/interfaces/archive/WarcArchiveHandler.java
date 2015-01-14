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
package uk.gov.nationalarchives.droid.core.interfaces.archive;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;

import org.jwat.warc.WarcHeader;
import org.jwat.warc.WarcReader;
import org.jwat.warc.WarcReaderFactory;
import org.jwat.warc.WarcRecord;

import uk.gov.nationalarchives.droid.core.interfaces.ResourceId;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;


/**
 * @author rflitcroft
 * @author gseaman
 *
 */
public class WarcArchiveHandler extends WebArchiveHandler implements ArchiveHandler {
    /**
     * Used to generate URIs within Warc file
     */
    protected static final String WEB_ARCHIVE_TYPE = "warc";

    /**
     * {@inheritDoc}
     */
    @Override
    public final void handle(IdentificationRequest request) throws IOException {

        final InputStream arcIn = request.getSourceInputStream();
        try {

            Iterable<WarcRecord> iterable = new Iterable<WarcRecord>() {
                @Override
                public final Iterator<WarcRecord> iterator() {
                    return new WarcArchiveEntryIterator(arcIn);
                }
            };

            WarcArchiveWalker walker = new WarcArchiveWalker(request.getIdentifier(), arcIn);
            walker.walk(iterable);
        } finally {
            if (arcIn != null) {
                arcIn.close();
            }
        }
    }
    

    /**
     * Adapts the JWAT ByteCountingPushbackInputStream to generate warc entries.
     * @author gseaman
     *
     */
    private static final class WarcArchiveEntryIterator
            extends ArchiveInputStreamIterator<WarcRecord, InputStream> {

        private Iterator<WarcRecord> iterator;

        WarcArchiveEntryIterator(InputStream in) {
            super(in); // dummy call - we override most of supers methods
            try {
                WarcReader warcReader = WarcReaderFactory.getReader(in);
                this.iterator = warcReader.iterator();
            } catch (IOException e) {
                // TODO log
                System.err.println(e);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected WarcRecord getNextEntry(InputStream in) throws IOException {
            WarcRecord record = null;
            if (this.iterator.hasNext()) {
                record = this.iterator.next();
                // skip all but responses, and only accept HTTP 200s
                while (record != null
                        && (!"response".equals(record.header.warcTypeStr)
                        ||  record.getHttpHeader() == null
                        || (HTTP_ACCEPTED != record.getHttpHeader().statusCode))) {
                    if (this.iterator.hasNext()) {
                        record = this.iterator.next();
                    } else {
                        record = null;
                    }
                }
            }
            return record;
        }

    }

    /**
     * Submits a request to droid.
     * @param entry the arc entry to submit
     * @param entryName the name of the entry
     * @param parentName the name of the parent file
     * @param in the archive input stream
     * @param correlationId the correlation Id for the request
     * @param originatorNodeId the Id of the originator node
     * @throws IOException if the input stream could not be read
     */
    final void submit(WarcRecord entry, String entryName, URI parentName,
                      InputStream in, ResourceId correlationId, long originatorNodeId) throws IOException {
        WarcHeader header = entry.header;
        long size = header.contentLength;
        Date time = header.warcDate;

        RequestMetaData metaData = new RequestMetaData(
                size == -1 ? null : size,
                time == null ? null : time.getTime(),
                entryName);

        super.submit(WEB_ARCHIVE_TYPE, metaData, parentName,
                entry.getPayloadContent(), correlationId, originatorNodeId);
    }


    /**
     * Archive walker for WARC archives.
     * modelled on walker for TAR archives
     * @author rflitcroft
     * @author gseaman
     */
    private final class WarcArchiveWalker extends ArchiveFileWalker<WarcRecord> {

        private final ResourceId parentId;
        private final long originatorNodeId;
        private final URI parentName;
        private final InputStream in;
        private final Map<String, ResourceId> directories = new HashMap<String, ResourceId>();

        WarcArchiveWalker(RequestIdentifier parent, InputStream in) {
            this.in = in;
            this.parentId = parent.getResourceId();
            this.parentName = parent.getUri();
            this.originatorNodeId = parent.getAncestorId();
        }


        @Override
        protected void handleEntry(WarcRecord entry) throws IOException {
            final int maxLEN = 4095;

            String entryUri = entry.header.warcTargetUriStr;
            String entryPath = new URL(entryUri).getFile();
            // remove querystring if any (may include slashes)
            final int queryPos = entryPath.indexOf('?');
            String querylessPath = queryPos > 0 ? entryPath.substring(0, queryPos) : entryPath;
            String entryName = entryPath.substring(querylessPath.lastIndexOf('/') + 1);
            String prefixPath = entryUri.substring(0, entryUri.length() - entryName.length());
            ResourceId correlationId = parentId; // by default, files are correlated to the parent.
            String requestUri = entry.getHttpHeader().requestUri;

            // If there is a path, get the actual correlation id for its parent folder:
            if (!prefixPath.isEmpty()) {
                correlationId = directories.get(prefixPath);
                // If we haven't seen the path before, add the ancestor folders not yet seen:
                if (correlationId == null) {
                    correlationId = processAncestorFolders(WEB_ARCHIVE_TYPE, prefixPath,
                            requestUri, parentId, parentName, directories);
                }
            }
            // if the file name (including querystring) is > 4096 chars, truncate it for the DB
            String truncatedName = entryName.length() < maxLEN ? entryName : entryName.substring(0, maxLEN);

            submit(entry, truncatedName, parentName, in, correlationId, originatorNodeId);
        }

    }
}
