/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package uk.gov.nationalarchives.droid.gui.help;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author sparkhi
 */
public class AboutDialog extends javax.swing.JDialog {

    /**
     * Creates new form AboutDialog.
     *
     * @param parent
     * @param modal
     * @param data
     */
    public AboutDialog(java.awt.Frame parent, boolean modal, AboutDialogData data) {
        super(parent, modal);
        initComponents();
        initData(data);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonOk = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        txtJavaVersion = new javax.swing.JTextField();
        labelJavaLocation = new javax.swing.JLabel();
        txtJavaLocation = new javax.swing.JTextField();
        labelJavaLocation1 = new javax.swing.JLabel();
        txtOSName = new javax.swing.JTextField();
        labelVersion = new javax.swing.JLabel();
        txtVersion = new javax.swing.JTextField();
        labelJavaVersion = new javax.swing.JLabel();
        labelDroidFolder = new javax.swing.JLabel();
        txtDroidFolder = new javax.swing.JTextField();
        buttonOpenDroidFolder = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(AboutDialog.class, "AboutDialog.title_1")); // NOI18N
        setMinimumSize(new java.awt.Dimension(640, 380));
        setModal(true);
        setPreferredSize(new java.awt.Dimension(640, 370));

        buttonOk.setText(org.openide.util.NbBundle.getMessage(AboutDialog.class, "AboutDialog.buttonOk.text")); // NOI18N
        buttonOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonOkActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        txtJavaVersion.setEditable(false);
        txtJavaVersion.setText(org.openide.util.NbBundle.getMessage(AboutDialog.class, "AboutDialog.txtJavaVersion.text")); // NOI18N
        txtJavaVersion.setFocusable(false);

        labelJavaLocation.setText(org.openide.util.NbBundle.getMessage(AboutDialog.class, "AboutDialog.labelJavaLocation.text")); // NOI18N

        txtJavaLocation.setEditable(false);
        txtJavaLocation.setText(org.openide.util.NbBundle.getMessage(AboutDialog.class, "AboutDialog.txtJavaLocation.text")); // NOI18N
        txtJavaLocation.setFocusable(false);

        labelJavaLocation1.setText(org.openide.util.NbBundle.getMessage(AboutDialog.class, "AboutDialog.labelJavaLocation1.text")); // NOI18N

        txtOSName.setEditable(false);
        txtOSName.setText(org.openide.util.NbBundle.getMessage(AboutDialog.class, "AboutDialog.txtOSName.text")); // NOI18N
        txtOSName.setFocusable(false);

        labelVersion.setText(org.openide.util.NbBundle.getMessage(AboutDialog.class, "AboutDialog.labelVersion.text")); // NOI18N

        txtVersion.setEditable(false);
        txtVersion.setText(org.openide.util.NbBundle.getMessage(AboutDialog.class, "AboutDialog.txtVersion.text")); // NOI18N
        txtVersion.setFocusable(false);

        labelJavaVersion.setText(org.openide.util.NbBundle.getMessage(AboutDialog.class, "AboutDialog.labelJavaVersion.text")); // NOI18N

        labelDroidFolder.setText(org.openide.util.NbBundle.getMessage(AboutDialog.class, "AboutDialog.labelDroidFolder.text")); // NOI18N

        txtDroidFolder.setEditable(false);
        txtDroidFolder.setText(org.openide.util.NbBundle.getMessage(AboutDialog.class, "AboutDialog.txtDroidFolder.text")); // NOI18N
        txtDroidFolder.setFocusable(false);

        buttonOpenDroidFolder.setText(org.openide.util.NbBundle.getMessage(AboutDialog.class, "AboutDialog.buttonOpenDroidFolder.text")); // NOI18N
        buttonOpenDroidFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    buttonOpenDroidFolderActionPerformed(evt);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(labelDroidFolder)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(labelVersion)
                                    .addComponent(labelJavaVersion))
                                .addGap(24, 24, 24)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtVersion)
                                    .addComponent(txtJavaVersion)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(labelJavaLocation)
                                    .addComponent(labelJavaLocation1))
                                .addGap(23, 23, 23)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtOSName)
                                    .addComponent(txtJavaLocation)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(txtDroidFolder, javax.swing.GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(buttonOpenDroidFolder)))))
                        .addGap(17, 17, 17))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelVersion)
                    .addComponent(txtVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelJavaVersion)
                    .addComponent(txtJavaVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelJavaLocation)
                    .addComponent(txtJavaLocation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelJavaLocation1)
                    .addComponent(txtOSName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelDroidFolder)
                    .addComponent(txtDroidFolder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonOpenDroidFolder))
                .addContainerGap(78, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(buttonOk)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(19, 19, 19))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(buttonOk)
                .addGap(18, 18, 18))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonOkActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_buttonOkActionPerformed

    private void buttonOpenDroidFolderActionPerformed(java.awt.event.ActionEvent evt) throws IOException {//GEN-FIRST:event_buttonOpenDroidFolderActionPerformed
        File file = new File(txtDroidFolder.getText());
        Desktop.getDesktop().open(file);
    }//GEN-LAST:event_buttonOpenDroidFolderActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonOk;
    private javax.swing.JButton buttonOpenDroidFolder;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel labelDroidFolder;
    private javax.swing.JLabel labelJavaLocation;
    private javax.swing.JLabel labelJavaLocation1;
    private javax.swing.JLabel labelJavaVersion;
    private javax.swing.JLabel labelVersion;
    private javax.swing.JTextField txtDroidFolder;
    private javax.swing.JTextField txtJavaLocation;
    private javax.swing.JTextField txtJavaVersion;
    private javax.swing.JTextField txtOSName;
    private javax.swing.JTextField txtVersion;
    // End of variables declaration//GEN-END:variables

    private void initData(AboutDialogData data) {
        txtVersion.setText(data.getDroidVersion());
        txtJavaVersion.setText(data.getJavaVersion());
        txtJavaLocation.setText(data.getJavaLocation());
        txtOSName.setText(data.getOperatingSystem());
        txtDroidFolder.setText(data.getDroidFolder());
    }
}