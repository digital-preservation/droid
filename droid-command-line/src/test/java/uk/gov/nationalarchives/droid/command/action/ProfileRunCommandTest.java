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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileInfo;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureManager;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureType;
import uk.gov.nationalarchives.droid.profile.FileProfileResource;
import uk.gov.nationalarchives.droid.profile.ProfileInstance;
import uk.gov.nationalarchives.droid.profile.ProfileManager;
import uk.gov.nationalarchives.droid.results.handlers.ProgressObserver;

/**
 * @author rflitcroft
 *
 */
public class ProfileRunCommandTest {

    private ProfileRunCommand command;
    private ProfileManager profileManager;
    private SignatureManager signatureManager;
    private LocationResolver locationResolver;
    
    @Before
    public void setup() {
        profileManager = mock(ProfileManager.class);
        signatureManager = mock(SignatureManager.class);
        locationResolver = mock(LocationResolver.class);
        command = new ProfileRunCommand();
        command.setProfileManager(profileManager);
        command.setSignatureManager(signatureManager);
        command.setLocationResolver(locationResolver);
    }
    
    @Test
    public void testRunProfileAndSaveToDefaultExtensionWhenNoExtensionSupplied() throws Exception {
        command.setDestination("test");
        command.setResources(new String[] {
            "test1.txt",
            "test2.txt",
        });
        
        
        
        SignatureFileInfo sig = mock(SignatureFileInfo.class);
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
        verify(profileManager).closeProfile("abcde");
    }
    
}
