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
 * @param <T> the type of the criterion value
 * @author rflitcroft
 *
 */
public abstract class AbstractFilterCriterion<T> implements FilterCriterion {

    private CriterionOperator operator;
    private CriterionFieldEnum field;
    private Object value;
    
    /**
     * @param field the field
     * @param operator the operator
     */
    public AbstractFilterCriterion(CriterionFieldEnum field, CriterionOperator operator) {
        this.field = field;
        this.operator = operator;
    }
    
    /**
     * Converts a string to a typed value.
     * @param s the String representation
     * @return the typed value
     */
    protected abstract T toTypedValue(String s);
    
    /**
     * {@inheritDoc}
     */
    @Override
    public CriterionFieldEnum getField() {
        return field;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public CriterionOperator getOperator() {
        return operator;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue() {
        return value;
    }
    
    /**
     * Sets the value.
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = toTypedValue(value);
    }
    
    /**
     * Sets mulitple values.
     * @param values multiple values to set
     */
    public void setValue(Collection<String> values) {
        Object[] typedValues = new Object[values.size()];
        int i = 0;
        for (String v : values) {
            typedValues[i++] = toTypedValue(v);
        }
        
        value = typedValues;
    }
}
