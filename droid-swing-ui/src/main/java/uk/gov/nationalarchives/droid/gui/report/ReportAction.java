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
package uk.gov.nationalarchives.droid.gui.report;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalConfig;
import uk.gov.nationalarchives.droid.report.interfaces.CancellableProgressObserver;
import uk.gov.nationalarchives.droid.report.interfaces.Report;
import uk.gov.nationalarchives.droid.report.interfaces.ReportCancelledException;
import uk.gov.nationalarchives.droid.report.interfaces.ReportManager;
import uk.gov.nationalarchives.droid.report.interfaces.ReportRequest;
import uk.gov.nationalarchives.droid.report.interfaces.ReportSpec;
import uk.gov.nationalarchives.droid.report.interfaces.ReportXmlWriter;

/**
 * @author Alok Kumar Dash
 * 
 */
public class ReportAction extends SwingWorker<Void, Integer> {

    private final Log log = LogFactory.getLog(getClass());
    
    private List<String> profileIds;
    private ReportManager reportManager;
    private ReportXmlWriter reportXmlWriter;
    private ReportSpec reportSpec;
    private ReportProgressDialog progressDialog;
    private ReportViewFrame viewDialog;
    private DroidGlobalConfig config;
    private ExportReportAction exportReportAction;
    
    private File targetFile;

    private CancellableProgressObserver backgroundProgressObserver;

    /**
     * Default constructor.
     */

    public ReportAction() {
        backgroundProgressObserver = new CancellableProgressObserver() {
            
            private boolean cancelled;
            
            @Override
            public void onProgress(Integer progress) {
                publish(progress);
            }
            
            @Override
            public void cancel() {
                cancelled = true;
            }
            
            @Override
            public boolean isCancelled() {
                return cancelled;
            }
        };
    }
    
    /**
     * @see javax.swing.SwingWorker#done()
     */
    @Override
    protected void done() {
        try {
            get();
        } catch (ExecutionException e) {
            log.error(e.getCause(), e);
        } catch (InterruptedException e) {
            log.debug(e);
        }
    }

    /**
     * Cancels a report.
     */
    public void cancel() {
        backgroundProgressObserver.cancel();
        progressDialog.setVisible(false);
        progressDialog.dispose();
    }

    /**
     * @param reportManager
     *            the reportManager to set
     */
    public void setReportManager(ReportManager reportManager) {
        this.reportManager = reportManager;
    }

    /**
     * @return Void void.
     * @see javax.swing.SwingWorker#doInBackground()
     */
    @Override
    protected Void doInBackground() {
        
        ReportRequest request = new ReportRequest();
        request.setReportSpec(reportSpec);
        request.setProfileIds(profileIds);

        FileWriter fileWriter = null;
        try {
            targetFile = File.createTempFile("report~", ".xml", config.getTempDir());
            Report report = reportManager.generateReport(request, null, backgroundProgressObserver);
            fileWriter = new FileWriter(targetFile);
            reportXmlWriter.writeReport(report, fileWriter);
        } catch (IOException e) {
            log.error(e);
            throw new RuntimeException(e);
        } catch (ReportCancelledException e) {
            cancel(false);
            log.info("Report cancelled by user.");
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    log.error("Error closing report writer", e);
                }
            }
        }
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void process(List<Integer> chunks) {
        for (Integer i : chunks) {
            progressDialog.onProgress(i);
        }
    }

    /**
     * @param profileIds
     *            the profileIds to set
     */
    public void setProfileIds(List<String> profileIds) {
        this.profileIds = profileIds;
    }
    
    /**
     * @param reportXmlWriter the reportXmlWriter to set
     */
    public void setReportXmlWriter(ReportXmlWriter reportXmlWriter) {
        this.reportXmlWriter = reportXmlWriter;
    }
    
    /**
     * @param reportSpec the reportSpec to set
     */
    public void setReportSpec(ReportSpec reportSpec) {
        this.reportSpec = reportSpec;
    }
    
    /**
     * @param viewDialog the viewDialog to set
     */
    public void setViewDialog(ReportViewFrame viewDialog) {
        this.viewDialog = viewDialog;
    }
    
    /**
     * @param config the config to set
     */
    public void setConfig(DroidGlobalConfig config) {
        this.config = config;
    }
    
    /**
     * @param progressDialog the progressDialog to set
     */
    public void setProgressDialog(final ReportProgressDialog progressDialog) {
        this.progressDialog = progressDialog;
        addPropertyChangeListener(new ReportFinishedListener());
    }
    
    /**
     * @param exportReportAction the exportReportAction to set
     */
    public void setExportReportAction(ExportReportAction exportReportAction) {
        this.exportReportAction = exportReportAction;
    }
    
    private final class ReportFinishedListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if ("state".equals(evt.getPropertyName()) 
                    && SwingWorker.StateValue.DONE == evt.getNewValue()) {
                progressDialog.setVisible(false);
                progressDialog.dispose();
                
                // now show the report in all its glory
                viewDialog.renderReport(targetFile, reportSpec.getXslTransforms());
                viewDialog.setExportAction(exportReportAction);
                viewDialog.setVisible(true);
            }
        }
    }
}
