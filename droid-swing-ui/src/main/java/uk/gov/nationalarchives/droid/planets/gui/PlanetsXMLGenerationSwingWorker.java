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
package uk.gov.nationalarchives.droid.planets.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import uk.gov.nationalarchives.droid.report.interfaces.ReportManager;
import uk.gov.nationalarchives.droid.results.handlers.ProgressObserver;

/**
 * @author Alok Kumar Dash
 */
public class PlanetsXMLGenerationSwingWorker extends SwingWorker {

    private ClassPathXmlApplicationContext context;
    private JProgressBar planetXMLGenerationProgressBar;
    private String filePath;
    private JButton cancelButton;
    private JButton okButton;
    private JLabel label;
    private String profileId;

    private ReportManager reportManager;

    /**
     */

    /**
     * 
     * @param planetXMLGenerationProgressBar
     *            Progress Bar for progress update.
     * @param filePath
     *            File path where planets xml to be saved.
     * @param cancelButton
     *            Cancel button
     * @param okButton
     *            Ok Button.
     * @param label
     *            Label to chege the message from doing to done.
     * @param profileId
     *            ProfileId
     * @param reportManager
     *            ReportManager
     */

    public PlanetsXMLGenerationSwingWorker(String profileId,
            JProgressBar planetXMLGenerationProgressBar, String filePath,
            JButton cancelButton, JButton okButton, JLabel label,
            ReportManager reportManager) {
        this.planetXMLGenerationProgressBar = planetXMLGenerationProgressBar;
        this.filePath = filePath;
        this.cancelButton = cancelButton;
        this.okButton = okButton;
        this.label = label;
        this.profileId = profileId;
        this.reportManager = reportManager;
    }

    @Override
    protected Integer doInBackground() {
        final ProgressObserver observer = new ProgressObserver() {
            @Override
            public void onProgress(Integer progress) {
                setProgress(progress);
            }
        };
        addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("progress".equals(evt.getPropertyName())) {
                    Integer value = (Integer) evt.getNewValue();
                    planetXMLGenerationProgressBar.setValue(value);
                }
            }
        });

        reportManager.setObserver(observer);

        reportManager.generatePlanetsXML(profileId, filePath);

        return null;
    }

    @Override
    protected void done() {
        cancelButton.hide();
        okButton.show();
        label.setText("PLANETS XML is at : ");
    }

    /**
     * @param reportManager
     *            the reportManager to set
     */
    public void setReportManager(ReportManager reportManager) {
        this.reportManager = reportManager;
    }

}
