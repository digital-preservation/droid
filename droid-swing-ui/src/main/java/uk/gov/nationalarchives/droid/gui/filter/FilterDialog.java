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
package uk.gov.nationalarchives.droid.gui.filter;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.xml.bind.JAXBException;


import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionFieldEnum;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionOperator;
import uk.gov.nationalarchives.droid.gui.DroidUIContext;
import uk.gov.nationalarchives.droid.gui.ProfileForm;
import uk.gov.nationalarchives.droid.gui.action.ApplyFilterToTreeTableAction;
import uk.gov.nationalarchives.droid.gui.filter.action.InitialiseFilterAction;
import uk.gov.nationalarchives.droid.gui.filter.action.LoadFilterAction;
import uk.gov.nationalarchives.droid.gui.filter.domain.DummyMetadata;
import uk.gov.nationalarchives.droid.gui.filter.domain.FilterDomain;
import uk.gov.nationalarchives.droid.gui.filter.domain.FilterValidationException;
import uk.gov.nationalarchives.droid.gui.filter.domain.GenericMetadata;
import uk.gov.nationalarchives.droid.gui.filter.domain.LastModifiedDateMetadata;
import uk.gov.nationalarchives.droid.profile.FilterCriterionImpl;
import uk.gov.nationalarchives.droid.profile.FilterImpl;
import uk.gov.nationalarchives.droid.profile.FilterSpecDao;
import uk.gov.nationalarchives.droid.profile.JaxbFilterSpecDao;
import uk.gov.nationalarchives.droid.profile.ProfileManager;

/**
 * @author Alok Kumar Dash.
 */
public class FilterDialog extends JDialog {

    /** */
    private static final int ROW_HEIGHT = 23;
    /** */
    private static final String REMOVE = "Remove";
    private static final int COL_0_WIDTH = 150;
    private static final int COL_1_WIDTH = 150;
    private static final int COL_3_WIDTH = 90;
    private static final int COL_2_WIDTH = 375;

    private static final int COL_0 = 0;
    private static final int COL_1 = 1;
    private static final int COL_2 = 2;
    private static final int COL_3 = 3;

    private static final long serialVersionUID = 5181319919824269596L;

    private DefaultTableModel tableModel = new DefaultTableModel();

    // FilterDomain is used to load Filter condition in filter dialog and
    // associated logic behind it.
    private FilterDomain filterDomain = new FilterDomain();
    // Filter Context is used to store user selected values in filter and later
    // used to persist in an xml in local disk.
    // FilterContext filterContext = new FilterContext();
    private FilterImpl filterContext;
    
    // temporary filter context is used to store previous state of FIlter
    // conditions before user stared to amend.
    // and it is used to assign to changed filterContext if user decide not to
    // apply changes.
    private FilterImpl tempFilterContext;

    private DroidUIContext droidContext;

    private ProfileManager profileManager;

    private JComboBox metaDataCombobox;

    private JComboBox operationCombobox;

    private DefaultComboBoxModel operationComboboxModel;

    private boolean filterPredicatesLoading;
    
    private JFileChooser filterFileChooser;
    
    
    /**
     * Creates new form FilterDialog. CLones Filter context.
     * 
     * @param parent
     *            Parent dialog.
     * @param modal
     *            If we want filter dialog to be not modal.
     * @param filterContext
     *            Filter Context
     * @param droidContext
     *            Droid Context.
     * @param profileManager
     *            Profile Manager.
     * @param filterFileChooser - the file chooser to load or save filters.
     */
    public FilterDialog(Frame parent, boolean modal, FilterImpl filterContext, 
            DroidUIContext droidContext, ProfileManager profileManager, JFileChooser filterFileChooser) {
        super(parent, modal);
        this.filterContext = filterContext;
        this.droidContext = droidContext;
        this.profileManager = profileManager;
        this.filterFileChooser = filterFileChooser;
        intialiseFilter();
        initComponents();
        myInitComponents();
        tempFilterContext = (FilterImpl) filterContext.clone();
        loadFilter();
        setLocationRelativeTo(parent);
    }

    /**
     * 
     * @return the filter
     */
    public FilterDomain getFilter() {
        return filterDomain;
    }

    /**
     * 
     * @param filter
     *            the filter to set
     */
    public void setFilter(FilterDomain filter) {
        this.filterDomain = filter;
    }

    /**
     * 
     * @return the filter context
     */
    public FilterImpl getFilterContext() {
        return filterContext;
    }

