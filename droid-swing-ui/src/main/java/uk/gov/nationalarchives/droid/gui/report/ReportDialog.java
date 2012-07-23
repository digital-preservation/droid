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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.openide.util.NbBundle;

import uk.gov.nationalarchives.droid.gui.DroidMainFrame;
import uk.gov.nationalarchives.droid.gui.ProfileForm;
import uk.gov.nationalarchives.droid.report.interfaces.ReportSpec;

/**
 *
 * @author rflitcroft
 */
public class ReportDialog extends JDialog {
    
    private static final long serialVersionUID = -4598078880004073202L;

    private DroidMainFrame droidMain;
    private DefaultTableModel tableModel;
    private List<ProfileWrapper> profilesRowData;
    private boolean approved;

    /** 
     * Creates new form ReportDialog.
     * @param parent the dialog's parent
     */
    public ReportDialog(final DroidMainFrame parent) {
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
        
        reportSelectCombo.setEditable(false);
        ListCellRenderer renderer = new ReportSpecRenderer();
        reportSelectCombo.setRenderer(renderer);
        
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        
        final List<ReportSpec> reports = droidMain.getGlobalContext().getReportManager().listReportSpecs();
        for (ReportSpec reportSpec : reports) {
            model.addElement(reportSpec);
        }
        reportSelectCombo.setModel(model);
        enableGenerateButton();
        approved = false;
        setVisible(true);
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
    @SuppressWarnings("unchecked")   
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {



        cancelButton = new JButton();
        generateButton = new JButton();
        profileSelectLabel = new JLabel();
        jScrollPane1 = new JScrollPane();
        profileSelectTable = new JTable();
        reportSelectLabel = new JLabel();
        reportSelectCombo = new JComboBox();

        setTitle(NbBundle.getMessage(ReportDialog.class, "ReportDialog.title_1")); // NOI18N
        cancelButton.setText(NbBundle.getMessage(ReportDialog.class, "ReportDialog.cancelButton.text")); // NOI18N
        cancelButton.setVerticalAlignment(SwingConstants.BOTTOM);
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        generateButton.setText(NbBundle.getMessage(ReportDialog.class, "ReportDialog.generateButton.text")); // NOI18N
        generateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                generateButtonActionPerformed(evt);
            }
        });

        profileSelectLabel.setText(NbBundle.getMessage(ReportDialog.class, "ReportDialog.profileSelectLabel.text_1")); // NOI18N
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


        reportSelectLabel.setText(NbBundle.getMessage(ReportDialog.class, "ReportDialog.reportSelectLabel.text")); // NOI18N
        reportSelectCombo.setModel(new DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                    .addComponent(jScrollPane1, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
                    .addComponent(profileSelectLabel, Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(generateButton)
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, 125, GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(reportSelectLabel)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(reportSelectCombo, 0, 384, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(profileSelectLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(reportSelectCombo, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
                    .addComponent(reportSelectLabel))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
                    .addComponent(generateButton))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void generateButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        approved = true;
        setVisible(false);
    }//GEN-LAST:event_jButton2ActionPerformed

//    private File getSuggestedReportFile() {
//        File suggestedFile = null;
//        ReportSpec spec = getSelectedReportSpec();
//        if (spec != null) {
//            File dir = saveReportFileChooser.getCurrentDirectory();    
//            suggestedFile = new File(dir, spec.getName() + ".xml");
//        }
//        return suggestedFile;
//    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton cancelButton;
    private JButton generateButton;
    private JScrollPane jScrollPane1;
    private JLabel profileSelectLabel;
    private JTable profileSelectTable;
    private JComboBox reportSelectCombo;
    private JLabel reportSelectLabel;
    // End of variables declaration//GEN-END:variables

    private void enableGenerateButton() {
        boolean profileSelected = false;
        for (ProfileWrapper profileWrapper : profilesRowData) {
            if (profileWrapper.isSelected()) {
                profileSelected = true;
                break;
            }
        }
        
        generateButton.setEnabled(profileSelected);
    }
    
    /**
     * @return the selectedReportSpec
     */
    public ReportSpec getSelectedReportSpec() {
        return (ReportSpec) reportSelectCombo.getSelectedItem();
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
    
    private final class ReportSpecRenderer extends JLabel implements ListCellRenderer {

        private static final long serialVersionUID = 6009453525457078052L;

        public ReportSpecRenderer() {
            setOpaque(true);
        }
        
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            //FIXME: if there are no reports defined, value is not a ReportSpec
            // and we get an invalid cast operation here.  There must be a better
            // fix than testing for instance of ReportSpec...?
            if (value instanceof ReportSpec) {
                ReportSpec spec = (ReportSpec) value;
                setText(spec.getName());
            } else {
                setText("");
            }
            return this;
        }
    }

    /**
     * @return true if the user approved the report generation; false otherwise.
     */
    public boolean isApproved() {
        return approved;
    }
}
