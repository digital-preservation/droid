/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui.filter.domain;

import java.util.ArrayList;
import java.util.List;

import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionFieldEnum;

/**
 * The filter domain - for getting filter reference data.
 * @author Alok Kumar Dash
 *
 */
public class FilterDomain {

    private List<GenericMetadata> possibleValues = new ArrayList<GenericMetadata>();

    private List<GenericMetadata> selectedValues = new ArrayList<GenericMetadata>();

    /**
     * @return all possible Metadata. 
     */
    public List<GenericMetadata> getPossibleValues() {
        return possibleValues;
    }

    /**
     * @return all selected values 
     */
    public List<GenericMetadata> getSelectedValues() {
        return selectedValues;
    }

    /**
     * @param metadata Generic Metadata.
     */
    public void addFilterCondition(GenericMetadata metadata) {
        possibleValues.add(metadata);
    }

    /**
     * 
     * @return the meta data names.
     */
    public CriterionFieldEnum[] getMetaDataNames() {

        CriterionFieldEnum[] names = new CriterionFieldEnum[possibleValues.size()];
        for (int i = 0; i < possibleValues.size(); i++) {
            names[i] = possibleValues.get(i).getMetadataName();
        }
        return names;

    }

    /**
     * @param selectedMetadataString metadata selection string
     * @return something
     */
    public GenericMetadata getMetaDataFromFieldType(CriterionFieldEnum selectedMetadataString) {
        GenericMetadata selectedMetadata = null;

        for (int i = 0; i < possibleValues.size(); i++) {
            if (selectedMetadataString.equals(possibleValues.get(i).getMetadataName())) {
                selectedMetadata = possibleValues.get(i);
            }
        }
        return selectedMetadata;
    }

}
