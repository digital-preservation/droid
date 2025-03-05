package uk.gov.nationalarchives.droid.core.interfaces.resource;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HttpTestUtils {
    static HttpClient mockHttpClient() {
        HttpClient httpClientMock = mock(HttpClient.class);

        byte[] responseBody = "test".getBytes();
        try {
            when(httpClientMock.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenAnswer(invocation -> {
                HttpRequest argument = invocation.getArgument(0);
                HttpResponse<byte[]> httpResponseMock = mock(HttpResponse.class);
                String lastModified = DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.ofInstant(Instant.EPOCH, ZoneId.of("UTC")));
                when(httpResponseMock.headers()).thenReturn(HttpHeaders.of(
                        Map.of("content-range", List.of("bytes 0-0/4"), "last-modified", List.of(lastModified)), (a, b) -> true
                ));
                String range = argument.headers().firstValue("Range").orElseThrow(() -> new RuntimeException("Missing range"));
                int rangeStart = Integer.parseInt(range.split("=")[1].split("-")[0]);
                if (rangeStart > responseBody.length) {
                    when(httpResponseMock.body()).thenThrow(new RuntimeException("Invalid range"));
                } else {
                    when(httpResponseMock.body()).thenReturn(responseBody);
                }
                return httpResponseMock;
            });
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
        return httpClientMock;
    }
}
