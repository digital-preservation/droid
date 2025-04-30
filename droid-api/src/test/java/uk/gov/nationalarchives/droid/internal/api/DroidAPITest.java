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

import com.sun.net.httpserver.HttpServer;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import uk.gov.nationalarchives.droid.core.SignatureParseException;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationMethod;
import uk.gov.nationalarchives.droid.internal.api.DroidAPITestUtils.ContainerType;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static uk.gov.nationalarchives.droid.internal.api.DroidAPITestUtils.*;

public class DroidAPITest {

    private static final String DATA = "TEST";

    private static DroidAPI api;
    private static HttpServer s3Server;
    private static HttpServer httpServer;
    private static URI endpointOverride;

    private static Stream<URI> getUris(String path) {
        URI fileUri = Paths.get(path).toUri();
        URI s3Uri = URI.create("s3://127.0.0.1" + ":" + s3Server.getAddress().getPort() + fileUri.getPath());
        URI httpUri = URI.create("http://127.0.0.1" + ":" + httpServer.getAddress().getPort() + fileUri.getPath());
        return Stream.of(fileUri, s3Uri, httpUri);
    }

    @BeforeAll
    public static void setup() throws SignatureParseException, IOException {
        s3Server = createS3Server();
        httpServer = createHttpServer();
        endpointOverride = URI.create("http://127.0.0.1" + ":" + s3Server.getAddress().getPort());
        api = DroidAPITestUtils.createApi(endpointOverride);
    }



    @Test
    public void should_create_non_null_instance_using_test_utility_class() {
        assertThat(api, is(notNullValue()));
    }

    public record ContainerTest(URI uri, ContainerType containerType, Optional<String> path) {}

    static Stream<ContainerTest> signatureTests() {
        ContainerType gzipContainerType = new ContainerType("GZIP", generateId(),"x-fmt/266");
        ContainerType zipContainerType = new ContainerType("ZIP", generateId(),"x-fmt/263");
        ContainerType ole2ContainerType = new ContainerType("OLE2", generateId(),"fmt/111");
        Stream<ContainerTest> gzipStream = getUris(generateGzFile(DATA).toString()).map(uri -> new ContainerTest(uri, gzipContainerType, Optional.empty()));
        Stream<ContainerTest> zipStream = getUris(generateZipFile(DATA, DATA).toString()).map(uri -> new ContainerTest(uri, zipContainerType, Optional.of(DATA)));
        Stream<ContainerTest> ole2Stream = getUris(generateOle2File(DATA, DATA).toString()).map(uri -> new ContainerTest(uri, ole2ContainerType, Optional.of(DATA)));
        return Stream.concat(Stream.concat(gzipStream, zipStream), ole2Stream);
    }


    @ParameterizedTest
    @MethodSource("signatureTests")
    public void should_match_container_files_if_provided_with_matching_signature(ContainerTest containerTest) throws IOException {
        ContainerFile containerFile = new ContainerFile(containerTest.containerType, DATA, "fmt/12345", containerTest.path);
        try (DroidAPI api = DroidAPITestUtils.createApiForContainer(endpointOverride, containerFile)) {
            List<ApiResult> results = api.submit(containerTest.uri);
            assertThat(results, hasSize(1));
            assertThat(results.getFirst().getPuid(), is("fmt/12345"));
            assertThat(results.getFirst().getMethod(), is(IdentificationMethod.CONTAINER));
        }
    }

    @Test
    public void should_throw_an_exception_if_file_cannot_be_read() {
        assertThrows(IOException.class, () -> api.submit(Path.of("/invalidpath").toUri()));
    }

    @Test
    public void should_throw_an_exception_if_container_file_cannot_be_read() {
        assertThrows(RuntimeException.class, () -> {
            DroidAPI.builder()
                    .binarySignature(signaturePath)
                    .containerSignature(Path.of("/invalidContainerPath"))
                    .build();
        });
    }

    @Test
    public void should_throw_an_exception_if_signature_file_cannot_be_read() {
        assertThrows(SignatureParseException.class, () -> {
            DroidAPI.builder()
                    .binarySignature(Path.of("/invalidSignaturePath"))
                    .containerSignature(containerPath)
                    .build();
        });
    }

    static Stream<URI> binarySignatureUris() {
        return getUris("src/test/resources/persistence.zip");
    }

