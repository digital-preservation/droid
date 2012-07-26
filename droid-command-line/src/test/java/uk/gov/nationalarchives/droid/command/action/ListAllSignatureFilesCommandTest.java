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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
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
public class ListAllSignatureFilesCommandTest {

    private ListAllSignatureFilesCommand command;
    private SignatureManager signatureManager;
    private PrintWriter printWriter;
    
    @Before
    public void setup() {
        command = new ListAllSignatureFilesCommand();
        
        printWriter = mock(PrintWriter.class);
        command.setPrintWriter(printWriter);
        
        signatureManager = mock(SignatureManager.class);
        command.setSignatureManager(signatureManager);
        
    }
    
    @Test
    public void testExecuteWithSignatureFilesPresent() {
        SignatureFileInfo info1 = new SignatureFileInfo(33, false, SignatureType.BINARY);
        info1.setFile(new File("foo/bar/version_33.xml"));
        SignatureFileInfo info2 = new SignatureFileInfo(45, false, SignatureType.BINARY);
        info2.setFile(new File("foo/bar/version_45.xml"));
        SignatureFileInfo info3 = new SignatureFileInfo(78, true, SignatureType.BINARY);
        info3.setFile(new File("foo/bar/version_78.xml"));
        
        SignatureFileInfo info4 = new SignatureFileInfo(133, false, SignatureType.CONTAINER);
        info4.setFile(new File("foo/bar/version_133.xml"));
        SignatureFileInfo info5 = new SignatureFileInfo(145, false, SignatureType.CONTAINER);
        info5.setFile(new File("foo/bar/version_145.xml"));

        SortedMap<String, SignatureFileInfo> binSigFiles = new TreeMap<String, SignatureFileInfo>();
        binSigFiles.put("33", info1);
        binSigFiles.put("45", info2);
        binSigFiles.put("78", info3);
        
        SortedMap<String, SignatureFileInfo> containerSigFiles = new TreeMap<String, SignatureFileInfo>();
        binSigFiles.put("133", info4);
        binSigFiles.put("145", info5);

        Map<SignatureType, SortedMap<String, SignatureFileInfo>> availableFiles = 
            new HashMap<SignatureType, SortedMap<String,SignatureFileInfo>>();
        
        availableFiles.put(SignatureType.BINARY, binSigFiles);
        availableFiles.put(SignatureType.CONTAINER, containerSigFiles);
        
        when(signatureManager.getAvailableSignatureFiles()).thenReturn(availableFiles);
        
        command.execute();
        
        verify(printWriter).println("Type: Binary Version:  33  File name: version_33.xml");
        verify(printWriter).println("Type: Binary Version:  45  File name: version_45.xml");
        verify(printWriter).println("Type: Binary Version:  78  File name: version_78.xml");
        verify(printWriter).println("Type: Container Version:  133  File name: version_133.xml");
        verify(printWriter).println("Type: Container Version:  145  File name: version_145.xml");
        
    }

    @Test
    public void testExecuteWithNoSignatureFilesPresent() {
        when(signatureManager.getAvailableSignatureFiles()).thenReturn(Collections.EMPTY_MAP);
        command.execute();
        
        verify(printWriter).println("No signature files available");
    }
}
