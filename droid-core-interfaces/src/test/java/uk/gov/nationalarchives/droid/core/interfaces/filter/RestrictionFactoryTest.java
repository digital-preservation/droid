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
