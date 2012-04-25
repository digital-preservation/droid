/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.command.action;

import org.apache.commons.cli.CommandLine;

import uk.gov.nationalarchives.droid.command.FilterFieldCommand;

/**
 * @author rflitcroft, Alok Kumar Dash
 *
 */
public interface CommandFactory {

    /**
     * @param cli the command line
     * @throws CommandLineSyntaxException command parse exception.
     * @return an export command
     */
    DroidCommand getExportFileCommand(CommandLine cli) throws CommandLineSyntaxException;


    /**
     * @param cli the command line
     * @throws CommandLineSyntaxException command parse exception.
     * @return an export command
     */
    DroidCommand getExportFormatCommand(CommandLine cli) throws CommandLineSyntaxException;
    
    
    
    /**
     * @param cli the command line
     * @throws CommandLineSyntaxException command parse exception. 
     * @return an report command
     */
    DroidCommand getReportCommand(CommandLine cli) throws CommandLineSyntaxException;


    /**
     * @return a new {@link FilterFieldCommand}
     */
    FilterFieldCommand getFilterFieldCommand();


    /**
     * @return a new {@link HelpCommand}
     */
    DroidCommand getHelpCommand();


    /**
     * @return a new version command
     */
    DroidCommand getVersionCommand();


    /**
     * @param cli the command line;
     * @return a new {@link RunProfileCommand}
     * @throws CommandLineSyntaxException if the command line args were invalid
     */
    DroidCommand getProfileCommand(CommandLine cli) throws CommandLineSyntaxException;


    /**
     * @return a new check signature update command.
     */
    DroidCommand getCheckSignatureUpdateCommand();


    /**
     * @return a new download signature update command
     */
    DroidCommand getDownloadSignatureUpdateCommand();


    /**
     * @return a new 'Display default signature version' command
     */
    DroidCommand getDisplayDefaultSignatureVersionCommand();


    /**
     * @param cli the command line
     * @return a new 'configure default signature file version' command
     * @throws CommandLineException if the version was not valid
     */
    DroidCommand getConfigureDefaultSignatureVersionCommand(CommandLine cli) throws CommandLineException;


    /**
     * @return command to list all signature file versions
     */
    DroidCommand getListAllSignatureVersionsCommand();


    /**
     * @return command to list reports and output formats.
     */
    DroidCommand getListReportCommand();


}
