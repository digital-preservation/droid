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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import uk.gov.nationalarchives.droid.core.interfaces.signature.ProxySettings;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileInfo;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureServiceException;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureType;
import uk.gov.nationalarchives.droid.util.FileUtil;

/**
 * @author rflitcroft
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:META-INF/spring-signature.xml")
public class PronomSignatureServiceTest {

    private static final int PROXY_PORT = 8080;
    private static final String PROXY_HOST = "wb-cacheclst1.web.local";
    
    @Autowired
    private PronomSignatureService importer;
    
    private Path sigFileDir;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());

    private String formatEndpointUrl() {
        return String.format("http://localhost:%d/pronom/service.asmx", wireMockRule.port());
    }

    @Before
    public void setup() throws Exception {
        stubFor(any(urlEqualTo("/pronom/service.asmx"))
                .withRequestBody(matchingXPath("//getSignatureFileVersionV1"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "text/xml")
                        .withBody("<?xml version=\"1.0\" encoding=\"utf-8\"?><soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"><soap:Body><getSignatureFileVersionV1Response xmlns=\"http://pronom.nationalarchives.gov.uk\"><Version><Version>104</Version></Version><Deprecated>false</Deprecated></getSignatureFileVersionV1Response></soap:Body></soap:Envelope>")));



        stubFor(any(urlEqualTo("/pronom/service.asmx"))
                .withRequestBody(matchingXPath("//getSignatureFileV1"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "text/xml")
                        .withBody(IOUtils.resourceToString("/uk/gov/nationalarchives/droid/signature/getSignatureFileResponse.xml", StandardCharsets.UTF_8))
                ));

        sigFileDir = Paths.get("target/tmp_sig_files");
        FileUtil.deleteQuietly(sigFileDir);
        FileUtil.mkdirsQuietly(sigFileDir);
        Files.deleteIfExists(Paths.get("target/tmp_sig_files/DROID_SignatureFile_V26.xml"));
        importer.setEndpointUrl(formatEndpointUrl());
        ProxySettings proxySettings = new ProxySettings();
        proxySettings.setEnabled(false);
        importer.onProxyChange(proxySettings);
    }
    
    //TODO this only works inside of TNA! We need to mock out the proxy call!
    @Ignore
    @Test
    public void testGetSigFileFromRemoteWebServiceSavesFileLocallyViaProxy() throws SignatureServiceException, IOException {
        
        ProxySettings proxySettings = new ProxySettings();
        
        proxySettings.setProxyHost(PROXY_HOST);
        proxySettings.setProxyPort(PROXY_PORT);
        proxySettings.setEnabled(true);
        
        importer.onProxyChange(proxySettings);
        
        List<Path> sigFiles = FileUtil.listFiles(sigFileDir, false, (DirectoryStream.Filter)null);
        assertEquals(0, sigFiles.size());
        
        SignatureFileInfo info = importer.importSignatureFile(sigFileDir);
        
        sigFiles = FileUtil.listFiles(sigFileDir, false, (DirectoryStream.Filter)null);
        assertEquals(1, sigFiles.size());
        
//        File file = new File("tmp_sig_files/DROID_SignatureFile_V" + CURRENT_VER + ".xml");
//        assertTrue(file.exists());
//        assertEquals(CURRENT_VER, info.getVersion());
        
        assertTrue(info.getVersion() > 0);
        assertEquals(false, info.isDeprecated());
        assertEquals(SignatureType.BINARY, info.getType());
    }

    @Test
    public void testGetLatestSigFileVersion() {
        
        SignatureFileInfo info = importer.getLatestVersion(1);
       
//        assertEquals(CURRENT_VER, info.getVersion());
        assertTrue(info.getVersion() > 0);
        assertEquals(false, info.isDeprecated());
        assertEquals(SignatureType.BINARY, info.getType());
    }
    
    //TODO this only works inside of TNA! We need to mock out the proxy call!
    @Ignore
    @Test
    public void testGetLatestSigFileVersionViaProxy() {
        
        ProxySettings proxySettings = new ProxySettings();
        
        proxySettings.setProxyHost(PROXY_HOST);
        proxySettings.setProxyPort(PROXY_PORT);
        proxySettings.setEnabled(true);
        
        importer.onProxyChange(proxySettings);
        SignatureFileInfo info = importer.getLatestVersion(1);
        
//        assertEquals(CURRENT_VER, info.getVersion());
        assertTrue(info.getVersion() > 0);
        assertEquals(false, info.isDeprecated());
        assertEquals(SignatureType.BINARY, info.getType());
    }

    @Test
    public void testGetSigFileFromRemoteWebServiceSavesFileLocally() throws SignatureServiceException, IOException {

        List<Path> sigFiles = FileUtil.listFiles(sigFileDir, false, (DirectoryStream.Filter)null);
        assertEquals(0, sigFiles.size());
        
        SignatureFileInfo info = importer.importSignatureFile(sigFileDir);
        sigFiles = FileUtil.listFiles(sigFileDir, false, (DirectoryStream.Filter)null);
        assertEquals(1, sigFiles.size());
        
//        File file = new File("tmp_sig_files/DROID_SignatureFile_V" + CURRENT_VER + ".xml");
//        assertTrue(file.exists());
//        assertEquals(CURRENT_VER, info.getVersion());
        
        assertTrue(info.getVersion() > 0);
        assertEquals(false, info.isDeprecated());
        assertEquals(SignatureType.BINARY, info.getType());
    }
}

