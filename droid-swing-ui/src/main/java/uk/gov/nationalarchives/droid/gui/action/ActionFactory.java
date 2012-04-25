/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui.action;

import uk.gov.nationalarchives.droid.gui.config.ListSignatureFilesAction;
import uk.gov.nationalarchives.droid.gui.config.UploadSignatureFileAction;
import uk.gov.nationalarchives.droid.gui.export.ExportAction;
import uk.gov.nationalarchives.droid.gui.report.ReportAction;
import uk.gov.nationalarchives.droid.gui.signature.CheckSignatureUpdateAction;
import uk.gov.nationalarchives.droid.gui.signature.UpdateSignatureAction;

/**
 * @author rflitcroft
 *
 */
public abstract class ActionFactory {
    
    /**
     * Gets a signature update action.
     * @return a signature update action
     */
    public abstract UpdateSignatureAction newSignaureUpdateAction();
    
    /**
     * Gets an action to check for signature updates.
     * @return a signature update action
     */
    public abstract CheckSignatureUpdateAction newCheckSignatureUpdateAction();

    /**
     * Gets a new profile action.
     * @return a new profile action
     */
    public abstract NewProfileAction newProfileAction();

    /**
     * @return a new export action
     */
    public abstract ExportAction newExportAction();
    
    /**
     * @return a new report action
     */
    public abstract ReportAction newReportAction();

    /**
     * @return a new action to List signature files
     */
    public abstract ListSignatureFilesAction newListSignatureFilesAction();

    /**
     * @return a new action to upload a signature file
     */
    public abstract UploadSignatureFileAction newUploadSignatureFileAction();

}
