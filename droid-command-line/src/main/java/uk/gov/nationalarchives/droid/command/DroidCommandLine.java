/**
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gsi.gov.uk>
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
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

import uk.gov.nationalarchives.droid.command.action.CommandExecutionException;
import uk.gov.nationalarchives.droid.command.action.CommandFactory;
import uk.gov.nationalarchives.droid.command.action.CommandFactoryImpl;
import uk.gov.nationalarchives.droid.command.action.CommandLineException;
import uk.gov.nationalarchives.droid.command.action.CommandLineParam;
import uk.gov.nationalarchives.droid.command.action.CommandLineSyntaxException;
import uk.gov.nationalarchives.droid.command.context.GlobalContext;
import uk.gov.nationalarchives.droid.command.context.SpringUiContext;
import uk.gov.nationalarchives.droid.core.interfaces.config.RuntimeConfig;

/**
 * The DROID command line user-interface.
 * 
 * @author rflitcroft, Alok Kumar Dash
 * 
 */
public final class DroidCommandLine {

    /** Options message. */
    public static final String USAGE = "droid [options]";
    /** Wrap width. */
    public static final int WRAP_WIDTH = 120;
    
    /**Logger Log4j.*/
    //private Log log = LogFactory.getLog(this.getClass());
    //private static Logger log = Logger.getLogger(DroidCommandLine.class);
    


    private final String[] args;
    private GlobalContext context = new SpringUiContext();
    


    
    private CommandFactory commandFactory;
    private CommandLine cli;
    
    /**
     * Default constructor.
     * 
     * @param args
     *            the command line arguments
     */
    DroidCommandLine(final String[] args) {
        this.args = args;
    }
    
    /**
     * 
     * @return GlobalContext object
     */
    public GlobalContext getContext() {
        return context;
    }

    /**
     * 
     * @param context object
     */
    public void setContext(GlobalContext context) {
        this.context = context;
    }
    
    /**
     * Runs the command line interface.
     * 
     * @throws CommandLineException
     *             if the command line failed for any reason
     */
    public void run() throws CommandLineException {
        // log.info("Starting DROID.");
        CommandLineParser parser = new GnuParser();

        try {
            cli = parser.parse(CommandLineParam.options(), args);

            String logThreshold = "INFO";
            if (cli.hasOption(CommandLineParam.QUIET.toString())) {
                logThreshold = "ERROR";
            }
            System.setProperty("consoleLogThreshold", logThreshold);

            CommandLineParam option = null;
            for (Option opt : cli.getOptions()) {
                option = CommandLineParam.TOP_LEVEL_COMMANDS.get(opt.getOpt());
                if (option != null) {
                    break;

                }
            }

            if (option != null) {
                option.getCommand(commandFactory, cli).execute();
            } else {
                throw new CommandLineSyntaxException(
                        "No command line options specified (use -h to see all available options)");
            }
        } catch (ParseException pe) {
            throw new CommandLineSyntaxException(pe);
        }

        // finally {
        // log.info("Closing DROID.");
        // }
    }

    /**
     * Main method for command line.
     * 
     * @param args
     *            the command line arguments
     * @throws CommandLineException if bad command
     * @throws CommandExecutionException if cannot execute command
     */
    public static void main(final String[] args) throws CommandLineException {

        //BNO: The configureRuntimeEnvironment() method was getting called twice as it is also
        // the first call in  commandLine.processExecution() below.  This was causing a problem
        // with the log configuration file path getting appended to the current directory thereby
        // producing an invalid file path, e.g. C:\Projects\Droid\droid\file:\C:\Users\Brian\.droid6\log4j.properties.
        // There appears to be no other reason for the 2 calls so I have sinply removed the first call here.
        //RuntimeConfig.configureRuntimeEnvironment();

        GlobalContext context = new SpringUiContext();
        
        int returnCode = 0;
    
        DroidCommandLine commandLine = new DroidCommandLine(args);
        returnCode = commandLine.processExecution();

        System.exit(returnCode);
    }

    /**
     * 
     * @return a status code 0 success otherwise 1
     * @throws CommandLineException on bad command
     */
    public int processExecution() throws CommandLineException {
        
        RuntimeConfig.configureRuntimeEnvironment();
        PrintWriter out = new PrintWriter(System.out);
        
        final CommandFactoryImpl localCommandFactory = new CommandFactoryImpl(context, out);
        
        this.setCommandFactory(localCommandFactory);

        int returnCode = 0;

        try {

            run();
            
            out.close();
            context.close();

        } catch (CommandExecutionException ceex) {
            returnCode = 1;
            PrintWriter err = new PrintWriter(System.err);
            HelpFormatter formatter = new HelpFormatter();
            formatter.printWrapped(err, WRAP_WIDTH, ceex.getMessage());
            err.close();
            //log.error("Droid Execution Error", ceex);
            throw ceex;

        } catch (CommandLineException clex) {
            returnCode = 1;
            PrintWriter err = new PrintWriter(System.err);
            HelpFormatter formatter = new HelpFormatter();
            formatter.printWrapped(err, WRAP_WIDTH, clex.getMessage());
            err.close();
            //log.error("Droid CommandLineException", clex);
            throw clex;
        }

        return returnCode;
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
     * 
     * @return the command line
     */
    public CommandLine getCommandLine() {
        return cli;
    }

}
