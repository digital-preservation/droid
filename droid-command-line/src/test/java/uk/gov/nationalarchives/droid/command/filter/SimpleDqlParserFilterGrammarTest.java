/**
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gov.uk>
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.joda.time.DateMidnight;
import org.junit.Test;

import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionFieldEnum;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionOperator;
import uk.gov.nationalarchives.droid.core.interfaces.filter.FilterCriterion;

import java.util.Locale;

/**
 * @author rflitcroft/boreilly
 *
 */
public class SimpleDqlParserFilterGrammarTest {

    private DqlFilterParser parser = new DqlFilterParser();

    //TODO: add more comprehensive tests of filter grammar.

    @Test(expected=DqlParseException.class)
    public void testMissingSingleFilterValue() {
        parser.parse("file_name = ");
    }

    @Test(expected=DqlParseException.class)
    public void testMissingListValues() {
        parser.parse("puid any ");
    }

    @Test(expected=DqlParseException.class)
    public void testInvalidFieldSingleValue() {
        parser.parse("xxx1 = ");
    }

    @Test(expected=DqlParseException.class)
    public void testInvalidFieldListValues() {
        parser.parse("xxx1 any ");
    }

    @Test(expected=DqlParseException.class)
    public void testParseEmptyString() {
        parser.parse("");
    }

    @Test(expected=DqlParseException.class)
    public void testInvalidFieldName() {
        parser.parse("1234 > 1000");
    }

    @Test
    public void testFilterOnEqualFileName() {
        
        String dql = "file_name = 'foo bar'";
        
        FilterCriterion criterion = parser.parse(dql);
        
        assertEquals(CriterionFieldEnum.FILE_NAME, criterion.getField());
        assertEquals(CriterionOperator.EQ, criterion.getOperator());
        assertEquals("foo bar".toUpperCase(), criterion.getValue());
        
    }

    @Test
    public void testFilterOnEqualFileNameWithDoubleQuote() {
        
        String dql = "file_name = 'foo \" bar'";
        
        FilterCriterion criterion = parser.parse(dql);
        
        assertEquals(CriterionFieldEnum.FILE_NAME, criterion.getField());
        assertEquals(CriterionOperator.EQ, criterion.getOperator());
        assertEquals("foo \" bar".toUpperCase(), criterion.getValue());

    }

    @Test
    public void testFilterOnDate() {

        String dql = "last_modified = 2010-01-23";

        DqlFilterParser parser = new DqlFilterParser();
        FilterCriterion criterion = parser.parse(dql);
        
        assertEquals(CriterionFieldEnum.LAST_MODIFIED_DATE, criterion.getField());
        assertEquals(CriterionOperator.EQ, criterion.getOperator());
        assertEquals(new DateMidnight(2010, 01, 23).toDate(), criterion.getValue());
        
    }

    @Test
    public void testFilterOnDateWithQuotes() {
        
        String dql = "last_modified = '2010-01-23'";
        
        FilterCriterion criterion = parser.parse(dql);
        
        assertEquals(CriterionFieldEnum.LAST_MODIFIED_DATE, criterion.getField());
        assertEquals(CriterionOperator.EQ, criterion.getOperator());
        assertEquals(new DateMidnight(2010, 01, 23).toDate(), criterion.getValue());
        
    }

    @Test
    public void testFilterOnAnySet() {
        
        String dql = "puid ANY a b c d";
        
        FilterCriterion criterion = parser.parse(dql);
        
        assertEquals(CriterionFieldEnum.PUID, criterion.getField());
        assertEquals(CriterionOperator.ANY_OF, criterion.getOperator());
        assertArrayEquals(new String[] {
            "a", "b", "c", "d",     
        }, (Object[]) criterion.getValue());
    }

    @Test
    public void testFilterOnAnySetWithWithSingleQuotes() {
        
        String dql = "puid ANY 'x-fmt/10' 'fmt/10' 'fmt/44' 'x-fmt/238'";
        
        FilterCriterion criterion = parser.parse(dql);
        
        assertEquals(CriterionFieldEnum.PUID, criterion.getField());
        assertEquals(CriterionOperator.ANY_OF, criterion.getOperator());
        assertArrayEquals(new String[] {
            "x-fmt/10", "fmt/10", "fmt/44", "x-fmt/238",     
        }, (Object[]) criterion.getValue());
    }

