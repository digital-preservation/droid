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
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import uk.gov.nationalarchives.droid.core.BinarySignatureIdentifier;

/**
 * @author rbrennan
 *
 */
public class NoProfileRunCommandTest {

    private NoProfileRunCommand command;
    private LocationResolver locationResolver;
    
    @Before
    public void setup() {
        locationResolver = mock(LocationResolver.class);
        command = new NoProfileRunCommand();
        command.setLocationResolver(locationResolver);
    }
    
    @Test
    public void testNoProfileRunWithNoSignatureFile() {
        
        command.setSignatureFile("test");
        command.setResources(new String[] {
            "test1.txt",
            "test2.txt",
        });
        try {
            command.execute();
            fail("Expected CommandExecutionException");
        } catch (CommandExecutionException x) {
            assertEquals("Signature file not found", x.getMessage());
        }
    }
    
    @Test
    public void testNoProfileRunWithNoResource() throws Exception {
        
        File sigFile = new File("sigFile");
        sigFile.createNewFile();
        command.setSignatureFile("sigFile");
        command.setResources(new String[] {
            "test1.txt",
            "test2.txt",
        });
        try {
            command.execute();
            fail("Expected CommandExecutionException");
        } catch (CommandExecutionException x) {
            assertEquals("Resources directory not found", x.getMessage());
        } finally {
            sigFile.delete();
        }
    }
    
    @Test
    public void testNoProfileRunWithInvalidSignatureFileAndResource() throws Exception {
        
        File sigFile = new File("sigFile");
        sigFile.createNewFile();
        command.setSignatureFile(sigFile.getAbsolutePath());
        
        File resource = new File("resource");
        resource.mkdir();
        command.setResources(new String[] {
            resource.getAbsolutePath()
        });
        try {
            command.execute();
            fail("Expected CommandExecutionException");
        } catch (CommandExecutionException x) {
            assertEquals("Can't parse signature file", x.getMessage());
        } finally {
            sigFile.delete();
            resource.delete();
        }
    }
    
    @Test
    public void testNoProfileRunWithValidSignatureFileAndResource() throws Exception {
        
        command.setSignatureFile("../droid-core/test_sig_files/DROID_SignatureFile_V26.xml");
        command.setResources(new String[] {
            "../droid-core/test_sig_files"
        });
        command.execute();
    }
}
