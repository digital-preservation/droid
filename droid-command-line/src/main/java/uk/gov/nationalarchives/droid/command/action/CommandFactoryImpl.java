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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.tree.OverrideCombiner;
import org.apache.commons.lang.StringUtils;

import uk.gov.nationalarchives.droid.command.context.GlobalContext;
import uk.gov.nationalarchives.droid.command.filter.DqlCriterionFactory;
import uk.gov.nationalarchives.droid.command.filter.DqlFilterParser;
import uk.gov.nationalarchives.droid.core.interfaces.ResourceType;
import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalProperty;
import uk.gov.nationalarchives.droid.core.interfaces.filter.BasicFilter;
import uk.gov.nationalarchives.droid.core.interfaces.filter.BasicFilterCriterion;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionFieldEnum;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionOperator;
import uk.gov.nationalarchives.droid.core.interfaces.filter.Filter;
import uk.gov.nationalarchives.droid.core.interfaces.filter.FilterCriterion;
import uk.gov.nationalarchives.droid.export.interfaces.ExportOptions;

/**
 * @author rflitcroft, Alok Kumar Dash
 *
 */
//CHECKSTYLE:OFF - ClassDataAbstractionCoupling and ClassFanOutComplexity just over limit.
public class CommandFactoryImpl implements CommandFactory {
    //CHECKSTYLE:ON

    private static final String NO_RESOURCES_SPECIFIED = "No resources specified.";
    private static final String NO_PROFILES_SPECIFIED_FOR_EXPORT = "No profiles specified for export.";
    private static final String PROFILE_PREFIX = "profile.";
    private static final String FILE_EXT_FIELD = "file_ext";
    private static final String ANY_OPERATOR = "any";
    private static final String SPACE = " ";

    private final GlobalContext context;
    private final PrintWriter printWriter;

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
        cmd.setQuoteAllFields(!cli.hasOption(CommandLineParam.QUOTE_COMMAS.getLongName()));

        if (cli.hasOption(CommandLineParam.COLUMNS_TO_WRITE.getLongName())) {
            String columns = String.join(SPACE, cli.getOptionValues(CommandLineParam.COLUMNS_TO_WRITE.getLongName()));
            cmd.setColumnsToWrite(columns);
        }

        if (cli.hasOption(CommandLineParam.ALL_FILTER.toString())) {
            cmd.setFilter(createFilter(cli.getOptionValues(CommandLineParam.ALL_FILTER.toString()), true));
        }

