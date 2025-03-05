package uk.gov.nationalarchives.droid.core.interfaces.resource;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.nationalarchives.droid.core.interfaces.resource.HttpTestUtils.mockHttpClient;

public class HttpUtilsTest {

    @Test
    public void testGetMetadataReturnsExpectedValues() {
        HttpClient httpClient = mockHttpClient();
        URI uri = URI.create("https://example.com");
        HttpUtils.HttpMetadata httpMetadata = new HttpUtils(httpClient).getHttpMetadata(uri);

        assertEquals(httpMetadata.fileSize(), 4);
        assertEquals(httpMetadata.lastModified(), 0);
        assertEquals(uri.toString(), "https://example.com");
    }
}
