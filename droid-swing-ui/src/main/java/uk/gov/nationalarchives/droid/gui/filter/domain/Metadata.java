/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui.filter.domain;

import java.util.List;

import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionFieldEnum;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionOperator;
import uk.gov.nationalarchives.droid.core.interfaces.filter.FilterValue;

/**
 * Interface for all filter metadata.
 * @author adash
 *
 */
public interface Metadata {

    /** 
     * Gets the metadata name. 
     * @return the metadata name
     */
    CriterionFieldEnum getMetadataName();

    /** 
     * Validates the selection.
     * @param stringToValidate the input to validate
     * @throws FilterValidationException if the input was invalid
     */
    void validate(String stringToValidate) throws FilterValidationException;

    /**
     * Gets the valid operations.
     * @return list of operations 
     */
    List<CriterionOperator> getOperationList();

    /**
     * @return true if the metadata allows free text search; false otherwise 
     */
    boolean isFreeText();

    /**
     * @return all the possible values 
     */
    List<FilterValue> getPossibleValues();

}
