/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.command.context;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import uk.gov.nationalarchives.droid.command.action.CheckSignatureUpdateCommand;
import uk.gov.nationalarchives.droid.command.action.ConfigureDefaultSignatureFileVersionCommand;
import uk.gov.nationalarchives.droid.command.action.DisplayDefaultSignatureFileVersionCommand;
import uk.gov.nationalarchives.droid.command.action.DownloadSignatureUpdateCommand;
import uk.gov.nationalarchives.droid.command.action.ExportCommand;
import uk.gov.nationalarchives.droid.command.action.ListAllSignatureFilesCommand;
import uk.gov.nationalarchives.droid.command.action.ListReportsCommand;
import uk.gov.nationalarchives.droid.command.action.ProfileRunCommand;
import uk.gov.nationalarchives.droid.command.action.ReportCommand;
import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalConfig;
import uk.gov.nationalarchives.droid.export.interfaces.ExportOptions;

/**
 * The evil singleton Spring Application context.
 * @author rflitcroft
 *
 */
public class SpringUiContext implements GlobalContext {

    private static boolean open;
    
    /**
     * Default constructor.
     */
    public SpringUiContext() { }

    /**
     * Evil lazy singleton holder for the Spring context.
     * @author rflitcroft
     *
     */
    private static final class LazyHolder {
        private static final ClassPathXmlApplicationContext CONTEXT;
        
        static {
            CONTEXT = new ClassPathXmlApplicationContext("classpath*:/META-INF/ui-spring.xml");
            CONTEXT.registerShutdownHook();
            open = true;
        }
        
        private LazyHolder() { }
    }
    
    private static ClassPathXmlApplicationContext getContext() {
        
        return LazyHolder.CONTEXT;
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public DroidGlobalConfig getGlobalConfig() {
        return (DroidGlobalConfig) getContext().getBean("globalConfig");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProfileRunCommand getProfileRunCommand() {
        return (ProfileRunCommand) getContext().getBean("profileRunCommand");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExportCommand getExportCommand(ExportOptions opt) {
        ExportCommand command = (ExportCommand) getContext().getBean("exportCommand");
        command.setExportOptions(opt);
        return command;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ReportCommand getReportCommand() {
        return (ReportCommand) getContext().getBean("reportCommand");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public CheckSignatureUpdateCommand getCheckSignatureUpdateCommand() {
        return (CheckSignatureUpdateCommand) getContext().getBean("checkSignatureUpdateCommand");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public DownloadSignatureUpdateCommand getDownloadSignatureUpdateCommand() {
        return (DownloadSignatureUpdateCommand) getContext().getBean("downloadSignatureUpdateCommand");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public DisplayDefaultSignatureFileVersionCommand getDisplayDefaultSignatureFileVersionCommand() {
        return (DisplayDefaultSignatureFileVersionCommand) getContext()
            .getBean("displayDefaultSignatureVersion");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ConfigureDefaultSignatureFileVersionCommand getConfigureDefaultSignatureFileVersionCommand() {
        return (ConfigureDefaultSignatureFileVersionCommand) getContext()
            .getBean("configureDefaultSignatureVersion");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ListAllSignatureFilesCommand getListAllSignatureFilesCommand() {
        return (ListAllSignatureFilesCommand) getContext().getBean("listAllSignatureFilesCommand");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        if (open) {
            getContext().close();
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ListReportsCommand getListReportsCommand() {
        return (ListReportsCommand) getContext().getBean("listReportsCommand");
    }

}
