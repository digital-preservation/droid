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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.openide.util.NbBundle;

import uk.gov.nationalarchives.droid.export.interfaces.ExportOptions;
import uk.gov.nationalarchives.droid.gui.DroidMainFrame;
import uk.gov.nationalarchives.droid.gui.ProfileForm;

/**
 *
 * @author rflitcroft
 */
public class ExportDialog extends JDialog {

    private static final long serialVersionUID = -4598078880004073202L;
    private static final int CAPACITY = 128;

    private DroidMainFrame droidMain;
    private DefaultTableModel tableModel;
    private List<ProfileWrapper> profilesRowData;
    private boolean approved;
    
    /** 
     * Creates new form ReportDialog.
     * @param parent the dialog's parent
     */
    public ExportDialog(final DroidMainFrame parent) {
        super(parent);
        droidMain = parent;
        setModal(true);
        
        initComponents();
        jScrollPane1.getViewport().setBackground(profileSelectTable.getBackground());
        pack();
        setLocationRelativeTo(getParent());
        
    }
    
    /**
     * Shows the dialog in modal mode.
     */
    public void showDialog() {

        profilesRowData = new ArrayList<ProfileWrapper>();
        Collection<ProfileForm> profiles = droidMain.getDroidContext().allProfiles();
        for (ProfileForm profile : profiles) {
            profilesRowData.add(new ProfileWrapper(profile));
        }
        
        tableModel = new DefaultTableModel(0, 1) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return ProfileForm.class;
            }
            
