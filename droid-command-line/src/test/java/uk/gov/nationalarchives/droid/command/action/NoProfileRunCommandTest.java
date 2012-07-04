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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author rbrennan
 *
 */
public class NoProfileRunCommandTest {

    private NoProfileRunCommand command;
    
    @Before
    public void setup() {
        command = new NoProfileRunCommand();
    }
    
    @Test
    public void testNoProfileRunWithNoResource() throws Exception {
        
//        File sigFile = new File("sigFile");
//        sigFile.createNewFile();
//        command.setSignatureFile("sigFile");
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
//            sigFile.delete();
        }
    }
    
    @Test
    public void testNoProfileRunWithNoSignatureFile() {
        
        File resource = new File("resource");
        resource.mkdir();
        command.setResources(new String[] {
            resource.getAbsolutePath()
        });
        command.setSignatureFile("test");
        try {
            command.execute();
            fail("Expected CommandExecutionException");
        } catch (CommandExecutionException x) {
            assertEquals("Signature file not found", x.getMessage());
        }
    }
    
    @Test
    public void testNoProfileRunWithInvalidSignatureFile() throws Exception {
        
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
    public void testNoProfileRunWithNoExtensionFilter() throws Exception {
        
        command.setSignatureFile("../droid-core/test_sig_files/DROID_SignatureFile_V26.xml");
        command.setResources(new String[] {
            "../droid-core/test_sig_files"
        });
        command.execute();
    }
    
    @Test
    public void testNoProfileRunWithExtensionFilter() throws Exception {
        
        command.setSignatureFile("../droid-core/test_sig_files/DROID_SignatureFile_V26.xml");
        command.setResources(new String[] {
            "../droid-core/test_sig_files"
        });
        command.setExtensionFilter(new String[] {"oojah", "maflip"});
        command.execute();
    }
}
