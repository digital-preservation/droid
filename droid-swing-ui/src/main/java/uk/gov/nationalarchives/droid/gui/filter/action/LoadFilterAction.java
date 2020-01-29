/**
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
package uk.gov.nationalarchives.droid.gui.filter.action;

import java.awt.Component;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.table.DefaultTableModel;

import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionFieldEnum;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionOperator;
import uk.gov.nationalarchives.droid.core.interfaces.filter.FilterValue;
import uk.gov.nationalarchives.droid.gui.filter.DatePicker;
import uk.gov.nationalarchives.droid.gui.filter.FilterDialog;
import uk.gov.nationalarchives.droid.gui.filter.TextBoxAndButton;
import uk.gov.nationalarchives.droid.gui.filter.domain.ExtensionMismatchMetadata;
import uk.gov.nationalarchives.droid.gui.filter.domain.GenericMetadata;
import uk.gov.nationalarchives.droid.gui.filter.domain.LastModifiedDateMetadata;
import uk.gov.nationalarchives.droid.profile.FilterCriterionImpl;

/**
 * @author Alok Kumar Dash
 * 
 */
public class LoadFilterAction {

    /**
     * Action method for loading filter after filter button is pressed at UI.
     * 
     * @param filterDialog
     *            FIlter Dialog.
     */

    @SuppressWarnings("deprecation")
    public void loadFilter(FilterDialog filterDialog) {

        DefaultTableModel tableModel = (DefaultTableModel) filterDialog
                .getFilterTable().getModel();
        for (int row = tableModel.getRowCount() - 2; row >= 0; row--) {
            tableModel.removeRow(row);
        }
        Component component = null;
        Map<Integer, FilterCriterionImpl> criteriaMap = filterDialog.getFilterContext().getFilterCriteriaMap();
        for (int i = 0; i < criteriaMap.size(); i++) {
            tableModel.addRow(filterDialog.getRowForTable());
            filterDialog.getFilterTable().revalidate();
            FilterCriterionImpl filterCriteria = criteriaMap.get(i);
            JComboBox metaDataComboBox = (JComboBox) tableModel.getValueAt(i, 0);
            JComboBox optionComboBox = (JComboBox) tableModel.getValueAt(i, 1);
            CriterionFieldEnum metadaName = filterCriteria.getField();
            GenericMetadata metadata = filterDialog.getFilterDomain().getMetaDataFromFieldType(metadaName);
            metaDataComboBox.getModel().setSelectedItem(metadata.getField());
            DefaultComboBoxModel secondComboBox = (DefaultComboBoxModel) optionComboBox.getModel();
            secondComboBox.removeAllElements();

            for (CriterionOperator metaDataItem : metadata.getOperationList()) {
                secondComboBox.addElement(metaDataItem);
            }
            secondComboBox.setSelectedItem(filterCriteria.getOperator());
            if (metadata instanceof LastModifiedDateMetadata) {
                component = new DatePicker();
                ((DatePicker) component).setDateCombos(filterCriteria.getValueFreeText());
            } else {
                if (metadata instanceof ExtensionMismatchMetadata) {
                    component = configureComboBox(filterCriteria, metadata);
                } else {
                    component = configureTextBoxAndButton(filterDialog, i, filterCriteria, metadata);
                }
            }
            tableModel.setValueAt(component, i, 2);
        }
    }

    private Component configureTextBoxAndButton(FilterDialog filterDialog,
            int i, FilterCriterionImpl filterCriteria, GenericMetadata metadata) {
        Component component;
        component = new TextBoxAndButton(filterDialog);
        ((TextBoxAndButton) component).getTextField().setText(
                filterCriteria.getValueFreeText());
        FilterCriterionImpl fci = filterDialog.getFilterContext()
                .getFilterCriterion(i);
        ((TextBoxAndButton) component).setType(metadata, fci);
        if (metadata.isFreeText()) {
            ((TextBoxAndButton) component).getButton().hide();
            filterDialog.getFilterTable().repaint();
        } else {
            ((TextBoxAndButton) component).getTextField().disable();
        }
        return component;
    }

    private Component configureComboBox(FilterCriterionImpl filterCriteria, GenericMetadata metadata) {
        Component component;
        JComboBox combo = new JComboBox();

        for (FilterValue v : metadata.getPossibleValues()) {
            combo.addItem(v.getDescription());
        }

        combo.setSelectedItem(filterCriteria.getValue().toString());
        component = (Component) combo;
        return component;
    }
}
