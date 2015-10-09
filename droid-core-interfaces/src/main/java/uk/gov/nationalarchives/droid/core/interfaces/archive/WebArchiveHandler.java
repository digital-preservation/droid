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
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;

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
 * Common code for WarcArchiveHandler and ArcArchiveHandler
 */
public abstract class WebArchiveHandler {
    /**
     * Save importing all the http codes
     */
    protected static final int HTTP_ACCEPTED = 200;
    private AsynchDroid droidCore;
    private IdentificationRequestFactory factory;
    private ResultHandler resultHandler;

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
     * @param webArchiveType
     * @param parentName
     * @param resourceName
     * @param entryName
     * @param correlationId
     * @return
     */
    private ResourceId submitDirectory(final String webArchiveType, final URI parentName,
                                       String resourceName, String entryName, ResourceId correlationId) {
        IdentificationResultImpl result = new IdentificationResultImpl();

        RequestMetaData metaData = new RequestMetaData(null, null, entryName);

        RequestIdentifier identifier = new RequestIdentifier(
            ArchiveFileUtils.toWebArchiveUri(webArchiveType, parentName, resourceName));

        result.setRequestMetaData(metaData);
        result.setIdentifier(identifier);
        return resultHandler.handleDirectory(result, correlationId, false);
    }


    /**
     * Submits a request for a single file to droid.
     * @param webArchiveType arc or warc
     * @param metaData file size, time and name
     * @param parentName the name of the parent file
     * @param payload the file input stream
     * @param correlationId the correlation Id for the request
     * @param originatorNodeId the Id of the originator node
     * @throws IOException if the input stream could not be read
     */
    final void submit(String webArchiveType, RequestMetaData metaData, URI parentName,
                      InputStream payload, ResourceId correlationId, long originatorNodeId) throws IOException {

        RequestIdentifier identifier =
                new RequestIdentifier(ArchiveFileUtils.toWebArchiveUri(webArchiveType, parentName, metaData.getName()));
        identifier.setAncestorId(originatorNodeId);
        identifier.setParentResourceId(correlationId);
        IdentificationRequest request = factory.newRequest(metaData, identifier);
        request.open(payload); // get the inputstream back from the record, and write it out to file
        droidCore.submit(request);
    }



    /**
     * Find all logical parents of the current file/folder.
     * @param url Url as string up to last containing directory
     * @return String[] a string array containing the parent folders, each of
     *         which is a url string in its own right (not just the names of each individual folder)
     */
    public static List<String> getAncestorUrls(String url) {
        final String schemeSeparator = "://";
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

    /**
     * Finds the longest path which has been seen before (if any),
     * and adds all the subsequent folders which haven't been seen.
     * @param webArchiveType arc or warc
     * @param url         URL of file
     * @param requestUri  URI requested by crawler
     * @param parentId    Id of parent directory
     * @param parentName  Name of parent directory
     * @param directories Map of all directory paths generated
     * @return id of longest path
     */
    protected ResourceId processAncestorFolders(String webArchiveType, String url, String requestUri,
                                    ResourceId parentId, URI parentName, Map<String, ResourceId> directories) {
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
            longestParentId = submitDirectory(webArchiveType, parentName, requestUri, dirName, longestParentId);
            directories.put(urlName, longestParentId);
        }

        return longestParentId;
    }

}

