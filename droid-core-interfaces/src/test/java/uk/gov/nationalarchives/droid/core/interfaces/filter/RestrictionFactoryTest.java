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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.joda.time.DateMidnight;
import org.junit.Before;
import org.junit.Test;

import uk.gov.nationalarchives.droid.core.interfaces.filter.expressions.QueryBuilder;

/**
 * @author rflitcroft
 *
 */
public class RestrictionFactoryTest {

    private FilterCriterion filterCriterion;
    private QueryBuilder queryBuilder;
    private Date date;
    
    private Date from;
    private Date to;
    
    @Before
    public void setup() {
        queryBuilder = QueryBuilder.forAlias("foo");
        
        date = new Date(1234267850000L);
        from = new Date(new DateMidnight(date).getMillis());
        to = new Date(new DateMidnight(date).plusDays(1).getMillis());
        
        filterCriterion = mock(FilterCriterion.class);
        when(filterCriterion.getValue()).thenReturn(date);
        when(filterCriterion.getField()).thenReturn(CriterionFieldEnum.LAST_MODIFIED_DATE);
    }
    
    @Test
    public void testMidnight() {
        DateMidnight dateMidnight = new DateMidnight(date);
        assertTrue(dateMidnight.toDate().before(date));
    }
    
    @Test
    public void testEqualDate() {
        when(filterCriterion.getOperator()).thenReturn(CriterionOperator.EQ);
        
        queryBuilder.add(RestrictionFactory.forFilterCriterion(filterCriterion));
        
        assertEquals("(foo.metaData.lastModifiedDate >= ? "
                + "AND foo.metaData.lastModifiedDate < ?)", queryBuilder.toEjbQl());
        
        assertArrayEquals(new Date[] {from, to}, queryBuilder.getValues());
    }

    @Test
    public void testNotEqualDate() {
        when(filterCriterion.getOperator()).thenReturn(CriterionOperator.NE);
        
        queryBuilder.add(RestrictionFactory.forFilterCriterion(filterCriterion));
        
        assertEquals("(foo.metaData.lastModifiedDate < ? "
                + "OR foo.metaData.lastModifiedDate >= ?)", queryBuilder.toEjbQl());

        assertArrayEquals(new Date[] {from, to}, queryBuilder.getValues());
    }
    
    @Test
    public void testLessThanDate() {
        when(filterCriterion.getOperator()).thenReturn(CriterionOperator.LT);
        
        queryBuilder.add(RestrictionFactory.forFilterCriterion(filterCriterion));
        
        assertEquals("foo.metaData.lastModifiedDate < ?", queryBuilder.toEjbQl());

        assertArrayEquals(new Date[] {from}, queryBuilder.getValues());
    }

    @Test
    public void testLessThanEqualDate() {
        when(filterCriterion.getOperator()).thenReturn(CriterionOperator.LTE);
        
        queryBuilder.add(RestrictionFactory.forFilterCriterion(filterCriterion));
        
        assertEquals("foo.metaData.lastModifiedDate < ?", queryBuilder.toEjbQl());
        assertArrayEquals(new Date[] {to}, queryBuilder.getValues());
    }

    @Test
    public void testGreaterThanDate() {
        when(filterCriterion.getOperator()).thenReturn(CriterionOperator.GT);
        
        queryBuilder.add(RestrictionFactory.forFilterCriterion(filterCriterion));
        
        assertEquals("foo.metaData.lastModifiedDate >= ?", queryBuilder.toEjbQl());

        assertArrayEquals(new Date[] {to}, queryBuilder.getValues());
    }

    @Test
    public void testGreaterThanEqualDate() {
        when(filterCriterion.getOperator()).thenReturn(CriterionOperator.GTE);
        
        queryBuilder.add(RestrictionFactory.forFilterCriterion(filterCriterion));
        
        assertEquals("foo.metaData.lastModifiedDate >= ?", queryBuilder.toEjbQl());

        assertArrayEquals(new Date[] {from}, queryBuilder.getValues());
    }
}
