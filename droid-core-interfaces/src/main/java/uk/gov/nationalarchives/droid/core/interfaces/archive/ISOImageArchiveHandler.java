/**
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gsi.gov.uk>
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

import com.github.stephenc.javaisotools.loopfs.iso9660.Iso9660FileEntry;
import com.github.stephenc.javaisotools.loopfs.iso9660.Iso9660FileSystem;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.gov.nationalarchives.droid.core.interfaces.*;
import uk.gov.nationalarchives.droid.core.interfaces.resource.FileSystemIdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rhubner on 2/13/17.
 */
public class ISOImageArchiveHandler implements ArchiveHandler {

    private AsynchDroid droid;
    private IdentificationRequestFactory<InputStream> factory;
    private ResultHandler resultHandler;

    @Override
    public void handle(IdentificationRequest request) throws IOException {

        FileSystemIdentificationRequest req = (FileSystemIdentificationRequest) request;

        Iso9660FileSystem fileSystem = new Iso9660FileSystem(req.getFile(), true);

        ISOImageArchiveWalker walker = new ISOImageArchiveWalker(droid, factory, resultHandler, fileSystem, request.getIdentifier());
        walker.walk(fileSystem);

    }

    public static class ISOImageArchiveWalker extends ArchiveFileWalker<Iso9660FileEntry> {

        private final AsynchDroid droid;
        private final IdentificationRequestFactory<InputStream> factory;
        private final ResultHandler resultHandler;


        private final Iso9660FileSystem fileSystem;
        private final ResourceId parentId;
        private final URI parentUri;
        private final long originatorNodeId;

        private final Map<String, ResourceId> directories = new HashMap<String, ResourceId>();
        private final Log log = LogFactory.getLog(this.getClass());


        public ISOImageArchiveWalker(AsynchDroid droid, IdentificationRequestFactory<InputStream> factory,
                                     ResultHandler resultHandler,
                                     Iso9660FileSystem fileSystem, RequestIdentifier requestIdentifier) {
            this.droid = droid;
            this.factory = factory;
            this.resultHandler = resultHandler;
            this.fileSystem = fileSystem;
            this.parentId = requestIdentifier.getResourceId();
            this.parentUri = requestIdentifier.getUri();
            this.originatorNodeId = requestIdentifier.getNodeId();
        }


        final void submitFile(Iso9660FileEntry entry) throws IOException, URISyntaxException {
            String path = FilenameUtils.getPath(entry.getPath());
            String name = entry.getName();


            ResourceId correlationId = this.directories.get(path);
            if(correlationId == null) {
                correlationId = parentId;
            }


            InputStream entryInputStream = fileSystem.getInputStream(entry);

            RequestIdentifier identifier = new RequestIdentifier(new URI("iso://" + parentUri.toString() + "!" + URLEncoder.encode(path + name, "UTF-8")));
            identifier.setAncestorId(originatorNodeId);
            identifier.setParentResourceId(correlationId);

            RequestMetaData metaData = new RequestMetaData(entry.getSize(), entry.getLastModifiedTime(), entry.getName());

            IdentificationRequest<InputStream> request = factory.newRequest(metaData, identifier);
            request.open(entryInputStream);
            droid.submit(request);
        }

        private ResourceId submitDirectory(Iso9660FileEntry entry) throws URISyntaxException {
            log.info("processing directory : " + entry.getPath());

            if(".".equals(entry.getName())) {   //Root directory is always "." (dot)
                directories.put("", parentId);
            }

            String name = entry.getPath();
            ResourceId resourceId = directories.get(name);
            if (resourceId == null) {

                RequestMetaData metaData = new RequestMetaData(null, entry.getLastModifiedTime(), entry.getName());
                RequestIdentifier identifier = new RequestIdentifier(new URI(parentUri.toString() + "!" + entry.getName()));

                IdentificationResultImpl result = new IdentificationResultImpl();
                result.setRequestMetaData(metaData);
                result.setIdentifier(identifier);

                resourceId = resultHandler.handleDirectory(result, parentId, false);
                this.directories.put(name, resourceId);

            }
            return resourceId;
        }

        @Override
        protected void handleEntry(Iso9660FileEntry entry) throws IOException {
            try {
              if(entry.isDirectory()) {
                  submitDirectory(entry);
              }else {
                    submitFile(entry);
              }
            } catch (URISyntaxException e) {
                throw new IOException("Wrong URI syntax", e);
            }
        }

    }

    public void setFactory(IdentificationRequestFactory<InputStream> factory) {
        this.factory = factory;
    }

    public void setDroid(AsynchDroid droid) {
        this.droid = droid;
    }

    public void setResultHandler(ResultHandler resultHandler) {
        this.resultHandler = resultHandler;
    }
}
