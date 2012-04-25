/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui;

import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalConfig;
import uk.gov.nationalarchives.droid.gui.action.ActionFactory;
import uk.gov.nationalarchives.droid.profile.ProfileManager;
import uk.gov.nationalarchives.droid.report.interfaces.ReportManager;

/**
 * @author rflitcroft
 *
 */
public interface GlobalContext {

    /**
     * 
     * @return the singleton action factory
     */
    ActionFactory getActionFactory();
    
    /**
     * 
     * @return the droid global config
     */
    DroidGlobalConfig getGlobalConfig();

    /**
     * @return the profile manager
     */
    ProfileManager getProfileManager();
    
    /**
     * @return the report manager
     */
    ReportManager getReportManager();
}