            @Override
            public boolean isCellEditable(int row, int column) {
                return profilesRowData.get(row).getProfile().getProfile().getState().isReportable();
            }
        };

        for (ProfileWrapper profile : profilesRowData) {
            Object[] row = new Object[] {profile}; 
            tableModel.addRow(row);
        }
        
        profileSelectTable.setModel(tableModel);
        
        profileSelectTable.setDefaultEditor(ProfileForm.class, new CheckBoxEditor());
        profileSelectTable.setDefaultRenderer(ProfileForm.class, new CheckBoxRenderer());
        
        jScrollPane1.setColumnHeaderView(null);
        profileSelectTable.setCellSelectionEnabled(false);
        
        enableGenerateButton();
        approved = false;
        setVisible(true);
    }
    
    
    /**
     * 
     * @return ExportOptions - what sort of export to perform.
     */
    public ExportOptions getExportOptions() {
        if (RadioOneRowPerFile.isSelected()) {
            return ExportOptions.ONE_ROW_PER_FILE;
        }
        return ExportOptions.ONE_ROW_PER_FORMAT;
    }

    /**
     * Get the character Output Encoding for the export file.
     *
     * @return The character encoding for the export file
     */
    public String getOutputEncoding() {
        final CharsetEncodingItem encoding = (CharsetEncodingItem) cmdEncoding.getSelectedItem();
        return encoding.getCharset().name();
    }

    /**
     * Get BOM flag for output encoding.
     * @return BOM flag status.
     */
    public boolean isBom() {
        final CharsetEncodingItem encoding = (CharsetEncodingItem) cmdEncoding.getSelectedItem();
        return encoding.isBom();
    }
    
    /**
     * 
     * @param options The export options.
     */
    public void setExportOptions(ExportOptions options) {
        if (options.equals(ExportOptions.ONE_ROW_PER_FILE)) {
            RadioOneRowPerFile.setSelected(true);
        } else if (options.equals(ExportOptions.ONE_ROW_PER_FORMAT)) {
            RadioOneRowPerIdentification.setSelected(true);
        }
    }
    
    /**
     * @return the profilesRowData
     */
    public List<String> getSelectedProfileIds() {
        List<String> selectedProfiles = new ArrayList<String>();
        
        for (ProfileWrapper profileWrapper : profilesRowData) {
            if (profileWrapper.isSelected()) {
                selectedProfiles.add(profileWrapper.getProfile().getProfile().getUuid());
            }
        }
        
        return selectedProfiles;
    }

    /**
     * @return Whether all columns should be quoted.
     */
    public boolean getQuoteAllColumns() {
        return jCheckBoxQuoteAll.isSelected();
    }

    /**
     * @return A space separated string containing all column names to export.
     */
    public String getColumnsToExport() {
        StringBuilder builder = new StringBuilder(CAPACITY);
        addColumn("ID", jCheckBoxId.isSelected(), builder);
        addColumn("PARENT_ID", jCheckBoxParentId.isSelected(), builder);
        addColumn("URI", jCheckBoxURI.isSelected(), builder);
        addColumn("FILE_PATH", jCheckBoxFilePath.isSelected(), builder);
        addColumn("NAME", jCheckBoxFileName.isSelected(), builder);
        addColumn("METHOD", jCheckBoxIdMethod.isSelected(), builder);
        addColumn("STATUS", jCheckBoxStatus.isSelected(), builder);
        addColumn("SIZE", jCheckBoxFileSize.isSelected(), builder);
        addColumn("TYPE", jCheckBoxResourceType.isSelected(), builder);
        addColumn("EXT", jCheckBoxExtension.isSelected(), builder);
        addColumn("LAST_MODIFIED", jCheckBoxLastModified.isSelected(), builder);
        addColumn("EXTENSION_MISMATCH", jCheckBoxExtMismatch.isSelected(), builder);
        addColumn("HASH", jCheckBoxFileHash.isSelected(), builder);
        addColumn("FORMAT_COUNT", jCheckBoxIdCount.isSelected(), builder);
        addColumn("PUID", jCheckBoxPUID.isSelected(), builder);
        addColumn("MIME_TYPE", jCheckBoxMIMEtype.isSelected(), builder);
        addColumn("FORMAT_NAME", jCheckBoxFormatName.isSelected(), builder);
        addColumn("FORMAT_VERSION", jCheckBoxFormatVersion.isSelected(), builder);
        return builder.toString().trim();
    }

    private void addColumn(String columnName, boolean selected, StringBuilder builder) {
        if (selected) {
            builder.append(columnName).append(' ');
        }
    }

    private void cancelButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        
        setVisible(false);
        dispose();
        
    }//GEN-LAST:event_cancelButtonActionPerformed

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new ButtonGroup();
        profileSelectLabel = new JLabel();
        jScrollPane1 = new JScrollPane();
        profileSelectTable = new JTable();
        jPanel1 = new JPanel();
        cancelButton = new JButton();
        exportButton = new JButton();
        RadioOneRowPerFile = new JRadioButton();
        RadioOneRowPerIdentification = new JRadioButton();
        cmdEncoding = new JComboBox();
        jLabel1 = new JLabel();
        jCheckBoxQuoteAll = new JCheckBox();
        toggleColumnButton = new JButton();
        jButtonSetAllColumns = new JButton();
        jPanel2 = new JPanel();
        profileSelectLabel1 = new JLabel();
        jCheckBoxId = new JCheckBox();
        jCheckBoxParentId = new JCheckBox();
        jCheckBoxURI = new JCheckBox();
        jCheckBoxFilePath = new JCheckBox();
        jCheckBoxFileName = new JCheckBox();
        jCheckBoxFileSize = new JCheckBox();
        jCheckBoxLastModified = new JCheckBox();
        jCheckBoxExtension = new JCheckBox();
        jCheckBoxResourceType = new JCheckBox();
        jCheckBoxIdMethod = new JCheckBox();
        jCheckBoxStatus = new JCheckBox();
        jCheckBoxIdCount = new JCheckBox();
        jCheckBoxPUID = new JCheckBox();
        jCheckBoxFormatName = new JCheckBox();
        jCheckBoxMIMEtype = new JCheckBox();
        jCheckBoxFormatVersion = new JCheckBox();
        jCheckBoxExtMismatch = new JCheckBox();
        jCheckBoxFileHash = new JCheckBox();

        setTitle(NbBundle.getMessage(ExportDialog.class, "ExportDialog.title_1")); // NOI18N
        setAlwaysOnTop(true);
        setName("exportDialog"); // NOI18N

        profileSelectLabel.setText(NbBundle.getMessage(ExportDialog.class, "ExportDialog.profileSelectLabel.text_1")); // NOI18N

        jScrollPane1.setPreferredSize(new Dimension(300, 402));

        profileSelectTable.setModel(new DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Title 1"
            }
        ));
        profileSelectTable.setRowHeight(20);
        profileSelectTable.setRowSelectionAllowed(false);
        profileSelectTable.setShowHorizontalLines(false);
        profileSelectTable.setShowVerticalLines(false);
        profileSelectTable.setTableHeader(null);
        jScrollPane1.setViewportView(profileSelectTable);

        cancelButton.setText(NbBundle.getMessage(ExportDialog.class, "ExportDialog.cancelButton.text")); // NOI18N
        cancelButton.setVerticalAlignment(SwingConstants.BOTTOM);
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        exportButton.setText(NbBundle.getMessage(ExportDialog.class, "ExportDialog.generateButton.text")); // NOI18N
        exportButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(RadioOneRowPerFile);
        RadioOneRowPerFile.setSelected(true);
        RadioOneRowPerFile.setText(NbBundle.getMessage(ExportDialog.class, "ExportDialog.RadioOneRowPerFile.text")); // NOI18N
        RadioOneRowPerFile.setToolTipText(NbBundle.getMessage(ExportDialog.class, "ExportDialog.RadioOneRowPerFile.toolTipText")); // NOI18N

        buttonGroup1.add(RadioOneRowPerIdentification);
        RadioOneRowPerIdentification.setText(NbBundle.getMessage(ExportDialog.class, "ExportDialog.RadioOneRowPerIdentification.text")); // NOI18N
        RadioOneRowPerIdentification.setToolTipText(NbBundle.getMessage(ExportDialog.class, "ExportDialog.RadioOneRowPerIdentification.toolTipText")); // NOI18N

        cmdEncoding.setModel(getOutputEncodings());
        cmdEncoding.setEditor(null);

        jLabel1.setText(NbBundle.getMessage(ExportDialog.class, "ExportDialog.jLabel1.text_1")); // NOI18N

        jCheckBoxQuoteAll.setSelected(true);
        jCheckBoxQuoteAll.setText(NbBundle.getMessage(ExportDialog.class, "ExportDialog.jCheckBoxQuoteAll.text")); // NOI18N
        jCheckBoxQuoteAll.setToolTipText(NbBundle.getMessage(ExportDialog.class, "ExportDialog.jCheckBoxQuoteAll.toolTipText")); // NOI18N
        jCheckBoxQuoteAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jCheckBoxQuoteAllActionPerformed(evt);
            }
        });

        toggleColumnButton.setText(NbBundle.getMessage(ExportDialog.class, "ExportDialog.toggleColumnButton.text")); // NOI18N
        toggleColumnButton.setToolTipText(NbBundle.getMessage(ExportDialog.class, "ExportDialog.toggleColumnButton.toolTipText")); // NOI18N
        toggleColumnButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                toggleColumnButtonActionPerformed(evt);
            }
        });

        jButtonSetAllColumns.setText(NbBundle.getMessage(ExportDialog.class, "ExportDialog.jButtonSetAllColumns.text")); // NOI18N
        jButtonSetAllColumns.setToolTipText(NbBundle.getMessage(ExportDialog.class, "ExportDialog.jButtonSetAllColumns.toolTipText")); // NOI18N
        jButtonSetAllColumns.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jButtonSetAllColumnsActionPerformed(evt);
            }
        });

        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(cmdEncoding, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(exportButton)
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, 125, GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(RadioOneRowPerFile)
                        .addGap(18, 18, 18)
                        .addComponent(RadioOneRowPerIdentification)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jCheckBoxQuoteAll)
                        .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButtonSetAllColumns)
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addComponent(toggleColumnButton)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(toggleColumnButton)
                    .addComponent(jCheckBoxQuoteAll)
                    .addComponent(jButtonSetAllColumns))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(RadioOneRowPerFile)
                    .addComponent(RadioOneRowPerIdentification))
                .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(exportButton)
                    .addComponent(cmdEncoding, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addContainerGap())
        );

        profileSelectLabel1.setText(NbBundle.getMessage(ExportDialog.class, "ExportDialog.profileSelectLabel1.text")); // NOI18N

        jCheckBoxId.setSelected(true);
        jCheckBoxId.setText(NbBundle.getMessage(ExportDialog.class, "ExportDialog.jCheckBoxId.text")); // NOI18N
        jCheckBoxId.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jCheckBoxIdActionPerformed(evt);
            }
        });

        jCheckBoxParentId.setSelected(true);
        jCheckBoxParentId.setText(NbBundle.getMessage(ExportDialog.class, "ExportDialog.jCheckBoxParentId.text")); // NOI18N

        jCheckBoxURI.setSelected(true);
        jCheckBoxURI.setText(NbBundle.getMessage(ExportDialog.class, "ExportDialog.jCheckBoxURI.text")); // NOI18N

        jCheckBoxFilePath.setSelected(true);
        jCheckBoxFilePath.setText(NbBundle.getMessage(ExportDialog.class, "ExportDialog.jCheckBoxFilePath.text")); // NOI18N

        jCheckBoxFileName.setSelected(true);
        jCheckBoxFileName.setText(NbBundle.getMessage(ExportDialog.class, "ExportDialog.jCheckBoxFileName.text")); // NOI18N

        jCheckBoxFileSize.setSelected(true);
        jCheckBoxFileSize.setText(NbBundle.getMessage(ExportDialog.class, "ExportDialog.jCheckBoxFileSize.text")); // NOI18N

        jCheckBoxLastModified.setSelected(true);
        jCheckBoxLastModified.setText(NbBundle.getMessage(ExportDialog.class, "ExportDialog.jCheckBoxLastModified.text")); // NOI18N
        jCheckBoxLastModified.setActionCommand(NbBundle.getMessage(ExportDialog.class, "ExportDialog.jCheckBoxLastModified.actionCommand")); // NOI18N

        jCheckBoxExtension.setSelected(true);
        jCheckBoxExtension.setText(NbBundle.getMessage(ExportDialog.class, "ExportDialog.jCheckBoxExtension.text")); // NOI18N
        jCheckBoxExtension.setActionCommand(NbBundle.getMessage(ExportDialog.class, "ExportDialog.jCheckBoxExtension.actionCommand")); // NOI18N

        jCheckBoxResourceType.setSelected(true);
        jCheckBoxResourceType.setText(NbBundle.getMessage(ExportDialog.class, "ExportDialog.jCheckBoxResourceType.text")); // NOI18N

        jCheckBoxIdMethod.setSelected(true);
        jCheckBoxIdMethod.setText(NbBundle.getMessage(ExportDialog.class, "ExportDialog.jCheckBoxIdMethod.text")); // NOI18N

        jCheckBoxStatus.setSelected(true);
        jCheckBoxStatus.setText(NbBundle.getMessage(ExportDialog.class, "ExportDialog.jCheckBoxStatus.text")); // NOI18N

        jCheckBoxIdCount.setSelected(true);
        jCheckBoxIdCount.setText(NbBundle.getMessage(ExportDialog.class, "ExportDialog.jCheckBoxIdCount.text")); // NOI18N
        jCheckBoxIdCount.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jCheckBoxIdCountActionPerformed(evt);
            }
        });

        jCheckBoxPUID.setSelected(true);
        jCheckBoxPUID.setText(NbBundle.getMessage(ExportDialog.class, "ExportDialog.jCheckBoxPUID.text")); // NOI18N

        jCheckBoxFormatName.setSelected(true);
        jCheckBoxFormatName.setText(NbBundle.getMessage(ExportDialog.class, "ExportDialog.jCheckBoxFormatName.text")); // NOI18N
        jCheckBoxFormatName.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jCheckBoxFormatNameActionPerformed(evt);
            }
        });

        jCheckBoxMIMEtype.setSelected(true);
        jCheckBoxMIMEtype.setText(NbBundle.getMessage(ExportDialog.class, "ExportDialog.jCheckBoxMIMEtype.text")); // NOI18N

        jCheckBoxFormatVersion.setSelected(true);
        jCheckBoxFormatVersion.setText(NbBundle.getMessage(ExportDialog.class, "ExportDialog.jCheckBoxFormatVersion.text")); // NOI18N

        jCheckBoxExtMismatch.setSelected(true);
        jCheckBoxExtMismatch.setText(NbBundle.getMessage(ExportDialog.class, "ExportDialog.jCheckBoxExtMismatch.text")); // NOI18N
        jCheckBoxExtMismatch.setToolTipText(NbBundle.getMessage(ExportDialog.class, "ExportDialog.jCheckBoxExtMismatch.toolTipText")); // NOI18N

        jCheckBoxFileHash.setSelected(true);
        jCheckBoxFileHash.setText(NbBundle.getMessage(ExportDialog.class, "ExportDialog.jCheckBoxFileHash.text")); // NOI18N

        GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(jPanel2Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(jCheckBoxId)
                    .addComponent(profileSelectLabel1)
                    .addComponent(jCheckBoxParentId)
                    .addComponent(jCheckBoxURI)
                    .addComponent(jCheckBoxFilePath)
                    .addComponent(jCheckBoxFileName)
                    .addComponent(jCheckBoxFileSize)
                    .addComponent(jCheckBoxLastModified)
                    .addComponent(jCheckBoxExtension)
                    .addComponent(jCheckBoxExtMismatch))
                .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(jCheckBoxFileHash)
                    .addGroup(jPanel2Layout.createParallelGroup(Alignment.LEADING)
                        .addComponent(jCheckBoxIdCount, Alignment.TRAILING)
                        .addComponent(jCheckBoxMIMEtype))
                    .addComponent(jCheckBoxFormatName)
                    .addGroup(jPanel2Layout.createParallelGroup(Alignment.LEADING)
                        .addGroup(Alignment.TRAILING, jPanel2Layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(jCheckBoxIdMethod)
                            .addComponent(jCheckBoxStatus)
                            .addComponent(jCheckBoxFormatVersion))
                        .addComponent(jCheckBoxPUID))
                    .addComponent(jCheckBoxResourceType))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(jPanel2Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(profileSelectLabel1)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jCheckBoxId)
                    .addComponent(jCheckBoxPUID))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jCheckBoxParentId)
                    .addComponent(jCheckBoxFormatName))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jCheckBoxURI)
                    .addComponent(jCheckBoxFormatVersion))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jCheckBoxFilePath)
                    .addComponent(jCheckBoxMIMEtype))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jCheckBoxFileName)
                    .addComponent(jCheckBoxIdCount))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jCheckBoxFileSize, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBoxIdMethod))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jCheckBoxLastModified)
                    .addComponent(jCheckBoxStatus))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jCheckBoxExtension)
                    .addComponent(jCheckBoxResourceType))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jCheckBoxExtMismatch)
                    .addComponent(jCheckBoxFileHash))
                .addGap(0, 9, Short.MAX_VALUE))
        );

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
            .addComponent(jPanel1, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(profileSelectLabel)
                    .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 300, GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, Short.MAX_VALUE)
                .addComponent(jPanel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(profileSelectLabel)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                    .addComponent(jPanel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jCheckBoxQuoteAllActionPerformed(ActionEvent evt) {//GEN-FIRST:event_jCheckBoxQuoteAllActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jCheckBoxQuoteAllActionPerformed

    private void jCheckBoxFormatNameActionPerformed(ActionEvent evt) {//GEN-FIRST:event_jCheckBoxFormatNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jCheckBoxFormatNameActionPerformed

    private void jCheckBoxIdCountActionPerformed(ActionEvent evt) {//GEN-FIRST:event_jCheckBoxIdCountActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jCheckBoxIdCountActionPerformed

    private void jCheckBoxIdActionPerformed(ActionEvent evt) {//GEN-FIRST:event_jCheckBoxIdActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jCheckBoxIdActionPerformed

    private void toggleColumnButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_toggleColumnButtonActionPerformed
        jCheckBoxId.setSelected(!jCheckBoxId.isSelected());
        jCheckBoxParentId.setSelected(!jCheckBoxParentId.isSelected());
        jCheckBoxURI.setSelected(!jCheckBoxURI.isSelected());
        jCheckBoxFilePath.setSelected(!jCheckBoxFilePath.isSelected());
        jCheckBoxFileName.setSelected(!jCheckBoxFileName.isSelected());
        jCheckBoxFileSize.setSelected(!jCheckBoxFileSize.isSelected());
        jCheckBoxLastModified.setSelected(!jCheckBoxLastModified.isSelected());
        jCheckBoxExtension.setSelected(!jCheckBoxExtension.isSelected());
        jCheckBoxExtMismatch.setSelected(!jCheckBoxExtMismatch.isSelected());
        jCheckBoxPUID.setSelected(!jCheckBoxPUID.isSelected());
        jCheckBoxFormatName.setSelected(!jCheckBoxFormatName.isSelected());
        jCheckBoxFormatVersion.setSelected(!jCheckBoxFormatVersion.isSelected());
        jCheckBoxMIMEtype.setSelected(!jCheckBoxMIMEtype.isSelected());
        jCheckBoxIdCount.setSelected(!jCheckBoxIdCount.isSelected());
        jCheckBoxIdMethod.setSelected(!jCheckBoxIdMethod.isSelected());
        jCheckBoxStatus.setSelected(!jCheckBoxStatus.isSelected());
        jCheckBoxResourceType.setSelected(!jCheckBoxResourceType.isSelected());
        jCheckBoxFileHash.setSelected(!jCheckBoxFileHash.isSelected());
    }//GEN-LAST:event_toggleColumnButtonActionPerformed

    private void jButtonSetAllColumnsActionPerformed(ActionEvent evt) {//GEN-FIRST:event_jButtonSetAllColumnsActionPerformed
       jCheckBoxId.setSelected(true);
        jCheckBoxParentId.setSelected(true);
        jCheckBoxURI.setSelected(true);
        jCheckBoxFilePath.setSelected(true);
        jCheckBoxFileName.setSelected(true);
        jCheckBoxFileSize.setSelected(true);
        jCheckBoxLastModified.setSelected(true);
        jCheckBoxExtension.setSelected(true);
        jCheckBoxExtMismatch.setSelected(true);
        jCheckBoxPUID.setSelected(true);
        jCheckBoxFormatName.setSelected(true);
        jCheckBoxFormatVersion.setSelected(true);
        jCheckBoxMIMEtype.setSelected(true);
        jCheckBoxIdCount.setSelected(true);
        jCheckBoxIdMethod.setSelected(true);
        jCheckBoxStatus.setSelected(true);
        jCheckBoxResourceType.setSelected(true);
        jCheckBoxFileHash.setSelected(true);
    }//GEN-LAST:event_jButtonSetAllColumnsActionPerformed

    /**
     * @param evt The event that triggers the action.
     */
    protected void exportButtonActionPerformed(ActionEvent evt) {
        // TODO Auto-generated method stub

        approved = true;
        setVisible(false);
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JRadioButton RadioOneRowPerFile;
    private JRadioButton RadioOneRowPerIdentification;
    private ButtonGroup buttonGroup1;
    private JButton cancelButton;
    private JComboBox cmdEncoding;
    private JButton exportButton;
    private JButton jButtonSetAllColumns;
    private JCheckBox jCheckBoxExtMismatch;
    private JCheckBox jCheckBoxExtension;
    private JCheckBox jCheckBoxFileHash;
    private JCheckBox jCheckBoxFileName;
    private JCheckBox jCheckBoxFilePath;
    private JCheckBox jCheckBoxFileSize;
    private JCheckBox jCheckBoxFormatName;
    private JCheckBox jCheckBoxFormatVersion;
    private JCheckBox jCheckBoxId;
    private JCheckBox jCheckBoxIdCount;
    private JCheckBox jCheckBoxIdMethod;
    private JCheckBox jCheckBoxLastModified;
    private JCheckBox jCheckBoxMIMEtype;
    private JCheckBox jCheckBoxPUID;
    private JCheckBox jCheckBoxParentId;
    private JCheckBox jCheckBoxQuoteAll;
    private JCheckBox jCheckBoxResourceType;
    private JCheckBox jCheckBoxStatus;
    private JCheckBox jCheckBoxURI;
    private JLabel jLabel1;
    private JPanel jPanel1;
    private JPanel jPanel2;
    private JScrollPane jScrollPane1;
    private JLabel profileSelectLabel;
    private JLabel profileSelectLabel1;
    private JTable profileSelectTable;
    private JButton toggleColumnButton;
    // End of variables declaration//GEN-END:variables

    private void enableGenerateButton() {
        boolean profileSelected = false;
        for (ProfileWrapper profileWrapper : profilesRowData) {
            if (profileWrapper.isSelected()) {
                profileSelected = true;
                break;
            }
        }
        
        exportButton.setEnabled(profileSelected);
    }

    /**
     * Simple tuple for holding combobox items
     * for character encoding.
     */
    class CharsetEncodingItem {
        private final String label;
        private final Charset charset;
        private final boolean bom;

        /**
         * @param label  The label to use in the combobox item
         * @param charset The character encoding value
         */
        public CharsetEncodingItem(final String label, final Charset charset) {
            this.label = label;
            this.charset = charset;
            this.bom = false;
        }

        /**
         * @param label  The label to use in the combobox item
         * @param charset The character encoding value
         * @param bom Add BOM to the file.
         */
        public CharsetEncodingItem(String label, Charset charset, boolean bom) {
            this.label = label;
            this.charset = charset;
            this.bom = bom;
        }

        /**
         * Gets the label
         *
         * @return the label
         */
        public String getLabel() {
            return label;
        }

        /**
         * Gets the charset
         *
         * @return the charset
         */
        public Charset getCharset() {
            return charset;
        }

        /**
         * Gets bom.
         * @return bom flag.
         */
        public boolean isBom() {
            return bom;
        }

        /**
         * Returns the label
         *
         * @return The label
         */
        @Override
        public String toString() {
            return label;
        }
    }
    
    private ComboBoxModel getOutputEncodings() {
        final DefaultComboBoxModel model = new DefaultComboBoxModel();
        final Charset defaultEncoding = Charset.defaultCharset();
        final Charset utf8 = Charset.forName("UTF-8");

        model.addElement(new CharsetEncodingItem("UTF 8", utf8));
        model.addElement(
            new CharsetEncodingItem("Platform Specific (" +  defaultEncoding.name() + ")", defaultEncoding)
        );
        model.addElement(new CharsetEncodingItem("UTF 8 with BOM", utf8, true));
        
        return model;
    }
    
    
    private final class CheckBoxRenderer extends JCheckBox implements TableCellRenderer {
        
        private static final long serialVersionUID = -4078523535790396904L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            ProfileWrapper profile = (ProfileWrapper) value;
            
            setSelected(profile.isSelected());
            setText(profile.getProfile().getName());
            setOpaque(false);
            
            boolean enabled = profilesRowData.get(row).getProfile()
                .getProfile().getState().isReportable();
            setEnabled(enabled);
            return this;
        }
    }
    
    private final class CheckBoxEditor extends DefaultCellEditor {

        private static final long serialVersionUID = 8023412072260282004L;
        private ProfileWrapper profile;
        
        /**
         * Constructor
         */
        public CheckBoxEditor() {
            super(new JCheckBox());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, 
                boolean isSelected, int row, int column) {

            profile = (ProfileWrapper) value;
            
            final JCheckBox checkBox = (JCheckBox) getComponent();
            
            checkBox.setText(profile.getProfile().getName());
            checkBox.setSelected(profile.isSelected());
            checkBox.setOpaque(false);
            
            return checkBox;
        }
        
        /**
         * @see javax.swing.CellEditor#getCellEditorValue()
         */
        @Override
        public Object getCellEditorValue() {
            profile.setSelected(((JCheckBox) getComponent()).isSelected());
            return profile;
        }
    }
    
    private final class ProfileWrapper {
        
        private ProfileForm profile;
        private boolean selected;
        
        ProfileWrapper(ProfileForm profile) {
            this.profile = profile;
            selected = profile.getProfile().getState().isReportable(); // select exportable profiles by default.
        }
        
        /**
         * @param selected the selected to set
         */
        public void setSelected(boolean selected) {
            this.selected = selected;
            enableGenerateButton();
        }
        
        /**
         * @return the selected
         */
        public boolean isSelected() {
            return selected;
        }
        
        /**
         * @return the profile
         */
        public ProfileForm getProfile() {
            return profile;
        }
    }
    

    /**
     * @return true if the user approved the export generation; false otherwise.
     */
    public boolean isApproved() {
        return approved;
    }
}
