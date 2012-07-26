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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
public class CheckSignatureUpdateCommandTest {

    private CheckSignatureUpdateCommand command;
    private SignatureManager signatureManager;
    private PrintWriter printWriter;

    @Before
    public void setup() {
        command = new CheckSignatureUpdateCommand();
        
        signatureManager = mock(SignatureManager.class);
        command.setSignatureManager(signatureManager);
        
        printWriter = mock(PrintWriter.class);
        command.setPrintWriter(printWriter);
    }
    
    @Test
    public void testCheckSignatureUpdateWhenUpdateIsAvailable() throws Exception {
        
        SignatureFileInfo sigFileInfo = new SignatureFileInfo(69, false, SignatureType.BINARY);
        Map<SignatureType, SignatureFileInfo> sigFiles = new HashMap<SignatureType, SignatureFileInfo>();
        sigFiles.put(SignatureType.BINARY, sigFileInfo);
        when(signatureManager.getLatestSignatureFiles()).thenReturn(sigFiles);
        
        command.execute();
        
        verify(printWriter).println("Binary signature update Version 69 is available");
    }
    
    
    @Test
    public void testCheckSignatureUpdateWhenNoUpdateIsAvailable() throws Exception {
        
        when(signatureManager.getLatestSignatureFiles()).thenReturn(Collections.EMPTY_MAP);

        command.execute();
        
        verify(printWriter).println("No signature updates are available");
    }
    
    @Test
    public void testCheckSignatureUpdateWhenConnectionFails() throws SignatureManagerException {
        
        Throwable cause = new UnknownHostException("proxy");
        
        SignatureManagerException e = new SignatureManagerException(new RuntimeException("Failed", cause));
        when(signatureManager.getLatestSignatureFiles()).thenThrow(e);
        
        try {
            command.execute();
            fail("Expected CommandExecutionEception");
        } catch (CommandExecutionException cee) {
            assertEquals("Error checking for signature update: Failed\n"
                    + "Caused by java.net.UnknownHostException: proxy", cee.getMessage());
        }
        
    }
    
}
