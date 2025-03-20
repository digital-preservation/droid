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

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

import org.apache.http.client.utils.URIBuilder;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Uri;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.S3Object;
import uk.gov.nationalarchives.droid.core.BinarySignatureIdentifier;
import uk.gov.nationalarchives.droid.core.SignatureParseException;
import uk.gov.nationalarchives.droid.core.interfaces.*;
import uk.gov.nationalarchives.droid.core.interfaces.archive.ContainerIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.resource.*;


/**
 * <p>
 * TNA INTERNAL !!! class which encapsulate DROID internal non-friendly api and expose it in simple way.
 * </p>
 * <p>
 * To obtain instance of this class, use the DroidAPIBuilder class to obtain an instance.
 * Obtaining instance is expensive operation and if used multiple time, instance should be cached.
 * Instance should be thread-safe, but we didn't run any internal audit. We suggest creating one instance for every thread.
 * </p>
 * <p>
 * To identify file, use method {@link #submit(URI)}. This method take full uri to file which should be identified.
 * The URI can point to either an s3, http, https or file URI.
 * It returns identification result which can contain 0..N signatures. Bear in mind that single file can have zero to multiple
 * signature matches!
 * </p>
 */
public final class DroidAPI implements AutoCloseable {

    private static final String ZIP_PUID = "x-fmt/263";
    private static final String OLE2_PUID = "fmt/111";
    private static final String S3_SCHEME = "s3";
    private static final String GZIP_PUID = "x-fmt/266";

    private static final AtomicLong ID_GENERATOR = new AtomicLong();

    private final DroidCore droidCore;

    private final ContainerIdentifier zipIdentifier;

    private final ContainerIdentifier ole2Identifier;

    private final ContainerIdentifier gzIdentifier;

    private final String containerSignatureVersion;

    private final String binarySignatureVersion;

    private final String droidVersion;

    private final S3Client s3Client;

    private final Region s3Region;

    private final HttpClient httpClient;


    private DroidAPI(DroidCore droidCore, ContainerIdentifier zipIdentifier, ContainerIdentifier ole2Identifier, ContainerIdentifier gzIdentifier, String containerSignatureVersion, String binarySignatureVersion, String droidVersion, S3Client s3Client, HttpClient httpClient, Region s3Region) {
        this.droidCore = droidCore;
        this.zipIdentifier = zipIdentifier;
        this.ole2Identifier = ole2Identifier;
        this.gzIdentifier = gzIdentifier;
        this.containerSignatureVersion = containerSignatureVersion;
        this.binarySignatureVersion = binarySignatureVersion;
        this.droidVersion = droidVersion;
        this.s3Region = getRegionOrDefault(s3Region);
        this.s3Client = getS3ClientOrDefault(s3Client);
        this.httpClient = getHttpClientOrDefault(httpClient);
    }

    private HttpClient getHttpClientOrDefault(HttpClient httpClient) {
        return Optional.ofNullable(httpClient)
                .orElse(HttpClient.newHttpClient());
    }

    private S3Client getS3ClientOrDefault(S3Client s3Client) {
        return Optional.ofNullable(s3Client)
                .orElse(S3Client.builder().region(this.s3Region).build());
    }

    private Region getRegionOrDefault(Region region) {
        if (region == null) {
            try {
                return DefaultAwsRegionProviderChain.builder().build().getRegion();
            } catch (SdkClientException e) {
                return Region.EU_WEST_2;
            }
        }
        return region;
    }

    @Override
    public void close() {
        this.httpClient.close();
        this.s3Client.close();
    }

    public static class DroidAPIBuilder {
        private Path binarySignature;
        private Path containerSignature;
        private S3Client s3Client;
        private Region s3Region;
        private HttpClient httpClient;

        public DroidAPIBuilder binarySignature(final Path binarySignature) {
            this.binarySignature = binarySignature;
            return this;
        }

        public DroidAPIBuilder containerSignature(final Path containerSignature) {
            this.containerSignature = containerSignature;
            return this;
        }

        public DroidAPIBuilder s3Client(final S3Client s3Client) {
            this.s3Client = s3Client;
            return this;
        }