        if (cli.hasOption(CommandLineParam.ANY_FILTER.toString())) {
            cmd.setFilter(createFilter(cli.getOptionValues(CommandLineParam.ANY_FILTER.toString()), false));
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
        cmd.setQuoteAllFields(!cli.hasOption(CommandLineParam.QUOTE_COMMAS.getLongName()));

        if (cli.hasOption(CommandLineParam.COLUMNS_TO_WRITE.getLongName())) {
            String columns = String.join(SPACE, cli.getOptionValues(CommandLineParam.COLUMNS_TO_WRITE.getLongName()));
            cmd.setColumnsToWrite(columns);
        }

        if (cli.hasOption(CommandLineParam.ALL_FILTER.toString())) {
            cmd.setFilter(createFilter(cli.getOptionValues(CommandLineParam.ALL_FILTER.toString()), true));
        }

        if (cli.hasOption(CommandLineParam.ANY_FILTER.toString())) {
            cmd.setFilter(createFilter(cli.getOptionValues(CommandLineParam.ANY_FILTER.toString()), false));
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
            cmd.setFilter(createFilter(cli.getOptionValues(CommandLineParam.ALL_FILTER.toString()), true));
        }

        if (cli.hasOption(CommandLineParam.ANY_FILTER.toString())) {
            cmd.setFilter(createFilter(cli.getOptionValues(CommandLineParam.ANY_FILTER.toString()), false));
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
        final ProfileRunCommand command = context.getProfileRunCommand();
        PropertiesConfiguration overrides = getOverrideProperties(cli);
        command.setResources(getResources(cli));
        command.setDestination(getDestination(cli, overrides)); // will also set the output csv file in overrides if present.
        command.setRecursive(cli.hasOption(CommandLineParam.RECURSIVE.toString()));
        command.setProperties(overrides); // must be called after we set destination.
        command.setContainerSignatureFile(cli.getOptionValue(CommandLineParam.CONTAINER_SIGNATURE_FILE.toString()));
        command.setSignatureFile(cli.getOptionValue(CommandLineParam.SIGNATURE_FILE.toString()));
        command.setResultsFilter(getResultsFilter(cli));
        command.setIdentificationFilter(getIdentificationFilter(cli));
        return command;
    }

    @Override
    public DroidCommand getNoProfileCommand(final CommandLine cli) throws CommandLineSyntaxException {
        final ProfileRunCommand command = context.getProfileRunCommand();
        PropertiesConfiguration overrides = getOverrideProperties(cli);
        overrides.setProperty(DroidGlobalProperty.QUOTE_ALL_FIELDS.getName(), false);
        overrides.setProperty(DroidGlobalProperty.COLUMNS_TO_WRITE.getName(), "NAME PUID");
        command.setResources(getNoProfileResources(cli));
        command.setDestination(getDestination(cli, overrides)); // will also set the output csv file in overrides if present.
        command.setRecursive(cli.hasOption(CommandLineParam.RECURSIVE.toString()));
        command.setProperties(overrides); // must be called after we set destination.
        command.setContainerSignatureFile(cli.getOptionValue(CommandLineParam.CONTAINER_SIGNATURE_FILE.toString()));
        command.setSignatureFile(cli.getOptionValue(CommandLineParam.SIGNATURE_FILE.toString()));
        command.setResultsFilter(getFileOnlyResultsFilter());
        command.setIdentificationFilter(getIdentificationFilter(cli));
        return command;
    }

    private Filter getFileOnlyResultsFilter() {
        Object[] filterValue = new Object[]{ResourceType.FOLDER};
        FilterCriterion criterion = new BasicFilterCriterion(CriterionFieldEnum.RESOURCE_TYPE, CriterionOperator.NONE_OF, filterValue);
        return new BasicFilter(criterion);
    }



    private String getDestination(CommandLine cli, PropertiesConfiguration overrideProperties) throws CommandLineSyntaxException {
        final String destination;
        // Determine if destination is to a database profile, or to a csv file output:
        if (cli.hasOption(CommandLineParam.PROFILES.getLongName())) {
            final String[] destinations = cli.getOptionValues(CommandLineParam.PROFILES.toString());
            if (destinations == null || destinations.length > 1) {
                throw new CommandLineSyntaxException("Must specify exactly one profile.");
            }
            destination = destinations[0];
        } else { // output to a file, or the console if not specified.
            destination = cli.hasOption(CommandLineParam.OUTPUT_FILE.getLongName())
                    ? cli.getOptionValue(CommandLineParam.OUTPUT_FILE.toString()) : "stdout";
            overrideProperties.setProperty(DroidGlobalProperty.OUTPUT_FILE_PATH.getName(), destination);
        }
        return destination;
    }

    private Filter getIdentificationFilter(CommandLine cli) {
        Filter result = null;
        if (cli.hasOption(CommandLineParam.ALL_FILTER_FILE.toString())) {
            result = createFilter(cli.getOptionValues(CommandLineParam.ALL_FILTER_FILE.toString()), true);
        } else if (cli.hasOption(CommandLineParam.ANY_FILTER_FILE.toString())) {
            result = createFilter(cli.getOptionValues(CommandLineParam.ANY_FILTER_FILE.toString()), false);
        }
        if (cli.hasOption(CommandLineParam.EXTENSION_LIST.toString())) {
            result = createExtensionFilter(result, cli.getOptionValues(CommandLineParam.EXTENSION_LIST.toString()));
        }
        return result;
    }

    private Filter getResultsFilter(CommandLine cli) {
        Filter result = null;
        if (cli.hasOption(CommandLineParam.ALL_FILTER.toString())) {
            result = createFilter(cli.getOptionValues(CommandLineParam.ALL_FILTER.toString()), true);
        } else if (cli.hasOption(CommandLineParam.ANY_FILTER.toString())) {
            result = createFilter(cli.getOptionValues(CommandLineParam.ANY_FILTER.toString()), false);
        }
        return result;
    }

    private String[] getResources(final CommandLine cli) throws CommandLineSyntaxException {
        String[] resources = cli.getOptionValues(CommandLineParam.RUN_PROFILE.toString());
        if (resources == null || resources.length == 0) {
            resources = cli.getArgs(); // if no profile resources specified, use unbound arguments:
            if (resources == null || resources.length == 0) {
                throw new CommandLineSyntaxException(NO_RESOURCES_SPECIFIED);
            }
        }
        return resources;
    }

    private String[] getNoProfileResources(CommandLine cli) throws CommandLineSyntaxException {
        String[] resources = cli.getOptionValues(CommandLineParam.RUN_NO_PROFILE.toString());
        if (resources == null || resources.length == 0) {
            resources = cli.getArgs(); // if no profile resources specified, use unbound arguments:
            if (resources == null || resources.length == 0) {
                throw new CommandLineSyntaxException(NO_RESOURCES_SPECIFIED);
            }
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
        } else {
            overrideProperties = new PropertiesConfiguration(); //
        }

        processCommandLineArchiveFlags(cli, overrideProperties);
        processCommandLineCSVOptions(cli, overrideProperties);

        return overrideProperties;
    }

    private void processCommandLineCSVOptions(CommandLine cli, PropertiesConfiguration overrideProperties) {
        if (cli.hasOption(CommandLineParam.COLUMNS_TO_WRITE.getLongName())) {
            overrideProperties.setProperty(DroidGlobalProperty.COLUMNS_TO_WRITE.getName(),
                    String.join(SPACE, cli.getOptionValues(CommandLineParam.COLUMNS_TO_WRITE.getLongName())));
        }
        if (cli.hasOption(CommandLineParam.QUOTE_COMMAS.getLongName())) {
            overrideProperties.setProperty(DroidGlobalProperty.QUOTE_ALL_FIELDS.getName(), false);
        }
        if (cli.hasOption(CommandLineParam.ROW_PER_FORMAT.getLongName())) {
            overrideProperties.setProperty(DroidGlobalProperty.EXPORT_OPTIONS.getName(), ExportOptions.ONE_ROW_PER_FORMAT.name());
        } else {
            overrideProperties.setProperty(DroidGlobalProperty.EXPORT_OPTIONS.getName(), ExportOptions.ONE_ROW_PER_FILE.name());
        }
    }

    private void processCommandLineArchiveFlags(CommandLine cli, PropertiesConfiguration overrideProperties) {
        // Special command line flags for archive processing which override all the others:
        if (cli.hasOption(CommandLineParam.ARCHIVES.toString())) { // Turn on all archives:
            setAllArchivesExpand(overrideProperties, true);
        } else if (cli.hasOption(CommandLineParam.ARCHIVE_TYPES.toString())) {
            setExpandArchiveTypes(overrideProperties, cli.getOptionValues(CommandLineParam.ARCHIVE_TYPES.toString()));
        }

        if (cli.hasOption(CommandLineParam.WEB_ARCHIVES.toString())) { // Turn on all web archives:
            setAllWebArchivesExpand(overrideProperties, true);
        } else if (cli.hasOption(CommandLineParam.WEB_ARCHIVE_TYPES.toString())) {
            setExpandWebArchiveTypes(overrideProperties, cli.getOptionValues(CommandLineParam.WEB_ARCHIVE_TYPES.toString()));
        }
    }

    private void setAllArchivesExpand(PropertiesConfiguration overrideProperties, boolean isOn) {
        overrideProperties.setProperty(DroidGlobalProperty.PROCESS_TAR.getName(), isOn);
        overrideProperties.setProperty(DroidGlobalProperty.PROCESS_ZIP.getName(), isOn);
        overrideProperties.setProperty(DroidGlobalProperty.PROCESS_GZIP.getName(), isOn);
        overrideProperties.setProperty(DroidGlobalProperty.PROCESS_RAR.getName(), isOn);
        overrideProperties.setProperty(DroidGlobalProperty.PROCESS_7ZIP.getName(), isOn);
        overrideProperties.setProperty(DroidGlobalProperty.PROCESS_ISO.getName(), isOn);
        overrideProperties.setProperty(DroidGlobalProperty.PROCESS_BZIP2.getName(), isOn);
    }

    private void setAllWebArchivesExpand(PropertiesConfiguration overrideProperties, boolean isOn) {
        overrideProperties.setProperty(DroidGlobalProperty.PROCESS_ARC.getName(), isOn);
        overrideProperties.setProperty(DroidGlobalProperty.PROCESS_WARC.getName(), isOn);
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

    /*
     * NOTE: this relies on all archive type properties following a common pattern of:
     * 1. "profile.process"
     * 2. Uppercase letter of archive type name.
     * 3. Lower case for the remainder of the archive type name.
     *
     * For example, for ZIP archives, the property must be profile.processZip.
     * Might be more robust to maintain a mapping of property to archive type...?
     */
    private String getArchivePropertyName(String archiveType) {
        return "profile.process" + archiveType.substring(0, 1).toUpperCase(Locale.ROOT) + archiveType.substring(1).toLowerCase(Locale.ROOT);
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
            addProperty(property.startsWith(PROFILE_PREFIX) ? property : PROFILE_PREFIX + property, result);
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

    private Filter createFilter(String[] filters, boolean isNarrowed) {
        List<FilterCriterion> criteria = new ArrayList<>();
        for (String dql : filters) {
            criteria.add(DqlFilterParser.parseDql(dql));
        }
        return new BasicFilter(criteria, isNarrowed);
    }

    private Filter createExtensionFilter(Filter existingFilter, String[] optionValues) {
        FilterCriterion criterion = DqlCriterionFactory.newCriterion(FILE_EXT_FIELD, ANY_OPERATOR, Arrays.asList(optionValues));
        final List<FilterCriterion> criteria;
        final boolean isNarrowed;
        if (existingFilter == null) {
            criteria = new ArrayList<>();
            isNarrowed = true;
        } else {
            criteria = existingFilter.getCriteria();
            isNarrowed = existingFilter.isNarrowed();
        }
        criteria.add(criterion);
        return new BasicFilter(criteria, isNarrowed);
    }
}
