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
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static uk.gov.nationalarchives.droid.internal.api.DroidAPITestUtils.*;
import static uk.gov.nationalarchives.droid.internal.api.HashAlgorithm.*;

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
            List<DroidAPI.APIResult> results = api.submit(containerTest.uri);
            assertThat(results, hasSize(1));
            assertThat(results.getFirst().identificationResults(), hasSize(1));
            DroidAPI.IdentificationResult result = results.getFirst().identificationResults().getFirst();
            assertThat(result.puid(), is("fmt/12345"));
            assertThat(result.method(), is(IdentificationMethod.CONTAINER));
        }
    }

    @Test
    public void should_throw_an_exception_if_file_cannot_be_read() {
        assertThrows(RuntimeException.class, () -> api.submit(Path.of("/invalidpath").toUri()));
    }

    @Test
    public void should_throw_an_exception_if_container_file_cannot_be_read() {
        assertThrows(RuntimeException.class, () -> DroidAPI.builder()
                .binarySignature(signaturePath)
                .containerSignature(Path.of("/invalidContainerPath"))
                .build());
    }

    @Test
    public void should_throw_an_exception_if_signature_file_cannot_be_read() {
        assertThrows(SignatureParseException.class, () -> DroidAPI.builder()
                .binarySignature(Path.of("/invalidSignaturePath"))
                .containerSignature(containerPath)
                .build());
    }

    static Stream<URI> binarySignatureUris() {
        return getUris("src/test/resources/persistence.zip");
    }

    @Execution(ExecutionMode.CONCURRENT)
    @ParameterizedTest
    @MethodSource("binarySignatureUris")
    public void should_identify_given_file_with_binary_signature(URI uri) throws IOException {
        List<DroidAPI.APIResult> results = api.submit(uri);
        assertThat(results, is(notNullValue()));

        assertThat(results.size(), is(1));
        assertThat(results.getFirst().identificationResults().size(), is(1));

        DroidAPI.IdentificationResult identificationResult = results.getFirst().identificationResults().getFirst();

        assertThat(identificationResult.puid(), is("x-fmt/263"));
        assertThat(identificationResult.name(), is("ZIP Format"));
        assertThat(identificationResult.method(), is(IdentificationMethod.BINARY_SIGNATURE));
        assertThat(results.getFirst().hashResults().isEmpty(), is(true));

    }

    static Stream<URI> containerSignatureUris() {
        return getUris("../droid-container/src/test/resources/odf_text.odt");
    }

    @Execution(ExecutionMode.CONCURRENT)
    @ParameterizedTest
    @MethodSource("containerSignatureUris")
    public void should_identify_given_file_using_container_signature(URI uri) throws IOException {
        List<DroidAPI.APIResult> results = api.submit(uri);
        assertThat(results, is(notNullValue()));

        assertThat(results.size(), is(1));
        assertThat(results.getFirst().identificationResults().size(), is(1));

        DroidAPI.IdentificationResult identificationResult = results.getFirst().identificationResults().getFirst();

        assertThat(identificationResult.puid(), is("fmt/291"));
        assertThat(identificationResult.name(), is("OpenDocument Text"));
        assertThat(identificationResult.method(), is(IdentificationMethod.CONTAINER));
    }

    static Stream<URI> fileExtensionUris() {
        return getUris("src/test/resources/test.txt");
    }

    @Execution(ExecutionMode.CONCURRENT)
    @ParameterizedTest
    @MethodSource("fileExtensionUris")
    public void should_identify_given_file_using_file_extension(URI uri) throws IOException {
        List<DroidAPI.APIResult> results = api.submit(uri);
        assertThat(results, is(notNullValue()));
        assertThat(results.size(), is(1));
        assertThat(results.getFirst().identificationResults().size(), is(1));

        DroidAPI.IdentificationResult singleResult = results.getFirst().identificationResults().getFirst();

        assertThat(singleResult.puid(), is("x-fmt/111"));
        assertThat(singleResult.method(), is(IdentificationMethod.EXTENSION));
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
        List<DroidAPI.APIResult> results = api.submit(uri, "docx");

        DroidAPI.IdentificationResult result = results.getFirst().identificationResults().getFirst();
        assertThat(result.extension(), is("docx"));
        assertThat(result.method(), is(IdentificationMethod.EXTENSION));
        assertThat(result.puid(), is("fmt/494"));
    }

    static Stream<URI> docxWithoutExtensionUris() {return getUris("src/test/resources/word97");}

    @ParameterizedTest
    @MethodSource("docxWithoutExtensionUris")
    public void should_return_extension_mismatch_if_extension_passed_does_not_match(URI uri) throws IOException {
        List<DroidAPI.APIResult> results = api.submit(uri, "pdf");

        DroidAPI.IdentificationResult result = results.getFirst().identificationResults().getFirst();
        assertThat(result.extension(), is("pdf"));
        assertThat(result.method(), is(IdentificationMethod.CONTAINER));
        assertThat(result.puid(), is("fmt/40"));
        assertThat(result.fileExtensionMismatch(), is(true));
    }

    @ParameterizedTest
    @MethodSource("docxWithoutExtensionUris")
    public void should_return_requested_checksums(URI uri) throws IOException, SignatureParseException {
        DroidAPI apiWithChecksums = createApi(endpointOverride, List.of(MD5, SHA1, SHA256, SHA512));
        List<DroidAPI.APIResult> results = apiWithChecksums.submit(uri, "docx");
        DroidAPI.APIResult result = results.getFirst();

        assertThat(result.hashResults().size(), is(4));
        assertThat(result.hashResults().get(MD5), is("6aff1fe59798e3ab4da40e50b21312ca"));
        assertThat(result.hashResults().get(SHA1), is("51fc5ba38e9762a0a64ef1ebe44b42651ef0799e"));
        assertThat(result.hashResults().get(SHA256), is("f59669d5c045b1a25b09cdd68c6f269901522cbff1fe3c1802bfcc8b25d47e44"));
        assertThat(result.hashResults().get(SHA512), is("64f4c15f9e56064c37b874ddf22958e9983bc9ff5f939d6fc16b06824d25a174e696c39ccd55b8cb1964110e16763fcd8855e319e069416e33249c8c59ed5b81"));
    }

    @ParameterizedTest
    @MethodSource("docxWithoutExtensionUris")
    public void should_return_single_checksum_if_one_requested(URI uri) throws IOException, SignatureParseException {
        DroidAPI apiWithChecksums = createApi(endpointOverride, List.of(MD5));
        List<DroidAPI.APIResult> results = apiWithChecksums.submit(uri, "docx");
        DroidAPI.APIResult result = results.getFirst();
        assertThat(result.hashResults().size(), is(1));
        assertThat(result.hashResults().get(MD5), is("6aff1fe59798e3ab4da40e50b21312ca"));
    }

    @Execution(ExecutionMode.CONCURRENT)
    @ParameterizedTest
    @MethodSource("correctExtensionUris")
    public void should_report_extension_of_the_file_under_identification_test(Pair<URI, URI> uriPair) throws IOException {
        List<DroidAPI.APIResult> resultsWithExtension = api.submit(uriPair.getLeft());
        List<DroidAPI.APIResult> resultsWithoutExtension = api.submit(uriPair.getRight());

        DroidAPI.IdentificationResult resultWithExtension = resultsWithExtension.getFirst().identificationResults().getFirst();
        DroidAPI.IdentificationResult resultWithoutExtension = resultsWithoutExtension.getFirst().identificationResults().getFirst();
        assertThat(resultWithExtension.extension(), is("txt"));
        assertThat(resultWithoutExtension.extension(), is(""));
    }

    static Stream<URI> doubleIdentificationUris() {
        return getUris("src/test/resources/double-identification.jpg");
    }

    @Execution(ExecutionMode.CONCURRENT)
    @ParameterizedTest
    @MethodSource("doubleIdentificationUris")
    public void should_report_all_puids_when_there_are_more_than_one_identification_hits(URI uri) throws IOException {
        List<DroidAPI.APIResult> results = api.submit(uri);
        assertThat(results.size(), is(1));
        assertThat(results.getFirst().identificationResults().size(), is(2));
        Supplier<Stream<DroidAPI.IdentificationResult>> identificationsStream = () -> results.stream()
                .flatMap(result -> result.identificationResults().stream());

        assertThat(identificationsStream.get()
                        .map(DroidAPI.IdentificationResult::puid).collect(Collectors.toList()),
                containsInAnyOrder("fmt/96", "fmt/41"));
        assertThat(identificationsStream.get().map(DroidAPI.IdentificationResult::name).collect(Collectors.toList()),
                containsInAnyOrder("Raw JPEG Stream", "Hypertext Markup Language"));
    }

    static Stream<URI> extensionMismatchUris() {
        return getUris("src/test/resources/docx-file-as-xls.xlsx");
    }

    @Execution(ExecutionMode.CONCURRENT)
    @ParameterizedTest
    @MethodSource("extensionMismatchUris")
    public void should_report_when_there_is_an_extension_mismatch(URI uri) throws IOException {
        List<DroidAPI.APIResult> results = api.submit(uri);
        assertThat(results.size(), is(1));

        DroidAPI.IdentificationResult result = results.getFirst().identificationResults().getFirst();
        assertThat(result.puid(), is("fmt/412"));
        assertThat(result.fileExtensionMismatch(), is(true));
    }

    static Stream<URI> directoryUris() {
        URI fileUri = Paths.get("src/test/resources").toUri();
        URI s3Uri = URI.create("s3://127.0.0.1" + ":" + s3Server.getAddress().getPort() + fileUri.getPath());
        return Stream.of(fileUri, s3Uri);
    }

    @ParameterizedTest
    @MethodSource("directoryUris")
    public void should_return_multiple_results_if_a_directory_is_passed_for_file_or_s3(URI uri) throws IOException {
        List<DroidAPI.APIResult> results = api.submit(uri);
        assertThat(results.size(), is(6));
        BiFunction<String, List<String>, Boolean> checkPuid = (fileName, expectedPuids) -> results.stream()
                .flatMap(result -> result.identificationResults().stream())
                .filter(result -> result.uri().toString().endsWith(fileName))
                .map(DroidAPI.IdentificationResult::puid)
                .allMatch(expectedPuids::contains);
        checkPuid.apply("persistence.zip", List.of("x-fmt/263"));
        checkPuid.apply("docx-file-as-xls.xlsx", List.of("fmt/412"));
        checkPuid.apply("double-identification.jpg", List.of("fmt/96", "fmt/41"));
        checkPuid.apply("test.txt", List.of("x-fmt/111"));
        checkPuid.apply("word97", List.of("fmt/40"));
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
        List<DroidAPI.APIResult> results = api.submit(Paths.get("src/test/resources/test").toUri());
        assertThat(results.getFirst().identificationResults(), hasSize(0));
    }

    @Execution(ExecutionMode.CONCURRENT)
    @ParameterizedTest
    @MethodSource("emptyFileUris")
    public void should_produce_results_for_every_time_a_file_is_submitted_for_identification() throws IOException {
        final int MAX_ITER = 5000;
        int acc = 0;
        for (int i = 0; i < MAX_ITER; i++) {
            List<DroidAPI.APIResult> results = api.submit(
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
        List<DroidAPI.APIResult> results = api.submit(
                Paths.get("../droid-container/src/test/resources/word97.doc").toUri());
        assertThat(results.getFirst().identificationResults().getFirst().name(), is("Microsoft Word Document"));
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
        assertEquals(Region.EU_WEST_2, api.getS3Region());
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
