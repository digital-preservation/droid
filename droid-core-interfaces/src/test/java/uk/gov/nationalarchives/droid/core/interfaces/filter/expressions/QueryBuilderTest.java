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
package uk.gov.nationalarchives.droid.core.interfaces.filter.expressions;

import java.util.Date;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author rflitcroft
 *
 */
public class QueryBuilderTest {

    @Test
    public void testAddFileNameCriterion() {
        
        QueryBuilder queryBuilder = QueryBuilder.forAlias("prn");
        queryBuilder.add(Restrictions.eq("metaData.name", "foo bar"));
        
        assertEquals("prn.metaData.name = ?", queryBuilder.toEjbQl());
        final Object[] values = queryBuilder.getValues();
        assertEquals(1, values.length);
        assertEquals("foo bar", values[0]);
        
    }

    @Test
    public void testLikeWithCriterion() {
        
        QueryBuilder queryBuilder = QueryBuilder.forAlias("prn");
        queryBuilder.add(Restrictions.like("metaData.name", "foo bar"));
        
        assertEquals("prn.metaData.name LIKE ?", queryBuilder.toEjbQl());
        final Object[] values = queryBuilder.getValues();
        assertEquals(1, values.length);
        assertEquals("foo bar", values[0]);
        
    }

    @Test
    public void testAddFileNameCriterionAndLastModifiedCriterion() {
        
        Date now = new Date();
        
        QueryBuilder queryBuilder = QueryBuilder.forAlias("prn");
        queryBuilder.add(Restrictions.eq("metaData.name", "foo bar"));
        queryBuilder.add(Restrictions.gt("metaData.lastModifiedDate", now));
        
        assertEquals("prn.metaData.name = ? AND "
                + "prn.metaData.lastModifiedDate > ?", queryBuilder.toEjbQl());
        final Object[] values = queryBuilder.getValues();
        assertEquals(2, values.length);
        assertEquals("foo bar", values[0]);
        assertEquals(now, values[1]);
    }

    @Test
    public void testAddFileNameCriterionAndLastModifiedCriterionAndPuidCriterion() {
        
        Date now = new Date();
        String[] puids = new String[] {
            "fmt/1",
            "fmt/2",
            "fmt/3",
        };
        
        QueryBuilder queryBuilder = QueryBuilder.forAlias("prn")
            .add(Restrictions.eq("metaData.name", "foo bar"))
            .add(Restrictions.gt("metaData.lastModifiedDate", now));
        
        queryBuilder.createAlias("fis")
            .add(Restrictions.in("fis.format.puid", puids));
        
        assertEquals("prn.metaData.name = ? AND "
                + "prn.metaData.lastModifiedDate > ? AND "
                + "fis.format.puid IN (?, ?, ?)", queryBuilder.toEjbQl());
        final Object[] values = queryBuilder.getValues();
        assertEquals(5, values.length);
        assertEquals("foo bar", values[0]);
        assertEquals(now, values[1]);
        assertEquals("fmt/1", values[2]);
        assertEquals("fmt/2", values[3]);
        assertEquals("fmt/3", values[4]);
    }
    
    @Test
    public void testOrQueryWithFileNameCriterionAndLastModifiedCriterionAndPuidCriterion() {
        
        Date now = new Date();
        String[] puids = new String[] {
            "fmt/1",
            "fmt/2",
            "fmt/3",
        };
        
        QueryBuilder queryBuilder = QueryBuilder.forAlias("prn");
        queryBuilder.createAlias("fis");
        queryBuilder.add(Restrictions.disjunction()
                .add(Restrictions.eq("metaData.name", "foo bar"))
                .add(Restrictions.gt("metaData.lastModifiedDate", now))
                .add(Restrictions.in("fis.format.puid", puids)));
        
        assertEquals("(prn.metaData.name = ? OR "
                + "prn.metaData.lastModifiedDate > ? OR "
                + "fis.format.puid IN (?, ?, ?))", queryBuilder.toEjbQl());
        final Object[] values = queryBuilder.getValues();
        assertEquals(5, values.length);
        assertEquals("foo bar", values[0]);
        assertEquals(now, values[1]);
        assertEquals("fmt/1", values[2]);
        assertEquals("fmt/2", values[3]);
        assertEquals("fmt/3", values[4]);
    }
}
