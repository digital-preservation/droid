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
package uk.gov.nationalarchives.droid.command;

import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.gov.nationalarchives.droid.command.action.CommandFactory;
import uk.gov.nationalarchives.droid.command.action.CommandFactoryImpl;
import uk.gov.nationalarchives.droid.command.action.CommandLineException;
import uk.gov.nationalarchives.droid.command.action.CommandLineParam;
import uk.gov.nationalarchives.droid.command.action.CommandLineSyntaxException;
import uk.gov.nationalarchives.droid.command.action.HelpCommand;
import uk.gov.nationalarchives.droid.command.context.GlobalContext;
import uk.gov.nationalarchives.droid.command.context.SpringUiContext;
import uk.gov.nationalarchives.droid.command.i18n.I18N;
import static uk.gov.nationalarchives.droid.command.i18n.I18N.getResource;
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

    private final String[] args;
    private CommandFactory commandFactory;
    private CommandLine cli;
    private Log log = LogFactory.getLog(this.getClass());

    /**
     * Default constructor.
     * 
     * @param args
     *            the command line arguments
     * @param commandFactory
     *            the Command Factory
     */
    DroidCommandLine(final String[] args, CommandFactory commandFactory) {
        this.args = args;
        this.commandFactory = commandFactory;
    }

    /**
     * Runs the command line interface.
     * 
     * @throws CommandLineException
     *             if the command line failed for any reason
     */
    public void run() throws CommandLineException {
        //log.info("Starting DROID.");
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
                throw new CommandLineSyntaxException("No command line options specified");
            }
            
        } catch (ParseException e) {
            throw new CommandLineSyntaxException(e.getMessage());
        }
        //finally {
        //    log.info("Closing DROID.");
        //}
    }

    /**
     * Main method for command line.
     * 
     * @param args
     *            the command line arguments
     */
    public static void main(final String[] args) {
        
        RuntimeConfig.configureRuntimeEnvironment();

        GlobalContext context = new SpringUiContext();
        try {
            PrintWriter out = new PrintWriter(System.out);
            CommandFactoryImpl commandFactory = new CommandFactoryImpl(context, out);
            DroidCommandLine commandLine = new DroidCommandLine(args, commandFactory);
            commandLine.run();
            out.close();
        } catch (CommandLineSyntaxException e) {
            PrintWriter err = new PrintWriter(System.err);
            HelpFormatter formatter = new HelpFormatter();
            formatter.printWrapped(err, WRAP_WIDTH, e.getMessage());
            formatter.printWrapped(err, WRAP_WIDTH, getResource(I18N.BAD_OPTIONS));
            HelpCommand help = new HelpCommand(err);
            help.execute();
            err.close();
        } catch (CommandLineException e) {
            PrintWriter err = new PrintWriter(System.err);
            HelpFormatter formatter = new HelpFormatter();
            formatter.printWrapped(err, WRAP_WIDTH, e.getMessage());
            err.close();
        } finally {
            context.close();
        }
    }

    /**
     * 
     * @return the command line
     */
    public CommandLine getCommandLine() {
        return cli;
    }

}
