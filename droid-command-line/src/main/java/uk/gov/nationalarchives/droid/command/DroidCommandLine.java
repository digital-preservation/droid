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
package uk.gov.nationalarchives.droid.command;

import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.nationalarchives.droid.command.action.CommandFactory;
import uk.gov.nationalarchives.droid.command.action.CommandFactoryImpl;
import uk.gov.nationalarchives.droid.command.action.CommandLineException;
import uk.gov.nationalarchives.droid.command.action.CommandLineParam;
import uk.gov.nationalarchives.droid.command.action.CommandLineSyntaxException;
import uk.gov.nationalarchives.droid.command.context.GlobalContext;
import uk.gov.nationalarchives.droid.command.context.SpringUiContext;
import uk.gov.nationalarchives.droid.command.filter.DqlParseException;
import uk.gov.nationalarchives.droid.core.interfaces.config.RuntimeConfig;

/**
 * The DROID command line user-interface.
 * 
 * @author rflitcroft, Alok Kumar Dash
 * 
 */
public final class DroidCommandLine implements AutoCloseable {

    //CHECKSTYLE:OFF
    /**
     * For testing. @see{uk.gov.nationalarchives.droid.command.TestContexCleanup}
     */
    public static boolean systemExit = true;
    //CHECKSTYLE:ON

    private static final String CLI_SYNTAX_INCORRECT = "Incorrect command line syntax: %s";
    private static final String UNKNOWN_ERROR = "An unknown error occurred: %s";
    private static final String INVALID_COMMAND_LINE = "No actionable command line options specified (use -h to see all available options): %s";
    private static final int WRAP_WIDTH = 120;
    private static final String STDOUT = "stdout";

    /**Logger slf4j.*/
    private final String[] args;
    private final PrintWriter errorPrintWriter;

    private Logger log;
    private CommandFactory commandFactory;
    private CommandLine cli;
    private GlobalContext context;
    private CommandLineParam mainCommand;
    private PrintWriter printWriter;

    /**
     * Default constructor.
     * 
     * @param args the command line arguments
     */
    public DroidCommandLine(final String[] args)  {
        this(args, null);
    }

    /**
     * Additional constructor, used only for unit tests involving exception.
     *
     * @param args the command line arguments
     * @param errorPrintWriter print writer for error messages
     */
    public DroidCommandLine(final String[] args, PrintWriter errorPrintWriter) {
        this.errorPrintWriter = errorPrintWriter == null ? new PrintWriter(System.err) : errorPrintWriter;
        this.args = args;
    }

    /**
     * Main method for command line.  Returns 0 for success and 1 for failure.
     * 
     * @param args the command line arguments
     */
    public static void main(final String[] args)  {
        int returnCode;
        try (DroidCommandLine commandLine = new DroidCommandLine(args)) {
            returnCode = commandLine.processExecution();
        }
        if (systemExit) {
            System.exit(returnCode);
        }
    }

    /**
     * Instantiates any context or command factories, then executes the top level command found when parsing.
     *
     * @return a status code 0 success otherwise 1
     */
    public int processExecution() {
        int returnCode = 0;
        try {
            init();
            mainCommand.getCommand(commandFactory, cli).execute();
        } catch (CommandLineSyntaxException clsx) {
            returnCode = 1;
            outputErrorMessage(String.format(CLI_SYNTAX_INCORRECT, clsx.getMessage()));
        } catch (CommandLineException ceex) {
            returnCode = 1;
            outputErrorMessage(ceex.getMessage());
        } catch (Exception ex) {
            returnCode = 1;
            outputErrorMessage(String.format(UNKNOWN_ERROR, ex.getMessage()));
        } finally {
            close();
        }
        return returnCode;
    }

    @Override
    public void close() {
        if (printWriter != null) {
            printWriter.flush();  //Only flush. Never close System.out
        }
        if (context != null) {
            try {
                context.close();
            } finally {
                context = null;
                commandFactory = null;
            }
        }
    }

