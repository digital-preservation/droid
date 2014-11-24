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
import java.util.regex.Matcher;

//import org.apache.commons.io.FilenameUtils;
import org.jwat.arc.ArcReader;
import org.jwat.arc.ArcReaderFactory;
import org.jwat.arc.ArcRecord;
import org.jwat.arc.ArcRecordBase;
//import org.jwat.common.Uri;

//import uk.gov.nationalarchives.droid.core.interfaces.*;
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
public class ArcArchiveHandler implements ArchiveHandler {

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

            Iterable<ArcRecordBase> iterable = new Iterable<ArcRecordBase>() {
                @Override
                public final Iterator<ArcRecordBase> iterator() {
                    return new ArcArchiveEntryIterator(arcIn);
                }
            };

            ArcArchiveWalker walker = new ArcArchiveWalker(request.getIdentifier(), arcIn);
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
     * Adapts the JWAT ByteCountingPushbackInputStream to generate arc entries.
     * @author gseaman
     *
     */
    private static final class ArcArchiveEntryIterator
            extends ArchiveInputStreamIterator<ArcRecordBase, InputStream> {

        private Iterator<ArcRecordBase> iterator;

        ArcArchiveEntryIterator(InputStream in) {
            super(in); // dummy call - we override most of supers methods
            try {
                ArcReader arcReader = ArcReaderFactory.getReader(in);
                this.iterator = arcReader.iterator();
            } catch (IOException e) {
                // TODO log
                System.out.println("boo");
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected ArcRecordBase getNextEntry(InputStream in) throws IOException {
            //get the next real ARC record (not header) and return it as an ArcRecord
            ArcRecordBase base = this.iterator.next();
            String scheme = base.getScheme();
            // skip the header record at the start and any dns requests
            while (!(base instanceof ArcRecord) || "dns".equals(scheme)) {
                base = this.iterator.next();
                scheme = base.getScheme();
            }
            return base;
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
    final void submit(ArcRecordBase entry, String entryName, URI parentName,
                      InputStream in, ResourceId correlationId, long originatorNodeId) throws IOException {
        long size = entry.getArchiveLength();
        Date time = entry.getArchiveDate();

        RequestMetaData metaData = new RequestMetaData(
                size == -1 ? null : size,
                time == null ? null : time.getTime(),
                entryName);

        RequestIdentifier identifier =
                new RequestIdentifier(ArchiveFileUtils.toArcUri(parentName, entryName));
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
                                       ArcRecordBase entry, String entryName, ResourceId correlationId) {
        IdentificationResultImpl result = new IdentificationResultImpl();
        long size = -1; //entry.getSize();
        Date date = null; //entry.getModTime();
        long time = date == null ? -1 : date.getTime();

        RequestMetaData metaData = new RequestMetaData(
                size != -1 ? size : null,
                time != -1 ? time : null,
                entryName);

        RequestIdentifier identifier = new RequestIdentifier(
                ArchiveFileUtils.toArcUri(parentName, entry.getFileName()));

        result.setRequestMetaData(metaData);
        result.setIdentifier(identifier);
        return resultHandler.handleDirectory(result, correlationId, false);
    }


    /**
     * Archive walker for ARC archives.
     * modelled on walker for TAR archives
     * @author rflitcroft
     * @author gseaman
     */
    private final class ArcArchiveWalker extends ArchiveFileWalker<ArcRecordBase> {

        private final ResourceId parentId;
        private final long originatorNodeId;
        private final URI parentName;
        private final InputStream in;
        private final Map<String, ResourceId> directories = new HashMap<String, ResourceId>();

        ArcArchiveWalker(RequestIdentifier parent, InputStream in) {
            this.in = in;
            this.parentId = parent.getResourceId();
            this.parentName = parent.getUri();
            this.originatorNodeId = parent.getAncestorId();
        }


        @Override
        protected void handleEntry(ArcRecordBase entry) throws IOException {
            String entryUri = entry.getUrl().toString();
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
        private ResourceId processAncestorFolders(String url, ArcRecordBase entry) {
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
                if (dirs[dirs.length - 1] != "") {
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
