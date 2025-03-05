package uk.gov.nationalarchives.droid.core.interfaces.resource;

import net.byteseek.io.reader.cache.WindowCache;
import net.byteseek.io.reader.windows.SoftWindow;
import net.byteseek.io.reader.windows.Window;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.nationalarchives.droid.core.interfaces.resource.HttpTestUtils.mockHttpClient;

public class HttpWindowReaderTest {

    @Test
    public void testWindowReaderReturnsExpectedWindow() throws Exception {
        WindowCache windowCache = mock(WindowCache.class);
        HttpClient httpClient = mockHttpClient();
        HttpUtils.HttpMetadata httpMetadata = new HttpUtils.HttpMetadata(4L, 0L, URI.create("https://example.com"));
        HttpWindowReader httpWindowReader = new HttpWindowReader(windowCache, httpMetadata, httpClient);

        byte[] testResponse = "test".getBytes();
        for (int i = 0; i < testResponse.length; i++) {
            Window window = httpWindowReader.createWindow(i);
            assertEquals(window.getClass(), SoftWindow.class);
            assertEquals(window.getWindowPosition(), i);
            assertEquals(window.length(), testResponse.length);
            assertEquals(window.getByte(i), testResponse[i]);
        }
    }

    @Test
    public void testWindowReaderReturnsNullIfPositionLessThanZero() throws Exception {
        WindowCache windowCache = mock(WindowCache.class);
        HttpClient httpClient = mockHttpClient();
        HttpUtils.HttpMetadata httpMetadata = new HttpUtils.HttpMetadata(4L, 0L, URI.create("https://example.com"));
        HttpWindowReader httpWindowReader = new HttpWindowReader(windowCache, httpMetadata, httpClient);

        assertNull(httpWindowReader.createWindow(-1));
    }

    @Test
    public void testWindowReaderReturnsErrorOnHttpFailure() throws Exception {
        WindowCache windowCache = mock(WindowCache.class);
        HttpClient httpClient = mock(HttpClient.class);
        HttpUtils.HttpMetadata httpMetadata = new HttpUtils.HttpMetadata(4L, 0L, URI.create("https://example.com"));
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenThrow(new IOException("Error contacting server"));
        assertThrows(RuntimeException.class, () -> new HttpWindowReader(windowCache, httpMetadata, httpClient).getWindow(0));
    }
}
