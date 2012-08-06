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

    private static final String NO_RESOURCES_SPECIFIED = "No resources specified.";
    private static final String NO_PROFILES_SPECIFIED_FOR_EXPORT = "No profiles specified for export.";
    private GlobalContext context;
    private PrintWriter printWriter;

    /**
     *
     * @param context the global context.
     * @param printWriter a print writer
     */
    public CommandFactoryImpl(final GlobalContext context,
        final PrintWriter printWriter) {
        
        this.context = context;
        this.printWriter = printWriter;
    }

   /**
    * @param cli the command line
    * @throws CommandLineSyntaxException command parse exception.
    * @return an export command
    */
    @Override
    public DroidCommand getExportFileCommand(final CommandLine cli) throws CommandLineSyntaxException {

        if (!cli.hasOption(CommandLineParam.PROFILES.toString())) {
            throw new CommandLineSyntaxException(NO_PROFILES_SPECIFIED_FOR_EXPORT);
        }

        final String destination = cli.getOptionValue(CommandLineParam.EXPORT_ONE_ROW_PER_FILE.toString());

        final String[] profiles = cli.getOptionValues(CommandLineParam.PROFILES.toString());
        final ExportCommand cmd = context.getExportCommand(ExportOptions.ONE_ROW_PER_FILE);
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
    @Override
    public DroidCommand getExportFormatCommand(final CommandLine cli) throws CommandLineSyntaxException {

        if (!cli.hasOption(CommandLineParam.PROFILES.toString())) {
            throw new CommandLineSyntaxException(NO_PROFILES_SPECIFIED_FOR_EXPORT);
        }

        final String destination = cli.getOptionValue(CommandLineParam.EXPORT_ONE_ROW_PER_FORMAT.toString());

        final String[] profiles = cli.getOptionValues(CommandLineParam.PROFILES.toString());
        final ExportCommand cmd = context.getExportCommand(ExportOptions.ONE_ROW_PER_FORMAT);
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
    @Override
    public DroidCommand getReportCommand(final CommandLine cli) throws CommandLineSyntaxException {

        if (!cli.hasOption(CommandLineParam.PROFILES.toString())) {
            throw new CommandLineSyntaxException(
                "No profiles specified for report.");
        }
        if (!cli.hasOption(CommandLineParam.REPORT_NAME.toString())) {
            throw new CommandLineSyntaxException("No name specified for report.");
        }

        final String reportType = cli.getOptionValue(CommandLineParam.REPORT_NAME.toString());
        String reportOutputType = cli.getOptionValue(CommandLineParam.REPORT_OUTPUT_TYPE.toString());
        reportOutputType = reportOutputType == null ? "pdf" : reportOutputType;
        final String destination = cli.getOptionValue(CommandLineParam.REPORT.toString());
        final String[] profiles = cli.getOptionValues(CommandLineParam.PROFILES.toString());
        final ReportCommand cmd = context.getReportCommand();
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
    public DroidCommand getProfileCommand(final CommandLine cli) throws CommandLineSyntaxException {
        final String[] resources = cli.getOptionValues(CommandLineParam.RUN_PROFILE.toString());
        if (resources.length == 0) {
            throw new CommandLineSyntaxException(NO_RESOURCES_SPECIFIED);
        }

        final String[] destination = cli.getOptionValues(CommandLineParam.PROFILES.toString());
        if (destination == null || destination.length > 1) {
            throw new CommandLineSyntaxException("Must specify exactly one profile.");
        }

        final ProfileRunCommand command = context.getProfileRunCommand();
        command.setDestination(destination[0]);
        command.setResources(resources);

        command.setRecursive(cli.hasOption(CommandLineParam.RECURSIVE.toString()));

        return command;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DroidCommand getNoProfileCommand(final CommandLine cli) throws CommandLineSyntaxException {
        final String[] resources = cli.getOptionValues(CommandLineParam.RUN_NO_PROFILE.toString());
        if (resources.length == 0) {
            throw new CommandLineSyntaxException(NO_RESOURCES_SPECIFIED);
        }
        if (!cli.hasOption(CommandLineParam.SIGNATURE_FILE.toString())) {
            throw new CommandLineSyntaxException("No signature file specified.");
        }

        final String signatureFile = cli.getOptionValue(CommandLineParam.SIGNATURE_FILE.toString());
        final String containerSignatureFile =
            cli.getOptionValue(CommandLineParam.CONTAINER_SIGNATURE_FILE.toString());        
        final String[] extensions = cli.getOptionValues(CommandLineParam.EXTENSION_LIST.toString());

        final NoProfileRunCommand command = context.getNoProfileRunCommand();
        command.setResources(resources);
        command.setSignatureFile(signatureFile);
        command.setContainerSignatureFile(containerSignatureFile);
        command.setRecursive(cli.hasOption(CommandLineParam.RECURSIVE.toString()));
        command.setArchives(cli.hasOption(CommandLineParam.ARCHIVES.toString()));
        command.setExtensionFilter(extensions);
        command.setQuiet(cli.hasOption(CommandLineParam.QUIET.toString()));

        return command;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DroidCommand getCheckSignatureUpdateCommand() {
        final CheckSignatureUpdateCommand command = context.getCheckSignatureUpdateCommand();
        command.setPrintWriter(printWriter);
        return command;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DroidCommand getDownloadSignatureUpdateCommand() {
        final DownloadSignatureUpdateCommand command = context.getDownloadSignatureUpdateCommand();
        command.setPrintWriter(printWriter);
        return command;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DroidCommand getDisplayDefaultSignatureVersionCommand() {
        final DisplayDefaultSignatureFileVersionCommand command = 
            context.getDisplayDefaultSignatureFileVersionCommand();
        command.setPrintWriter(printWriter);
        return command;
    }

    /**
     * {@inheritDoc}
     *
     * @throws CommandLineSyntaxException
     */
    @Override
    public DroidCommand getConfigureDefaultSignatureVersionCommand(final CommandLine cli) throws CommandLineException {
        final String newVersion = cli.getOptionValue(CommandLineParam.CONFIGURE_DEFAULT_SIGNATURE_VERSION.toString());
        final ConfigureDefaultSignatureFileVersionCommand command =
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
        final ListAllSignatureFilesCommand command = context.getListAllSignatureFilesCommand();
        command.setPrintWriter(printWriter);
        return command;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DroidCommand getListReportCommand() {
        final ListReportsCommand command = context.getListReportsCommand();
        command.setPrintWriter(printWriter);
        return command;
    }
}
