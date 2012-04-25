/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui.filter.action;

import java.awt.Component;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.table.DefaultTableModel;

import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionFieldEnum;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionOperator;
import uk.gov.nationalarchives.droid.gui.filter.DatePicker;
import uk.gov.nationalarchives.droid.gui.filter.FilterDialog;
import uk.gov.nationalarchives.droid.gui.filter.TextBoxAndButton;
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
                component = new TextBoxAndButton(filterDialog);
                ((TextBoxAndButton) component).getTextField().setText(filterCriteria.getValueFreeText());
                ((TextBoxAndButton) component).setType(metadata, filterDialog.getFilterContext().getFilterCriterion(i));
                if (metadata.isFreeText()) {
                    ((TextBoxAndButton) component).getButton().hide();
                    filterDialog.getFilterTable().repaint();
                } else {
                    ((TextBoxAndButton) component).getTextField().disable();
                }
            }
            tableModel.setValueAt(component, i, 2);
        }
    }

}
