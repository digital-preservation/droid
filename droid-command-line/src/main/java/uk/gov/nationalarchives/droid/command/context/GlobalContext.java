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
package uk.gov.nationalarchives.droid.command.context;

import uk.gov.nationalarchives.droid.command.action.CheckSignatureUpdateCommand;
import uk.gov.nationalarchives.droid.command.action.ConfigureDefaultSignatureFileVersionCommand;
import uk.gov.nationalarchives.droid.command.action.DisplayDefaultSignatureFileVersionCommand;
import uk.gov.nationalarchives.droid.command.action.DownloadSignatureUpdateCommand;
import uk.gov.nationalarchives.droid.command.action.ExportCommand;
import uk.gov.nationalarchives.droid.command.action.ListAllSignatureFilesCommand;
import uk.gov.nationalarchives.droid.command.action.ListReportsCommand;
import uk.gov.nationalarchives.droid.command.action.NoProfileRunCommand;
import uk.gov.nationalarchives.droid.command.action.ProfileRunCommand;
import uk.gov.nationalarchives.droid.command.action.ReportCommand;
import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalConfig;
import uk.gov.nationalarchives.droid.export.interfaces.ExportOptions;


/**
 * @author rflitcroft
 *
 */
public interface GlobalContext {

    /**
     * 
     * @return the droid global config
     */
    DroidGlobalConfig getGlobalConfig();

    /**
     * @return a profile run command
     */
    ProfileRunCommand getProfileRunCommand();

    /**
     * @return a no-profile run command
     */
    NoProfileRunCommand getNoProfileRunCommand();

    /**
     * @param opt the Export options to use when exporting.
     * @return an export command
     */
    ExportCommand getExportCommand(ExportOptions opt);
    
    /**
     * @return an report command
     */
    ReportCommand getReportCommand();

    /**
     * 
     */
    void close();

    /**
     * @return a check signature update command
     */
    CheckSignatureUpdateCommand getCheckSignatureUpdateCommand();

    /**
     * @return a download signature update command
     */
    DownloadSignatureUpdateCommand getDownloadSignatureUpdateCommand();

    /**
     * @return a display default signature file version command
     */
    DisplayDefaultSignatureFileVersionCommand getDisplayDefaultSignatureFileVersionCommand();

    /**
     * @return a 'configure default signature file version' command
     */
    ConfigureDefaultSignatureFileVersionCommand getConfigureDefaultSignatureFileVersionCommand();

    /**
     * @return command to list all signature files.
     */
    ListAllSignatureFilesCommand getListAllSignatureFilesCommand();
    
    /**
     * @return command to list available reports and output formats.
     */
    ListReportsCommand getListReportsCommand();    

}
