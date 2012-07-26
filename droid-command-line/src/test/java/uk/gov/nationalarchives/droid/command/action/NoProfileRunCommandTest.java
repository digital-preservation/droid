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
