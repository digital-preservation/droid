/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.command.action;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

import uk.gov.nationalarchives.droid.command.i18n.I18N;

/**
 * @author rflitcroft
 *
 */
public enum CommandLineParam {
    
    /** help. */
    HELP("h", "help", I18N.HELP_HELP) {
        @Override
        public DroidCommand getCommand(CommandFactory commandFactory, CommandLine cli) {
            return commandFactory.getHelpCommand();
        }
    },
    
    /** Version. */
    VERSION("v", "version", I18N.VERSION_HELP) {
        @Override
        public DroidCommand getCommand(CommandFactory commandFactory, CommandLine cli) {
            return commandFactory.getVersionCommand();
        }
    }, 
    
    /** Export with one row per file. */
    EXPORT_ONE_ROW_PER_FILE("e", "export-file", false, 1, I18N.EXPORT_FILE_HELP, filename()) {
        @Override
        public DroidCommand getCommand(CommandFactory commandFactory, CommandLine cli) 
            throws CommandLineSyntaxException {
            return commandFactory.getExportFileCommand(cli);
        }
    },
    
    /** Export with one row per format. */
    EXPORT_ONE_ROW_PER_FORMAT("E", "export-format", false, 1, I18N.EXPORT_FORMAT_HELP, filename()) {
        @Override
        public DroidCommand getCommand(CommandFactory commandFactory, CommandLine cli) 
            throws CommandLineSyntaxException {
            return commandFactory.getExportFormatCommand(cli);
        }
    }, 
    
    
    /** List of profiles to be worked on. */
    PROFILES("p", "profile", true, -1, I18N.PROFILES_HELP, "filename ...") {
        @Override
        public DroidCommand getCommand(CommandFactory commandFactory, CommandLine cli) {
            return null;
        }
    },
    
    /** Narrow filter. */
    ALL_FILTER("f", "filter-all", true, -1, I18N.ALL_FILTER, filters()) {
        @Override
        public DroidCommand getCommand(CommandFactory commandFactory,
                CommandLine cli) {
            return null;
        }
    },

    /** Narrow filter. */
    ANY_FILTER("F", "filter-any", true, -1, I18N.ANY_FILTER, filters()) {
        @Override
        public DroidCommand getCommand(CommandFactory commandFactory,
                CommandLine cli) {
            return null;
        }
    },

    /** List of reports to be worked on. */
    REPORT("r", "report", true, 1, I18N.REPORT_HELP, filename()) {
        @Override
        public DroidCommand getCommand(CommandFactory commandFactory, CommandLine cli)
            throws CommandLineSyntaxException {
            return commandFactory.getReportCommand(cli);
        }
    }, 
    /** List of  report to be worked on. */    
    REPORT_NAME("n", "report-name", true, 1, I18N.REPORT_NAME_HELP, "report name") {
        @Override
        public DroidCommand getCommand(CommandFactory commandFactory, CommandLine cli) {
            return null;
        }
    },
    
    /** Set the report output type. */
    REPORT_OUTPUT_TYPE("t", "report-output-type", true, 1, I18N.REPORT_TYPE_HELP, "report output type") {
        @Override
        public DroidCommand getCommand(CommandFactory commandFactory, CommandLine cli) {
            return null;
        }
    },
    
    /** Lists the reports. */
    LIST_REPORTS("l", "list-reports", I18N.LIST_REPORTS_HELP) {
        @Override
        public DroidCommand getCommand(CommandFactory commandFactory,
                CommandLine cli) {
            return commandFactory.getListReportCommand();
        }
    },    
    
    /** Lists the filter fields. */
    LIST_FILTER_FIELD("k", "filter-fields", I18N.LIST_FILTER_FIELD) {
        @Override
        public DroidCommand getCommand(CommandFactory commandFactory,
                CommandLine cli) {
            return commandFactory.getFilterFieldCommand();
        }
    },
    
    /** Runs a profile with the specified resources. */
    RUN_PROFILE("a", "profile-resources", true, -1, I18N.RUN_PROFILE_HELP, "resources...") {
        @Override
        public DroidCommand getCommand(CommandFactory commandFactory, CommandLine cli)
            throws CommandLineSyntaxException {
            return commandFactory.getProfileCommand(cli);
        }
    },
    
    /** Recursive operation flag. */
    RECURSIVE("R", "recurse", I18N.RECURSE_HELP) {
        @Override
        public DroidCommand getCommand(CommandFactory commandFactory, CommandLine cli) {
            return null;
        }
    },
    
    /** Recursive operation flag. */
    QUIET("q", "quiet", I18N.QUIET_HELP) {
        @Override
        public DroidCommand getCommand(CommandFactory commandFactory, CommandLine cli) {
            return null;
        }
    },
    
    /** Check for signature updates. */
    CHECK_SIGNATURE_UPDATE("c", "check-signature-update", I18N.CHECK_SIGNATURE_UPDATE_HELP) {
        @Override
        public DroidCommand getCommand(CommandFactory commandFactory, CommandLine cli) {
            return commandFactory.getCheckSignatureUpdateCommand();
        }
    },
    
