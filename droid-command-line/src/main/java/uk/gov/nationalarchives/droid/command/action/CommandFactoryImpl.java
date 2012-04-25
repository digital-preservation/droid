/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.command.action;

import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang.StringUtils;

import uk.gov.nationalarchives.droid.command.FilterFieldCommand;
import uk.gov.nationalarchives.droid.command.context.GlobalContext;
import uk.gov.nationalarchives.droid.command.filter.CommandLineFilter;
import uk.gov.nationalarchives.droid.command.filter.CommandLineFilter.FilterType;
import uk.gov.nationalarchives.droid.export.interfaces.ExportOptions;

/**
 * @author rflitcroft, Alok Kumar Dash
 *
 */
public class CommandFactoryImpl implements CommandFactory {
    
    /**
     * 
     */
    private static final String NO_PROFILES_SPECIFIED_FOR_EXPORT = "No profiles specified for export.";
    private GlobalContext context;
    private PrintWriter printWriter;

    /**
     * 
     * @param context the global context.
     * @param printWriter a print writer
     */
    public CommandFactoryImpl(GlobalContext context, PrintWriter printWriter) {
        this.context = context;
        this.printWriter = printWriter;
    }
    
    /**
     * @param cli the command line
     * @throws CommandLineSyntaxException command parse exception. 
     * @return an export command
     */
    public DroidCommand getExportFileCommand(CommandLine cli) throws CommandLineSyntaxException {
        
        if (!cli.hasOption(CommandLineParam.PROFILES.toString())) {
            throw new CommandLineSyntaxException(NO_PROFILES_SPECIFIED_FOR_EXPORT);
        }
        
        String destination = cli.getOptionValue(CommandLineParam.EXPORT_ONE_ROW_PER_FILE.toString());
        
        String[] profiles = cli.getOptionValues(CommandLineParam.PROFILES.toString());
        ExportCommand cmd = context.getExportCommand(ExportOptions.ONE_ROW_PER_FILE);
        cmd.setDestination(destination);
        cmd.setProfiles(profiles);
        
        if (cli.hasOption(CommandLineParam.ALL_FILTER.toString())) {
            cmd.setFilter(new CommandLineFilter(cli.getOptionValues(
                    CommandLineParam.ALL_FILTER.toString()), FilterType.ALL));
        }
        
        if (cli.hasOption(CommandLineParam.ANY_FILTER.toString())) {
            cmd.setFilter(new CommandLineFilter(cli.getOptionValues(
                    CommandLineParam.ANY_FILTER.toString()), FilterType.ANY));
        }

        return cmd;
    }
    

