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
package uk.gov.nationalarchives.droid.core.interfaces.filter;

import java.util.Date;

import org.joda.time.DateMidnight;

import uk.gov.nationalarchives.droid.core.interfaces.filter.expressions.Criterion;
import uk.gov.nationalarchives.droid.core.interfaces.filter.expressions.Restrictions;

/**
 * MApping between Criterion Fields and EJBQL.
 * @author rflitcroft
 *
 */
public final class RestrictionFactory {

    private RestrictionFactory() { }
    
    // CHECKSTYLE:OFF (TODO: switch statement causes cyclomatic complexity - better to refactor)
    public static Criterion forFilterCriterion(FilterCriterion criterion) {
        // CHECKSTYLE:ON
        
        // Dates need special handling since they are ranges.
        if (criterion.getValue() instanceof Date) {
            return forDateCriterion(criterion);
        }
        
        final CriterionFieldEnum field = criterion.getField();
       
        String propertyName = field.getPropertyName();
//        if (field.ordinal() >= CriterionFieldEnum.PUID.ordinal()) {
//            propertyName = "format." + field.getPropertyName();
//        }
        
        Criterion restriction;
        final Object value = criterion.getValue();
        switch (criterion.getOperator()) {
            case EQ:
                restriction = Restrictions.eq(propertyName, value);
                break;
            case NE:
                restriction = Restrictions.neq(propertyName, value);
                break;
            case GT:
                restriction = Restrictions.gt(propertyName, value);
                break;
            case GTE:
                restriction = Restrictions.gte(propertyName, value);
                break;
            case LT:
                restriction = Restrictions.lt(propertyName, value);
                break;
            case LTE:
                restriction = Restrictions.lte(propertyName, value);
                break;
            case ANY_OF:
                restriction = Restrictions.in(propertyName, (Object[]) value);
                break;
            case CONTAINS:
                restriction = Restrictions.like(propertyName, '%' + (String) value + '%');
                break;
            case ENDS_WITH:
                restriction = Restrictions.like(propertyName, '%' + (String) value);
                break;
            case STARTS_WITH:
                restriction = Restrictions.like(propertyName, (String) value + '%');
                break;
            case NOT_CONTAINS:
                restriction = Restrictions.notLike(propertyName, '%' + (String) value + '%');
                break;
            case NOT_ENDS_WITH:
                restriction = Restrictions.notLike(propertyName, '%' + (String) value);
                break;
            case NOT_STARTS_WITH:
                restriction = Restrictions.notLike(propertyName, (String) value + '%');
                break;                
            case NONE_OF:
                restriction = Restrictions.notIn(propertyName, (Object[]) value);
                break;
            default:
                throw new IllegalArgumentException(
                        String.format("Invalid operator [%s]", criterion.getOperator()));
        }
        
        return restriction;
    }
    
        
    /**
     * Dates need special handling!
     * @param dateCriterion the date criterion
     * @return the date criterion
     */
    private static Criterion forDateCriterion(FilterCriterion dateCriterion) {
        final CriterionFieldEnum field = dateCriterion.getField();
        final Date dateValue = (Date) dateCriterion.getValue();
        
        Date from = new DateMidnight(dateValue).toDate();
        Date to = new DateMidnight(dateValue).plusDays(1).toDate();
        
        String propertyName = field.getPropertyName();

        Criterion criterion;
        
        switch (dateCriterion.getOperator()) {
            case EQ:
                criterion = Restrictions.and(Restrictions.gte(propertyName, from), Restrictions.lt(propertyName, to));
                break;
            case NE:
                criterion = Restrictions.or(Restrictions.lt(propertyName, from), Restrictions.gte(propertyName, to));
                break;
            case LT:
                criterion = Restrictions.lt(propertyName, from);
                break;
            case LTE:
                criterion = Restrictions.lt(propertyName, to);
                break;
            case GT:
                criterion = Restrictions.gte(propertyName, to);
                break;
            case GTE:
                criterion = Restrictions.gte(propertyName, from);
                break;
            default:
                throw new IllegalArgumentException(
                        String.format("Invalid operator for date [%s]", dateCriterion.getOperator()));
        }
        
        return criterion;
        
    }
    
}
