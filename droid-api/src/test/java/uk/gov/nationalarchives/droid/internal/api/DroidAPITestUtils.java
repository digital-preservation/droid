/*
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gov.uk>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following
 * conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of the The National Archives nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package uk.gov.nationalarchives.droid.internal.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import uk.gov.nationalarchives.droid.core.SignatureParseException;
import uk.gov.nationalarchives.droid.core.interfaces.http.S3ClientFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class to create an instance of DroidAPI for testing purpose.
 * It makes use of hardcoded signature paths for current version
 */
public class DroidAPITestUtils {

    static Path signaturePath = Paths.get("../droid-results/custom_home/signature_files/DROID_SignatureFile_V119.xml");
    static Path containerPath = Paths.get("../droid-results/custom_home/container_sigs/container-signature-20240715.xml");

    public static DroidAPI createApi(URI endpointOverride) throws SignatureParseException {
        DroidAPI.DroidAPIBuilder droidAPIBuilder = DroidAPI.builder()
                .binarySignature(signaturePath)
                .containerSignature(containerPath)
                .httpClient(HttpClient.newHttpClient());
        if(endpointOverride != null) {
            S3Client s3Client = S3Client.builder().region(Region.EU_WEST_2).endpointOverride(endpointOverride).build();
            return droidAPIBuilder.s3Client(s3Client).build();
        }
        return droidAPIBuilder.build();

    }

    static HttpServer createHttpServer() throws IOException {
        HttpServer httpServer = HttpServer.create();
        httpServer.createContext("/", exchange -> {
            String range = exchange.getRequestHeaders().get("Range").getFirst();
            long size = Files.size(Path.of(exchange.getRequestURI().toString()));
            byte[] bytesForRange = getBytesForRange(exchange.getRequestURI().getPath(), range);

            exchange.getResponseHeaders().add("Content-Range", range.replace("=", " ") + "/" + size);
            exchange.getResponseHeaders().add("Last-Modified", "1970-01-01T00:00:00.000Z");
            exchange.sendResponseHeaders(200, bytesForRange.length);
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(bytesForRange);
            outputStream.close();
        });
        httpServer.bind(new InetSocketAddress(0), 0);
        httpServer.start();
        return httpServer;
    }

    static HttpServer createS3Server() throws IOException {
        HttpServer s3Server = HttpServer.create();
        s3Server.createContext("/", exchange -> {
            Map<String, String> queryParams = URLEncodedUtils
                    .parse(exchange.getRequestURI(), Charset.defaultCharset())
                    .stream().collect(Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));

            if (exchange.getRequestMethod().equals("GET") && queryParams.containsKey("list-type") && queryParams.get("list-type").equals("2")) {
                String fileName = queryParams.get("prefix");
                long size = Files.size(Path.of("/" + fileName));
                String response =
                        "<ListBucketResult>" +
                                "<Contents>" +
                                "<Key>" + fileName + "</Key>" +
                                "<LastModified>1970-01-01T00:00:00.000Z</LastModified>" +
                                "<Size>" + size + "</Size>" +
                                "</Contents>" +
                                "</ListBucketResult>";
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream responseBody = exchange.getResponseBody();
                responseBody.write(response.getBytes());
                responseBody.close();
            } else if (exchange.getRequestMethod().equals("HEAD")) {
                String fullPath = exchange.getRequestURI().getPath().substring(1);
                String filePath = fullPath.substring(fullPath.indexOf("/"));
                long size = Files.size(Path.of("/" + filePath));
                exchange.getResponseHeaders().add("Content-Length", Long.toString(size));
                exchange.getResponseHeaders().add("Last-Modified", "Mon, 03 Mar 2025 17:29:48 GMT");
                exchange.sendResponseHeaders(200, -1);
                OutputStream responseBody = exchange.getResponseBody();
                responseBody.write("".getBytes());
                responseBody.close();
            } else if (exchange.getRequestMethod().equals("GET")) {
                String fullPath = exchange.getRequestURI().getPath().substring(1);
                String filePath = fullPath.substring(fullPath.indexOf("/"));
                String range = exchange.getRequestHeaders().get("Range").getFirst();
                byte[] bytesForRange = getBytesForRange(filePath, range);
                exchange.sendResponseHeaders(200, bytesForRange.length);
                OutputStream responseBody = exchange.getResponseBody();
                responseBody.write(bytesForRange);
                responseBody.close();
            }
        });
        s3Server.bind(new InetSocketAddress(0), 0);
        s3Server.start();
        return s3Server;
    }

    public static DroidAPI createApi() throws SignatureParseException {
        return createApi(null);
    }

    public static byte[] getBytesForRange(String filePath, String range) {
        String[] rangeArr = range.split("=")[1].split("-");
        int rangeStart = Integer.parseInt(rangeArr[0]);
        int rangeEnd = Integer.parseInt(rangeArr[1]);
        int length = rangeEnd - rangeStart + 1;
        try (RandomAccessFile raf = new RandomAccessFile(filePath, "r")) {
            raf.seek(rangeStart);
            byte[] buffer = new byte[length];
            int bytesRead = raf.read(buffer);
            return bytesRead == length ? buffer : Arrays.copyOf(buffer, bytesRead);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
