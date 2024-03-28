/*
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
package uk.gov.nationalarchives.droid.gui.export;

import java.io.File;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.nationalarchives.droid.export.interfaces.ExportDetails;
import uk.gov.nationalarchives.droid.export.interfaces.ExportManager;
import uk.gov.nationalarchives.droid.export.interfaces.ExportOptions;
import uk.gov.nationalarchives.droid.gui.action.ActionDoneCallback;

/**
 * @author rflitcroft
 *
 */
public class ExportAction extends SwingWorker<Void, Integer> {

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private ExportManager exportManager;
    private File destination;
    private ExportOptions options;
    private String outputEncoding;
    private boolean bom;
    private boolean quoteAllFields;
    private String columnsToWrite;
    private String exportTemplatePath;
    private List<String> profileIds;
    private ActionDoneCallback<ExportAction> callback;
    
    private Future<?> exportTask;
    
    /**
     * Default Constructor. 
     */
    public ExportAction() {
    }

    /**
     * Parameterized constructor.
     * @param exportManager The exportmanager to use.
     * @param destination The destination file.
     * @param options The output options.
     * @param outputEncoding The output encoding.
     * @param bom use the bom flag in the output.
     * @param quoteAllFields whether to quote all fields in export, or just those that contain commas.
     * @param columnsToWrite A space separated list of columns to write, or null or empty for all columns.
     * @param profileIds The ids of the profiles to export.
     */
    public ExportAction(ExportManager exportManager, File destination, ExportOptions options, String outputEncoding,
                        boolean bom, boolean quoteAllFields, String columnsToWrite, List<String> profileIds) {
        setExportManager(exportManager);
        setDestination(destination);
        setExportOptions(options);
        setOutputEncoding(outputEncoding);
        setBom(bom);
        setProfileIds(profileIds);
        setQuoteAllFields(quoteAllFields);
    }

    /**
     * Initialsies the export action.
     */
    public void init() {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void done() {
        callback.done(this);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void process(List<Integer> chunks) {
        super.process(chunks);
    }
    
    /**
     * @return nothing.
     */
    @Override
    protected Void doInBackground() {
        //TODO: configure columns to write in UI.
        exportTask = exportManager.exportProfiles(profileIds, destination.getPath(), null, getExportDetails());
        try {
            exportTask.get();
        } catch (InterruptedException e) {
            log.debug(e.getMessage(), e);
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            log.error(e.getCause().toString(), e);
            throw new RuntimeException(e.getCause());
        } catch (CancellationException e) {
            cancel(false);
            log.info("Export task was cancelled");
        }
        return null;
    }
    
    /**
     * @param exportManager the exportManager to set
     */
    public void setExportManager(ExportManager exportManager) {
        this.exportManager = exportManager;
    }
    
    /**
     * @param destination the destination to set
     */
    public void setDestination(File destination) {
        this.destination = destination;
    }
    
    
    /**
     * 
     * @param opt The export options to use.
     */
    public void setExportOptions(ExportOptions opt) {
        this.options = opt;
    }
    
    /**
     * @param profileIds the profileIds to set
     */
    public void setProfileIds(List<String> profileIds) {
        this.profileIds = profileIds;
    }
    
    /**
     * @param callback the callback to set
     */
    public void setCallback(ActionDoneCallback<ExportAction> callback) {
        this.callback = callback;
    }

    /**
     * Cancels this export action.
     */
    public void cancel() {
        exportTask.cancel(true);
    }

    /**
     * Set's the character encoding for the export file.
     *
     * @param outputEncoding The character encoding to use in the output
     */
    public void setOutputEncoding(final String outputEncoding) {
        this.outputEncoding = outputEncoding;
    }

    /**
     * Set's encoding BOM flag.
     * @param bom Bom flag.
     */
    public void setBom(boolean bom) {
        this.bom = bom;
    }

    /**
     * @param quoteAllFields whether to quote all fields or just those that contain commas.
     */
    public void setQuoteAllFields(boolean quoteAllFields) {
        this.quoteAllFields = quoteAllFields;
    }

    /**
     * @param columnsToWrite A space separated list of columns to export, null or empty for all columns.
     */
    public void setColumnsToWrite(String columnsToWrite) {
        this.columnsToWrite = columnsToWrite;
    }

    /**
     * Set the absolute path of the export template to the ExportAction.
     * @param exportTemplatePath absolute path of the export template
     */
    public void setExportTemplatePath(String exportTemplatePath) {
        this.exportTemplatePath = exportTemplatePath;
    }

    /**
     *
     * @return the export details for this export command.
     */
    private ExportDetails getExportDetails() {
        ExportDetails.ExportDetailsBuilder builder = new ExportDetails.ExportDetailsBuilder();
        return builder.withExportOptions(options)
                .withOutpuEncoding(outputEncoding)
                .withBomFlag(bom)
                .withQuotingAllFields(quoteAllFields)
                .withColumnsToWrite(columnsToWrite)
                .withExportTemplatePath(exportTemplatePath)
                .build();
    }

}