    /** Download latest signature update. */
    DOWNLOAD_SIGNATURE_UPDATE("d", "download-signature-update", I18N.DOWNLOAD_SIGNATURE_UPDATE_HELP) {
        @Override
        public DroidCommand getCommand(CommandFactory commandFactory, CommandLine cli) {
            return commandFactory.getDownloadSignatureUpdateCommand();
        }
    },
    
    /** List all signature files. */
    LIST_SIGNATURE_VERSIONS("X", "list-signature-files", I18N.LIST_SIGNATURE_VERSIONS_HELP) {
        @Override
        public DroidCommand getCommand(CommandFactory commandFactory, CommandLine cli) {
            return commandFactory.getListAllSignatureVersionsCommand();
        }
    },
    

    /** Display the default signature update. */
    DEFAULT_SIGNATURE_VERSION("x", "display-signature-file", I18N.DEFAULT_SIGNATURE_VERSION_HELP) {
        @Override
        public DroidCommand getCommand(CommandFactory commandFactory, CommandLine cli) {
            return commandFactory.getDisplayDefaultSignatureVersionCommand();
        }
    },

    /** Display the default signature update. */
    CONFIGURE_DEFAULT_SIGNATURE_VERSION("s", "configure-signature-file", true, 1,
            I18N.CONFIGURE_DEFAULT_SIGNATURE_VERSION_HELP, I18N.getResource(I18N.VERSION)) {
        @Override
        public DroidCommand getCommand(CommandFactory commandFactory, CommandLine cli) 
            throws CommandLineException {
            return commandFactory.getConfigureDefaultSignatureVersionCommand(cli);
        }
    };

    /**
     * All the top-level command line options.
     */
    public static final Map<String, CommandLineParam> TOP_LEVEL_COMMANDS = new HashMap<String, CommandLineParam>();
    
    static {
        addTopLevelCommand(HELP);
        addTopLevelCommand(VERSION);
        addTopLevelCommand(EXPORT_ONE_ROW_PER_FILE);
        addTopLevelCommand(EXPORT_ONE_ROW_PER_FORMAT);
        addTopLevelCommand(REPORT);
        addTopLevelCommand(LIST_FILTER_FIELD);
        addTopLevelCommand(RUN_PROFILE);
        addTopLevelCommand(CHECK_SIGNATURE_UPDATE);
        addTopLevelCommand(DOWNLOAD_SIGNATURE_UPDATE);
        addTopLevelCommand(DEFAULT_SIGNATURE_VERSION);
        addTopLevelCommand(CONFIGURE_DEFAULT_SIGNATURE_VERSION);
        addTopLevelCommand(LIST_SIGNATURE_VERSIONS);
        addTopLevelCommand(LIST_REPORTS);
    }

    private static final String FILENAME = "filename";
    
    private String shortName;
    private String longName;
    private String resourceKey;
    private boolean argsRequired;
    private int maxArgs;
    private String argName;
    
    
    private CommandLineParam(String shortName, String longName, String resourceKey) { 
        this.shortName = shortName;
        this.longName = longName;
        this.resourceKey = resourceKey;
    }
    
    private CommandLineParam(String shortName, String longName, boolean argsRequired, 
            int maxArgs, String resourceKey, String argName) { 
        this(shortName, longName, resourceKey);
        this.maxArgs = maxArgs;
        this.argName = argName;
        this.argsRequired = argsRequired;
    }

    private static String filename() {
        return FILENAME;
    }
    
    private static void addTopLevelCommand(CommandLineParam command) {
        TOP_LEVEL_COMMANDS.put(command.toString(), command);
    }
    
    /**
     * Gets a droid command for this command line option.
     * @param commandFactory the command factory
     * @param cli the command line
     * @throws CommandLineException command parse exception. 
     * @return a droid command.
     */
    public abstract DroidCommand getCommand(CommandFactory commandFactory, CommandLine cli)
        throws CommandLineException;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return shortName;
    }

    /**
     * 
     * @return a new option for the parameter.
     */
    Option newOption() {
        Option option = new Option(shortName, longName, maxArgs != 0, I18N.getResource(resourceKey));
        option.setArgs(maxArgs == -1 ? Option.UNLIMITED_VALUES : maxArgs);
        if (maxArgs != 0) {
            option.setOptionalArg(!argsRequired);
        }
        option.setArgName(argName);
        
        return option;
    }
    
    
    private static String filters() {
        return "filter ...";
    }
    
    /**
     * All options.
     * @return all options
     */
    public static Options options() {
        Options options = new Options();
        
        OptionGroup topGroup = new OptionGroup();

        for (CommandLineParam param : TOP_LEVEL_COMMANDS.values()) {
            topGroup.addOption(param.newOption());
        }
        
        options.addOption(PROFILES.newOption());
        options.addOption(REPORT_NAME.newOption());
        options.addOption(REPORT_OUTPUT_TYPE.newOption());
        
        OptionGroup filterOptions = new OptionGroup();
        filterOptions.addOption(ALL_FILTER.newOption());
        filterOptions.addOption(ANY_FILTER.newOption());
        filterOptions.addOption(RECURSIVE.newOption());
        filterOptions.addOption(QUIET.newOption());
        
        options.addOptionGroup(filterOptions);
        options.addOptionGroup(topGroup);
        
        return options;

    }
    
}
