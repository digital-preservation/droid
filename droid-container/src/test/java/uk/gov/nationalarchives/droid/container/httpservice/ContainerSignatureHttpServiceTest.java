/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.container.httpservice;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import uk.gov.nationalarchives.droid.core.interfaces.signature.ProxySettings;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileInfo;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureServiceException;

/**
 * @author rflitcroft
 *
 */
public class ContainerSignatureHttpServiceTest {

    private ContainerSignatureHttpService httpService;
    private ProxySettings proxySettings;
    
    @Before
    public void setup() {
        httpService = new ContainerSignatureHttpService();
        httpService.setEndpointUrl("http://www.nationalarchives.gov.uk/pronom/container-signature.xml");
        proxySettings = mock(ProxySettings.class);
    }
    
    @Test
    public void testGetLatestVersion() throws SignatureServiceException {
        when(proxySettings.isEnabled()).thenReturn(false);
        httpService.onProxyChange(proxySettings);

        SignatureFileInfo sigFileInfo = httpService.getLatestVersion(20100101);
        assertNull(sigFileInfo.getFile());
        assertTrue(sigFileInfo.getVersion() > 20110114);
        //assertEquals(20110114, sigFileInfo.getVersion());
        assertEquals(false, sigFileInfo.isDeprecated());
    }
    
    @Test
    public void testGetLatestVersionViaProxy() throws SignatureServiceException {
        when(proxySettings.isEnabled()).thenReturn(true);
        when(proxySettings.getProxyHost()).thenReturn("wb-cacheclst1.web.local");
        when(proxySettings.getProxyPort()).thenReturn(8080);
        httpService.onProxyChange(proxySettings);
        
        SignatureFileInfo sigFileInfo = httpService.getLatestVersion(20100101);
        assertNull(sigFileInfo.getFile());
        assertTrue(sigFileInfo.getVersion() > 20110114);
        //assertEquals(20110114, sigFileInfo.getVersion());
        assertEquals(false, sigFileInfo.isDeprecated());
    }

    @Test
    public void testImportSignatureFile() {
        when(proxySettings.isEnabled()).thenReturn(false);
        httpService.onProxyChange(proxySettings);

        File tmpDir = new File("tmp");
        FileUtils.deleteQuietly(tmpDir);

        tmpDir.mkdir();
        assertEquals(0, tmpDir.list().length);
        SignatureFileInfo sigFileInfo;
        try {
            sigFileInfo = httpService.importSignatureFile(tmpDir);
            assertEquals(false, sigFileInfo.isDeprecated());
            //assertEquals(20110114, sigFileInfo.getVersion());
            assertTrue(sigFileInfo.getVersion() > 20110114);
        } catch (SignatureServiceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        assertEquals(1, tmpDir.list().length);
    }
    
    @Test
    public void testImportSignatureFileViaProxy() {
        when(proxySettings.isEnabled()).thenReturn(true);
        when(proxySettings.getProxyHost()).thenReturn("wb-cacheclst1.web.local");
        when(proxySettings.getProxyPort()).thenReturn(8080);
        httpService.onProxyChange(proxySettings);

        File tmpDir = new File("tmp");
        FileUtils.deleteQuietly(tmpDir);

        tmpDir.mkdir();
        assertEquals(0, tmpDir.list().length);
        SignatureFileInfo sigFileInfo;
        try {
            sigFileInfo = httpService.importSignatureFile(tmpDir);
            assertEquals(false, sigFileInfo.isDeprecated());
            //assertEquals(20110114, sigFileInfo.getVersion());
            assertTrue(sigFileInfo.getVersion() > 20110114);
        } catch (SignatureServiceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        assertEquals(1, tmpDir.list().length);
    }
}