    @Test
    public void testFilterOnNoneSet() {
        
        String dql = "puid NONE a b c d";
        
        FilterCriterion criterion = parser.parse(dql);

        assertEquals(CriterionFieldEnum.PUID, criterion.getField());
        assertEquals(CriterionOperator.NONE_OF, criterion.getOperator());
        assertArrayEquals(new String[] {
            "a", "b", "c", "d",     
        }, (Object[]) criterion.getValue());
    }

    @Test
    public void testFilterOnNoneSetWithSingleQuotes() {
        
        String dql = "puid NONE 'a' 'b' 'c' 'd'";
        
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
        
        parser.parse(dql);
        
    }

    @Test
    public void testValidOperators() {
        testNumOperator("<", CriterionOperator.LT);
        testNumOperator("<=", CriterionOperator.LTE);
        testNumOperator("=", CriterionOperator.EQ);
        testNumOperator("<>", CriterionOperator.NE);
        testNumOperator(">", CriterionOperator.GT);
        testNumOperator(">=", CriterionOperator.GTE);
        testStringOperator("starts", CriterionOperator.STARTS_WITH);
        testStringOperator("ends", CriterionOperator.ENDS_WITH);
        testStringOperator("contains", CriterionOperator.CONTAINS);
        testStringOperator("not starts", CriterionOperator.NOT_STARTS_WITH);
        testStringOperator("not ends", CriterionOperator.NOT_ENDS_WITH);
        testStringOperator("not contains", CriterionOperator.NOT_CONTAINS);
        testListOperator("any", CriterionOperator.ANY_OF);
        testListOperator("none", CriterionOperator.NONE_OF);
    }

    private void testNumOperator(String dqlOperator, CriterionOperator operatorType) {
        // test without spaces between field/operator/value for numeric operators:
        FilterCriterion crit = parser.parse("file_size" + dqlOperator + "1000");
        assertEquals(operatorType, crit.getOperator());

        // test with spaces
        crit = parser.parse("  file_size   " + dqlOperator + "   1000  ");
        assertEquals(operatorType, crit.getOperator());
    }

    private void testStringOperator(String dqlOperator, CriterionOperator operatorType) {
        FilterCriterion crit = parser.parse("file_name " + dqlOperator + " invoice");
        assertEquals(operatorType, crit.getOperator());
    }

    private void testListOperator(String dqlOperator, CriterionOperator operatorType) {
        FilterCriterion crit = parser.parse("puid " + dqlOperator + " fmt/1 fmt2 fmt/3");
        assertEquals(operatorType, crit.getOperator());
    }

    /**
     * It looks like the user is passing in a list of different items, but "contains" is
     * looking for a single value.  So the parser should interpret this as a single
     * value with quotes at the start and end, not a list of items.  This would probably
     * be a mistake by a user - but the parser still needs to parse it correctly, not throw
     * an error or "helpfully" interpret it as a list (which can't be filtered by the contains operator).
     * It's still possible that this is a valid thing to look for.
     */
    @Test
    public void testContainsQuotedStringNotAListOfItems() {
        String dql = "file_name  contains 'abc' 'def' 'ghi'";
        FilterCriterion criterion = parser.parse(dql);
        
        assertEquals(CriterionFieldEnum.FILE_NAME, criterion.getField());
        assertEquals(CriterionOperator.CONTAINS, criterion.getOperator());
        assertEquals("abc' 'def' 'ghi".toUpperCase(), criterion.getValue());
        
    }
    
    @Test
    public void testNotContains() {
        String dql = "file_name not contains 'foo'";
        FilterCriterion criterion = parser.parse(dql);
        
        assertEquals(CriterionFieldEnum.FILE_NAME, criterion.getField());
        assertEquals(CriterionOperator.NOT_CONTAINS, criterion.getOperator());
        assertEquals("foo".toUpperCase(), criterion.getValue());
        
    }

    @Test
    public void testNotStartsWith() {
        String dql = "file_name not starts 'foo'";
        FilterCriterion criterion = parser.parse(dql);
        
        assertEquals(CriterionFieldEnum.FILE_NAME, criterion.getField());
        assertEquals(CriterionOperator.NOT_STARTS_WITH, criterion.getOperator());
        assertEquals("foo".toUpperCase(), criterion.getValue());
    }

    @Test
    public void testNotEndsWith() {
        String dql = "file_name not ends 'foo'";
        FilterCriterion criterion = parser.parse(dql);
        
        assertEquals(CriterionFieldEnum.FILE_NAME, criterion.getField());
        assertEquals(CriterionOperator.NOT_ENDS_WITH, criterion.getOperator());
        assertEquals("foo".toUpperCase(), criterion.getValue());
    }
}
