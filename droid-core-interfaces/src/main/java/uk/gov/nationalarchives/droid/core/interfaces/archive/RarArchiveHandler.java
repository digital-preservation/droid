package uk.gov.nationalarchives.droid.core.interfaces.archive;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.impl.FileVolumeManager;
import com.github.junrar.rarfile.FileHeader;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.gov.nationalarchives.droid.core.interfaces.*;
import uk.gov.nationalarchives.droid.core.interfaces.resource.FileSystemIdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RarIdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rhubner on 3/21/17.
 */
public final class RarArchiveHandler implements ArchiveHandler {

    private final Log log = LogFactory.getLog(this.getClass());

    private ResultHandler resultHandler;
    private AsynchDroid droid;

    @Override
    public void handle(IdentificationRequest request) throws IOException {

        if (request.getClass().isAssignableFrom(FileSystemIdentificationRequest.class)) {

            FileSystemIdentificationRequest req = (FileSystemIdentificationRequest) request;

            FileVolumeManager fileVolumeManager = new FileVolumeManager(req.getFile());
            try {
                try (Archive archive = new Archive(fileVolumeManager)){
                    if(archive.isEncrypted()) {
                        throw new RuntimeException("Encrypted archive");
                    }
                    RarWalker walker = new RarWalker(archive);
                    walker.walk(archive.getFileHeaders());
                }
            }catch (RarException ex) {
                throw new RuntimeException("Rar procesing failed :", ex);
            }
        } else {
            log.info("Identification request for RAR archive ignored due to limited support.");
        }
    }

    private final class RarWalker extends ArchiveFileWalker<FileHeader> {

        private final Map<String, ResourceId> directories = new HashMap<String, ResourceId>();
        private final Log log = LogFactory.getLog(this.getClass());
        private final Archive archive;

        private final URI parentURI = null;         //TODO
        private final long originatorNodeId = -1;   //TODO


        private RarWalker(Archive archive) {
            this.archive = archive;
        }

        private ResourceId submitDirectory(String path, Date cTime)
                throws URISyntaxException, UnsupportedEncodingException {

            String parentPath = FilenameUtils.getPath(path.substring(0, path.length() - 1));

            String name = FilenameUtils.getName(path.substring(0, path.length() - 1));

            log.info("processing path: " + path + " name: " + name);

            ResourceId resourceId = directories.get(name);
            if (resourceId == null) {

                ResourceId parentID = directories.get(parentPath);
                if (parentID == null) {
                    parentID = submitDirectory(parentPath, cTime);
                }

                RequestMetaData metaData = new RequestMetaData(null, cTime.getTime() / 1000, name);


                RequestIdentifier identifier = null; //TODO  //new RequestIdentifier(ArchiveFileUtils.toIsoImageUri(isoFileUri, path));

                IdentificationResultImpl result = new IdentificationResultImpl();
                result.setRequestMetaData(metaData);
                result.setIdentifier(identifier);


                resourceId = resultHandler.handleDirectory(result, parentID, false);
                this.directories.put(path, resourceId);

            }
            return resourceId;
        }

        private void submitFile(FileHeader entry) throws IOException, URISyntaxException, RarException {
            String path = FilenameUtils.getPath(entry.getFileNameString());
            String name = FilenameUtils.getBaseName(entry.getFileNameString());

            ResourceId correlationId = this.directories.get(path);
            if (correlationId == null) {
                correlationId = submitDirectory(path, entry.getCTime());
            }

            InputStream entryInputStream = archive.getInputStream(entry);

            RequestIdentifier identifier = new RequestIdentifier(ArchiveFileUtils.toRarImageUri(parentURI, path + name));
            identifier.setAncestorId(originatorNodeId);
            identifier.setParentResourceId(correlationId);

            RequestMetaData metaData = new RequestMetaData(entry.getUnpSize(),      //TODO test which size!!!
                    entry.getCTime().getTime() / 1000, name);


            IdentificationRequest<InputStream> request = new RarIdentificationRequest(metaData, identifier, null);    //TODO, create factory

            request.open(entryInputStream);
            droid.submit(request);
        }




        @Override
        protected void handleEntry(FileHeader entry) throws IOException {
            try {
                if (entry.isDirectory()) {
                    String path = entry.getFileNameString();
                    if(!path.endsWith("/")) {
                        path += "/";
                    }
                    submitDirectory(path, entry.getCTime());
                } else if(entry.isEncrypted()){
                    throw new RuntimeException("Encrypted entry : " + entry.getFileNameString());
                }else {
                    submitFile(entry);
                }

            }catch (URISyntaxException ex) {
                throw new RuntimeException("Malformed uri for entry : " + entry.getFileNameString(), ex);
            }catch (RarException rarEx) {
                throw new RuntimeException("Probem with RAR extraction : " + entry.getFileNameString(), rarEx);

            }

        }
    }

}
