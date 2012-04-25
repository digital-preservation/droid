/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.command.action;

import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileInfo;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureManager;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureManagerException;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureType;

/**
 * @author rflitcroft
 *
 */
public class DownloadSignatureUpdateCommandTest {

    private DownloadSignatureUpdateCommand command;
    private SignatureManager signatureManager;
    private PrintWriter printWriter;

    @Before
    public void setup() {
        command = new DownloadSignatureUpdateCommand();
        
        signatureManager = mock(SignatureManager.class);
        
        Map<SignatureType, SignatureFileInfo> mockValues = new HashMap<SignatureType, SignatureFileInfo>();
        mockValues.put(SignatureType.BINARY, new SignatureFileInfo(69, false, SignatureType.BINARY));
        mockValues.put(SignatureType.CONTAINER, new SignatureFileInfo(69, false, SignatureType.CONTAINER));
        try {
            when(signatureManager.getLatestSignatureFiles()).thenReturn(mockValues);
        } catch (SignatureManagerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        command.setSignatureManager(signatureManager);
        
        printWriter = mock(PrintWriter.class);
        command.setPrintWriter(printWriter);
    }
    
    @Test
    public void testDownloadSignatureUpdate() throws Exception {
       
        SignatureFileInfo sigFileInfo = new SignatureFileInfo(69, false, SignatureType.BINARY);
        when(signatureManager.downloadLatest(SignatureType.BINARY)).thenReturn(sigFileInfo);
        when(signatureManager.downloadLatest(SignatureType.CONTAINER)).thenReturn(sigFileInfo);
        command.execute();
        
        verify(printWriter, times(2)).println("Signature update version 69 has been downloaded");
    }
    
    @Test
    public void testDownloadSignatureUpdateWhenConnectionFails() throws SignatureManagerException {
        
        Throwable cause = new UnknownHostException("proxy");
        
        SignatureManagerException updateException = new SignatureManagerException(
                new RuntimeException("Failed", cause));
        when(signatureManager.downloadLatest(SignatureType.BINARY)).thenThrow(updateException);
        when(signatureManager.downloadLatest(SignatureType.CONTAINER)).thenThrow(updateException);
        try {
            command.execute();
            fail("Expected CommandExecutionException");
        } catch (CommandExecutionException e) {
            assertEquals("Error downloading signature updates: Failed\n"
                    + "Caused by java.net.UnknownHostException: proxy", e.getMessage());
        }
        
//        verify(printWriter).println("Error downloading signature update: Failed");
//        verify(printWriter).println("Caused by java.net.UnknownHostException: proxy");
    }

}