    @Execution(ExecutionMode.CONCURRENT)
    @ParameterizedTest
    @MethodSource("binarySignatureUris")
    public void should_identify_given_file_with_binary_signature(URI uri) throws IOException {
        List<ApiResult> results = api.submit(uri);
        assertThat(results, is(notNullValue()));

        assertThat(results.size(), is(1));

        ApiResult identificationResult = results.get(0);

        assertThat(identificationResult.getPuid(), is("x-fmt/263"));
        assertThat(identificationResult.getName(), is("ZIP Format"));
        assertThat(identificationResult.getMethod(), is(IdentificationMethod.BINARY_SIGNATURE));

    }

    static Stream<URI> containerSignatureUris() {
        return getUris("../droid-container/src/test/resources/odf_text.odt");
    }

    @Execution(ExecutionMode.CONCURRENT)
    @ParameterizedTest
    @MethodSource("containerSignatureUris")
    public void should_identify_given_file_using_container_signature(URI uri) throws IOException {
        List<ApiResult> results = api.submit(uri);
        assertThat(results, is(notNullValue()));

        assertThat(results.size(), is(1));

        ApiResult identificationResult = results.getFirst();

        assertThat(identificationResult.getPuid(), is("fmt/291"));
        assertThat(identificationResult.getName(), is("OpenDocument Text"));
        assertThat(identificationResult.getMethod(), is(IdentificationMethod.CONTAINER));
    }

    static Stream<URI> fileExtensionUris() {
        return getUris("src/test/resources/test.txt");
    }

    @Execution(ExecutionMode.CONCURRENT)
    @ParameterizedTest
    @MethodSource("fileExtensionUris")
    public void should_identify_given_file_using_file_extension(URI uri) throws IOException {
        List<ApiResult> results = api.submit(uri);
        assertThat(results, is(notNullValue()));
        assertThat(results, hasSize(1));

        ApiResult singleResult = results.getFirst();

        assertThat(singleResult.getPuid(), is("x-fmt/111"));
        assertThat(singleResult.getMethod(), is(IdentificationMethod.EXTENSION));
    }

    static Stream<Pair<URI, URI>> correctExtensionUris() {
        List<URI> withExtensionList = getUris("src/test/resources/test.txt").toList();
        List<URI> withoutExtensionList = getUris("src/test/resources/word97").toList();
        return Stream.of(
                Pair.of(withExtensionList.getFirst(), withoutExtensionList.getFirst()),
                Pair.of(withExtensionList.getLast(), withoutExtensionList.getLast())
        );
    }

    static Stream<URI> noFileExtensionUris() {
        return getUris("src/test/resources/test");
    }

    @ParameterizedTest
    @MethodSource("noFileExtensionUris")
    public void should_report_extension_only_match_if_extension_is_provided(URI uri) throws IOException {
        List<ApiResult> results = api.submit(uri, "docx");

        ApiResult result = results.getFirst();
        assertThat(result.getExtension(), is("docx"));
        assertThat(result.getMethod(), is(IdentificationMethod.EXTENSION));
        assertThat(result.getPuid(), is("fmt/494"));
    }

    static Stream<URI> docxWithoutExtensionUris() {return getUris("src/test/resources/word97");}

    @ParameterizedTest
    @MethodSource("docxWithoutExtensionUris")
    public void should_return_extension_mismatch_if_extension_passed_does_not_match(URI uri) throws IOException {
        List<ApiResult> results = api.submit(uri, "pdf");

        ApiResult result = results.getFirst();
        assertThat(result.getExtension(), is("pdf"));
        assertThat(result.getMethod(), is(IdentificationMethod.CONTAINER));
        assertThat(result.getPuid(), is("fmt/40"));
        assertThat(result.isFileExtensionMismatch(), is(true));
    }

    @Execution(ExecutionMode.CONCURRENT)
    @ParameterizedTest
    @MethodSource("correctExtensionUris")
    public void should_report_extension_of_the_file_under_identification_test(Pair<URI, URI> uriPair) throws IOException {
        List<ApiResult> resultsWithExtension = api.submit(uriPair.getLeft());
        List<ApiResult> resultsWithoutExtension = api.submit(uriPair.getRight());

        assertThat(resultsWithExtension.getFirst().getExtension(), is("txt"));
        assertThat(resultsWithoutExtension.getFirst().getExtension(), is(""));
    }

    static Stream<URI> doubleIdentificationUris() {
        return getUris("src/test/resources/double-identification.jpg");
    }

