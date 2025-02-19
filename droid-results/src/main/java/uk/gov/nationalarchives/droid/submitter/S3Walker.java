package uk.gov.nationalarchives.droid.submitter;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Uri;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;
import uk.gov.nationalarchives.droid.core.interfaces.*;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;
import uk.gov.nationalarchives.droid.profile.AbstractProfileResource;
import uk.gov.nationalarchives.droid.profile.S3ProfileResource;
import uk.gov.nationalarchives.droid.results.handlers.ProgressMonitor;
import uk.gov.nationalarchives.droid.util.FileUtil;

import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;

public class S3Walker {

    private static final String FORWARD_SLASH = "/";
    private static final String S3_SCHEME = "s3://";

    private final ProgressMonitor progressMonitor;

    private final ResultHandler resultHandler;

    private final S3EventHandler s3EventHandler;

    public S3Walker(final ProgressMonitor progressMonitor, final ResultHandler resultHandler, final S3EventHandler s3EventHandler) {
        this.progressMonitor = progressMonitor;
        this.resultHandler = resultHandler;
        this.s3EventHandler = s3EventHandler;
    }

    public void walk(AbstractProfileResource resource) {
        S3Result s3Result = getS3Result(resource);
        progressMonitor.setTargetCount(s3Result.totalCount());
        ArrayList<String> keysList = new ArrayList<>(s3Result.dirToFileMap().keySet());
        Map<String, ResourceId> pathToResourceId = new HashMap<>();
        keysList.sort(Comparator.comparingInt(String::length));
        for (int i=0; i < keysList.size(); i++) {
            URI dirUri = URI.create(keysList.get(i));
            Path dirPath = getPath(dirUri);
            progressMonitor.startJob(dirUri);
            ResourceId fileParentNode = handleS3Directory(dirPath, pathToResourceId.get(dirPath.getParent().toUri().toString()), i+1);
            pathToResourceId.put(dirUri + FORWARD_SLASH, fileParentNode);
            for (String objectUri: s3Result.dirToFileMap().get(keysList.get(i))) {
                progressMonitor.startJob(URI.create(objectUri));
                s3EventHandler.onS3Event(new S3ProfileResource(objectUri), fileParentNode);
            }
        }
    }

    private ResourceId handleS3Directory(final Path dir, ResourceId parentId, int depth) {
        IdentificationResultImpl result = new IdentificationResultImpl();
        result.setMethod(IdentificationMethod.NULL);

        RequestMetaData metaData = new RequestMetaData(-1L, new Date(0).getTime(), depth == 0 ? dir.toAbsolutePath().toString() : FileUtil.fileName(dir));

        RequestIdentifier identifier = new RequestIdentifier(dir.toUri());
        identifier.setParentResourceId(parentId);
        result.setRequestMetaData(metaData);
        result.setIdentifier(identifier);
        return resultHandler.handleDirectory(result, parentId, false);
    }

    private S3Result getS3Result(AbstractProfileResource resource) {
        URI uri = resource.getUri();
        S3Uri s3Uri = S3Utilities.builder().region(Region.EU_WEST_2).build().parseUri(uri);
        String bucket = s3Uri.bucket().orElseThrow(() -> new RuntimeException("Bucket not found in uri " + uri));
        String prefix = s3Uri.key().orElseThrow(() -> new RuntimeException("Key not found in uri " + uri));

        ListObjectsV2Request request = ListObjectsV2Request.builder().bucket(bucket).prefix(prefix).build();
        ListObjectsV2Iterable responseIterable = this.s3EventHandler.getS3Client().listObjectsV2Paginator(request);

        Map<String, List<String>> dirToFileMap = new HashMap<>();
        int totalCount = 0;

        for (S3Object s3Object: responseIterable.contents()) {
            int lastSlashIndex = (FORWARD_SLASH + s3Object.key()).lastIndexOf(FORWARD_SLASH);
            String keyUri = S3_SCHEME + bucket + FORWARD_SLASH + s3Object.key();
            String parent = S3_SCHEME + bucket + FORWARD_SLASH + s3Object.key().substring(0, lastSlashIndex -1);
            if (!dirToFileMap.containsKey(parent)) {
                List<String> existingKeys = new ArrayList<>();
                existingKeys.add(keyUri);
                dirToFileMap.put(parent, existingKeys);
                totalCount = totalCount + 2;
            } else {
                List<String> existingKeys = dirToFileMap.get(parent);
                existingKeys.add(keyUri);
                dirToFileMap.put(parent, existingKeys);
                totalCount++;
            }
        }
        return new S3Result(dirToFileMap, totalCount);
    }

    private record S3Result(Map<String, List<String>> dirToFileMap, int totalCount) {
    }

    private Path getPath(URI uri) {
        return FileSystems.getFileSystem(uri).getPath(uri.getPath());
    }
}
