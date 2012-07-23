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
