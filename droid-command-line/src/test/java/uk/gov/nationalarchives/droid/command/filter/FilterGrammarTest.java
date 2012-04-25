/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.command.filter;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.joda.time.DateMidnight;
import org.junit.Ignore;
import org.junit.Test;

import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionFieldEnum;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionOperator;
import uk.gov.nationalarchives.droid.core.interfaces.filter.FilterCriterion;

/**
 * @author rflitcroft
 *
 */
public class FilterGrammarTest {

    @Test
    public void testFilterOnEqualFileName() {
        
        String dql = "file_name = 'foo bar'";
        
        AntlrDqlParser parser = new AntlrDqlParser();
        FilterCriterion criterion = parser.parse(dql);
        
        assertEquals(CriterionFieldEnum.FILE_NAME, criterion.getField());
        assertEquals(CriterionOperator.EQ, criterion.getOperator());
        assertEquals("foo bar", criterion.getValue());
        
    }

    @Test
    public void testFilterOnEqualFileNameWithDoubleQuote() {
        
        String dql = "file_name = 'foo \" bar'";
        
        AntlrDqlParser parser = new AntlrDqlParser();
        FilterCriterion criterion = parser.parse(dql);
        
        assertEquals(CriterionFieldEnum.FILE_NAME, criterion.getField());
        assertEquals(CriterionOperator.EQ, criterion.getOperator());
        assertEquals("foo \" bar", criterion.getValue());
        
    }

    @Test
    public void testFilterOnEqualFileNameWithEscapedSingleQuote() {
        
        String dql = "file_name = 'foo \\' bar'";
        
        AntlrDqlParser parser = new AntlrDqlParser();
        FilterCriterion criterion = parser.parse(dql);
        
        assertEquals(CriterionFieldEnum.FILE_NAME, criterion.getField());
        assertEquals(CriterionOperator.EQ, criterion.getOperator());
        assertEquals("foo ' bar", criterion.getValue());
        
    }

    @Test
    public void testFilterOnDate() {
        
        String dql = "last_modified = 2010-01-23";
        
        AntlrDqlParser parser = new AntlrDqlParser();
        FilterCriterion criterion = parser.parse(dql);
        
        assertEquals(CriterionFieldEnum.LAST_MODIFIED_DATE, criterion.getField());
        assertEquals(CriterionOperator.EQ, criterion.getOperator());
        assertEquals(new DateMidnight(2010, 01, 23).toDate(), criterion.getValue());
        
    }

    @Test
    @Ignore("are we supporting years like this?")
    public void testFilterOnYear() {
        
        String dql = "last_modified = 2010";
        
        AntlrDqlParser parser = new AntlrDqlParser();
        FilterCriterion criterion = parser.parse(dql);
        
        assertEquals(CriterionFieldEnum.LAST_MODIFIED_DATE, criterion.getField());
        assertEquals(CriterionOperator.EQ, criterion.getOperator());
        assertEquals(2010L, criterion.getValue());
        
    }

    @Test
    public void testFilterOnAnySet() {
        
        String dql = "puid ANY a b c d";
        
        AntlrDqlParser parser = new AntlrDqlParser();
        FilterCriterion criterion = parser.parse(dql);
        
        assertEquals(CriterionFieldEnum.PUID, criterion.getField());
        assertEquals(CriterionOperator.ANY_OF, criterion.getOperator());
        assertArrayEquals(new String[] {
            "a", "b", "c", "d",     
        }, (Object[]) criterion.getValue());
    }

    @Test
    public void testFilterOnNoneSet() {
        
        String dql = "puid NONE a b c d";
        
        AntlrDqlParser parser = new AntlrDqlParser();
        FilterCriterion criterion = parser.parse(dql);

        assertEquals(CriterionFieldEnum.PUID, criterion.getField());
        assertEquals(CriterionOperator.NONE_OF, criterion.getOperator());
        assertArrayEquals(new String[] {
            "a", "b", "c", "d",     
        }, (Object[]) criterion.getValue());
    }

    @Test(expected = DqlParseException.class)
    public void testInvalidOperator() {
        
        String dql = "size IN a b c d";
        
        AntlrDqlParser parser = new AntlrDqlParser();
        parser.parse(dql);
        
    }
    
    @Test
    public void testNotContains() {
        String dql = "file_name not contains 'foo'";
        AntlrDqlParser parser = new AntlrDqlParser();
        FilterCriterion criterion = parser.parse(dql);
        
        assertEquals(CriterionFieldEnum.FILE_NAME, criterion.getField());
        assertEquals(CriterionOperator.NOT_CONTAINS, criterion.getOperator());
        assertEquals("foo", criterion.getValue());
        
    }

    @Test
    public void testNotStartsWith() {
        String dql = "file_name not starts 'foo'";
        AntlrDqlParser parser = new AntlrDqlParser();
        FilterCriterion criterion = parser.parse(dql);
        
        assertEquals(CriterionFieldEnum.FILE_NAME, criterion.getField());
        assertEquals(CriterionOperator.NOT_STARTS_WITH, criterion.getOperator());
        assertEquals("foo", criterion.getValue());
    }

    @Test
    public void testNotEndsWith() {
        String dql = "file_name not ends 'foo'";
        AntlrDqlParser parser = new AntlrDqlParser();
        FilterCriterion criterion = parser.parse(dql);
        
        assertEquals(CriterionFieldEnum.FILE_NAME, criterion.getField());
        assertEquals(CriterionOperator.NOT_ENDS_WITH, criterion.getOperator());
        assertEquals("foo", criterion.getValue());
    }
}
