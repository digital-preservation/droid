package uk.gov.nationalarchives.droid.core.interfaces.resource;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class HttpUtils {

    private final HttpClient httpClient;

    public HttpUtils(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public record HttpMetadata(Long fileSize, Long lastModified, URI uri) {}

    public HttpMetadata getHttpMetadata(URI uri) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Range", "bytes=0-1")
                .GET()
                .build();
        HttpHeaders headers;
        try {
            headers = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray())
                    .headers();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        Long lastModified = headers.firstValue("last-modified").map(lastModifiedString -> {
            try {
                ZonedDateTime parsedDate = ZonedDateTime.parse(lastModifiedString, DateTimeFormatter.RFC_1123_DATE_TIME);
                return parsedDate.toEpochSecond();
            } catch (DateTimeParseException e) {
                return Instant.now().getEpochSecond();
            }
        }).orElse(Instant.now().getEpochSecond());
        Long contentLength = Long.parseLong(
                headers
                        .firstValue("content-range")
                        .map(range -> range.split("/")[1])
                        .orElse("0")
        );
        return new HttpMetadata(contentLength, lastModified, uri);
    }
}
