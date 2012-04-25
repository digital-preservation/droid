/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui.config;

import org.junit.Ignore;

/**
 * @author rflitcroft
 *
 */
@Ignore
public class ConfigActionTest {

//    private ConfigAction configAction;
//    private ConfigDialog configDialog;
//    private DroidGlobalConfig globalConfig;
//    
//    @Before
//    public void setup() {
//        configDialog = mock(ConfigDialog.class);
//        globalConfig = mock(DroidGlobalConfig.class);
//        configAction = new ConfigAction(configDialog, globalConfig);
//    }
//    
//    @Test
//    public void testApproveUpdatesConfiguration() throws Exception {
//        
//        Map<String, Object> changedProperties = new HashMap<String, Object>();
//        changedProperties.put(DroidGlobalProperty.DEFAULT_THROTTLE.getName(), "123");
//        
//        when(configDialog.getResponse()).thenReturn(ConfigDialog.OK);
//        when(configDialog.getGlobalConfig()).thenReturn(changedProperties);
//        
//        configAction.execute();
//        
//        verify(globalConfig).update(changedProperties);
//        
//    }
//
//    @Test
//    public void testCancelDoesNotUpdateConfiguration() throws Exception {
//        
//        Map<String, Object> changedProperties = new HashMap<String, Object>();
//        changedProperties.put(DroidGlobalProperty.DEFAULT_THROTTLE.getName(), "123");
//        
//        when(configDialog.getResponse()).thenReturn(ConfigDialog.CANCEL);
//        when(configDialog.getGlobalConfig()).thenReturn(changedProperties);
//        configAction.execute();
//        
//        verify(globalConfig, never()).update(changedProperties);
//        
//    }
}
