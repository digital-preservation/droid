/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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
