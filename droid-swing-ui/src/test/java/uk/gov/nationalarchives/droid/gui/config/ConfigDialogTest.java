/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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
