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
import org.junit.Ignore;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    @Before
    public void setup() throws IOException {
        stubFor(get(urlEqualTo("/pronom/container-signature.xml"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "text/xml")
                        .withHeader("Last-Modified", "Wed, 10 May 2023 12:51:08 GMT")
                        .withBody(IOUtils.resourceToString("/container-signature-20230510.xml", StandardCharsets.UTF_8))
                ));


        httpService = new ContainerSignatureHttpService();
        httpService.setEndpointUrl(wireMockRule.url("pronom/container-signature.xml"));
        proxySettings = mock(ProxySettings.class);
    }
    
    @Test
    public void testGetLatestVersion() throws SignatureServiceException {
        when(proxySettings.isEnabled()).thenReturn(false);
        httpService.onProxyChange(proxySettings);

        SignatureFileInfo sigFileInfo = httpService.getLatestVersion(20100101);
        assertNull(sigFileInfo.getFile());
        assertThat(sigFileInfo.getVersion(), greaterThan(20110114));
        assertEquals(false, sigFileInfo.isDeprecated());
    }
    
    //TODO this only works inside of TNA! We need to mock out the proxy call!
    @Ignore
    @Test
    public void testGetLatestVersionViaProxy() throws SignatureServiceException {
        when(proxySettings.isEnabled()).thenReturn(true);
        when(proxySettings.getProxyHost()).thenReturn("wb-cacheclst1.web.local");
        when(proxySettings.getProxyPort()).thenReturn(8080);
        httpService.onProxyChange(proxySettings);
        
        SignatureFileInfo sigFileInfo = httpService.getLatestVersion(20100101);
        assertNull(sigFileInfo.getFile());
        assertThat(sigFileInfo.getVersion(), greaterThan(20110114));
        assertEquals(false, sigFileInfo.isDeprecated());
    }

    @Test
    public void testImportSignatureFile() throws IOException {
        when(proxySettings.isEnabled()).thenReturn(false);
        httpService.onProxyChange(proxySettings);

        final File tmpDir = temporaryFolder.newFolder("testImportSignatureFileEmptyFolder");
        tmpDir.mkdir();

        SignatureFileInfo sigFileInfo;
        try {
            sigFileInfo = httpService.importSignatureFile(tmpDir.toPath());
            assertEquals(false, sigFileInfo.isDeprecated());
            assertThat(sigFileInfo.getVersion(), greaterThan(20110114));
        } catch (SignatureServiceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        assertEquals(1, tmpDir.list().length);
    }
    
    //TODO this only works inside of TNA! We need to mock out the proxy call!
    @Ignore
    @Test
    public void testImportSignatureFileViaProxy() throws IOException {
        when(proxySettings.isEnabled()).thenReturn(true);
        when(proxySettings.getProxyHost()).thenReturn("wb-cacheclst1.web.local");
        when(proxySettings.getProxyPort()).thenReturn(8080);
        httpService.onProxyChange(proxySettings);

        final File tmpDir = temporaryFolder.newFolder("testImportSignatureFileViaProxy");
        tmpDir.mkdir();

        assertEquals(0, tmpDir.list().length);
        SignatureFileInfo sigFileInfo;
        try {
            sigFileInfo = httpService.importSignatureFile(tmpDir.toPath());
            assertEquals(false, sigFileInfo.isDeprecated());
            assertThat(sigFileInfo.getVersion(), greaterThan(20110114));
        } catch (SignatureServiceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        assertEquals(1, tmpDir.list().length);
    }
}
