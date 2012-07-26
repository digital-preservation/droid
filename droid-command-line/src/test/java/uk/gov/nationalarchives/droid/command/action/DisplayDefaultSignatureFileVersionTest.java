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

import java.io.File;
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileInfo;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureManager;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureType;

/**
 * @author rflitcroft
 *
 */
public class DisplayDefaultSignatureFileVersionTest {

    private DisplayDefaultSignatureFileVersionCommand command;
    private PrintWriter printWriter;
    private SignatureManager signatureManager;
    
    @Before
    public void setup() {
        command = new DisplayDefaultSignatureFileVersionCommand();
        
        printWriter = mock(PrintWriter.class);
        command.setPrintWriter(printWriter);
        
        signatureManager = mock(SignatureManager.class);
        command.setSignatureManager(signatureManager);
    }
    
    @Test
    public void testDisplayDefaultSignatureFileVersion() throws Exception {
        File sigFile1 = new File("foo/bar/myBinSigFile.xml");
        File sigFile2 = new File("foo/bar/myContainerSigFile.xml");

        SignatureFileInfo sigFileInfo1 = new SignatureFileInfo(69, false, SignatureType.BINARY);
        SignatureFileInfo sigFileInfo2 = new SignatureFileInfo(71, false, SignatureType.CONTAINER);
        
        Map<SignatureType, SignatureFileInfo> allSigFiles = new TreeMap<SignatureType, SignatureFileInfo>();
        allSigFiles.put(SignatureType.BINARY, sigFileInfo1);
        allSigFiles.put(SignatureType.CONTAINER, sigFileInfo2);
        sigFileInfo1.setFile(sigFile1);
        sigFileInfo2.setFile(sigFile2);
        
        when(signatureManager.getDefaultSignatures()).thenReturn(allSigFiles);
        
        command.execute();
        
        verify(printWriter).println("Type: Binary Version:  69  File name: myBinSigFile.xml");
        verify(printWriter).println("Type: Container Version:  71  File name: myContainerSigFile.xml");
    }
}
