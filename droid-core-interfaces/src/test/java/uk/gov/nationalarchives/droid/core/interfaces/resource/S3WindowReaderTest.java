package uk.gov.nationalarchives.droid.core.interfaces.resource;

import net.byteseek.io.reader.cache.WindowCache;
import net.byteseek.io.reader.windows.SoftWindow;
import net.byteseek.io.reader.windows.Window;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Uri;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.net.URI;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.nationalarchives.droid.core.interfaces.resource.S3TestUtils.mockS3Client;

public class S3WindowReaderTest {

    @Test
    public void testWindowReaderReturnsExpectedWindow() throws Exception {
        WindowCache windowCache = mock(WindowCache.class);
        S3Client s3Client = mockS3Client();
        S3Uri s3Uri = S3Uri.builder().uri(URI.create("s3://bucket/key")).build();
        S3Utils.S3ObjectMetadata s3ObjectMetadata = new S3Utils.S3ObjectMetadata("bucket", Optional.of("key"), s3Uri, 1L, 1L);
        S3WindowReader s3WindowReader = new S3WindowReader(windowCache, s3ObjectMetadata, s3Client);

        byte[] testResponse = "test".getBytes();
        for (int i = 0; i < testResponse.length; i++) {
            Window window = s3WindowReader.createWindow(i);
            assertEquals(window.getClass(), SoftWindow.class);
            assertEquals(window.getWindowPosition(), i);
            assertEquals(window.length(), testResponse.length);
            assertEquals(window.getByte(i), testResponse[i]);
        }
    }

    @Test
    public void testWindowReaderReturnsNullIfPositionLessThanZero() throws Exception {
        WindowCache windowCache = mock(WindowCache.class);
        S3Client s3Client = mock(S3Client.class);
        S3Uri s3Uri = S3Uri.builder().uri(URI.create("s3://bucket/key")).build();
        S3Utils.S3ObjectMetadata s3ObjectMetadata = new S3Utils.S3ObjectMetadata("bucket", Optional.of("key"), s3Uri, 1L, 1L);
        S3WindowReader s3WindowReader = new S3WindowReader(windowCache, s3ObjectMetadata, s3Client);

        assertNull(s3WindowReader.createWindow(-1));
    }

    @Test
    public void testWindowReaderReturnsErrorOnS3Failure() throws Exception {
        WindowCache windowCache = mock(WindowCache.class);
        S3Client s3Client = mock(S3Client.class);
        when(s3Client.getObject(any(GetObjectRequest.class))).thenThrow(S3Exception.builder().message("Error contacting s3").build());
        S3Uri s3Uri = S3Uri.builder().uri(URI.create("s3://bucket/key")).build();
        S3Utils.S3ObjectMetadata s3ObjectMetadata = new S3Utils.S3ObjectMetadata("bucket", Optional.of("key"), s3Uri, 1L, 1L);
        S3WindowReader s3WindowReader = new S3WindowReader(windowCache, s3ObjectMetadata, s3Client);

        assertThrows(S3Exception.class, () -> s3WindowReader.createWindow(0));
    }
}
