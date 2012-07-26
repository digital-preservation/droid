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
package uk.gov.nationalarchives.droid.command.context;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import uk.gov.nationalarchives.droid.command.action.CheckSignatureUpdateCommand;
import uk.gov.nationalarchives.droid.command.action.ConfigureDefaultSignatureFileVersionCommand;
import uk.gov.nationalarchives.droid.command.action.DisplayDefaultSignatureFileVersionCommand;
import uk.gov.nationalarchives.droid.command.action.DownloadSignatureUpdateCommand;
import uk.gov.nationalarchives.droid.command.action.ExportCommand;
import uk.gov.nationalarchives.droid.command.action.ListAllSignatureFilesCommand;
import uk.gov.nationalarchives.droid.command.action.ListReportsCommand;
import uk.gov.nationalarchives.droid.command.action.NoProfileRunCommand;
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
    public NoProfileRunCommand getNoProfileRunCommand() {
        return (NoProfileRunCommand) getContext().getBean("noProfileRunCommand");
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
