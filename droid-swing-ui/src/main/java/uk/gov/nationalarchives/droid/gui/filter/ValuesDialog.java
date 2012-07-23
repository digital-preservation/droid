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

import java.awt.Dialog;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JDialog;

import uk.gov.nationalarchives.droid.core.interfaces.filter.FilterValue;
import uk.gov.nationalarchives.droid.gui.filter.domain.GenericMetadata;
import uk.gov.nationalarchives.droid.profile.FilterCriterionImpl;

/**
 * @author Alok Kumar Dash
 */
public class ValuesDialog extends JDialog {

    private static final long serialVersionUID = -6126734059735497128L;

    private FilterCriterionImpl filterCriteria;

    private String selectedString = "";

    private List<FilterValue> tempSelectedValues;

    private DefaultListModel possibleListModel = new DefaultListModel();
    private DefaultListModel selectedListModel = new DefaultListModel();
    
    private String selectedValues = "";


    /** 
     * Creates new form ValuesDialog. 
     * @param parent this dialog's parent
     * @param modal the modality
     * @param metadata some metadata
     * @param filterCriteria the filter criteria 
     * 
     */
    public ValuesDialog(Dialog parent, boolean modal, GenericMetadata metadata, FilterCriterionImpl filterCriteria) {
        super(parent, modal);

        initComponents();
        setLocationRelativeTo(parent);
        this.filterCriteria = filterCriteria;
        this.tempSelectedValues = filterCriteria.getSelectedValues(); 

        jLabel1.setText("");
        
        if (filterCriteria.getSelectedValues() != null) {
            // add all the items in possible list values
            for (int i = 0; i < metadata.getPossibleValues().size(); i++) {
                possibleListModel.add(i, metadata.getPossibleValues().get(i));
            }

            // add which ever is being selected in selected values.
            for (int i = 0; i < filterCriteria.getSelectedValues().size(); i++) {
                selectedListModel.add(i, filterCriteria.getSelectedValues().get(i));
            }
            // remove all those selected items  from possible list values.
            // first find out indexes to remove because as items deleted indexes re adjust.
            for (FilterValue values : filterCriteria.getSelectedValues()) {
                // find index in possible list value
                possibleListModel.remove(findIndexes(values.getDescription()));
            }
        } else {
            for (int i = 0; i < metadata.getPossibleValues().size(); i++) {
                possibleListModel.add(i, metadata.getPossibleValues().get(i));
            }
        }
        
        jListPossibleValues.setModel(possibleListModel);
        jListSelectedValues.setModel(selectedListModel);

    }

    /**
     * @return FIXME: javadoc
     */
    public String getSelectedValues() {
        return selectedValues;
    }

    /**
     * @return FIXME: javadoc
     */
    public String getSelectedString() {
        return selectedString;
    }


    /**
     * @param selectedValues FIXME: javadoc
     */
    public void setSelectedValues(String selectedValues) {
        this.selectedValues = selectedValues;
    }




    private int findIndexes(String valuesNameFromSelectedModel) {
        for (int j = 0; j < possibleListModel.getSize(); j++) {
            String valuesNameFromPossibleModel = ((FilterValue) possibleListModel.get(j)).getDescription();

            if (valuesNameFromPossibleModel.equals(valuesNameFromSelectedModel)) {
                return j;
            }
        }
        return 0;
    }



    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jListPossibleValues = new javax.swing.JList();
        jScrollPane2 = new javax.swing.JScrollPane();
        jListSelectedValues = new javax.swing.JList();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jButtonCancel = new javax.swing.JButton();
        jButtonOk = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jButtonSelect = new javax.swing.JButton();
        jButtonRemove = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jListPossibleValues.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(jListPossibleValues);

        jListSelectedValues.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane2.setViewportView(jListSelectedValues);

        jLabel1.setText(org.openide.util.NbBundle.getMessage(ValuesDialog.class, "ValuesDialog.jLabel1.text")); // NOI18N

