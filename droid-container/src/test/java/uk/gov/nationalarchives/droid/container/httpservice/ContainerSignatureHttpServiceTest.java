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

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import uk.gov.nationalarchives.droid.core.interfaces.signature.ProxySettings;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileInfo;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureServiceException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.*;

/**
 * @author rflitcroft
 *
 */
public class ContainerSignatureHttpServiceTest {

    private ContainerSignatureHttpService httpService;
    private ProxySettings proxySettings;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());

    @Rule
    public WireMockRule proxyWireMockRule = new WireMockRule(wireMockConfig().dynamicPort());

    @Before
    public void setup() throws IOException {
        proxyWireMockRule.stubFor(any(urlMatching(".*"))
                .willReturn(aResponse()
                        .proxiedFrom("http://localhost:" + wireMockRule.port())));

        httpService = new ContainerSignatureHttpService();
        httpService.setEndpointUrl(wireMockRule.url("pronom/container-signature.xml"));
        proxySettings = new ProxySettings();
        proxySettings.setEnabled(true);
        proxySettings.setProxyHost("localhost");
        proxySettings.setProxyPort(proxyWireMockRule.port());
        wireMockRule.resetAll();
    }

    @Test
    public void testGetLatestVersion() throws SignatureServiceException, IOException {
        stubOriginalEndpoint();
        httpService.onProxyChange(new ProxySettings());

        SignatureFileInfo sigFileInfo = httpService.getLatestVersion(20100101);
        assertNull(sigFileInfo.getFile());
        assertThat(sigFileInfo.getVersion(), greaterThan(20110114));
        assertEquals(false, sigFileInfo.isDeprecated());
    }

    @Test
    public void testGetLatestVersionNewEndpoint() throws SignatureServiceException, IOException {
        stubNewEndpoint();
        httpService.onProxyChange(new ProxySettings());

        SignatureFileInfo sigFileInfo = httpService.getLatestVersion(20100101);
        assertNull(sigFileInfo.getFile());
        assertEquals(sigFileInfo.getVersion(), 20251007);
        assertEquals(false, sigFileInfo.isDeprecated());
        wireMockRule.resetAll();
    }
    
    @Test
    public void testGetLatestVersionViaProxy() throws SignatureServiceException, IOException {
        stubOriginalEndpoint();
        httpService.onProxyChange(proxySettings);
        
        SignatureFileInfo sigFileInfo = httpService.getLatestVersion(20100101);
        assertNull(sigFileInfo.getFile());
        assertThat(sigFileInfo.getVersion(), greaterThan(20110114));
        assertEquals(false, sigFileInfo.isDeprecated());
    }

    @Test
    public void testGetLatestVersionViaProxyNewEndpoint() throws SignatureServiceException, IOException {
        stubNewEndpoint();
        httpService.onProxyChange(proxySettings);

        SignatureFileInfo sigFileInfo = httpService.getLatestVersion(20100101);
        assertNull(sigFileInfo.getFile());
        assertEquals(sigFileInfo.getVersion(), 20251007);
        assertEquals(false, sigFileInfo.isDeprecated());
    }

    @Test
    public void testImportSignatureFile() throws IOException, SignatureServiceException {
        stubOriginalEndpoint();
        httpService.onProxyChange(new ProxySettings());

        final File tmpDir = temporaryFolder.newFolder("testImportSignatureFileEmptyFolder");
        tmpDir.mkdir();

        SignatureFileInfo sigFileInfo = httpService.importSignatureFile(tmpDir.toPath(), 20110114);
        assertEquals(false, sigFileInfo.isDeprecated());
        assertEquals(sigFileInfo.getVersion(), 20110114);

        assertEquals(1, tmpDir.list().length);
    }

    @Test
    public void testImportSignatureFileNewEndpoint() throws IOException, SignatureServiceException {
        stubOriginalEndpoint();
        httpService.onProxyChange(new ProxySettings());

        final File tmpDir = temporaryFolder.newFolder("testImportSignatureFileEmptyFolder");
        tmpDir.mkdir();

        SignatureFileInfo sigFileInfo = httpService.importSignatureFile(tmpDir.toPath(), 20110114);
        assertEquals(false, sigFileInfo.isDeprecated());
        assertEquals(sigFileInfo.getVersion(), 20110114);

        assertEquals(1, tmpDir.list().length);
    }
    
    @Test
    public void testImportSignatureFileViaProxy() throws IOException, SignatureServiceException {
        stubOriginalEndpoint();
        httpService.onProxyChange(proxySettings);

        final File tmpDir = temporaryFolder.newFolder("testImportSignatureFileViaProxy");
        tmpDir.mkdir();

        assertEquals(0, tmpDir.list().length);

        SignatureFileInfo sigFileInfo = httpService.importSignatureFile(tmpDir.toPath(), 20110114);
        assertEquals(false, sigFileInfo.isDeprecated());
        assertEquals(sigFileInfo.getVersion(), 20110114);

        assertEquals(1, tmpDir.list().length);
    }

    @Test
    public void testImportSignatureFileViaProxyNewEndpoint() throws IOException, SignatureServiceException {
        stubNewEndpoint();
        httpService.onProxyChange(proxySettings);

        final File tmpDir = temporaryFolder.newFolder("testImportSignatureFileViaProxy");
        tmpDir.mkdir();

        assertEquals(0, tmpDir.list().length);

        SignatureFileInfo sigFileInfo = httpService.importSignatureFile(tmpDir.toPath(), 20110114);
        assertEquals(false, sigFileInfo.isDeprecated());
        assertEquals(sigFileInfo.getVersion(), 20110114);

        assertEquals(1, tmpDir.list().length);
    }

    private void stubNewEndpoint() {
        try {
            String responseJson = """
                    {
                      "latest_signature": {
                        "name": "DROID Signature File V122",
                        "location": "/signatures/DROID_SignatureFile_V122.xml",
                        "version": "122"
                      },
                      "latest_container_signature": {
                        "name": "07 October 2025",
                        "location": "/container-signatures/container-signature-20251007.xml",
                        "version": "20251007"
                      }
                    }
                    """;
            wireMockRule.stubFor(get(urlEqualTo("/signatures.json"))
                    .willReturn(aResponse().withStatus(200).withBody(responseJson)));
            wireMockRule.stubFor(get(urlEqualTo("/container-signatures/container-signature-20110114.xml"))
                    .willReturn(aResponse().withStatus(200).withBody(IOUtils.resourceToString("/container-signature-20250925.xml", StandardCharsets.UTF_8))));
        } catch (IOException ignored) {}
    }

    private void stubOriginalEndpoint() {
        try {
            wireMockRule.stubFor(get(urlEqualTo("/signatures.json"))
                    .willReturn(aResponse().withStatus(404)));
            wireMockRule.stubFor(get(urlEqualTo("/container-signatures/container-signature-20110114.xml"))
                    .willReturn(aResponse().withStatus(404)));
            wireMockRule.stubFor(get(urlEqualTo("/pronom/container-signature.xml"))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "text/xml")
                            .withHeader("Last-Modified", "Tue, 22 Aug 2023 12:47:41 GMT")
                            .withBody(IOUtils.resourceToString("/container-signature-20250925.xml", StandardCharsets.UTF_8))
                    ));
        } catch (IOException ignored) {}
    }

}
