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
     * @param cli the command line;
     * @return a new {@link RunNoProfileCommand}
     * @throws CommandLineSyntaxException if the command line args were invalid
     */
    DroidCommand getNoProfileCommand(CommandLine cli) throws CommandLineSyntaxException;


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
