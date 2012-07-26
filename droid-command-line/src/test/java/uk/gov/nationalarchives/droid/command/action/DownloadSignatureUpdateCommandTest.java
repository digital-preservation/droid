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
