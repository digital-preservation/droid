/**
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gov.uk>
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

import org.springframework.context.support.AbstractXmlApplicationContext;
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
public final class SpringUiContext implements GlobalContext {

    private final AbstractXmlApplicationContext context;
    
    /**
     * private constructor.
     */
    private SpringUiContext(AbstractXmlApplicationContext context) {
        this.context = context;
    }

    /**
     * Factory method to create instance of GlobalContext. Global context IS NOT singleton.
     * @return Instance of GlobalContext.
     */
    public static GlobalContext getInstance() {
        AbstractXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath*:/META-INF/ui-spring.xml");
        context.registerShutdownHook();

        return new SpringUiContext(context);
    }

    @Override
    public DroidGlobalConfig getGlobalConfig() {
        return context.getBean("globalConfig", DroidGlobalConfig.class);
    }

    @Override
    public ProfileRunCommand getProfileRunCommand() {
        return context.getBean("profileRunCommand", ProfileRunCommand.class);
    }

    @Override
    public NoProfileRunCommand getNoProfileRunCommand() {
        return context.getBean("noProfileRunCommand", NoProfileRunCommand.class);
    }

    @Override
    public ExportCommand getExportCommand(ExportOptions opt) {
        ExportCommand command = context.getBean("exportCommand", ExportCommand.class);
        command.setExportOptions(opt);
        return command;
    }

    @Override
    public ReportCommand getReportCommand() {
        return context.getBean("reportCommand", ReportCommand.class);
    }
    
    @Override
    public CheckSignatureUpdateCommand getCheckSignatureUpdateCommand() {
        return context.getBean("checkSignatureUpdateCommand", CheckSignatureUpdateCommand.class);
    }
    
    @Override
    public DownloadSignatureUpdateCommand getDownloadSignatureUpdateCommand() {
        return (DownloadSignatureUpdateCommand) context.getBean("downloadSignatureUpdateCommand", DownloadSignatureUpdateCommand.class);
    }
    
    @Override
    public DisplayDefaultSignatureFileVersionCommand getDisplayDefaultSignatureFileVersionCommand() {
        return context.getBean("displayDefaultSignatureVersion", DisplayDefaultSignatureFileVersionCommand.class);
    }
    
    @Override
    public ConfigureDefaultSignatureFileVersionCommand getConfigureDefaultSignatureFileVersionCommand() {
        return context.getBean("configureDefaultSignatureVersion", ConfigureDefaultSignatureFileVersionCommand.class);
    }
    
    @Override
    public ListAllSignatureFilesCommand getListAllSignatureFilesCommand() {
        return context.getBean("listAllSignatureFilesCommand", ListAllSignatureFilesCommand.class);
    }
    
    @Override
    public void close() {
        context.close();
    }

    @Override
    public ListReportsCommand getListReportsCommand() {
        return context.getBean("listReportsCommand", ListReportsCommand.class);
    }

}
