/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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
