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
package uk.gov.nationalarchives.droid.core.interfaces.filter;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class StringListParserTest {

    @Test
    public void testEmptyValue() {
        assertTrue(StringListParser.STRING_LIST_PARSER.parseListValues("").isEmpty());
    }

    @Test
    public void testOnlyWhitespaceIsEmpty() {
        assertTrue(StringListParser.STRING_LIST_PARSER.parseListValues("        ").isEmpty());
    }

    @Test
    public void testSingleUnquotedItem() {
        testSingleUnquotedItem("one");
        testSingleUnquotedItem("1");
        testSingleUnquotedItem("Alongerstring");
        testSingleUnquotedItem(".");
        testSingleUnquotedItem("\"");
    }

    private void testSingleUnquotedItem(String value) {
        List<String> list = StringListParser.STRING_LIST_PARSER.parseListValues(value);
        assertEquals(1, list.size());
        assertEquals(value, list.get(0));

        String valueWithWhitespace = "   " + value + "       ";
        list = StringListParser.STRING_LIST_PARSER.parseListValues(valueWithWhitespace);
        assertEquals(1, list.size());
        assertEquals(value, list.get(0));
    }

    @Test
    public void testSingleQuotedItem() {
        testSingleQuotedItem("'one'");
        testSingleQuotedItem("'1'");
        testSingleQuotedItem("'Alongerstring'");
        testSingleQuotedItem("'.'");
        testSingleQuotedItem("'\"'");
        testSingleQuotedItem("'A longer string with spaces'");
    }

    private void testSingleQuotedItem(String value) {
        List<String> list = StringListParser.STRING_LIST_PARSER.parseListValues(value);
        assertEquals(1, list.size());
        assertEquals(value.substring(1, value.length() - 1), list.get(0)); // quotes are stripped out.

        String valueWithWhitespace = "   " + value + "       ";
        list = StringListParser.STRING_LIST_PARSER.parseListValues(valueWithWhitespace);
        assertEquals(1, list.size());
        assertEquals(value.substring(1, value.length() - 1), list.get(0)); // quotes are stripped out.
    }

    @Test
    public void testMultipleUnquotedItems() {
        testMultipleItems("1 2", "1", "2" );
        testMultipleItems("one two three", "one", "two", "three" );
        testMultipleItems("one two    three   ", "one", "two", "three" );
        testMultipleItems("a b c d e f g", "a", "b", "c", "d", "e", "f", "g");
        testMultipleItems("a   b   c          d   e   f    g", "a", "b", "c", "d", "e", "f", "g");
    }

    @Test
    public void testMultipleQuotedItems() {
        testMultipleItems("'1' '2'", "1", "2" );
        testMultipleItems("'one' 'two' 'three'", "one", "two", "three" );
        testMultipleItems("'one' 'two'    'three'   ", "one", "two", "three" );
        testMultipleItems("'a' 'b' 'c' 'd' 'e' 'f' 'g'", "a", "b", "c", "d", "e", "f", "g");
        testMultipleItems("'a'   'b'   'c'          'd'   'e'   'f'    'g'", "a", "b", "c", "d", "e", "f", "g");
    }

    @Test
    public void testMixedQuotedAndUnquotedItems() {
        testMultipleItems("'1' 2", "1", "2" );
        testMultipleItems("'one' 'two' three", "one", "two", "three" );
        testMultipleItems("one 'two'    'three'   ", "one", "two", "three" );
        testMultipleItems("'a' b 'c' 'd' e 'f' 'g'", "a", "b", "c", "d", "e", "f", "g");
        testMultipleItems("'a'   'b'   'c'          d   e   'f'    'g'", "a", "b", "c", "d", "e", "f", "g");
    }

    @Test
    public void testQuotedItemsWithSpaces() {
        testMultipleItems("'several spaces here'   'more spaces'", "several spaces here", "more spaces");
        testMultipleItems("'one two'   three  'four five'", "one two", "three", "four five");
    }

    @Test
    public void testQuotesInsideSpaceSeparatedItems() {
        testMultipleItems("astring'withquote ins'de", "astring'withquote", "ins'de");
        testMultipleItems("a'b   cd'on xxxx", "a'b", "cd'on", "xxxx");
    }

    private void testMultipleItems(String value, String... values) {
        List<String> list = StringListParser.STRING_LIST_PARSER.parseListValues(value);
        assertEquals(values.length, list.size());
        for (int i = 0; i < list.size(); i++) {
            assertEquals(values[i], list.get(i));
        }

        String valueWithWhitespace = "   " + value + "       ";
        list = StringListParser.STRING_LIST_PARSER.parseListValues(valueWithWhitespace);
        assertEquals(values.length, list.size());
        for (int i = 0; i < list.size(); i++) {
            assertEquals(values[i], list.get(i));
        }
    }

}