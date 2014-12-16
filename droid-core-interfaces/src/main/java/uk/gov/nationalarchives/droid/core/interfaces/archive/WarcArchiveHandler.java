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
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.regex.Pattern;

import org.jwat.warc.WarcHeader;
import org.jwat.warc.WarcReader;
import org.jwat.warc.WarcReaderFactory;
import org.jwat.warc.WarcRecord;

import uk.gov.nationalarchives.droid.core.interfaces.ResourceId;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultImpl;
import uk.gov.nationalarchives.droid.core.interfaces.AsynchDroid;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.ResultHandler;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;



/**
 * @author rflitcroft
 * @author gseaman
 *
 */
public class WarcArchiveHandler implements ArchiveHandler {
    /**
     * Save importing all the http codes
     */
    protected static final int HTTP_ACCEPTED = 200;
    private AsynchDroid droidCore;
    private IdentificationRequestFactory factory;
    private ResultHandler resultHandler;


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
     * @param factory the factory to set
     */
    public final void setFactory(IdentificationRequestFactory factory) {
        this.factory = factory;
    }

    /**
     * @param droidCore the droidCore to set
     */
    public final void setDroidCore(AsynchDroid droidCore) {
        this.droidCore = droidCore;
    }

    /**
     * @param resultHandler the resultHandler to set
     */
    public final void setResultHandler(ResultHandler resultHandler) {
        this.resultHandler = resultHandler;
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
                // skip WARC-internal records, any dns requests, and non 200 responses
                while ("warc-fields".equals(record.header.contentType.mediaType)
                        || "dns".equals(record.header.contentType.mediaType)
                        ||  record.getHttpHeader().statusCode == null
                        || (HTTP_ACCEPTED != record.getHttpHeader().statusCode)) {
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

        RequestIdentifier identifier =
                new RequestIdentifier(ArchiveFileUtils.toWarcUri(parentName, entryName));
        identifier.setAncestorId(originatorNodeId);
        identifier.setParentResourceId(correlationId);
        IdentificationRequest request = factory.newRequest(metaData, identifier);
        request.open(entry.getPayloadContent()); // get the inputstream back from the record, and write it out to file
        droidCore.submit(request);
    }

    /**
     *
     * @param parentName
     * @param entry
     * @param entryName
     * @param correlationId
     * @return
     */
    private ResourceId submitDirectory(final URI parentName,
                                       WarcRecord entry, String entryName, ResourceId correlationId) {
        IdentificationResultImpl result = new IdentificationResultImpl();

        RequestMetaData metaData = new RequestMetaData(null, null, entryName);

        RequestIdentifier identifier = new RequestIdentifier(
            ArchiveFileUtils.toWarcUri(parentName, entry.getHttpHeader().requestUri));

        result.setRequestMetaData(metaData);
        result.setIdentifier(identifier);
        return resultHandler.handleDirectory(result, correlationId, false);
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
            String entryUri = entry.header.warcTargetUriStr;
            String entryPath = new URL(entryUri).getFile();
            String entryName = entryPath.substring(entryPath.lastIndexOf('/') + 1);
            String prefixPath = entryUri.substring(0, entryUri.length() - entryName.length());
            ResourceId correlationId = parentId; // by default, files are correlated to the parent.

            // If there is a path, get the actual correlation id for its parent folder:
            if (!prefixPath.isEmpty()) {
                correlationId = directories.get(prefixPath);
                // If we haven't seen the path before, add the ancestor folders not yet seen:
                if (correlationId == null) {
                    correlationId = processAncestorFolders(prefixPath, entry);
                }
            }

            submit(entry, entryName, parentName, in, correlationId, originatorNodeId);
        }




        /**
         * Finds the longest path which has been seen before (if any),
         * and adds all the subsequent folders which haven't been seen.
         * @param prefixPath the path of
         */
        private ResourceId processAncestorFolders(String url, WarcRecord entry) {
            // Split the path string into a list of ancestor paths:
            List<String> urls = getAncestorUrls(url);
            ResourceId longestParentId = parentId;

            // Find the longest path we *have* seen before (if any):
            // (ancestor paths are ordered longest first)
            int longestSeenBefore = urls.size();
            ResourceId correlationId = null;
            for (int urlIndex = 0; urlIndex < urls.size(); urlIndex++) {
                correlationId = directories.get(urls.get(urlIndex));
                if (correlationId != null) {
                    longestSeenBefore = urlIndex;
                    longestParentId = correlationId;
                    break;
                }
            }

            // Add any that haven't yet been processed (from shortest to longest)
            Pattern p = Pattern.compile("/");
            for (int urlIndex = longestSeenBefore - 1; urlIndex >= 0; urlIndex--) {
                final String urlName = urls.get(urlIndex);
                String dirName = urlName;
                String[] dirs = p.split(urlName);
                // extract the last directory name if any
                if (!"".equals(dirs[dirs.length - 1])) {
                    dirName = dirs[dirs.length - 1];
                }
                longestParentId = submitDirectory(parentName, entry, dirName, longestParentId);
                directories.put(urlName, longestParentId);
            }

            return longestParentId;
        }

    }
    /**
     *
     * @param url Url as string up to last containing directory
     * @return String[] a string array containing the parent folders, each of
     *         which is a url string in its own right (not just the names of each individual folder)
     */
    public static List<String> getAncestorUrls(String url) {
        String schemeSeparator = "://";
        ArrayList<String> urls = new ArrayList<String>();
        if (url != null && !url.isEmpty()) {
            String urlPath = url.substring(url.indexOf(schemeSeparator) + schemeSeparator.length());
            String urlScheme = url.substring(0, url.indexOf(schemeSeparator) + schemeSeparator.length());
            char separator = '/'; // even on Windows
            // allow for urls without trailing slash
            int lastSeparator = urlPath.length();
            if (separator == urlPath.charAt(urlPath.length() - 1)) {
                lastSeparator -= 1;
            }
            while (lastSeparator >= 0) {
                urlPath = urlPath.substring(0, lastSeparator);
                urls.add(urlScheme + urlPath + String.valueOf(separator));
                lastSeparator = urlPath.lastIndexOf(separator);
            }
        }
        return urls;
    }

}
