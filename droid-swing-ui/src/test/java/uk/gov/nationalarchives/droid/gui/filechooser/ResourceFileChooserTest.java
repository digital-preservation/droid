/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui.filechooser;

import javax.swing.UIManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ResourceFileChooserTest {

    private ResourceFileChooser fc;
    
    @Before
    public void setup() throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        //UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
        
        fc = new ResourceFileChooser();
    }
    
    @Test
    public void testFileChooserSetup() {
        
        assertTrue(fc.isAcceptAllFileFilterUsed());
        assertEquals("All Files", fc.getFileFilter().getDescription());
        assertTrue(fc.isMultiSelectionEnabled());
    }
    
    @Test
    @Ignore
    public void testGui() {
        
        fc.showOpenDialog(null);
        
    }
}
