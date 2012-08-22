/**
 * Copyright (c) 2012, The National Archives <pronom@nationalarchives.gsi.gov.uk>
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

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.gov.nationalarchives.droid.core.interfaces.signature.ProxySettings;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileInfo;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureServiceException;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureType;

/**
 * @author rflitcroft
 *
 */
//@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:META-INF/spring-signature.xml")
public class PronomSignatureServiceTest {

    private static final String ENDPOINT_URL = "http://www.nationalarchives.gov.uk/pronom/service.asmx";
//    private static final String ENDPOINT_URL = "http://localhost:6666/pronom/service.asmx";
    
    private static final int PROXY_PORT = 8080;
//    private static final String PROXY_HOST = "localhost";
    private static final String PROXY_HOST = "wb-cacheclst1.web.local";

    private static final int CURRENT_VER = 52;    
    
    @Autowired
    private PronomSignatureService importer;
    
    private File sigFileDir;
    

    
    @Before
    public void setup() throws Exception {

        sigFileDir = new File("tmp_sig_files");
        FileUtils.deleteQuietly(sigFileDir);
        sigFileDir.mkdir();
        new File("tmp_sig_files/DROID_SignatureFile_V26.xml").delete();
        importer.setEndpointUrl(ENDPOINT_URL);
        ProxySettings proxySettings = new ProxySettings();
        proxySettings.setEnabled(false);
        importer.onProxyChange(proxySettings);
    }
    
    //TODO this only works inside of TNA! We need to mock out the proxy call!
    @Ignore
    @Test
    public void testGetSigFileFromRemoteWebServiceSavesFileLocallyViaProxy() throws SignatureServiceException {
        
        ProxySettings proxySettings = new ProxySettings();
        
        proxySettings.setProxyHost(PROXY_HOST);
        proxySettings.setProxyPort(PROXY_PORT);
        proxySettings.setEnabled(true);
        
        importer.onProxyChange(proxySettings);
        
        File[] sigFiles = sigFileDir.listFiles();
        assertEquals(0, sigFiles.length);
        
        SignatureFileInfo info = importer.importSignatureFile(sigFileDir);
        
        sigFiles = sigFileDir.listFiles();
        assertEquals(1, sigFiles.length);
        
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
    public void testGetSigFileFromRemoteWebServiceSavesFileLocally() throws SignatureServiceException {
        
        File[] sigFiles = sigFileDir.listFiles();
        assertEquals(0, sigFiles.length);
        
        SignatureFileInfo info = importer.importSignatureFile(sigFileDir);
        sigFiles = sigFileDir.listFiles();
        assertEquals(1, sigFiles.length);
        
//        File file = new File("tmp_sig_files/DROID_SignatureFile_V" + CURRENT_VER + ".xml");
//        assertTrue(file.exists());
//        assertEquals(CURRENT_VER, info.getVersion());
        
        assertTrue(info.getVersion() > 0);
        assertEquals(false, info.isDeprecated());
        assertEquals(SignatureType.BINARY, info.getType());
    }
}

