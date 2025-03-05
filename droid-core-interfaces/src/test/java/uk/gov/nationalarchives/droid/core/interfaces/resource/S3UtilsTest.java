package uk.gov.nationalarchives.droid.core.interfaces.resource;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Uri;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.nationalarchives.droid.core.interfaces.resource.S3TestUtils.mockS3Client;

public class S3UtilsTest {

    @Test
    public void getObjectMetadataReturnsCorrectMetadata() {
        S3Client s3Client = mockS3Client();
        S3Utils s3Utils = new S3Utils(s3Client, Region.EU_WEST_2);
        URI uri = URI.create("s3://bucket/key");
        S3Utils.S3ObjectMetadata s3ObjectMetadataFromUri = s3Utils.getS3ObjectMetadata(uri);
        S3Uri s3Uri = S3Uri.builder().uri(uri).bucket("bucket").key("key").build();
        S3Utils.S3ObjectMetadata s3ObjectMetadataFromS3Uri = s3Utils.getS3ObjectMetadata(s3Uri);

        for (S3Utils.S3ObjectMetadata s3ObjectMetadata: List.of(s3ObjectMetadataFromUri, s3ObjectMetadataFromS3Uri)) {
            assertTrue(s3ObjectMetadata.key().isPresent());
            assertEquals(s3ObjectMetadata.key().get(), "key");
            assertEquals(s3ObjectMetadata.bucket(), "bucket");
            assertEquals(s3ObjectMetadata.uri().uri(), uri);
            assertEquals(s3ObjectMetadata.contentLength(), 1);
            assertEquals(s3ObjectMetadata.lastModified(), 1);
        }
    }

    @Test
    public void listObjectsReturnsAllItemsWhenItemsArePaginated() {
        S3Client s3Client = mock(S3Client.class);

        ListObjectsV2Response firstResponse = generateListObjectsResponse(0, true);
        ListObjectsV2Response secondResponse = generateListObjectsResponse(1, false);

        ListObjectsV2Iterable responseIterable = new ListObjectsV2Iterable(s3Client, ListObjectsV2Request.builder().build());

        ArgumentCaptor<ListObjectsV2Request> requestArgumentCaptor = ArgumentCaptor.forClass(ListObjectsV2Request.class);

        when(s3Client.listObjectsV2(requestArgumentCaptor.capture())).thenReturn(firstResponse, secondResponse);
        when(s3Client.listObjectsV2Paginator(any(ListObjectsV2Request.class))).thenReturn(responseIterable);

        S3Utils.S3ObjectList objectList = new S3Utils(s3Client).listObjects(URI.create("s3://bucket/key"));

        Iterable<S3Object> contents = objectList.contents();
        List<S3Object> contentsList = new ArrayList<>();
        contents.iterator().forEachRemaining(contentsList::add);

        assertEquals(contentsList.size(), 2);
        S3Object firstObject = contentsList.getFirst();
        S3Object secondObject = contentsList.getLast();

        assertEquals(firstObject.key(), "key0");
        assertEquals(firstObject.lastModified().getEpochSecond(), 0);
        assertEquals(firstObject.size(), 0);
        assertEquals(firstObject.eTag(), "etag0");

        assertEquals(secondObject.key(), "key1");
        assertEquals(secondObject.lastModified().getEpochSecond(), 1);
        assertEquals(secondObject.size(), 1);
        assertEquals(secondObject.eTag(), "etag1");

        List<ListObjectsV2Request> requestValues = requestArgumentCaptor.getAllValues();

        assertNull(requestValues.getFirst().continuationToken());
        assertEquals(requestValues.getLast().continuationToken(), "token");
    }

    private ListObjectsV2Response generateListObjectsResponse(int suffix, boolean isTruncated) {
        S3Object s3Object = S3Object.builder()
                .key("key" + suffix)
                .lastModified(Instant.ofEpochSecond(suffix))
                .size((long) suffix)
                .eTag("etag" + suffix)
                .build();

        ListObjectsV2Response response;
        ListObjectsV2Response.Builder builder = ListObjectsV2Response.builder().contents(s3Object).isTruncated(isTruncated);
        if (isTruncated) {
            response = builder.nextContinuationToken("token").build();
        } else {
            response = builder.build();
        }
        return response;
    }
}
