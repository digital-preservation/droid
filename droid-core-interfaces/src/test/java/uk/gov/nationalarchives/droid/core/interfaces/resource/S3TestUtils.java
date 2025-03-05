package uk.gov.nationalarchives.droid.core.interfaces.resource;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

import java.io.ByteArrayInputStream;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class S3TestUtils {

    static S3Client mockS3Client() {
        S3Client mockS3Client = mock(S3Client.class);
        HeadObjectResponse response = HeadObjectResponse.builder().contentLength(1L).lastModified(Instant.ofEpochSecond(1)).build();
        GetObjectResponse getObjectResponse = GetObjectResponse.builder().build();
        ResponseInputStream<GetObjectResponse> responseInputStream = new ResponseInputStream<>(getObjectResponse, new ByteArrayInputStream("test".getBytes()));
        when(mockS3Client.headObject(any(HeadObjectRequest.class))).thenReturn(response);
        when(mockS3Client.getObject(any(GetObjectRequest.class))).thenAnswer(invocation -> {
            GetObjectRequest argument = invocation.getArgument(0, GetObjectRequest.class);
            String range = argument.range();
            byte[] outputBytes = "test".getBytes();
            int rangeStart = Integer.parseInt(range.split("=")[1].split("-")[0]);
            if (rangeStart > outputBytes.length) {
                throw new IllegalArgumentException("Range start larger than file size");
            }
            return new ResponseInputStream<>(getObjectResponse, new ByteArrayInputStream(outputBytes));
        });
        return mockS3Client;
    }
}
