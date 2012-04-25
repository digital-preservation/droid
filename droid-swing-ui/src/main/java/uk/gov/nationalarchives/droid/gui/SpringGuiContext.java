/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalConfig;
import uk.gov.nationalarchives.droid.gui.action.ActionFactory;
import uk.gov.nationalarchives.droid.profile.ProfileManager;
import uk.gov.nationalarchives.droid.report.interfaces.ReportManager;

/**
 * @author rflitcroft
 *
 */
public class SpringGuiContext implements GlobalContext {

    private ClassPathXmlApplicationContext context;
    
    /**
     * Default constructor.
     */
    public SpringGuiContext() {
        context = new ClassPathXmlApplicationContext("classpath*:/META-INF/gui-spring.xml");
    }
    
    /**
     * 
     * @return the singleton action factory
     */
    public ActionFactory getActionFactory() {
        return (ActionFactory) context.getBean("actionFactory");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public DroidGlobalConfig getGlobalConfig() {
        return (DroidGlobalConfig) context.getBean("globalConfig");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProfileManager getProfileManager() {
        return (ProfileManager) context.getBean("profileManager");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ReportManager getReportManager() {
        return (ReportManager) context.getBean("reportManager");
    }
    
}
