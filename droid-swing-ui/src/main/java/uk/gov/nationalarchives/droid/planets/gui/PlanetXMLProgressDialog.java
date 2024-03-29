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
package uk.gov.nationalarchives.droid.planets.gui;

import java.awt.event.WindowEvent;

import uk.gov.nationalarchives.droid.report.interfaces.ReportManager;

/**
 * @author Alok Kumar Dash
 */
public class PlanetXMLProgressDialog extends javax.swing.JDialog {

    private PlanetsXMLGenerationSwingWorker worker;

    /**
     * Creates new form PlanetXMLProgressDialog.
     * 
     * @param parent
     *            Parent Dialog.
     * @param modal
     *            modal to parent.
     * @param filePath
     *            file Path to save.
     * @param profileId
     *            ProfileId.
     * @param reportManager
     *            ReportManager.
     * */
    public PlanetXMLProgressDialog(java.awt.Frame parent, boolean modal,
            String filePath, String profileId, ReportManager reportManager) {
        super(parent, modal);
        initComponents();
        planetXMLFileName.setText(filePath);
        jLabel1.setText("PLANETS XML is being generated at :");
        cancelButton.setText("Cancel");
        okButton.setText("Ok");
        okButton.hide();

        worker = new PlanetsXMLGenerationSwingWorker(profileId,
                planetXMLGenerationProgressBar, filePath, cancelButton,
                okButton, jLabel1, reportManager);
        worker.execute();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed"
    // desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        planetXMLFileName = new javax.swing.JLabel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        planetXMLGenerationProgressBar = new javax.swing.JProgressBar();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        // planetXMLFileName.setText(org.openide.util.NbBundle.getMessage(PlanetXMLProgressDialog.class,
        // "PlanetXMLProgressDialog.planetXMLFileName.text")); // NOI18N

        // okButton.setText(org.openide.util.NbBundle.getMessage(PlanetXMLProgressDialog.class,
        // "PlanetXMLProgressDialog.okButton.text")); // NOI18N
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        // cancelButton.setText(org.openide.util.NbBundle.getMessage(PlanetXMLProgressDialog.class,
        // "PlanetXMLProgressDialog.cancelButton.text")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        planetXMLGenerationProgressBar
                .addPropertyChangeListener(new java.beans.PropertyChangeListener() {
                    public void propertyChange(
                            java.beans.PropertyChangeEvent evt) {
                        planetXMLGenerationProgressBarPropertyChange(evt);
                    }
                });

        // jLabel1.setText(org.openide.util.NbBundle.getMessage(PlanetXMLProgressDialog.class,
        // "PlanetXMLProgressDialog.jLabel1.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(
                getContentPane());
        getContentPane().setLayout(layout);
        layout
                .setHorizontalGroup(layout
                        .createParallelGroup(
                                javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(
                                layout
                                        .createSequentialGroup()
                                        .addGroup(
                                                layout
                                                        .createParallelGroup(
                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(
                                                                layout
                                                                        .createSequentialGroup()
                                                                        .addContainerGap()
                                                                        .addGroup(
                                                                                layout
                                                                                        .createParallelGroup(
                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                        .addGroup(
                                                                                                layout
                                                                                                        .createSequentialGroup()
                                                                                                        .addComponent(
                                                                                                                jLabel1)
                                                                                                        .addPreferredGap(
                                                                                                          javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                                                        .addComponent(
                                                                                                                planetXMLFileName,
                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                244,
                                                                                                                Short.MAX_VALUE))
                                                                                        .addComponent(
                                                                                                planetXMLGenerationProgressBar,
                                                                                                javax.swing.GroupLayout.Alignment.TRAILING,
                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                486,
                                                                                                Short.MAX_VALUE)))
                                                        .addGroup(
                                                                layout
                                                                        .createSequentialGroup()
                                                                        .addGap(
                                                                                217,
                                                                                217,
                                                                                217)
                                                                        .addComponent(
                                                                                okButton)
                                                                        .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                                                169,
                                                                                Short.MAX_VALUE)
                                                                        .addComponent(
                                                                                cancelButton)))
                                        .addContainerGap()));
        layout
                .setVerticalGroup(layout
                        .createParallelGroup(
                                javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(
                                layout
                                        .createSequentialGroup()
                                        .addGap(15, 15, 15)
                                        .addGroup(
                                                layout
                                                        .createParallelGroup(
                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(jLabel1)
                                                        .addComponent(
                                                                planetXMLFileName))
                                        .addGap(18, 18, 18)
                                        .addComponent(
                                                planetXMLGenerationProgressBar,
                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addGroup(
                                                layout
                                                        .createParallelGroup(
                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(okButton)
                                                        .addComponent(
                                                                cancelButton))
                                        .addContainerGap(41, Short.MAX_VALUE)));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void planetXMLGenerationProgressBarPropertyChange(
            java.beans.PropertyChangeEvent evt) {// GEN-FIRST:event_planetXMLGenerationProgressBarPropertyChange

    }// GEN-LAST:event_planetXMLGenerationProgressBarPropertyChange

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cancelButtonActionPerformed
        worker.cancel(true);
        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }// GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_okButtonActionPerformed
        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }// GEN-LAST:event_okButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel planetXMLFileName;
    private javax.swing.JProgressBar planetXMLGenerationProgressBar;
    // End of variables declaration//GEN-END:variables

}
