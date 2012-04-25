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
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Before;
import org.junit.Test;

import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalConfig;
import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalProperty;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileInfo;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureManager;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureType;

/**
 * @author rflitcroft
 *
 */
public class ConfigureDefaultSignatureFileVersionCommandTest {

    private ConfigureDefaultSignatureFileVersionCommand command;
    private PrintWriter printWriter;
    private SignatureManager signatureManager;
    private DroidGlobalConfig globalConfig;
    
    @Before
    public void setup() {
        command = new ConfigureDefaultSignatureFileVersionCommand();
        
        printWriter = mock(PrintWriter.class);
        command.setPrintWriter(printWriter);
        
        signatureManager = mock(SignatureManager.class);
        command.setSignatureManager(signatureManager);
        command.setType(SignatureType.BINARY);
        
        globalConfig = mock(DroidGlobalConfig.class);
        command.setGlobalConfig(globalConfig);
    }
    
    @Test
    public void testExecuteWithAValidVersion() throws Exception {
        
        SignatureFileInfo info1 = new SignatureFileInfo(33, false, SignatureType.BINARY);
        SignatureFileInfo info2 = new SignatureFileInfo(45, false, SignatureType.BINARY);
        info2.setFile(new File("foo/bar/version_45.xml"));

        SortedMap<String, SignatureFileInfo> binSigFileInfos = new TreeMap<String, SignatureFileInfo>();
        binSigFileInfos.put("v33", info1);
        binSigFileInfos.put("v45", info2);
        
        Map<SignatureType, SortedMap<String, SignatureFileInfo>> allSigFileInfos = new HashMap<SignatureType, SortedMap<String,SignatureFileInfo>>();
        allSigFileInfos.put(SignatureType.BINARY, binSigFileInfos);
        
        Map<SignatureType, SignatureFileInfo> defaultSigFiles = new HashMap<SignatureType, SignatureFileInfo>();
        defaultSigFiles.put(SignatureType.BINARY, info2);
        
        when(signatureManager.getAvailableSignatureFiles()).thenReturn(allSigFileInfos);
        when(signatureManager.getDefaultSignatures()).thenReturn(defaultSigFiles);
        
        PropertiesConfiguration properties = mock(PropertiesConfiguration.class);
        
        when(globalConfig.getProperties()).thenReturn(properties);
        
        command.setSignatureFileVersion(45);
        command.execute();
        
        verify(properties).setProperty(DroidGlobalProperty.DEFAULT_BINARY_SIG_FILE_VERSION.getName(), "v45");
        verify(properties).save();
        
        verify(printWriter).println("Default signature file updated. Version: 45  File name: version_45.xml");
    }
    
    @Test
    public void testExecuteWithAnUnknownVersion() {
        
        SignatureFileInfo info1 = new SignatureFileInfo(33, false, SignatureType.BINARY);
        SignatureFileInfo info2 = new SignatureFileInfo(45, false, SignatureType.BINARY);

        SortedMap<String, SignatureFileInfo> binSigFileInfos = new TreeMap<String, SignatureFileInfo>();
        binSigFileInfos.put("v33", info1);
        binSigFileInfos.put("v45", info2);
        
        Map<SignatureType, SortedMap<String, SignatureFileInfo>> allSigFileInfos = new HashMap<SignatureType, SortedMap<String,SignatureFileInfo>>();
        allSigFileInfos.put(SignatureType.BINARY, binSigFileInfos);
        
        Map<SignatureType, SignatureFileInfo> defaultSigFiles = new HashMap<SignatureType, SignatureFileInfo>();
        defaultSigFiles.put(SignatureType.BINARY, info2);

        when(signatureManager.getAvailableSignatureFiles()).thenReturn(allSigFileInfos);
        
        command.setSignatureFileVersion(78);
        
        try {
            command.execute();
            fail("Expected CommandExecutionException");
        } catch (CommandExecutionException e) {
            assertEquals("Unknown signature file version: 78", e.getMessage());
        }
        
    }
}
