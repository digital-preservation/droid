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
package uk.gov.nationalarchives.droid.command.action;

import java.io.PrintWriter;
import java.util.Locale;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.tree.OverrideCombiner;
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
        final boolean bom = cli.hasOption(CommandLineParam.BOM.toString());

        cmd.setDestination(destination);
        cmd.setProfiles(profiles);
        cmd.setBom(bom);

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
        final boolean bom = cli.hasOption(CommandLineParam.BOM.toString());
        cmd.setDestination(destination);
        cmd.setProfiles(profiles);
        cmd.setBom(bom);

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

    @Override
    public FilterFieldCommand getFilterFieldCommand() {
        return new FilterFieldCommand(printWriter);
    }

    @Override
    public DroidCommand getHelpCommand() {
        return new HelpCommand(printWriter);
    }

    @Override
    public DroidCommand getVersionCommand() {
        return new VersionCommand(printWriter);
    }

    @Override
    public DroidCommand getProfileCommand(final CommandLine cli) throws CommandLineSyntaxException {
        String[] resources = getResources(cli);
        PropertiesConfiguration overrides = getOverrideProperties(cli);

        final String destination;
        // Determine if destination is to a database profile, or to a csv file output:
        if (cli.hasOption(CommandLineParam.PROFILES.getLongName())) {
            final String[] destinations = cli.getOptionValues(CommandLineParam.PROFILES.toString());
            if (destinations == null || destinations.length > 1) {
                throw new CommandLineSyntaxException("Must specify exactly one profile.");
            }
            destination = destinations[0];
        } else { // output to a file, or the console if not specified.
            destination = cli.hasOption(CommandLineParam.OUTPUT_FILE.getLongName())?
                          cli.getOptionValue(CommandLineParam.OUTPUT_FILE.toString()) : "stdout";
            if (overrides == null) {
                overrides = new PropertiesConfiguration();
            }
            overrides.setProperty("profile.outputFilePath", destination);
        }

        final ProfileRunCommand command = context.getProfileRunCommand();
        command.setResources(resources);
        command.setDestination(destination);
        command.setRecursive(cli.hasOption(CommandLineParam.RECURSIVE.toString()));
        command.setProperties(overrides);
        command.setContainerSignatureFile(cli.getOptionValue(CommandLineParam.CONTAINER_SIGNATURE_FILE.toString()));
        command.setSignatureFile(cli.getOptionValue(CommandLineParam.SIGNATURE_FILE.toString()));
        return command;
    }

    private String[] getResources(final CommandLine cli) throws CommandLineSyntaxException {
        String[] resources = cli.getOptionValues(CommandLineParam.RUN_PROFILE.toString());
        if (resources == null || resources.length == 0) {
            resources = cli.getArgs(); // if no profile resources specified, use unbound arguments:
            if (resources == null || resources.length == 0)
                throw new CommandLineSyntaxException(NO_RESOURCES_SPECIFIED);
        }
        return resources;
    }

    private PropertiesConfiguration getOverrideProperties(CommandLine cli) throws CommandLineSyntaxException {
        PropertiesConfiguration overrideProperties = null;

        // Get properties from a file:
        final String propertyFile = cli.getOptionValue(CommandLineParam.PROPERTY_FILE.toString());
        if (propertyFile != null && !propertyFile.isEmpty()) {
            try {
                overrideProperties = new PropertiesConfiguration(propertyFile);
            } catch (ConfigurationException e) {
                throw new CommandLineSyntaxException(e);
            }
        }

        // Get properties from the command line directly:
        final String[] propertyOverrides = cli.getOptionValues(CommandLineParam.PROFILE_PROPERTY.toString());
        if (propertyOverrides != null && propertyOverrides.length > 0) {
            PropertiesConfiguration commandLineProperties = createProperties(propertyOverrides);
            if (overrideProperties == null) {
                overrideProperties = commandLineProperties;
            } else {
                CombinedConfiguration combined = new CombinedConfiguration();
                combined.setNodeCombiner(new OverrideCombiner());
                combined.addConfiguration(commandLineProperties);
                combined.addConfiguration(overrideProperties);
                PropertiesConfiguration merged = new PropertiesConfiguration();
                merged.append(combined);
                overrideProperties = merged;
            }
        }

        // Special command line flags for archive processing which override all the others:
        if (cli.hasOption(CommandLineParam.ARCHIVES.toString())) { // Turn on all archives:
            setAllArchivesExpand(overrideProperties,true);
        } else if (cli.hasOption(CommandLineParam.ARCHIVE_TYPES.toString())) {
            setExpandArchiveTypes(overrideProperties, cli.getOptionValues(CommandLineParam.ARCHIVE_TYPES.toString()));
        }

        if (cli.hasOption(CommandLineParam.WEB_ARCHIVES.toString())) { // Turn on all web archives:
            setAllWebArchivesExpand(overrideProperties, true);
        } else if (cli.hasOption(CommandLineParam.WEB_ARCHIVE_TYPES.toString())) {
            setExpandWebArchiveTypes(overrideProperties, cli.getOptionValues(CommandLineParam.WEB_ARCHIVE_TYPES.toString()));
        }
        return overrideProperties;
    }

    private void setAllArchivesExpand(PropertiesConfiguration overrideProperties, boolean onOrOff) {
        overrideProperties.setProperty("profile.processTar", onOrOff);
        overrideProperties.setProperty("profile.processZip", onOrOff);
        overrideProperties.setProperty("profile.processGzip", onOrOff);
        overrideProperties.setProperty("profile.processRar", onOrOff);
        overrideProperties.setProperty("profile.process7zip", onOrOff);
        overrideProperties.setProperty("profile.processIso", onOrOff);
        overrideProperties.setProperty("profile.processBzip2", onOrOff);
    }

    private void setAllWebArchivesExpand(PropertiesConfiguration overrideProperties, boolean OnOrOff) {
        overrideProperties.setProperty("profile.processArc", OnOrOff);
        overrideProperties.setProperty("profile.processWarc", OnOrOff);
    }

    private void setExpandArchiveTypes(PropertiesConfiguration overrideProperties, String[] archiveTypes) {
        setAllArchivesExpand(overrideProperties, false);
        for (String archiveType : archiveTypes) {
            overrideProperties.setProperty(getArchivePropertyName(archiveType), true);
        }
    }

    private void setExpandWebArchiveTypes(PropertiesConfiguration overrideProperties, String[] webArchiveTypes) {
        setAllWebArchivesExpand(overrideProperties, false);
        for (String archiveType : webArchiveTypes) {
            overrideProperties.setProperty(getArchivePropertyName(archiveType), true);
        }
    }

    private String getArchivePropertyName(String archiveType) {
        return "profile.process" + archiveType.substring(0, 1).toUpperCase(Locale.ROOT) + archiveType.substring(1).toLowerCase(Locale.ROOT);
    }

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
        command.setExpandAllArchives(cli.hasOption(CommandLineParam.ARCHIVES.toString()));
        command.setExpandArchiveTypes(cli.getOptionValues(CommandLineParam.ARCHIVE_TYPES.toString()));
        command.setExpandAllWebArchives(cli.hasOption(CommandLineParam.WEB_ARCHIVES.toString()));
        command.setExpandWebArchiveTypes(cli.getOptionValues(CommandLineParam.WEB_ARCHIVE_TYPES.toString()));
        command.setExtensionFilter(extensions);
        command.setQuiet(cli.hasOption(CommandLineParam.QUIET.toString()));

        return command;
    }

    @Override
    public DroidCommand getCheckSignatureUpdateCommand() {
        final CheckSignatureUpdateCommand command = context.getCheckSignatureUpdateCommand();
        command.setPrintWriter(printWriter);
        return command;
    }

    @Override
    public DroidCommand getDownloadSignatureUpdateCommand() {
        final DownloadSignatureUpdateCommand command = context.getDownloadSignatureUpdateCommand();
        command.setPrintWriter(printWriter);
        return command;
    }

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
     * @throws CommandLineSyntaxException on bad syntax in command
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

    @Override
    public DroidCommand getListAllSignatureVersionsCommand() {
        final ListAllSignatureFilesCommand command = context.getListAllSignatureFilesCommand();
        command.setPrintWriter(printWriter);
        return command;
    }

    @Override
    public DroidCommand getListReportCommand() {
        final ListReportsCommand command = context.getListReportsCommand();
        command.setPrintWriter(printWriter);
        return command;
    }

    private PropertiesConfiguration createProperties(String[] properties) {
        PropertiesConfiguration result = new PropertiesConfiguration();
        for (String property : properties) {
            addProperty(property, result);
        }
        return result;
    }

    private void addProperty(String property, PropertiesConfiguration properties) {
        final int separator = property.indexOf('=');
        if (separator > 0) {
            String key = property.substring(0, separator);
            String value = property.substring(separator + 1);
            properties.addProperty(key, value);
        }
    }
}
