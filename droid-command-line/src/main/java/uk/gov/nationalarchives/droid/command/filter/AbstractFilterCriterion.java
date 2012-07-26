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
