/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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