    private void loadFilter() {

        if (isFilterPrevioudlyLoaded()) {
            filterPredicatesLoading = true;
            filterEnabledCheckbox.setSelected(filterContext.isEnabled());
            setFilterMode();
            LoadFilterAction loadFilterAction = new LoadFilterAction();
            loadFilterAction.loadFilter(this);
            filterPredicatesLoading = false;
        }
    }

    /**
     * 
     */
    private void setFilterMode() {
        if (filterContext.isNarrowed()) {
            narrowResultRadioButton.setSelected(true);
        } else {
            widenResultsRadioButton.setSelected(true);
        }
    }

    private boolean isFilterPrevioudlyLoaded() {
        return filterContext.hasCriteria();
    }

    private void intialiseFilter() {

        String profileId = droidContext.getSelectedProfile().getProfile().getUuid();
        InitialiseFilterAction initialiseFilterAction = new InitialiseFilterAction();
        initialiseFilterAction.initialiseFilter(profileId, profileManager, filterDomain);

    }

    /**
     * @return Object[] Object Array for the entire row in the filter table.
     */

    public Object[] getRowForTable() {
        Component component = new TextBoxAndButton(this);
        component.hide();
        // get all the meta data and load the combo
        metaDataCombobox = new JComboBox(filterDomain.getMetaDataNames());
        metaDataCombobox.setRenderer(new BasicComboBoxRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                final String displayText = value == null ? "<Please select...>" : value.toString();
                return super.getListCellRendererComponent(list, displayText, index, isSelected, cellHasFocus);
            }
        });

        operationCombobox = new JComboBox();
        operationComboboxModel = new DefaultComboBoxModel();
        operationCombobox.setModel(operationComboboxModel);


        metaDataCombobox.addItemListener(new MetaDataComboItemListner());
        operationCombobox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {

                if (e.getStateChange() == ItemEvent.SELECTED && !filterPredicatesLoading) {
                    // set the corresponding value at filter context.
                    FilterCriterionImpl filterCriterion = 
                        filterContext.getFilterCriterion(filterTable.getSelectedRow());
                    JComboBox sourceCombo = (JComboBox) e.getSource();
                    filterCriterion.setOperator((CriterionOperator) sourceCombo.getSelectedItem());
                }
            }
        });

        // Remove button and its listner.

        JButton jButton2 = new JButton(REMOVE);

        // CHECKSTYLE:OFF FIXME - inner class is too long
        jButton2.addActionListener(new ActionListener() {
            // CHECKSTYLE:ON
            @Override
            public void actionPerformed(ActionEvent e) {
                if (tableModel.getRowCount() > 1 && (filterTable.getSelectedRow() + 1) != tableModel.getRowCount()) {
                    int size = filterContext.getNumberOfFilterCriterion();

                    filterContext.removeFilterCriterion(filterTable.getSelectedRow());
                    // fill the empty criteria which is been removed.
                    for (int k = filterTable.getSelectedRow() + 1; k < size; k++) {
                        filterContext.getFilterCriteriaMap().put(k - 1, filterContext.getFilterCriteriaMap().get(k));
                    }
                    // remove the last one
                    filterContext.removeFilterCriterion(size - 1);

                    tableModel.removeRow(filterTable.getSelectedRow());
                    tableModel.fireTableRowsDeleted(filterTable.getSelectedRow(), filterTable.getSelectedRow());
                    filterTable.revalidate();
                    initColumnSizes(filterTable);
                }
            }
        });
        Object[] data = {metaDataCombobox, operationCombobox, component, jButton2 };
        metaDataCombobox.setMaximumRowCount(filterDomain.getMetaDataNames().length);
        return data;
    }

    
    
    // CHECKSTYLE:OFF FIXME - anonymous class is way too long    
    private class MetaDataComboItemListner implements ItemListener {
        @SuppressWarnings("deprecation")
        @Override
   // CHECKSTYLE:ON
        public void itemStateChanged(ItemEvent e) {
            if (!filterPredicatesLoading) {

                
                CriterionFieldEnum deSelectedItem =  null;
                if (e.getStateChange() == e.DESELECTED) {
                    deSelectedItem = (CriterionFieldEnum) e.getItem();    
                }

                Component comp = null;
                int selectedRow = filterTable.getSelectedRow();
                // Get the source combo, and corresponding option combo and
                // textbox and button references.
                JComboBox sourceCombo = (JComboBox) e.getSource();
                // find what is selected.
                CriterionFieldEnum selectedItem = (CriterionFieldEnum) sourceCombo.getSelectedItem();
                if (selectedItem != null) {
                    // get Metadata Object from the selected string.
                    GenericMetadata metadata = filterDomain.getMetaDataFromFieldType(selectedItem);

                    comp = metadata instanceof LastModifiedDateMetadata ? new DatePicker() : new TextBoxAndButton(
                            FilterDialog.this);

                    tableModel.setValueAt(comp, filterTable.getSelectedRow(), 2);

                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        // Add a new row if metadata is selected at last
                        // row.
                        if (tableModel.getRowCount() == selectedRow + 1) {
                            tableModel.addRow(getRowForTable());
                            filterTable.revalidate();
                        }
                        JComboBox comboBox = (JComboBox) tableModel.getValueAt(selectedRow, 1);
                        operationComboboxModel = (DefaultComboBoxModel) comboBox.getModel();
                        // If filter criteria is not there that means fresh
                        // criteria for selected row and create one

                        FilterCriterionImpl newCriteria = new FilterCriterionImpl();
                        newCriteria.setRowNumber(filterTable.getSelectedRow());
                        newCriteria.setField(metadata.getField());
                        filterContext.addFilterCiterion(newCriteria, filterTable.getSelectedRow());

                        // Apply metadaUi logic.
                        applyMetadaUILogic(comp, metadata);

                        operationComboboxModel.removeAllElements();
                        for (CriterionOperator metaDataOp : metadata.getOperationList()) {
                            operationComboboxModel.addElement(metaDataOp);
                        }

                        if (comp instanceof TextBoxAndButton) {
                            ((TextBoxAndButton) comp).setType(metadata, filterContext.getFilterCriterion(filterTable
                                    .getSelectedRow()));
                        }
                        filterTable.repaint();
                    }
                } else {
                    GenericMetadata metadata = getFilterDomain().getMetaDataFromFieldType(deSelectedItem);
                    sourceCombo.getModel().setSelectedItem(metadata.getField());
                }
            }
        }

        /**
         * @param comp
         * @param metadata
         */
        @SuppressWarnings("deprecation")
        private void applyMetadaUILogic(Component comp, GenericMetadata metadata) {
            if (metadata.isFreeText()) {
                if (comp instanceof TextBoxAndButton) {
                    ((TextBoxAndButton) comp).getButton().hide();
                    ((TextBoxAndButton) comp).getTextField().show();
                }
            } else {
                ((TextBoxAndButton) comp).getTextField().disable();
                comp.show();
            }
            if (metadata instanceof DummyMetadata) {
                comp.hide();
            }
        }
    }        

    
    
    private void myInitComponents() {

        filterTable = new FilterTable();
        filterTable.setRowHeight(ROW_HEIGHT);
        filterTable.setDefaultRenderer(JComponent.class, new JComponentCellRenderer());
        filterTable.setDefaultEditor(JComponent.class, new JComponentCellEditor());

        filterTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tableModel.addColumn("Field");
        tableModel.addColumn("Operation");
        tableModel.addColumn("Values");
        tableModel.addColumn(REMOVE);

        tableModel.insertRow(0, getRowForTable());

        filterTable.setModel(tableModel);
        initColumnSizes(filterTable);

        jScrollPane1.setViewportView(filterTable);

        buttonGroup1.add(widenResultsRadioButton);
        buttonGroup1.add(narrowResultRadioButton);
        narrowResultRadioButton.setSelected(true);

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed"
    // <editor-fold defaultstate="collapsed"
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        jPanel2 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        widenResultsRadioButton = new javax.swing.JRadioButton();
        narrowResultRadioButton = new javax.swing.JRadioButton();
        jPanel5 = new javax.swing.JPanel();
        filterEnabledCheckbox = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        jButtonCancle = new javax.swing.JButton();
        jButtonApply = new javax.swing.JButton();
        LoadFilterButton = new javax.swing.JButton();
        SaveFilterButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        filterTable = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(FilterDialog.class, "FilterDialog.title")); // NOI18N

        widenResultsRadioButton.setText(org.openide.util.NbBundle.getMessage(FilterDialog.class, "FilterDialog.widenResultsRadioButton.text")); // NOI18N

        narrowResultRadioButton.setText(org.openide.util.NbBundle.getMessage(FilterDialog.class, "FilterDialog.narrowResultRadioButton.text")); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(narrowResultRadioButton)
                .addGap(18, 18, 18)
                .addComponent(widenResultsRadioButton)
                .addContainerGap(77, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(narrowResultRadioButton)
                    .addComponent(widenResultsRadioButton)))
        );

        filterEnabledCheckbox.setSelected(true);
        filterEnabledCheckbox.setText(org.openide.util.NbBundle.getMessage(FilterDialog.class, "FilterDialog.filterEnabledCheckbox.text")); // NOI18N
        filterEnabledCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterEnabledCheckboxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(filterEnabledCheckbox)
                .addContainerGap(266, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(filterEnabledCheckbox))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(108, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jButtonCancle.setText(org.openide.util.NbBundle.getMessage(FilterDialog.class, "FilterDialog.jButtonCancle.text")); // NOI18N
        jButtonCancle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancleActionPerformed(evt);
            }
        });

        jButtonApply.setText(org.openide.util.NbBundle.getMessage(FilterDialog.class, "FilterDialog.jButtonApply.text")); // NOI18N
        jButtonApply.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonApplyActionPerformed(evt);
            }
        });

        LoadFilterButton.setText(org.openide.util.NbBundle.getMessage(FilterDialog.class, "FilterDialog.LoadFilterButton.text")); // NOI18N
        LoadFilterButton.setToolTipText(org.openide.util.NbBundle.getMessage(FilterDialog.class, "FilterDialog.LoadFilterButton.toolTipText")); // NOI18N
        LoadFilterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LoadFilterButtonActionPerformed(evt);
            }
        });

        SaveFilterButton.setText(org.openide.util.NbBundle.getMessage(FilterDialog.class, "FilterDialog.SaveFilterButton.text")); // NOI18N
        SaveFilterButton.setToolTipText(org.openide.util.NbBundle.getMessage(FilterDialog.class, "FilterDialog.SaveFilterButton.toolTipText")); // NOI18N
        SaveFilterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SaveFilterButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(LoadFilterButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(SaveFilterButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 228, Short.MAX_VALUE)
                .addComponent(jButtonApply, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonCancle, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jButtonApply, jButtonCancle});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(LoadFilterButton)
                    .addComponent(SaveFilterButton)
                    .addComponent(jButtonCancle)
                    .addComponent(jButtonApply))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jButtonApply, jButtonCancle});

        filterTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(filterTable);
        filterTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(FilterDialog.class, "FilterDialog.jTable1.columnModel.title0")); // NOI18N
        filterTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(FilterDialog.class, "FilterDialog.jTable1.columnModel.title1")); // NOI18N
        filterTable.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(FilterDialog.class, "FilterDialog.jTable1.columnModel.title2")); // NOI18N
        filterTable.getColumnModel().getColumn(3).setHeaderValue(org.openide.util.NbBundle.getMessage(FilterDialog.class, "FilterDialog.jTable1.columnModel.title3")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jPanel2, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 656, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void LoadFilterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LoadFilterButtonActionPerformed
        int result = filterFileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            //FIXME: should wire this up using spring, rather than hard coding references to
            // particular objects here.
            try {
                FilterSpecDao reader = new JaxbFilterSpecDao();
                FileInputStream in = new FileInputStream(filterFileChooser.getSelectedFile());
                filterContext = reader.loadFilter(in);
                in.close();
                loadFilter();
            } catch (JAXBException e) {
                JOptionPane.showMessageDialog(this, "There was a problem loading the filter.", "Filter warning", JOptionPane.ERROR_MESSAGE);
            } catch (FileNotFoundException e) {
                JOptionPane.showMessageDialog(this, "There was a problem loading the filter.", "Filter warning", JOptionPane.ERROR_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "There was a problem loading the filter.", "Filter warning", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_LoadFilterButtonActionPerformed

    private void SaveFilterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaveFilterButtonActionPerformed
        if (applyValuesToContext()) {
            int result = filterFileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                FileOutputStream out;
                try {
                    out = new FileOutputStream(filterFileChooser.getSelectedFile());
                    FilterSpecDao writer = new JaxbFilterSpecDao();
                    writer.saveFilter(filterContext, out);
                    out.close();
                } catch (FileNotFoundException e) {
                    JOptionPane.showMessageDialog(this, "There was a problem saving the filter.", "Filter warning", JOptionPane.ERROR_MESSAGE);
                } catch (JAXBException e) {
                    JOptionPane.showMessageDialog(this, "There was a problem saving the filter.", "Filter warning", JOptionPane.ERROR_MESSAGE);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, "There was a problem saving the filter.", "Filter warning", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }//GEN-LAST:event_SaveFilterButtonActionPerformed

    private void filterEnabledCheckboxActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_filterEnabledCheckboxActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_filterEnabledCheckboxActionPerformed

    private void jButtonCancleActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButtonCancleActionPerformed
        // filterContext = tempFilterContext ;
        droidContext.getSelectedProfile().getProfile().setFilter(tempFilterContext);
        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));

    }// GEN-LAST:event_jButtonCancleActionPerformed

    private void jButtonApplyActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButtonApplyActionPerformed

        filterContext.setEnabled(filterEnabledCheckbox.isSelected());
        filterContext.setNarrowed(narrowResultRadioButton.isSelected());
        //FilterImpl filter = droidContext.getSelectedProfile().getProfile().getFilter();
        droidContext.getSelectedProfile().getProfile().setFilter(filterContext);
        
        if (applyValuesToContext()) {
            getDroidContext().getSelectedProfile().getProfile().setDirty(true);
            ProfileForm profileToFilter = getDroidContext().getSelectedProfile();
            ApplyFilterToTreeTableAction applyFilterToTreeAction = 
                new ApplyFilterToTreeTableAction(profileToFilter, getProfileManager());
            applyFilterToTreeAction.applyFilter();
            dispatchEvent(new WindowEvent(this,  WindowEvent.WINDOW_CLOSING));
        }
        
        //ApplyFilterAction applyFilterAction = new ApplyFilterAction();
        //applyFilterAction.applyFilter(this);
    }// GEN-LAST:event_jButtonApplyActionPerformed

    /**
     * Attempts to apply the values in the dialog to the filter.
     * @return Whether the attempt was successful or not.
     */
    private boolean applyValuesToContext() {
        DefaultTableModel tModel = (DefaultTableModel) getFilterTable().getModel();
        List<String> errorStrings = new ArrayList<String>();
        // Iterate through the table and set all the free text.
        // do not anything for the last row.
        for (int i = 0; i < tModel.getRowCount() - 1; i++) {
            // get the first combobox selected item.
            final JComboBox comboBox1 = (JComboBox) tModel.getValueAt(i, 0);
            CriterionFieldEnum firstComboBoxSelectedItem = (CriterionFieldEnum) comboBox1.getSelectedItem();
            // get the Component .
            Component componentAtThirdRow = (Component) tModel.getValueAt(
                    i, 2);

            String freeTextAtRow = null;
            try {
                if (componentAtThirdRow instanceof TextBoxAndButton) {
                    freeTextAtRow = ((TextBoxAndButton) componentAtThirdRow)
                            .getTextField().getText();
                } else if (componentAtThirdRow instanceof DatePicker) {
                    freeTextAtRow = ((DatePicker) componentAtThirdRow)
                            .getDateString();
                } else {
                    throw new RuntimeException("Fatal error");
                }
    
                // get meta data object from the selected string.
                GenericMetadata metadata = getFilterDomain()
                        .getMetaDataFromFieldType(firstComboBoxSelectedItem);

                // Validate the values.
                metadata.validate(freeTextAtRow);
            } catch (FilterValidationException filterValidationException) {
                errorStrings.add(filterValidationException.getMessage());
            }
            FilterCriterionImpl filterCriteria = getFilterContext().getFilterCriterion(i);
            filterCriteria.setValueFreeText(freeTextAtRow);
        }
        if (errorStrings.size() > 0) {
            JOptionPane.showMessageDialog(this, errorStrings.toArray());
            return false;
        }
        return true;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton LoadFilterButton;
    private javax.swing.JButton SaveFilterButton;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JCheckBox filterEnabledCheckbox;
    private javax.swing.JTable filterTable;
    private javax.swing.JButton jButtonApply;
    private javax.swing.JButton jButtonCancle;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JRadioButton narrowResultRadioButton;
    private javax.swing.JRadioButton widenResultsRadioButton;
    // End of variables declaration//GEN-END:variables

    private void initColumnSizes(JTable table) {

        // Disable auto resizing
        // table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Set the first visible column to 100 pixels wide
        TableColumn col = table.getColumnModel().getColumn(COL_0);
        col.setPreferredWidth(COL_0_WIDTH);

        col = table.getColumnModel().getColumn(COL_1);
        col.setPreferredWidth(COL_1_WIDTH);

        col = table.getColumnModel().getColumn(COL_2);
        col.setPreferredWidth(COL_2_WIDTH);

        col = table.getColumnModel().getColumn(COL_3);
        col.setPreferredWidth(COL_3_WIDTH);

    }

    /**
     * @return the filterDomain
     */
    public FilterDomain getFilterDomain() {
        return filterDomain;
    }

    /**
     * @return the droidContext
     */
    public DroidUIContext getDroidContext() {
        return droidContext;
    }

    /**
     * @return the jTable1
     */
    public javax.swing.JTable getFilterTable() {
        return filterTable;
    }

    /**
     * @return the profileManager
     */
    public ProfileManager getProfileManager() {
        return profileManager;
    }
}
