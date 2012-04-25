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
 * Abstract Filter criterion.
 * @param <T> the type of criterion
 * @author rflitcroft
 *
 */
public abstract class AbstractFilterCriterion<T> implements FilterCriterion {
    
    private CriterionFieldEnum field;
    private CriterionOperator operator;
    
    /**
     * 
     * @param field the field
     * @param operator the operator
     */
    protected AbstractFilterCriterion(CriterionFieldEnum field, CriterionOperator operator) {
        this.field = field;
        this.operator = operator;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final CriterionFieldEnum getField() {
        return field;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final CriterionOperator getOperator() {
        return operator;
    }
    
    /**
     * 
     * @return the valid operators for the criterion.
     */
    protected abstract CriterionOperator[] operators();

}