    /**
     * 
     * @return CommandFactory object
     */
    public CommandFactory getCommandFactory() {
        return commandFactory;
    }

    /**
     * 
     * @param commandFactory the factory
     */
    public void setCommandFactory(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    /**
     * Sets the global context used to generate the beans by the command factory.
     * @param context the global context.
     */
    public void setContext(GlobalContext context) {
        this.context = context;
    }

    /**
     * Sets the print writer for commands to write output
     * @param writer The print writer to write command output to.
     */
    public void setPrintWriter(PrintWriter writer) {
        printWriter = writer;
    }

    /**
     * @return The PrintWriter used to output messages from the commands.
     */
    public PrintWriter getPrintWriter() {
        return printWriter;
    }

    /**
     * @return The global context used to generate the beans by the command factory.
     */
    public GlobalContext getContext() {
        return context;
    }

    /**
     * @return the command line
     */
    public CommandLine getCommandLine() throws CommandLineException {
        return cli;
    }

    /*
     * Private functions
     */

    /**
     * Initialises all the objects needed to start processing the main command.
     *
     * @throws CommandLineException if there's a problem parsing the command line.
     */
    private void init() throws CommandLineException {
        parseCommandLine();
        configureQuietLogging();
        RuntimeConfig.configureRuntimeEnvironment();
        if (log == null) { // don't want to instantiate logger until all logging configuration is done.
            log = LoggerFactory.getLogger(this.getClass());
        }
        log.info("Starting DROID.");
        if (context == null) {
            setContext(SpringUiContext.getInstance());
        }
        if (printWriter == null) {
            setPrintWriter(new PrintWriter(System.out));
        }
        if (commandFactory == null) {
            setCommandFactory(new CommandFactoryImpl(context, printWriter));
        }
    }

    private void configureQuietLogging() {
        if (isQuiet() || isOutputtingToConsole()) {
            System.setProperty("consoleLogThreshold", "ERROR");
        }
    }

    private void parseCommandLine() throws CommandLineException {
        try {
            try {
                cli = new GnuParser().parse(CommandLineParam.options(), args);
                mainCommand = findTopLevelOption();

                // If we don't find any top level option, but we have unbound arguments, interpret them as adding files to a profile:
                if (mainCommand == null) {
                    if (cli.getArgs().length > 0) {
                        mainCommand = CommandLineParam.RUN_PROFILE;
                    } else { // no top level option, and no unbound arguments...  error.
                        throw new CommandLineSyntaxException(String.format(INVALID_COMMAND_LINE, String.join(" ", args)));
                    }
                }
            } catch (DqlParseException pe) {
                throw new CommandLineSyntaxException(pe.getMessage(), pe);
            }
        } catch (ParseException pe) {
            throw new CommandLineSyntaxException(pe.getMessage(), pe);
        }
    }

    private CommandLineParam findTopLevelOption() {
        CommandLineParam result = null;
        for (Option opt : cli.getOptions()) {
            result = CommandLineParam.TOP_LEVEL_COMMANDS.get(opt.getOpt());
            if (result != null) {
                break;
            }
        }
        return result;
    }

    /**
     * @return true if the quiet flag is set on the command line.
     */
    private boolean isQuiet() {
        return cli.hasOption(CommandLineParam.QUIET.toString());
    }

    /**
     *  We output to console if the command is a profile and there either isn't an output file, or it's to stdout.
     *  The export, report and database profiles always write to a file or database.
     * @return true if the command will write its output to the console rather than to a file.
     */
    private boolean isOutputtingToConsole()  {
        return mainCommand == CommandLineParam.RUN_PROFILE
                && (!cli.hasOption(CommandLineParam.OUTPUT_FILE.getLongName())
                  || cli.getOptionValue(CommandLineParam.OUTPUT_FILE.getLongName()).trim().equals(STDOUT));
    }

    private void outputErrorMessage(String message) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printWrapped(errorPrintWriter, WRAP_WIDTH, message);
        errorPrintWriter.flush(); //Only flush. Never close System.err
        //log.error("Droid CommandLineException", clex);
    }

}

