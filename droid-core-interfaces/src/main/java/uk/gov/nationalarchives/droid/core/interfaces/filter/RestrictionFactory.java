/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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
