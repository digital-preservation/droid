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
package uk.gov.nationalarchives.droid.gui.signature;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
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
public class UpdateSignatureActionTest {

    private CheckSignatureUpdateAction action;
    private SignatureManager signatureManager;
    
    @Before
    public void setup() {
        signatureManager = mock(SignatureManager.class);
        action = new CheckSignatureUpdateAction();
        action.setSignatureManager(signatureManager);
    }
    
    @Test
    public void testCheckForUpdatesWhenNewFileIsAvailable() throws Exception {
        
        SignatureFileInfo sigFileInfo = new SignatureFileInfo(666, true, SignatureType.BINARY);
        Map<SignatureType, SignatureFileInfo> updates = new HashMap<SignatureType, SignatureFileInfo>();
        updates.put(SignatureType.BINARY, sigFileInfo);
        
        when(signatureManager.getLatestSignatureFiles()).thenReturn(updates);
        
        action.execute();
        Map<SignatureType, SignatureFileInfo> availableUpdates = action.get();
        
        assertEquals(1, availableUpdates.size());
        assertEquals(666, availableUpdates.get(SignatureType.BINARY).getVersion());
        assertEquals(SignatureType.BINARY, availableUpdates.get(SignatureType.BINARY).getType());
    }
    
    @Test
    public void testCheckForUpdatesWhenNewFileIsNotAvailable() throws Exception {
        when(signatureManager.getLatestSignatureFiles()).thenReturn(Collections.EMPTY_MAP);

        action.execute();
        Map<SignatureType, SignatureFileInfo> availableUpdates = action.get();
        assertTrue(availableUpdates.isEmpty());
        assertSame(availableUpdates, action.getSignatureFileInfos());
    }
    
}