    /**
     * @param cli the command line
     * @throws CommandLineSyntaxException command parse exception. 
     * @return an export command
     */
    public DroidCommand getExportFormatCommand(CommandLine cli) throws CommandLineSyntaxException {
        
        if (!cli.hasOption(CommandLineParam.PROFILES.toString())) {
            throw new CommandLineSyntaxException(NO_PROFILES_SPECIFIED_FOR_EXPORT);
        }
        
        String destination = cli.getOptionValue(CommandLineParam.EXPORT_ONE_ROW_PER_FORMAT.toString());
        
        String[] profiles = cli.getOptionValues(CommandLineParam.PROFILES.toString());
        ExportCommand cmd = context.getExportCommand(ExportOptions.ONE_ROW_PER_FORMAT);
        cmd.setDestination(destination);
        cmd.setProfiles(profiles);
        
        if (cli.hasOption(CommandLineParam.ALL_FILTER.toString())) {
            cmd.setFilter(new CommandLineFilter(cli.getOptionValues(
                    CommandLineParam.ALL_FILTER.toString()), FilterType.ALL));
        }
        
        if (cli.hasOption(CommandLineParam.ANY_FILTER.toString())) {
            cmd.setFilter(new CommandLineFilter(cli.getOptionValues(
                    CommandLineParam.ANY_FILTER.toString()), FilterType.ANY));
        }

        return cmd;
    }
    
    
    /**
     * @param cli the command line
     * @throws CommandLineSyntaxException command parse exception. 
     * @return an report command
     */
    public DroidCommand getReportCommand(CommandLine cli) throws CommandLineSyntaxException {
        
        if (!cli.hasOption(CommandLineParam.PROFILES.toString())) {
            throw new CommandLineSyntaxException(
                    "No profiles specified for report.");
        }
        if (!cli.hasOption(CommandLineParam.REPORT_NAME.toString())) {
            throw new CommandLineSyntaxException("No name specified for report.");
        } 

        String reportType = cli.getOptionValue(CommandLineParam.REPORT_NAME.toString());
        String reportOutputType = cli.getOptionValue(CommandLineParam.REPORT_OUTPUT_TYPE.toString());
        reportOutputType = reportOutputType == null ? "pdf" : reportOutputType;
        String destination = cli.getOptionValue(CommandLineParam.REPORT.toString());
        String[] profiles = cli.getOptionValues(CommandLineParam.PROFILES.toString());
        ReportCommand cmd = context.getReportCommand();
        cmd.setDestination(destination);
        cmd.setProfiles(profiles);
        cmd.setReportType(reportType);
        cmd.setReportOutputType(reportOutputType);

        if (cli.hasOption(CommandLineParam.ALL_FILTER.toString())) {
            cmd.setFilter(new CommandLineFilter(cli.getOptionValues(
                    CommandLineParam.ALL_FILTER.toString()), FilterType.ALL));
        }
        
        if (cli.hasOption(CommandLineParam.ANY_FILTER.toString())) {
            cmd.setFilter(new CommandLineFilter(cli.getOptionValues(
                    CommandLineParam.ANY_FILTER.toString()), FilterType.ANY));
        }
        
        return cmd;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public FilterFieldCommand getFilterFieldCommand() {
        return new FilterFieldCommand(printWriter);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public DroidCommand getHelpCommand() {
        return new HelpCommand(printWriter);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public DroidCommand getVersionCommand() {
        return new VersionCommand(printWriter);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public DroidCommand getProfileCommand(CommandLine cli) throws CommandLineSyntaxException {
        String[] resources = cli.getOptionValues(CommandLineParam.RUN_PROFILE.toString());
        if (resources.length == 0) {
            throw new CommandLineSyntaxException("No resources specified.");
        }
        
        String[] destination = cli.getOptionValues(CommandLineParam.PROFILES.toString());
        if (destination == null || destination.length > 1) {
            throw new CommandLineSyntaxException("Must specifiy exactly one profile.");
        }
        
        ProfileRunCommand command = context.getProfileRunCommand();
        command.setDestination(destination[0]);
        command.setResources(resources);
        
        command.setRecursive(cli.hasOption(CommandLineParam.RECURSIVE.toString()));
        
        return command;
        
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public DroidCommand getCheckSignatureUpdateCommand() {
        CheckSignatureUpdateCommand command = context.getCheckSignatureUpdateCommand();
        command.setPrintWriter(printWriter);
        return command;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public DroidCommand getDownloadSignatureUpdateCommand() {
        DownloadSignatureUpdateCommand command = context.getDownloadSignatureUpdateCommand();
        command.setPrintWriter(printWriter);
        return command;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public DroidCommand getDisplayDefaultSignatureVersionCommand() {
        DisplayDefaultSignatureFileVersionCommand command = context.getDisplayDefaultSignatureFileVersionCommand();
        command.setPrintWriter(printWriter);
        return command;
    }
    
    /**
     * {@inheritDoc}
     * @throws CommandLineSyntaxException 
     */
    @Override
    public DroidCommand getConfigureDefaultSignatureVersionCommand(CommandLine cli) throws CommandLineException {
        String newVersion = cli.getOptionValue(CommandLineParam.CONFIGURE_DEFAULT_SIGNATURE_VERSION.toString());
        ConfigureDefaultSignatureFileVersionCommand command = 
            context.getConfigureDefaultSignatureFileVersionCommand();
        command.setPrintWriter(printWriter);
        try {
            command.setSignatureFileVersion(Integer.parseInt(StringUtils.trimToEmpty(newVersion)));
            return command;
        } catch (NumberFormatException e) {
            throw new CommandLineSyntaxException("Invalid version: " + newVersion);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public DroidCommand getListAllSignatureVersionsCommand() {
        ListAllSignatureFilesCommand command = context.getListAllSignatureFilesCommand();
        command.setPrintWriter(printWriter);
        return command;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DroidCommand getListReportCommand() {
        ListReportsCommand command = context.getListReportsCommand();
        command.setPrintWriter(printWriter);
        return command;
    }
}
