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
package uk.gov.nationalarchives.droid.gui.filter.action;

import java.awt.Component;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionFieldEnum;
import uk.gov.nationalarchives.droid.gui.ProfileForm;
import uk.gov.nationalarchives.droid.gui.action.ApplyFilterToTreeTableAction;
import uk.gov.nationalarchives.droid.gui.filter.DatePicker;
import uk.gov.nationalarchives.droid.gui.filter.FilterDialog;
import uk.gov.nationalarchives.droid.gui.filter.TextBoxAndButton;
import uk.gov.nationalarchives.droid.gui.filter.domain.FilterValidationException;
import uk.gov.nationalarchives.droid.gui.filter.domain.GenericMetadata;
import uk.gov.nationalarchives.droid.profile.FilterCriterionImpl;

/**
 * @author Alok Kumar Dash
 *
 */
public class ApplyFilterAction {

    private Log log = LogFactory.getLog(this.getClass());
    
    /**
     * Applies filter after Apply filter button is pressed.
     * @param filterDialog FIlterDialog.
     */
    public void applyFilter(FilterDialog filterDialog) {
        
        
        
        
        DefaultTableModel tableModel = (DefaultTableModel) filterDialog.getFilterTable().getModel();
        List<String> errorStrings = new ArrayList<String>();
        // Iterate through the table and set all the free text.
        // do not anything for the last row.
        for (int i = 0; i < tableModel.getRowCount() - 1; i++) {
            // get the first combobox selected item.
            final JComboBox comboBox1 = (JComboBox) tableModel.getValueAt(i, 0);
            CriterionFieldEnum firstComboBoxSelectedItem = (CriterionFieldEnum) comboBox1.getSelectedItem();
            // get the Component .
            Component componentAtThirdRow = (Component) tableModel.getValueAt(
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
                    String message = "Error applying filter values from user interface.";
                    log.error(message);
                    throw new RuntimeException(message);
                }
    
                // get meta data object from the selected string.
                GenericMetadata metadata = filterDialog.getFilterDomain()
                        .getMetaDataFromFieldType(firstComboBoxSelectedItem);

                // Validate the values.
                metadata.validate(freeTextAtRow);
            } catch (FilterValidationException filterValidationException) {
                errorStrings.add(filterValidationException.getMessage());
            }
            FilterCriterionImpl filterCriteria = filterDialog.getFilterContext().getFilterCriterion(i);
            filterCriteria.setValueFreeText(freeTextAtRow);
        }

        
        if (errorStrings.size() > 0) {
            JOptionPane.showMessageDialog(filterDialog, errorStrings.toArray());
        } else {
            filterDialog.getDroidContext().getSelectedProfile().getProfile().setDirty(true);
//            filterDialog.getDroidContext().getSelectedProfile().onResourceChanged();
            ProfileForm profileToFilter = filterDialog.getDroidContext().getSelectedProfile();
            ApplyFilterToTreeTableAction applyFilterToTreeAction = 
                new ApplyFilterToTreeTableAction(profileToFilter, filterDialog.getProfileManager());
            applyFilterToTreeAction.applyFilter();
            filterDialog.dispatchEvent(new WindowEvent(filterDialog,  WindowEvent.WINDOW_CLOSING));
        }
    }

    
}
