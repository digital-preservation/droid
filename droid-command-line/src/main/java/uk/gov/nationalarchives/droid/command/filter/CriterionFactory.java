/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.command.filter;

import java.util.Collection;

import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionFieldEnum;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionOperator;
import uk.gov.nationalarchives.droid.core.interfaces.filter.FilterCriterion;

/**
 * @author rflitcroft
 *
 */
public interface CriterionFactory {
    
    /**
     * Builds a new filter criterion.
     * @param dqlValue the string value
     * @param field the field
     * @param operator the operator
     * @return a filter criterion
     */
    FilterCriterion newCriterion(CriterionFieldEnum field, CriterionOperator operator, String dqlValue);

    /**
     * Builds a new filter criterion.
     * @param dqlValues the string values
     * @param field the field
     * @param operator the operator
     * @return a filter criterion
     */
    FilterCriterion newCriterion(CriterionFieldEnum field, CriterionOperator operator, Collection<String> dqlValues);
}
