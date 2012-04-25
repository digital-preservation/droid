/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui.filechooser;

import java.io.File;

import javax.swing.JFileChooser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ProfileFileChooserTest {

    private ProfileFileChooser fc;
    
    @Before
    public void setup() {
        fc = new ProfileFileChooser();
    }
    
    @Test
    public void testFileChooserSetup() {
        
        assertTrue(fc.isAcceptAllFileFilterUsed());
        assertEquals("DROID 6 profile", fc.getFileFilter().getDescription());
        
        assertTrue(fc.getFileFilter().accept(new File("foo.droid")));
        assertFalse(fc.getFileFilter().accept(new File("foo.txt")));
        assertFalse(fc.getFileFilter().accept(new File("foo")));
        
        assertFalse(fc.isMultiSelectionEnabled());
    }
    
    @Test
    @Ignore
    public void testApproveWhenFileExists() {
        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        fc.setSelectedFile(new File("/"));
        
        fc.approveSelection();
        
    }
}
