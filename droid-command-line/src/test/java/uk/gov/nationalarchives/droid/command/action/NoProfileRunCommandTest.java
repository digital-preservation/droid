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
    public void testNoProfileRunWithNonexistentSignatureFile() {
        
        command.setSignatureFile("test");
        command.setResources(new String[] {
            "test1.txt",
            "test2.txt",
        });
        try {
            command.execute();
            fail("Expected CommandExecutionEception");
        } catch (CommandExecutionException x) {
            assertEquals("Signature file not found", x.getMessage());
        }
    }
    @Ignore
    @Test
    public void testNoRunProfileAndSaveToDefaultExtensionWhenNoExtensionSupplied() throws Exception {
        command.setSignatureFile("test");
        command.setResources(new String[] {
            "test1.txt",
            "test2.txt",
        });
        
/*        SignatureFileInfo sig = mock(SignatureFileInfo.class);
        Map<SignatureType, SignatureFileInfo> sigs = new HashMap<SignatureType, SignatureFileInfo>();
        sigs.put(SignatureType.BINARY, sig);
        
        when(signatureManager.getDefaultSignatures()).thenReturn(sigs);
        
        ProfileInstance profileInstance = mock(ProfileInstance.class);
        when(profileInstance.getUuid()).thenReturn("abcde");
        when(profileManager.createProfile(sigs)).thenReturn(profileInstance);
        
        Future future = mock(Future.class);
        when(profileManager.start("abcde")).thenReturn(future);
        
        FileProfileResource resource1 = new FileProfileResource(new File("test1.txt"));
        FileProfileResource resource2 = new FileProfileResource(new File("test2.txt"));
        
        when(locationResolver.getResource("test1.txt", false)).thenReturn(resource1);
        when(locationResolver.getResource("test2.txt", false)).thenReturn(resource2);
        
        command.execute();
        
        verify(profileInstance).addResource(resource1);
        verify(profileInstance).addResource(resource2);
        verify(profileManager).createProfile(sigs);
        verify(profileManager).start("abcde");
        verify(future).get();
        verify(profileManager).save(eq("abcde"), eq(new File("test")), any(ProgressObserver.class));
        verify(profileManager).closeProfile("abcde"); */
    }
    
}