        public DroidAPIBuilder s3Region(final Region s3Region) {
            this.s3Region = s3Region;
            return this;
        }

        public DroidAPIBuilder httpClient(final HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        public DroidAPI build() throws SignatureParseException {
            if (this.binarySignature == null || this.containerSignature == null) {
                throw new IllegalArgumentException("Container signature and binary signature are mandatory arguments");
            }
            BinarySignatureIdentifier droidCore = new BinarySignatureIdentifier();
            droidCore.setSignatureFile(binarySignature.toAbsolutePath().toString());
            droidCore.init();
            droidCore.setMaxBytesToScan(Long.MAX_VALUE);
            droidCore.getSigFile().prepareForUse();
            String containerVersion = StringUtils.substringAfterLast(containerSignature.getFileName().toString(), "-").split("\\.")[0];
            String droidVersion = ResourceBundle.getBundle("options").getString("version_no");
            ContainerApi containerApi = new ContainerApi(droidCore, containerSignature);
            return new DroidAPI(droidCore, containerApi.zipIdentifier(), containerApi.ole2Identifier(), containerApi.gzIdentifier(), containerVersion, droidCore.getSigFile().getVersion(), droidVersion, this.s3Client, this.httpClient, this.s3Region);
        }
    }

    public static DroidAPIBuilder builder() {
        return new DroidAPIBuilder();
    }

    /**
     * Submit file for identification. It's important that file has proper file extension. If file
     * can't be identified via binary or container signature, then we use file extension for identification.
     * @param uri Full URI of the file for identification.
     * @return File identification result. File can have multiple matching signatures.
     * @throws IOException If File can't be read or there is IO error.
     */
    public List<ApiResult> submit(final URI uri) throws IOException {
        if (S3_SCHEME.equals(uri.getScheme())) {
            return submitS3Identification(uri);
        } else if (List.of("http", "https").contains(uri.getScheme())) {
            return submitHttpIdentification(uri);
        } else {
            return submitFileSystemIdentification(Path.of(uri));
        }
    }

    private List<ApiResult> submitHttpIdentification(final URI uri) throws IOException {
        HttpClient httpClient = this.httpClient == null ? HttpClient.newHttpClient() : this.httpClient;
        HttpUtils httpUtils = new HttpUtils(httpClient);
        HttpUtils.HttpMetadata httpMetadata = httpUtils.getHttpMetadata(uri);
        Long fileSize = httpMetadata.fileSize();
        Long lastModified = httpMetadata.lastModified();

        final RequestMetaData metaData = new RequestMetaData(
                fileSize,
                lastModified,
                uri.toString()
        );

        final RequestIdentifier id = getRequestIdentifier(uri);


        try (final HttpIdentificationRequest request = new HttpIdentificationRequest(metaData, id, httpClient)) {
            request.open(uri);
            return getApiResults(request);
        }
    }

