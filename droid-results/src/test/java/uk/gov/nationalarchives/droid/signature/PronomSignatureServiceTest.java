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
package uk.gov.nationalarchives.droid.signature;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;

import uk.gov.nationalarchives.droid.core.interfaces.signature.ProxySettings;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileInfo;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureServiceException;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureType;
import uk.gov.nationalarchives.droid.util.FileUtil;
import uk.gov.nationalarchives.pronom.PronomService;

/**
 * @author rflitcroft
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:META-INF/spring-signature.xml")
public class PronomSignatureServiceTest {

    private static final String PROXY_HOST = "localhost";
    private static final int CURRENT_VERSION = 104;

    @Autowired
    private ApplicationContext context;

    private Path sigFileDir;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());

    @Rule
    public WireMockRule proxyWireMockRule = new WireMockRule(wireMockConfig().port(34567)); //Doesn't work with a dynamic port. I don't know why.

    private String formatEndpointUrl() {
        return String.format("http://localhost:%d/pronom/service.asmx", wireMockRule.port());
    }

    @Before
    public void setup() throws Exception {
        proxyWireMockRule.stubFor(any(urlMatching(".*"))
                .willReturn(aResponse()
                        .proxiedFrom("http://localhost:" + wireMockRule.port())));
        sigFileDir = Paths.get("target/tmp_sig_files");
        FileUtil.deleteQuietly(sigFileDir);
        FileUtil.mkdirsQuietly(sigFileDir);
        Files.deleteIfExists(Paths.get("target/tmp_sig_files/DROID_SignatureFile_V26.xml"));
        wireMockRule.resetAll();
    }

    @Test
    public void testGetSigFileFromRemoteWebServiceSavesFileLocallyViaProxy() throws IOException, SignatureServiceException {
        stubOriginalEndpoint();
        getSigFileByProxy();
    }

    @Test
    public void testGetSigFileFromRemoteWebServiceSavesFileLocallyViaProxyNewEndpoint() throws IOException, SignatureServiceException {
        stubNewEndpoint();
        getSigFileByProxy();
    }

    @Test
    public void testGetLatestSigFileVersion() throws IOException {
        stubOriginalEndpoint();
        getLatestSigFileVersion();
    }

    @Test
    public void testGetLatestSigFileVersionNewEndpoint() throws IOException {
        stubNewEndpoint();
        getLatestSigFileVersion();
    }

    @Test
    public void testGetLatestSigFileVersionViaProxy() throws IOException {
        stubOriginalEndpoint();
        getSigFileVersionViaProxy();
    }

    @Test
    public void testGetSigFileFromRemoteWebServiceSavesFileLocally() throws SignatureServiceException, IOException {
        stubOriginalEndpoint();
        getSigFileWithoutProxy();
    }

    @Test
    public void testGetSigFileFromRemoteWebServiceSavesFileLocallyNewEndpoint() throws SignatureServiceException, IOException {
        stubNewEndpoint();
        getSigFileWithoutProxy();
    }

    private void getSigFileVersionViaProxy() {
        ProxySettings proxySettings = new ProxySettings();

        proxySettings.setProxyHost(PROXY_HOST);
        proxySettings.setProxyPort(proxyWireMockRule.port());
        proxySettings.setEnabled(true);

        PronomSignatureService importer = createSignatureService(proxySettings);
        SignatureFileInfo info = importer.getLatestVersion(1);

        assertEquals(CURRENT_VERSION, info.getVersion());
        assertEquals(104, info.getVersion());
        assertFalse(info.isDeprecated());
        assertEquals(SignatureType.BINARY, info.getType());
    }


    private void getLatestSigFileVersion() {
        PronomSignatureService importer = createSignatureService(new ProxySettings());
        SignatureFileInfo info = importer.getLatestVersion(1);

        assertEquals(CURRENT_VERSION, info.getVersion());
        assertEquals(104, info.getVersion());
        assertFalse(info.isDeprecated());
        assertEquals(SignatureType.BINARY, info.getType());
    }

    private void getSigFileWithoutProxy() throws IOException, SignatureServiceException {
        List<Path> sigFiles = FileUtil.listFiles(sigFileDir, false, (DirectoryStream.Filter<Path>)null);
        assertEquals(0, sigFiles.size());

        PronomSignatureService importer = createSignatureService(new ProxySettings());
        SignatureFileInfo info = importer.importSignatureFile(sigFileDir, 104);
        sigFiles = FileUtil.listFiles(sigFileDir, false, (DirectoryStream.Filter<Path>)null);
        assertEquals(1, sigFiles.size());

        assertEquals(104, info.getVersion());
        assertFalse(info.isDeprecated());
        assertEquals(SignatureType.BINARY, info.getType());
    }

    public void stubNewEndpoint() throws IOException {
        String responseJson = "{\"latest_signature\":{\"name\":\"DROID Signature File V104\",\"location\":\"/signatures/DROID_SignatureFile_V104.xml\",\"version\":\"104\"}," +
                "\"latest_container_signature\":{\"name\":\"07 October 2025\",\"location\":\"/container-signatures/container-signature-20251007.xml\",\"version\":\"20251007\"}}\n";
        wireMockRule.stubFor(get(urlEqualTo("/signatures.json"))
                .willReturn(aResponse().withStatus(200).withBody(responseJson)));
        wireMockRule.stubFor(get(urlEqualTo("/signatures/DROID_SignatureFile_V104.xml"))
                .willReturn(aResponse().withStatus(200).withBody(IOUtils.resourceToString("/uk/gov/nationalarchives/droid/signature/getSignatureFileResponse.xml", StandardCharsets.UTF_8))));
    }

    public void stubOriginalEndpoint() throws IOException {
        wireMockRule.stubFor(get(urlEqualTo("/signatures.json"))
                .willReturn(aResponse().withStatus(404)));
        wireMockRule.stubFor(get(urlEqualTo("/signatures/DROID_SignatureFile_V104.xml"))
                .willReturn(aResponse().withStatus(404)));
        wireMockRule.stubFor(any(urlEqualTo("/pronom/service.asmx"))
                .withRequestBody(matchingXPath("//getSignatureFileVersionV1"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "text/xml")
                        .withBody("<?xml version=\"1.0\" encoding=\"utf-8\"?><soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"><soap:Body><getSignatureFileVersionV1Response xmlns=\"http://pronom.nationalarchives.gov.uk\"><Version><Version>104</Version></Version><Deprecated>false</Deprecated></getSignatureFileVersionV1Response></soap:Body></soap:Envelope>")));



        wireMockRule.stubFor(any(urlEqualTo("/pronom/service.asmx"))
                .withRequestBody(matchingXPath("//getSignatureFileV1"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "text/xml")
                        .withBody(IOUtils.resourceToString("/uk/gov/nationalarchives/droid/signature/getSignatureFileResponse.xml", StandardCharsets.UTF_8))
                ));
    }

    private PronomSignatureService createSignatureService(ProxySettings proxySettings) {
        PronomService pronomService = context.getBean(PronomService.class);
        PronomSignatureService pronomSignatureService = new PronomSignatureService(pronomService, "DROID_SignatureFile_V%s.xml");
        pronomSignatureService.setEndpointUrl(formatEndpointUrl());
        pronomSignatureService.onProxyChange(proxySettings);
        return pronomSignatureService;
    }

    private void getSigFileByProxy() throws IOException, SignatureServiceException {
        ProxySettings proxySettings = new ProxySettings();

        proxySettings.setProxyHost(PROXY_HOST);
        proxySettings.setProxyPort(proxyWireMockRule.port());
        proxySettings.setEnabled(true);

        PronomSignatureService importer = createSignatureService(proxySettings);

        List<Path> sigFiles = FileUtil.listFiles(sigFileDir, false, (DirectoryStream.Filter<Path>)null);
        assertEquals(0, sigFiles.size());

        SignatureFileInfo info = importer.importSignatureFile(sigFileDir, 104);
        sigFiles = FileUtil.listFiles(sigFileDir, false, (DirectoryStream.Filter<Path>)null);
        assertEquals(1, sigFiles.size());

        assertEquals(104, info.getVersion());
        assertFalse(info.isDeprecated());
        assertEquals(SignatureType.BINARY, info.getType());
    }
}

