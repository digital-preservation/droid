/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui.widgetwrapper;

import java.util.List;

import uk.gov.nationalarchives.droid.gui.ProfileForm;

/**
 * Interface for profile selection dialogs.
 * @author rflitcroft
 *
 */
public interface ProfileSelectionDialog extends JOptionPaneProxy {

    /**
     * @return the selected profiles
     */
    List<ProfileForm> getSelectedProfiles();
    
    /**
     * Opens the dialog.
     */
    void open();
    
}
