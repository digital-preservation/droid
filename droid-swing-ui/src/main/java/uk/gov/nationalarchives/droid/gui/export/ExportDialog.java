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
package uk.gov.nationalarchives.droid.gui.export;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
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
        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(175, Short.MAX_VALUE)
                .addComponent(exportButton)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, 125, GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(RadioOneRowPerFile)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(RadioOneRowPerIdentification)
                .addContainerGap(127, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(RadioOneRowPerFile)
                    .addComponent(RadioOneRowPerIdentification))
                .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
                    .addComponent(exportButton))
                .addContainerGap())
        );

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addComponent(jPanel1, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 411, Short.MAX_VALUE)
                    .addComponent(profileSelectLabel))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(profileSelectLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 232, Short.MAX_VALUE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

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
    private JButton exportButton;
    private JPanel jPanel1;
    private JScrollPane jScrollPane1;
    private JLabel profileSelectLabel;
    private JTable profileSelectTable;
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
         * @param checkBox
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
