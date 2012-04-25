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

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author rflitcroft
 *
 */
@Ignore
public class ResourceSelectorTest {

    @Test
    public void testResourceSelector() throws Exception {
        
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        ResourceSelectorDialog selector = new ResourceSelectorDialog(null);
        selector.setModal(true);
        selector.setVisible(true);
    }
}
