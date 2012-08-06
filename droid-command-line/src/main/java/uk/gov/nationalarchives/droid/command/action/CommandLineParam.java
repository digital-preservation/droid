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
    PROFILES("p", "profile(s)", true, -1, I18N.PROFILES_HELP, "filename(s)") {
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
    REPORT_OUTPUT_TYPE("t", "report-type", true, 1, I18N.REPORT_TYPE_HELP, "report type") {
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
    RUN_PROFILE("a", "profile-resources", true, -1, I18N.RUN_PROFILE_HELP, "resources") {
        @Override
        public DroidCommand getCommand(CommandFactory commandFactory, CommandLine cli)
            throws CommandLineSyntaxException {
            return commandFactory.getProfileCommand(cli);
        }
    },
    
    /** Runs without a profile and with the specified resources. */
    RUN_NO_PROFILE("Nr", "no-profile-resource", true, -1, I18N.RUN_NO_PROFILE_HELP, "folder") {
        @Override
        public DroidCommand getCommand(CommandFactory commandFactory, CommandLine cli)
            throws CommandLineSyntaxException {
            return commandFactory.getNoProfileCommand(cli);
        }
    },
    
    /** Signature file. */
    SIGNATURE_FILE("Ns", "signature-file", true, 1, I18N.SIGNATURE_FILE_HELP, filename()) {
        @Override
        public DroidCommand getCommand(CommandFactory commandFactory, CommandLine cli) {
            return null;
        }
    },
    
    /** Container signature file. */
    CONTAINER_SIGNATURE_FILE("Nc", "container-file", true, 1,
            I18N.CONTAINER_SIGNATURE_FILE_HELP, filename()) {
        @Override
        public DroidCommand getCommand(CommandFactory commandFactory, CommandLine cli) {
            return null;
        }
    },
    
    /** Extensions to match. */
    EXTENSION_LIST("Nx", "extension-list", true, -1, I18N.EXTENSION_LIST_HELP, "extensions") {
        @Override
        public DroidCommand getCommand(CommandFactory commandFactory, CommandLine cli) {
            return null;
        }
    },
    
    /** Recursive operation flag. */
    RECURSIVE("R", "recurse", I18N.RECURSE_HELP) {
        @Override
        public DroidCommand getCommand(CommandFactory commandFactory, CommandLine cli) {
            return null;
        }
    },
    
    /** Open archives flag. */
    ARCHIVES("A", "open-archives", I18N.ARCHIVES_HELP) {
        @Override
        public DroidCommand getCommand(CommandFactory commandFactory, CommandLine cli) {
            return null;
        }
    },
    
    /** Quiet operation flag. */
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
    CONFIGURE_DEFAULT_SIGNATURE_VERSION("s", "set-signature-file", true, 1,
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
        addTopLevelCommand(RUN_NO_PROFILE);
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
        options.addOption(SIGNATURE_FILE.newOption());
        options.addOption(CONTAINER_SIGNATURE_FILE.newOption());
        options.addOption(EXTENSION_LIST.newOption());
        options.addOption(ARCHIVES.newOption());
        options.addOption(RECURSIVE.newOption());
        options.addOption(QUIET.newOption());
        
        OptionGroup filterOptions = new OptionGroup();
        filterOptions.addOption(ALL_FILTER.newOption());
        filterOptions.addOption(ANY_FILTER.newOption());
        
        options.addOptionGroup(filterOptions);
        options.addOptionGroup(topGroup);
        
        return options;

    }
    
    /**
     * Single Options.
     * 
     * @return the Single Options
     */
    public static Options singleOptions() {
        Options options = new Options();
        
        options.addOption(CHECK_SIGNATURE_UPDATE.newOption());
        options.addOption(DOWNLOAD_SIGNATURE_UPDATE.newOption());
        options.addOption(HELP.newOption());
        options.addOption(LIST_REPORTS.newOption());
        options.addOption(CONFIGURE_DEFAULT_SIGNATURE_VERSION.newOption());
        options.addOption(VERSION.newOption());
        options.addOption(DEFAULT_SIGNATURE_VERSION.newOption());
        options.addOption(LIST_SIGNATURE_VERSIONS.newOption());
        
        return options;
    }
    
    /**
     * No Profile Run sub-options.
     * 
     * @return sub-options for no-profile run
     */
    public static Options noProfileRunSubOptions() {
        Options options = new Options();
        
        options.addOption(SIGNATURE_FILE.newOption());
        options.addOption(CONTAINER_SIGNATURE_FILE.newOption());
        options.addOption(EXTENSION_LIST.newOption());
        options.addOption(ARCHIVES.newOption());
        options.addOption(RECURSIVE.newOption());
        options.addOption(QUIET.newOption());
        
        return options;
    }
    
    /**
     * Profile Run sub-options.
     * 
     * @return sub-options for profile run
     */
    public static Options profileRunSubOptions() {
        Options options = new Options();
        
        options.addOption(PROFILES.newOption());
        options.addOption(RECURSIVE.newOption());
        options.addOption(QUIET.newOption());
        
        return options;
    }
    
    /**
     * Export sub-options.
     * 
     * @return sub-options for Export
     */
    public static Options exportSubOptions() {
        Options options = new Options();
        
        options.addOption(PROFILES.newOption());
        options.addOption(ANY_FILTER.newOption());
        options.addOption(ALL_FILTER.newOption());
        
        return options;
    }
    
    /**
     * Report sub-options.
     * 
     * @return sub-options for Report
     */
    public static Options reportSubOptions() {
        Options options = new Options();
        
        options.addOption(PROFILES.newOption());
        options.addOption(REPORT_NAME.newOption());
        options.addOption(REPORT_OUTPUT_TYPE.newOption());
        
        return options;
    }
    
    /**
     * Gets Options from Command Line parameter.
     * 
     * @param commandLineParam The command line parameter
     * 
     * @return options for Command Line parameter
     */
    public static Options getOptions(final CommandLineParam commandLineParam) {
        Options options = new Options();
        
        options.addOption(commandLineParam.newOption());
        return options;
    }
}
