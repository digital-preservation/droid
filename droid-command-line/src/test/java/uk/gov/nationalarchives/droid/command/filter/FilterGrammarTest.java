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