        jButtonCancel.setText(org.openide.util.NbBundle.getMessage(ValuesDialog.class, "ValuesDialog.jButtonCancel.text")); // NOI18N
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });

        jButtonOk.setText(org.openide.util.NbBundle.getMessage(ValuesDialog.class, "ValuesDialog.jButtonOk.text")); // NOI18N
        jButtonOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOkActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(480, Short.MAX_VALUE)
                .addComponent(jButtonOk, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonCancel))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jButtonCancel, jButtonOk});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonCancel)
                    .addComponent(jButtonOk))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jButtonCancel, jButtonOk});

        jButtonSelect.setText(org.openide.util.NbBundle.getMessage(ValuesDialog.class, "ValuesDialog.jButtonSelect.text")); // NOI18N
        jButtonSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSelectActionPerformed(evt);
            }
        });

        jButtonRemove.setText(org.openide.util.NbBundle.getMessage(ValuesDialog.class, "ValuesDialog.jButtonRemove.text")); // NOI18N
        jButtonRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonRemove, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonSelect, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jButtonRemove, jButtonSelect});

        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addComponent(jButtonSelect)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonRemove)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jButtonRemove, jButtonSelect});

        jPanel3.setPreferredSize(new java.awt.Dimension(132, 132));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 132, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 132, Short.MAX_VALUE)
        );

        jPanel4.setPreferredSize(new java.awt.Dimension(132, 132));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 132, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 119, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 626, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 338, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 338, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGap(27, 27, 27)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSelectActionPerformed

    	int[] listOfSelectedIndices =   jListPossibleValues.getSelectedIndices();
    	//if some thing selected.
    	if (listOfSelectedIndices.length >0){
    		//cache total number of selected indices.
    		int looplength = listOfSelectedIndices.length;
    		//initialise selected values.
			if(filterCriteria.getSelectedValues() == null){
				filterCriteria.setSelectedValues(new ArrayList<FilterValue>());
			}

    		for(int i=0; i< looplength ; i++){
    			selectedListModel.addElement(possibleListModel.get(listOfSelectedIndices[i]));    			
    		}
			
    		for(int i=0; i< looplength ; i++){
    			possibleListModel.remove(listOfSelectedIndices[0]);
    			listOfSelectedIndices =   jListPossibleValues.getSelectedIndices();
    		}
    	}
    	

    }//GEN-LAST:event_jButtonSelectActionPerformed

    private void jButtonRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveActionPerformed

    	int[] listOfSelectedIndices =   jListSelectedValues.getSelectedIndices();

    	if (listOfSelectedIndices.length >0){
    		int looplength = listOfSelectedIndices.length;
    		for(int i=0; i< looplength ; i++){
    			possibleListModel.addElement(selectedListModel.get(listOfSelectedIndices[i]));
    		}
    		for(int i=0; i< looplength ; i++){
    			selectedListModel.remove(listOfSelectedIndices[0]);
    			listOfSelectedIndices =   jListSelectedValues.getSelectedIndices();
    		}
    	}    		
    }//GEN-LAST:event_jButtonRemoveActionPerformed

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        filterCriteria.setSelectedValues(tempSelectedValues);
        if (tempSelectedValues != null) {
            for(int i =0; i<tempSelectedValues.size(); i++){
                selectedString = selectedString + "\"" +(tempSelectedValues.get(i)).getDescription() + "\" "; 
            }
        }
    	this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    	
    }//GEN-LAST:event_jButtonCancelActionPerformed

    private void jButtonOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOkActionPerformed
    	filterCriteria.setSelectedValues(new ArrayList<FilterValue>());
    	for(int i =0; i<selectedListModel.getSize(); i++){
    		filterCriteria.addSelectedValue((FilterValue)selectedListModel.get(i));
    		selectedString = selectedString + "\"" +((FilterValue)selectedListModel.get(i)).getDescription() + "\" "; 
    	}
    	this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));  
    	
    	
    }//GEN-LAST:event_jButtonOkActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonOk;
    private javax.swing.JButton jButtonRemove;
    private javax.swing.JButton jButtonSelect;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JList jListPossibleValues;
    private javax.swing.JList jListSelectedValues;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    // End of variables declaration//GEN-END:variables

}
