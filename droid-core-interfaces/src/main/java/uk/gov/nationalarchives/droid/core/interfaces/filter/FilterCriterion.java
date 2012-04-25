/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.core.interfaces.filter;

/**
 * @author rflitcroft
 *
 */
public interface FilterCriterion {

    /**
     * @return the filtered attribute.
     */
    CriterionFieldEnum getField();

    /**
     * @return the filter condition
     */
    CriterionOperator getOperator();

//    /**
//     * @return the single filter parameter
//     */
//    String getValueFreeText();

    /**
     * @return the single filter parameter
     */
    Object getValue();

//    /**
//     * @return the set of filter parameters.
//     */
//    List<FilterValue> getSelectedValues();

}
