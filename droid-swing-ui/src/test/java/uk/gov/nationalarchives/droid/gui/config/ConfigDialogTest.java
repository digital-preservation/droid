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
package uk.gov.nationalarchives.droid.gui.config;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Ignore;
import org.junit.Test;

import junit.extensions.jfcunit.JFCTestCase;
import junit.extensions.jfcunit.JFCTestHelper;
import junit.extensions.jfcunit.RobotTestHelper;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import junit.extensions.jfcunit.eventdata.StringEventData;



import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalConfig;
import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalProperty;
import uk.gov.nationalarchives.droid.gui.GlobalContext;

/**
 * @author rflitcroft
 *
 */
@Ignore
public class ConfigDialogTest extends JFCTestCase {

    private ConfigDialog configDialog;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Choose the text Helper
        //setHelper(new JFCTestHelper()); // Uses the AWT Event Queue.
        setHelper(new RobotTestHelper()); // Uses the OS Event Queue.

        final HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put(DroidGlobalProperty.UPDATE_AUTO_CHECK.getName(), Boolean.FALSE);
        properties.put(DroidGlobalProperty.UPDATE_USE_PROXY.getName(), Boolean.FALSE);
        properties.put(DroidGlobalProperty.UPDATE_FREQUENCY_DAYS.getName(), 7);
        properties.put(DroidGlobalProperty.UPDATE_ON_STARTUP.getName(), Boolean.FALSE);
        properties.put(DroidGlobalProperty.UPDATE_AUTOSET_DEFAULT.getName(), Boolean.FALSE);
        
        DroidGlobalConfig globalConfig = mock(DroidGlobalConfig.class);
        when(globalConfig.getPropertiesMap()).thenReturn(properties);
        
        GlobalContext globalContext = mock(GlobalContext.class);
        when(globalContext.getGlobalConfig()).thenReturn(globalConfig);
        
        configDialog = new ConfigDialog(null, globalContext);
        configDialog.setModal(false);
        configDialog.setVisible(true);
    }

    @Override
    protected void tearDown() throws Exception {
        configDialog = null;
        JFCTestHelper.cleanUp(this);
        super.tearDown();
    }

    @Test
    public void testChangeDefaultThrottleAndApprove() {
        
        final JFormattedTextField defaultThrottleTextBox = configDialog.getDefaultThrottleTextBox();
        getHelper().sendString(new StringEventData(this, defaultThrottleTextBox, "999"));

        final JButton okButton = configDialog.getOkButton();
        getHelper().enterClickAndLeave(new MouseEventData(this, okButton));        
        
        Map<String, Object> changedProperties = configDialog.getGlobalConfig();
        assertEquals(999L, changedProperties.get(DroidGlobalProperty.DEFAULT_THROTTLE.getName()));
        assertEquals(ConfigDialog.OK, configDialog.getResponse());
        
    }

    @Test
    public void testChangeDefaultThrottleAndCancel() {
        final JFormattedTextField defaultThrottleTextBox = configDialog.getDefaultThrottleTextBox();
        getHelper().sendString(new StringEventData(this, defaultThrottleTextBox, "999"));

        final JButton cancelButton = configDialog.getCancelButton();
        getHelper().enterClickAndLeave(new MouseEventData(this, cancelButton));        
        
        Map<String, Object> changedProperties = configDialog.getGlobalConfig();
        assertEquals(999L, changedProperties.get(DroidGlobalProperty.DEFAULT_THROTTLE.getName()));
        assertEquals(ConfigDialog.CANCEL, configDialog.getResponse());
    }
}
