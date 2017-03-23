package uk.gov.nationalarchives.droid.core.interfaces.archive;

import org.apache.ant.compress.util.SevenZStreamFactory;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.io.FilenameUtils;
import uk.gov.nationalarchives.droid.core.interfaces.*;
import uk.gov.nationalarchives.droid.core.interfaces.resource.FileSystemIdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by schaudhry on 17/03/17.
 */
public class SevenZipArchiveHandler implements ArchiveHandler {

    private AsynchDroid droid;
    private IdentificationRequestFactory<InputStream> factory;
    private ResultHandler resultHandler;

    @Override
    public void handle(IdentificationRequest request) throws IOException {
        if (request instanceof FileSystemIdentificationRequest) {
            FileSystemIdentificationRequest fileSystemIdentificationRequest = (FileSystemIdentificationRequest) request;
            File file = fileSystemIdentificationRequest.getFile();
            SevenZFile sevenZFile = new SevenZFile(file);
            SevenZStreamFactory sevenZStreamFactory = new SevenZStreamFactory();

            ArchiveInputStream archiveStream = sevenZStreamFactory.getArchiveInputStream(((FileSystemIdentificationRequest) request).getFile(), null);

            final Iterable<SevenZArchiveEntry> entries = sevenZFile.getEntries();
            SevenZArchiveWalker walker = new SevenZArchiveWalker(droid, factory, archiveStream, request.getIdentifier());
            walker.walk(entries);
        }
    }


    public static class SevenZArchiveWalker extends ArchiveFileWalker<SevenZArchiveEntry> {

        private final AsynchDroid droid;
        private IdentificationRequestFactory<InputStream> factory;
        private InputStream in;
        private final ResourceId parentId;
        private final URI parentName;
        private final long originatorNodeId;
        private final Map<String, ResourceId> directories = new HashMap<String, ResourceId>();


        public SevenZArchiveWalker(AsynchDroid droid,
                                   IdentificationRequestFactory<InputStream> factory,
                                   InputStream in,
                                   RequestIdentifier parent) {
            this.in = in;
            this.droid = droid;
            this.factory = factory;
            this.parentId = parent.getResourceId();
            this.parentName = parent.getUri();
            this.originatorNodeId = parent.getAncestorId();
        }

        @Override
        protected void handleEntry(SevenZArchiveEntry entry) throws IOException {
            String entryName = entry.getName();
            final String prefixPath = FilenameUtils.getPath(entryName);
            ResourceId correlationId = parentId; // by default, files are correlated to the parent.

            // If there is a path, get the actual correlation id for its parent folder:
            if (!prefixPath.isEmpty()) {
                correlationId = directories.get(prefixPath);
                // If we haven't seen the path before, add the ancestor folders not yet seen:
                if (correlationId == null) {
                    correlationId = processAncestorFolders(prefixPath);
                }
            }

            // If there is a file, submit the file:
            entryName = FilenameUtils.getName(entryName);
            if (!entryName.isEmpty()) {
                submit(entry, entryName, parentName, in, correlationId, originatorNodeId);
            }
        }
        private ResourceId processAncestorFolders(String path) {
            // TODO : fix up
            ResourceId longestParentId = parentId;
            return longestParentId;
        }

        /**
         * Submits a request to droid.
         * @param entry the tar entry to submit
         * @param entryName the name of the entry
         * @param parentName the name of the parent file
         * @param in the archive input stream
         * @param correlationId the correlation iod for the request
         * @param originatorNodeId the ID of the originator node
         * @throws IOException if the input stream could not be read
         */
        final void submit(SevenZArchiveEntry entry, String entryName, URI parentName,
                          InputStream in, ResourceId correlationId, long originatorNodeId) throws IOException {
            long size = entry.getSize();
            Date time = new Date();

            RequestMetaData metaData = new RequestMetaData(
                    size == -1 ? null : size,
                    time == null ? null : time.getTime(),
                    entryName);

            RequestIdentifier identifier =
                    new RequestIdentifier(ArchiveFileUtils.toTarUri(parentName, entry.getName()));
            identifier.setAncestorId(originatorNodeId);
            identifier.setParentResourceId(correlationId);
            IdentificationRequest<InputStream> request = factory.newRequest(metaData, identifier);
            request.open(in);
            droid.submit(request);
        }

    }



    /**
     * Set factory.
     * @param factory f.
     */
    public void setFactory(IdentificationRequestFactory<InputStream> factory) {
        this.factory = factory;
    }

    /**
     * Sed droid.
     * @param droid d.
     */
    public void setDroid(AsynchDroid droid) {
        this.droid = droid;
    }

    public void setResultHandler(ResultHandler resultHandler) {
        this.resultHandler = resultHandler;
    }
}