    private List<ApiResult> submitS3Identification(final URI uri) throws IOException {
        S3Utils s3Utils = new S3Utils(s3Client);
        S3Utils.S3ObjectList objectList = s3Utils.listObjects(uri);
        List<ApiResult> apiResults = new ArrayList<>();

        for (S3Object s3Object: objectList.contents()) {
            URIBuilder uriBuilder = new URIBuilder();
            URI objectUri;
            try {
                objectUri = uriBuilder.setScheme(S3_SCHEME).setHost(objectList.bucket()).setPath(s3Object.key()).build();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            S3Uri s3Uri = S3Utilities.builder().region(s3Region).build().parseUri(objectUri);
            final RequestIdentifier id = getRequestIdentifier(s3Uri.uri());
            RequestMetaData metaData = new RequestMetaData(s3Object.size(), s3Object.lastModified().getEpochSecond(), s3Uri.uri().toString());
            try (final S3IdentificationRequest request = new S3IdentificationRequest(metaData, id, s3Client)) {
                request.open(s3Uri);
                apiResults.addAll(getApiResults(request));
            }
        }
        return apiResults;
    }

    private static RequestIdentifier getRequestIdentifier(URI uri) {
        final RequestIdentifier id = new RequestIdentifier(uri);
        id.setParentId(ID_GENERATOR.getAndIncrement());
        id.setNodeId(ID_GENERATOR.getAndIncrement());
        return id;
    }


    private List<ApiResult> submitFileSystemIdentification(final Path file) throws IOException {
        final RequestMetaData metaData = new RequestMetaData(
                Files.size(file),
                Files.getLastModifiedTime(file).toMillis(),
                file.toAbsolutePath().toString()
        );

        final RequestIdentifier id = getRequestIdentifier(file.toAbsolutePath().toUri());

        try (final FileSystemIdentificationRequest request = new FileSystemIdentificationRequest(metaData, id)) {
            request.open(file);
            return getApiResults(request);
        }
    }

    private <T> List<ApiResult> getApiResults(IdentificationRequest<T> request) throws IOException {
        IdentificationResultCollection resultCollection;
        String extension = request.getExtension();

        IdentificationResultCollection binaryResult = droidCore.matchBinarySignatures(request);
        Optional<String> containerPuid = getContainerPuid(binaryResult);

        if (containerPuid.isPresent()) {
            resultCollection = handleContainer(binaryResult, request, containerPuid.get());
        } else {
            droidCore.removeLowerPriorityHits(binaryResult);
            droidCore.checkForExtensionsMismatches(binaryResult, request.getExtension());
            if (binaryResult.getResults().isEmpty()) {
                resultCollection = identifyByExtension(request);
            } else {
                resultCollection = binaryResult;
            }
        }

        boolean fileExtensionMismatch = resultCollection.getExtensionMismatch();

        return resultCollection.getResults()
                .stream().map(res -> createApiResult(res, extension, fileExtensionMismatch, request.getIdentifier().getUri()))
                .collect(Collectors.toList());
    }

    private ApiResult createApiResult(IdentificationResult result, String extension, boolean extensionMismatch, URI uri) {
        String name = result.getName();
        if (result.getMethod().equals(IdentificationMethod.CONTAINER)
                && (droidCore.formatNameByPuid(result.getPuid()) != null)) {
            name = droidCore.formatNameByPuid(result.getPuid());
        }
        return new ApiResult(extension, result.getMethod(), result.getPuid(), name, extensionMismatch, uri);
    }

    private <T> IdentificationResultCollection identifyByExtension(final IdentificationRequest<T> identificationRequest) {
        IdentificationResultCollection extensionResult = droidCore.matchExtensions(identificationRequest, false);
        droidCore.removeLowerPriorityHits(extensionResult);
        return extensionResult;
    }

    private Optional<String> getContainerPuid(final IdentificationResultCollection binaryResult) {
        List<String> containerPuids = Arrays.asList(ZIP_PUID, OLE2_PUID, GZIP_PUID);
        return binaryResult.getResults().stream()
                .map(IdentificationResult::getPuid)
                .filter(containerPuids::contains).findFirst();
    }

    private <T> IdentificationResultCollection handleContainer(final IdentificationResultCollection binaryResult,
                                                           final IdentificationRequest<T> identificationRequest, final String containerPuid) throws IOException {
        ContainerIdentifier identifier = switch (containerPuid) {
            case ZIP_PUID -> zipIdentifier;
            case OLE2_PUID -> ole2Identifier;
            case GZIP_PUID -> gzIdentifier;
            default -> throw new RuntimeException("Unknown container PUID : " + containerPuid);
        };

        IdentificationResultCollection containerResults = identifier.submit(identificationRequest);
        droidCore.removeLowerPriorityHits(containerResults);
        droidCore.checkForExtensionsMismatches(containerResults, identificationRequest.getExtension());
        containerResults.setFileLength(identificationRequest.size());
        containerResults.setRequestMetaData(identificationRequest.getRequestMetaData());

        return containerResults.getResults().isEmpty() ? binaryResult : containerResults;
    }

    public String getContainerSignatureVersion() {
        return containerSignatureVersion;
    }

    public String getBinarySignatureVersion() {
        return binarySignatureVersion;
    }

    public String getDroidVersion() {
        return droidVersion;
    }

    public S3Client getS3Client() {
        return s3Client;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public Region getS3Region() {
        return s3Region;
    }

}