    @Execution(ExecutionMode.CONCURRENT)
    @ParameterizedTest
    @MethodSource("doubleIdentificationUris")
    public void should_report_all_puids_when_there_are_more_than_one_identification_hits(URI uri) throws IOException {
        List<ApiResult> results = api.submit(uri);
        assertThat(results.size(), is(2));
        assertThat(results.stream().map(ApiResult::getPuid).collect(Collectors.toList()),
                containsInAnyOrder("fmt/96", "fmt/41"));
        assertThat(results.stream().map(ApiResult::getName).collect(Collectors.toList()),
                containsInAnyOrder("Raw JPEG Stream", "Hypertext Markup Language"));
    }

    static Stream<URI> extensionMismatchUris() {
        return getUris("src/test/resources/docx-file-as-xls.xlsx");
    }

    @Execution(ExecutionMode.CONCURRENT)
    @ParameterizedTest
    @MethodSource("extensionMismatchUris")
    public void should_report_when_there_is_an_extension_mismatch() throws IOException {
        List<ApiResult> results = api.submit(Paths.get("src/test/resources/docx-file-as-xls.xlsx").toUri());
        assertThat(results.size(), is(1));
        assertThat(results.getFirst().getPuid(), is("fmt/412"));
        assertThat(results.getFirst().isFileExtensionMismatch(), is(true));
    }

    @Test
    public void should_report_correct_version_for_the_binary_and_container_signature() {
        assertThat(api.getContainerSignatureVersion(), is("20240715"));
        assertThat(api.getDroidVersion(), is(ResourceBundle.getBundle("options").getString("version_no")));
        assertThat(api.getBinarySignatureVersion(), is("120"));
    }

    static Stream<URI> emptyFileUris() {
        return getUris("src/test/resources/test");
    }

    @Test
    public void should_produce_zero_results_for_an_empty_file() throws IOException {
        List<ApiResult> results = api.submit(Paths.get("src/test/resources/test").toUri());
        assertThat(results, hasSize(0));
    }

    @Execution(ExecutionMode.CONCURRENT)
    @ParameterizedTest
    @MethodSource("emptyFileUris")
    public void should_produce_results_for_every_time_a_file_is_submitted_for_identification() throws IOException {
        final int MAX_ITER = 5000;
        int acc = 0;
        for (int i = 0; i < MAX_ITER; i++) {
            List<ApiResult> results = api.submit(
                    Paths.get("../droid-container/src/test/resources/odf_text.odt").toUri());
            acc += results.size();
        }
        assertThat(acc, is(MAX_ITER));
    }

    static Stream<URI> fmtFortyUris() {
        return getUris("../droid-container/src/test/resources/word97.doc");
    }

    @Execution(ExecutionMode.CONCURRENT)
    @ParameterizedTest
    @MethodSource("fmtFortyUris")
    public void should_identify_fmt_40_correctly_with_container_identification_method() throws IOException {
        List<ApiResult> results = api.submit(
                Paths.get("../droid-container/src/test/resources/word97.doc").toUri());
        assertThat(results.getFirst().getName(), is("Microsoft Word Document"));
    }

    @Test
    public void should_return_an_error_if_both_signature_paths_are_not_set() {
        assertThrows(IllegalArgumentException.class, () -> DroidAPI.builder().build());
        assertThrows(IllegalArgumentException.class, () -> DroidAPI.builder().containerSignature(containerPath).build());
        assertThrows(IllegalArgumentException.class, () -> DroidAPI.builder().binarySignature(signaturePath).build());
    }

    @Test
    public void should_provide_default_clients_if_none_are_provided() throws SignatureParseException {
        DroidAPI.builder().binarySignature(signaturePath).containerSignature(containerPath).build();
        assertNotNull(api.getS3Client());
        assertNotNull(api.getHttpClient());
    }

    @Test
    public void should_default_to_london_region_if_no_region_provided() throws SignatureParseException {
        DroidAPI api = DroidAPI.builder().binarySignature(signaturePath).containerSignature(containerPath).build();
        assertEquals(api.getS3Region(), Region.EU_WEST_2);
    }

    @Test
    public void should_close_clients_after_use() throws SignatureParseException {
        S3Client s3Client;
        HttpClient httpClient;
        try (DroidAPI api = DroidAPI.builder().binarySignature(signaturePath).containerSignature(containerPath).build()) {
            httpClient = api.getHttpClient();
            s3Client = api.getS3Client();
        }
        assertTrue(httpClient.isTerminated());
        assertThrows(IllegalStateException.class, s3Client::listBuckets);
    }
}
