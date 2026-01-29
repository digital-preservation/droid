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
package uk.gov.nationalarchives.droid.container.httpservice;

import org.apache.commons.configuration2.event.ConfigurationEvent;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalConfig;
import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalProperty;
import uk.gov.nationalarchives.droid.core.interfaces.http.HttpUtil;
import uk.gov.nationalarchives.droid.core.interfaces.signature.ProxySettings;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileInfo;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureServiceException;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureType;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureUpdateService;

import java.io.IOException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.file.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author rflitcroft
 *
 */
public class ContainerSignatureHttpService implements SignatureUpdateService {

    private static final String DATE_PATTERN = "yyyyMMdd";
    private static final String LAST_MODIFIED_HEADER = "Last-Modified";
    private static final String FILENAME_PATTERN = "container-signature-%s.xml";
    private static final String ERROR_MESSAGE_PATTERN = "The web server could not serve the signature file "
        + "from the address\n[%s]\nThe server gave the response [%s]";
    private static final String COULD_NOT_FIND_SERVER = "Could not contact the signature web server at\n%s";
    private static final String FILE_NOT_FOUND_404 = "The signature file was not found on the signature web"
        + "server at\n[%s]"; 

    private String endpointUrl;
    private CloseableHttpClient client = HttpClientBuilder.create().build();
    private HttpClient httpClient;

    /**
     * Empty bean constructor.
     */
    public ContainerSignatureHttpService() {
    }

    /**
     * Constructs a ContainerSignatureHttpService with the endpoint URL to use.
     * @param endpointUrl The endpoint url the signature service should use.
     */
    public ContainerSignatureHttpService(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    @Override
    public SignatureFileInfo getLatestVersion(int currentVersion) throws SignatureServiceException {
        try {
            HttpUtil.Signatures signaturesJson = new HttpUtil(this.httpClient).getSignaturesJson(endpointUrl);
            return new SignatureFileInfo(signaturesJson.latestContainerSignature().version(), false, SignatureType.CONTAINER);
        } catch (IOException | InterruptedException e) {
            HttpGet get = new HttpGet(endpointUrl);
            try {
                Date versionDate = getDateFromVersion(currentVersion);
                String dateString = DateUtils.formatDate(versionDate);
                get.addHeader("If-Modified-Since", dateString);

                HttpResponse response = client.execute(get);
                int statusCode = response.getStatusLine().getStatusCode();
                checkStatusCode(statusCode);
                int version = getVersion(response);
                return new SignatureFileInfo(version, false, SignatureType.CONTAINER);
            } catch (UnknownHostException uhe) {
                throw new SignatureServiceException(
                        String.format(COULD_NOT_FIND_SERVER, endpointUrl));
            } catch (IOException | ParseException | DateParseException dpe) {
                throw new SignatureServiceException(e);
            } finally {
                get.releaseConnection();
            }
        }
    }
    
    private static int getVersion(HttpResponse httpResponse) throws DateParseException {
        int version = 0;
        Header header = httpResponse.getFirstHeader(LAST_MODIFIED_HEADER);
        if (header != null) {
            String lastModified = header.getValue();
            Date lastModifiedDate = DateUtils.parseDate(lastModified);
            if (lastModifiedDate == null) {
                throw new DateParseException(lastModified);
            }
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
            version = Integer.parseInt(sdf.format(lastModifiedDate));
        }
        return version;
    }
    
    private static Date getDateFromVersion(int versionNumber) throws ParseException {
        String versionString = Integer.toString(versionNumber);
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        return sdf.parse(versionString);
    }

    @Override
    public SignatureFileInfo importSignatureFile(final Path targetDir, int version) throws SignatureServiceException {
        final String fileName = String.format(FILENAME_PATTERN, version);
        String baseUrl = HttpUtil.getBaseUrl(this.endpointUrl);
        HttpRequest request = HttpRequest
                .newBuilder(URI.create(baseUrl + "/container-signatures/" + fileName)).GET().build();
        try {
            Path downloadPath = Paths.get(targetDir.toString(), fileName);
            java.net.http.HttpResponse<Path> response = this.httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofFile(downloadPath));
            checkStatusCode(response.statusCode());
            return new SignatureFileInfo(version, false, SignatureType.CONTAINER, response.body());

        } catch (IOException | InterruptedException | SignatureServiceException e) {
            final HttpGet get = new HttpGet(endpointUrl);

            try {
                final HttpResponse response = client.execute(get);
                final int statusCode = response.getStatusLine().getStatusCode();
                checkStatusCode(statusCode);
                final SignatureFileInfo signatureFileInfo = new SignatureFileInfo(version, false, SignatureType.CONTAINER);

                final Path targetFile = targetDir.resolve(fileName);
                Files.copy(response.getEntity().getContent(), targetFile, StandardCopyOption.REPLACE_EXISTING);

                signatureFileInfo.setFile(targetFile);
                return signatureFileInfo;

            } catch (final UnknownHostException uhe) {
                throw new SignatureServiceException(
                        String.format(COULD_NOT_FIND_SERVER, endpointUrl));
            } catch (final IOException | DateParseException dpe) {
                throw new SignatureServiceException(e);
            } finally {
                get.releaseConnection();
            }
        }
    }

    private void checkStatusCode(int statusCode) throws SignatureServiceException {
        if (statusCode == HttpStatus.SC_NOT_FOUND) {
            throw new SignatureServiceException(
                    String.format(FILE_NOT_FOUND_404, endpointUrl));
        } else if (statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_NOT_MODIFIED) {
            throw new SignatureServiceException(
                    String.format(ERROR_MESSAGE_PATTERN, endpointUrl, statusCode));
        }
    }

    /**
     * Sets the endpoint URL.
     * @param url the URL to set
     */
    void setEndpointUrl(String url) {
        this.endpointUrl = url;
    }

    @Override
    public void onEvent(ConfigurationEvent evt) {
        final String propertyName = evt.getPropertyName();
        if (propertyName.equals(DroidGlobalProperty.CONTAINER_UPDATE_URL.getName())) {
            setEndpointUrl((String) evt.getPropertyValue());
        }
    }

    @Override
    public void onProxyChange(ProxySettings proxySettings) {
        this.httpClient = HttpUtil.createHttpClient(proxySettings);
        if (proxySettings.isEnabled()) {
            HttpRoutePlanner proxyRoutePlanner = new DefaultProxyRoutePlanner(
                    new HttpHost(proxySettings.getProxyHost(), proxySettings.getProxyPort()));
            client = HttpClients.custom().setRoutePlanner(proxyRoutePlanner).build();
        } else {
            client = HttpClientBuilder.create().build();
        }
        
    }

    @Override
    public void init(DroidGlobalConfig config) {
        setEndpointUrl(config.getProperties().getString(DroidGlobalProperty.CONTAINER_UPDATE_URL.getName()));
    }
}
