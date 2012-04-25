/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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
