/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui;

/**
 * GUI model for checklist cells.
 * @author rflitcroft
 *
 */
public class CheckListCellModel {

    private String label;
    private boolean selected;
    
    /**
     * 
     * @param label the label for the list item
     * @param selected whether the item is checked.
     */
    public CheckListCellModel(String label, boolean selected) {
        this.label = label;
        this.selected = selected;
    }
    
    /**
     * Toggles the selection property.
     */
    public void toggleSelection() {
        selected = !selected;
    }

    /**
     * Sets the selection of the cell directly.
     * @param isSelected whether the cell is selected or not.
     */
    public void setSelection(boolean isSelected) {
        selected = isSelected;
    }
    
    /**
     * 
     * @return true if the item is selected, false otherwiese
     */
    public boolean isSelected() {
        return selected;
    }
    
    /**
     * 
     * @return the item's label.
     */
    public String getLabel() {
        return label;
    }
}
