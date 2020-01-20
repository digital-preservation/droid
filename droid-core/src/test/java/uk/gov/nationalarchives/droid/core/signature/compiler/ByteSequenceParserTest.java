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
package uk.gov.nationalarchives.droid.core.signature.compiler;

import net.byteseek.compiler.CompileException;
import net.byteseek.compiler.matcher.SequenceMatcherCompiler;
import net.byteseek.matcher.bytes.ByteMatcher;
import net.byteseek.matcher.sequence.SequenceMatcher;
import net.byteseek.parser.ParseException;
import net.byteseek.parser.tree.ParseTree;
import net.byteseek.parser.tree.ParseTreeType;
import net.byteseek.parser.tree.ParseTreeUtils;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;

import static org.junit.Assert.*;

public class ByteSequenceParserTest {

    private ByteSequenceParser PARSER = ByteSequenceParser.PARSER;

    @Test
    public void testParseEmptyExpression() throws Exception {
        // Completely empty string
        ParseTree empty = PARSER.parse("");
        assertEquals(ParseTreeType.SEQUENCE, empty.getParseTreeType());
        assertEquals(0, empty.getNumChildren());

        // Just basic whitespace
        empty = PARSER.parse("               ");
        assertEquals(ParseTreeType.SEQUENCE, empty.getParseTreeType());
        assertEquals(0, empty.getNumChildren());

        // Just spaces, tabs, newlines and carriage returns:
        empty = PARSER.parse("        \n   \t  \r     ");
        assertEquals(ParseTreeType.SEQUENCE, empty.getParseTreeType());
        assertEquals(0, empty.getNumChildren());
    }

    @Test(expected = ParseException.class)
    public void testMissingHexDigitException() throws Exception {
        PARSER.parse("0");
    }

    @Test(expected = ParseException.class)
    public void testMissingHexDigitException2() throws Exception {
        PARSER.parse("0a 34d");
    }

    @Test
    public void parseByteSequences() throws Exception {
        // without whitespace:
        testParseByteSequences("00", 0);
        testParseByteSequences("0001", 0, 1);
        testParseByteSequences("0100", 1, 0);
        testParseByteSequences("0a0b0c0d0e0f", 10, 11, 12, 13, 14, 15);

        // with whitespace:
        testParseByteSequences(" 0a 0b 0c 0d 0e 0f ", 10, 11, 12, 13, 14, 15);
        testParseByteSequences(" \t0a 0b\n0c 0d\r 0e 0f ", 10, 11, 12, 13, 14, 15);
    }

    private void testParseByteSequences(String sequence, int... values) throws ParseException {
        ParseTree result = PARSER.parse(sequence);
        assertEquals(ParseTreeType.SEQUENCE, result.getParseTreeType());
        assertEquals(values.length, result.getNumChildren());
        for (int i = 0; i < values.length; i++) {
            ParseTree byteNode = result.getChild(i);
            assertEquals(ParseTreeType.BYTE, byteNode.getParseTreeType());
            assertEquals(values[i], byteNode.getByteValue() & 0xFF);
        }
    }

    @Test(expected = ParseException.class)
    public void testUnclosedStringException() throws Exception {
        PARSER.parse("   'a string which isn't closed");
    }

    @Test(expected = ParseException.class)
    public void testUnopenedStringException() throws Exception {
        PARSER.parse("   a string which isn't opened'");
    }

    @Test(expected = ParseException.class)
    public void testAnotherUnclosedStringException() throws Exception {
        PARSER.parse("   'first string is closed' 'a string which isn't closed");
    }

    @Test(expected = ParseException.class)
    public void testStringWithMissingHexDigitException() throws Exception {
        PARSER.parse("'there is a string followed by an incomplete hex digit' ff 0");
    }

    @Test
    public void parseStrings() throws Exception {
        // Empty string can be parsed (but won't make any sense once compiled...). //TODO: should we allow empty string?
        testParseStrings("''");

        // Single byte strings:
        testParseStrings("' '");
        testParseStrings("'*'");
        testParseStrings("'A'");

        // Longer strings:
        testParseStrings("'abcdefg'");
        testParseStrings("'01234 567 abasdfq3wasvasdfas'");

        // Multiple strings in one expression:
        testParseStrings("' '  'abcde'  '0123456789'   ", " ", "abcde", "0123456789");
        testParseStrings("'a''b''c''d''e''f'", "a", "b", "c", "d", "e", "f");

        // Mixed bytes and strings:
        testParseStrings("'012345' 01 02 03 04 05", "012345", "0x01", "0x02", "0x03", "0x04", "0x05");
        testParseStrings(" FF 'some text' 01 02 03 aa 'more text' ", "0xFF", "some text", "0x01", "0x02", "0x03", "0xaa", "more text");
        testParseStrings("  \t '00' 00 'fe' FE 'Aa' aa", "00", "0x00", "fe", "0xfe", "Aa", "0xAA");
    }

    private void testParseStrings(String sequence, String... results) throws ParseException {
        ParseTree seq = PARSER.parse(sequence);
        assertEquals(ParseTreeType.SEQUENCE, seq.getParseTreeType());
        if (results.length == 0) { // optimisation - if you don't specify any results, just trim the input string, avoids retyping the same value in the test for a single string.
            assertEquals(1, seq.getNumChildren());
            ParseTree stringChild = seq.getChild(0);
            assertEquals(ParseTreeType.STRING, stringChild.getParseTreeType());
            String innerString = sequence.substring(1, sequence.length() - 1);
            assertEquals(innerString, stringChild.getTextValue());
        } else {
            assertEquals(results.length, seq.getNumChildren());
            for (int i = 0; i < results.length; i++) {
                ParseTree child = seq.getChild(i);
                String resultString = results[i];

                if (resultString.startsWith("0x")) {
                    int resultValue = Integer.valueOf(resultString.substring(2), 16);
                    assertEquals(ParseTreeType.BYTE, child.getParseTreeType());
                    assertEquals(resultValue, child.getByteValue() & 0xFF);
                } else {
                    assertEquals(ParseTreeType.STRING, child.getParseTreeType());
                    assertEquals(resultString, child.getTextValue());
                }
            }
        }
    }

    @Test(expected = ParseException.class)
    public void testEmptyRoundBracketsException() throws Exception {
        PARSER.parse("( )");
    }

    @Test(expected = ParseException.class)
    public void testEmptyAlternativesException() throws Exception {
        PARSER.parse("(|)");
    }

    @Test(expected = ParseException.class)
    public void testEmptyFirstAlternativeException() throws Exception {
        PARSER.parse("(|01)");
    }

    @Test(expected = ParseException.class)
    public void testEmptySecondAlternativeException() throws Exception {
        PARSER.parse("(ff|)");
    }

    @Test(expected = ParseException.class)
    public void testEmptyMiddleAlternativeException() throws Exception {
        PARSER.parse("(ff||00)");
    }

    @Test(expected = ParseException.class)
    public void testUnclosedAlternativeException() throws Exception {
        PARSER.parse("(ff|fe|00     ");
    }

    @Test
    public void testOneValueAlternatives() throws Exception {
        ParseTree result = PARSER.parse("(cd)");
        assertEquals(ParseTreeType.SEQUENCE, result.getParseTreeType());
        assertEquals(1, result.getNumChildren());
        ParseTree value = result.getChild(0);
        assertEquals(ParseTreeType.BYTE, value.getParseTreeType());
        assertEquals((byte) 0xcd, value.getByteValue());
    }

    @Test
    public void testSingleByteAlternatives() throws Exception {
        testSingleByteAlternatives("(01|02)", 1, 2);
        testSingleByteAlternatives("(01|02|03)", 1, 2, 3);
        testSingleByteAlternatives("(ff|fe|a1|a6|45)", 255, 254, 161, 166, 69);
    }

    private void testSingleByteAlternatives(String expression, int... values) throws ParseException {
        ParseTree alts = testHeader(expression);
        assertEquals(ParseTreeType.ALTERNATIVES, alts.getParseTreeType());
        assertEquals(values.length, alts.getNumChildren());
        for (int i = 0; i < values.length; i++) {
            ParseTree altValue = alts.getChild(i);
            assertEquals(ParseTreeType.BYTE, altValue.getParseTreeType());
            assertEquals(values[i], altValue.getByteValue() & 0xFF);
        }
    }

    @Test
    public void testStringAlternatives() throws Exception {
        testStringAlternatives(" ( 'one' | 'two' | 'three' ) ", "one", "two", "three");
        testStringAlternatives(" ( 'aa' | 'more strings' | 'even more' | 'fourth' ) ", "aa", "more strings", "even more", "fourth");
        testStringAlternatives(  "('1'|'2'|'3'|'4'|'5'|'6'|'7'|'8'|'9'|'0')", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0");

    }

    private void testStringAlternatives(String expression, String... values) throws ParseException {
        ParseTree alts = testHeader(expression);
        assertEquals(ParseTreeType.ALTERNATIVES, alts.getParseTreeType());
        assertEquals(values.length, alts.getNumChildren());
        for (int i = 0; i < values.length; i++) {
            ParseTree altValue = alts.getChild(i);
            assertEquals(ParseTreeType.STRING, altValue.getParseTreeType());
            assertEquals(values[i], altValue.getTextValue());
        }
    }

    @Test(expected = ParseException.class)
    public void testInvalidCharInAlternatives() throws Exception {
        PARSER.parse("(01 02 03| 04 05 'string' [&01] | ^ 'not good char before')");
    }

    @Test
    public void testSequenceAlternatives() throws Exception {
        // Basic alternative sequences:
        testSequenceAlternatives("  (01 02 03| ff fe fd | 06 c1 08 df)", "010203", "FFFEFD", "06c108df");
        testSequenceAlternatives("  (03 04| a1 a2 a3 a4)", "0304", "a1A2a3A4");

        // Mixed byte and byte sequences:
        testSequenceAlternatives("  (01|fd a1 c3 b4|02 03 04)", "01", "fdA1c3B4", "020304");
        testSequenceAlternatives("(01|0f|0c|bcbdbe)", "01", "0f", "0C", "bcBDbe");

        // Strings in alternatives:
        testSequenceAlternatives("('one'|'two'|'three')", "one", "two", "three" );

        // Mixed strings, byte and byte sequences:
        testSequenceAlternatives("(00| 01 02 03 04 05 | 'string')", "00", "0102030405", "string");

        // Ranges in alternatives:
        testSequenceAlternatives("([00-23]|[45-47]|[Ce-df])", "[00-23]", "[45-47]", "[ce-df]");

        // ?? in alternatives:
        testSequenceAlternatives("(??|'string'|01 02)", ".", "string", "0102");

        // Bitmasks in alternatives:
        testSequenceAlternatives("([&01]|[&10]|[&fd])", "&01", "&10", "&fd");
    }

    private void testSequenceAlternatives(String expression, String... hexValues) throws ParseException, CompileException {
        ParseTree alts = testHeader(expression);
        assertEquals(hexValues.length, alts.getNumChildren());
        for (int i = 0; i < hexValues.length; i++) {
            String hexValue = hexValues[i];
            ParseTree altValue = alts.getChild(i);
            switch (altValue.getParseTreeType()) {
                case STRING: {
                    assertEquals(hexValue, altValue.getTextValue());
                    break;
                }
                case BYTE: {
                    int numHexValues = hexValue.length() / 2;
                    assertEquals(1, numHexValues);
                    int byteValue = Integer.valueOf(hexValue, 16);
                    assertEquals(byteValue, altValue.getByteValue() & 0xFF);
                    break;
                }
                case SEQUENCE: { // ASSUMES a sequence of byte values only, not other types - need to extend?
                    int numHexValues = hexValue.length() / 2;
                    assertEquals(ParseTreeType.SEQUENCE, altValue.getParseTreeType());
                    assertEquals(numHexValues, altValue.getNumChildren());
                    for (int byteIndex = 0; byteIndex < numHexValues; byteIndex++) {
                        ParseTree byteNode = altValue.getChild(byteIndex);
                        assertEquals(ParseTreeType.BYTE, byteNode.getParseTreeType());
                        int stringPosition = 2 * byteIndex;
                        int byteValue = Integer.valueOf(hexValue.substring(stringPosition, stringPosition + 2), 16);
                        assertEquals(byteValue, byteNode.getByteValue() & 0xFF);
                    }
                    break;
                }
                case SET: {
                    SequenceMatcher reference = SequenceMatcherCompiler.compileFrom(hexValue);
                    ByteMatcher matcher = reference.getMatcherForPosition(0);
                    Set<Byte> values = ParseTreeUtils.calculateSetValues(altValue);
                    assertEquals(matcher.getNumberOfMatchingBytes(), values.size());
                    for (byte b : matcher.getMatchingBytes()) {
                        assertTrue(values.contains(b));
                    }
                    break;
                }
                case ANY: {
                    break; // nothing to test - it matches everything.
                }
                default :  fail("Unknown alternative sequence type: " + altValue);
            }

        }
    }

    @Test
    public void testAsteriskMakesZeroToManyNode() throws ParseException {
        testAsteriskMakesZeroToManyNode("*", 1, 0);
        testAsteriskMakesZeroToManyNode("01 02 03 *", 4, 3);
        testAsteriskMakesZeroToManyNode(" 'starting' ff fe * fe ff 'ending'", 7, 3);
    }

    private void testAsteriskMakesZeroToManyNode(String expression, int numChildren, int repeatPos) throws ParseException {
        ParseTree result = PARSER.parse(expression);
        assertEquals(ParseTreeType.SEQUENCE, result.getParseTreeType());
        assertEquals(numChildren, result.getNumChildren());
        ParseTree node = result.getChild(repeatPos);
        assertEquals(ParseTreeType.ZERO_TO_MANY, node.getParseTreeType());
        assertEquals(1, node.getNumChildren());
        assertEquals(ParseTreeType.ANY, node.getChild(0).getParseTreeType());
    }

    @Test(expected = ParseException.class)
    public void testMissingSecondQuestionMarkException() throws Exception {
        PARSER.parse("01 02 ? 04");
    }

    @Test
    public void testQuestionMarkAnyBytes() throws Exception {
        testQuestionMark("??", 1, 0);
        testQuestionMark("0f f2  34 ??", 4, 3);
        testQuestionMark("0f f2 'astring' 34 ?? 04 33 43 23", 9, 4);
    }

    private void testQuestionMark(String expression, int numChildren, int anyPos) throws ParseException {
        ParseTree result = PARSER.parse(expression);
        assertEquals(ParseTreeType.SEQUENCE, result.getParseTreeType());
        assertEquals(numChildren, result.getNumChildren());
        ParseTree node = result.getChild(anyPos);
        assertEquals(ParseTreeType.ANY, node.getParseTreeType());
        assertEquals(0, node.getNumChildren());
    }

    @Test(expected = ParseException.class)
    public void testUnclosedByteRange() throws Exception {
        PARSER.parse("[01");
    }

    @Test(expected = ParseException.class)
    public void testUnclosedByteRange2() throws Exception {
        PARSER.parse("[01 04 05 06");
    }

    @Test(expected = ParseException.class)
    public void testUnopenedByteRange() throws Exception {
        PARSER.parse("01]");
    }

    @Test(expected = ParseException.class)
    public void testUnclosedInvertedByteRange() throws Exception {
        PARSER.parse("[!01");
    }

    @Test(expected = ParseException.class)
    public void testUnclosedInvertedByteRange2() throws Exception {
        PARSER.parse("[!01 04 05 06");
    }

    @Test(expected = ParseException.class)
    public void testUnopenedInvertedByteRange() throws Exception {
        PARSER.parse("!01]");
    }

    @Test
    public void testSingleByteRange() throws ParseException {
        for (int byteValue = 0; byteValue < 256; byteValue++) {
            String expression = "[" + String.format("%02x", byteValue) + "]";
            ParseTree result = PARSER.parse(expression);
            assertEquals(ParseTreeType.SEQUENCE, result.getParseTreeType());
            assertEquals(1, result.getNumChildren());
            ParseTree setNode = result.getChild(0);

            assertEquals(ParseTreeType.SET, setNode.getParseTreeType());
            assertEquals(1, setNode.getNumChildren());
            assertFalse(setNode.isValueInverted());
            ParseTree byteNode = setNode.getChild(0);

            assertEquals(ParseTreeType.BYTE, byteNode.getParseTreeType());
            assertEquals(byteValue, byteNode.getByteValue() & 0xFF);
        }
    }

    @Test
    public void testInvertedSingleByteRange() throws ParseException {
        for (int byteValue = 0; byteValue < 256; byteValue++) {
            String expression = "[!" + String.format("%02x", byteValue) + "]";
            ParseTree result = PARSER.parse(expression);
            assertEquals(ParseTreeType.SEQUENCE, result.getParseTreeType());
            assertEquals(1, result.getNumChildren());
            ParseTree setNode = result.getChild(0);

            assertEquals(ParseTreeType.SET, setNode.getParseTreeType());
            assertTrue(setNode.isValueInverted());
            assertEquals(1, setNode.getNumChildren());
            ParseTree byteNode = setNode.getChild(0);

            assertEquals(ParseTreeType.BYTE, byteNode.getParseTreeType());
            assertEquals(byteValue, byteNode.getByteValue() & 0xFF);
        }
    }

    @Test
    public void testByteRange() throws ParseException {
        for (int byteValue = 0; byteValue < 256; byteValue++) {
            for (int secondValue = 0; secondValue < 256; secondValue ++) {
                String expression = "[" + String.format("%02x:%02x", byteValue, secondValue) + "]";
                ParseTree result = PARSER.parse(expression);

                assertEquals(ParseTreeType.SEQUENCE, result.getParseTreeType());
                assertEquals(1, result.getNumChildren());
                ParseTree setNode = result.getChild(0);

                assertEquals(ParseTreeType.SET, setNode.getParseTreeType());
                assertEquals(1, setNode.getNumChildren());

                ParseTree rangeNode = setNode.getChild(0);

                assertEquals(ParseTreeType.RANGE, rangeNode.getParseTreeType());
                assertFalse(rangeNode.isValueInverted());
                assertEquals(2, rangeNode.getNumChildren());
                assertEquals(byteValue, rangeNode.getChild(0).getByteValue() & 0xFF);
                assertEquals(secondValue, rangeNode.getChild(1).getByteValue() & 0xFF);
            }
        }
    }

    @Test
    public void testInvertedByteRange() throws ParseException {
        for (int byteValue = 0; byteValue < 256; byteValue++) {
            for (int secondValue = 0; secondValue < 256; secondValue ++) {
                String expression = "[!" + String.format("%02x:%02x", byteValue, secondValue) + "]";
                ParseTree result = PARSER.parse(expression);
                assertEquals(ParseTreeType.SEQUENCE, result.getParseTreeType());
                assertEquals(1, result.getNumChildren());
                ParseTree setNode = result.getChild(0);

                assertEquals(ParseTreeType.SET, setNode.getParseTreeType());
                assertEquals(1, setNode.getNumChildren());
                assertTrue(setNode.isValueInverted());
                ParseTree rangeNode = setNode.getChild(0);

                assertEquals(ParseTreeType.RANGE, rangeNode.getParseTreeType());
                assertEquals(2, rangeNode.getNumChildren());
                assertEquals(byteValue, rangeNode.getChild(0).getByteValue() & 0xFF);
                assertEquals(secondValue, rangeNode.getChild(1).getByteValue() & 0xFF);
            }
        }
    }


    @Test(expected = ParseException.class)
    public void testMissingFirstRangeValueException() throws Exception {
        PARSER.parse("[:02]");
    }

    @Test(expected = ParseException.class)
    public void testMissingSecondRangeValueException() throws Exception {
        PARSER.parse("[01:]");
    }

    @Test(expected = ParseException.class)
    public void testMissingFirstInvertedRangeValueException() throws Exception {
        PARSER.parse("[!:02]");
    }

    @Test(expected = ParseException.class)
    public void testMissingSecondInvertedRangeValueException() throws Exception {
        PARSER.parse("[!01:]");
    }

    @Test(expected = ParseException.class)
    public void testWhiteSpaceNotAllowedInRangesException() throws Exception {
        PARSER.parse("[01 : 02]");
    }

    @Test(expected = ParseException.class)
    public void testUnclosedCurlyBrackets() throws Exception {
        PARSER.parse("{5");
    }

    @Test(expected = ParseException.class)
    public void testUnopenedCurlyBrackets() throws Exception {
        PARSER.parse("5}");
    }

    @Test(expected = ParseException.class)
    public void testUnclosedCurlyBrackets2() throws Exception {
        PARSER.parse("01 {5 03");
    }

    @Test
    public void testFixedGapCurlyBrackets() throws Exception {
        testFixedGapCurlyBrackets("{5}", 5);
        testFixedGapCurlyBrackets("{0}", 0); // probably not compilable, but should parse.
        testFixedGapCurlyBrackets("{512}", 512); // probably not compilable, but should parse.
        testFixedGapCurlyBrackets("{1023}", 1023); // probably not compilable, but should parse.
    }

    private void testFixedGapCurlyBrackets(String expression, int numToRepeat) throws ParseException {
        ParseTree result = PARSER.parse(expression);
        assertEquals(ParseTreeType.SEQUENCE, result.getParseTreeType());
        assertEquals(1, result.getNumChildren());

        ParseTree gapNode = result.getChild(0);
        assertEquals(ParseTreeType.REPEAT, gapNode.getParseTreeType());
        assertEquals(2, gapNode.getNumChildren());

        ParseTree repeatNum = gapNode.getChild(0);
        assertEquals(ParseTreeType.INTEGER, repeatNum.getParseTreeType());
        assertEquals(numToRepeat, repeatNum.getIntValue());

        ParseTree nodeToRepeat = gapNode.getChild(1);
        assertEquals(ParseTreeType.ANY, nodeToRepeat.getParseTreeType());
    }


    @Test(expected = ParseException.class)
    public void testMissingFirstValue() throws Exception {
        PARSER.parse("{-56}");
    }

    @Test(expected = ParseException.class)
    public void testMissingSecondValue() throws Exception {
        PARSER.parse("{56-}");
    }

    @Test(expected = ParseException.class)
    public void testMissingAllValues() throws Exception {
        PARSER.parse("{-}");
    }

    @Test(expected = ParseException.class)
    public void testMissingCurlyClosingBracket() throws Exception {
        PARSER.parse("{56-34");
    }

    @Test(expected = ParseException.class)
    public void testMissingCurlyOpeningBracket() throws Exception {
        PARSER.parse("56-34");
    }

    @Test
    public void testMinToMaxGaps() throws Exception {
        testMinMaxGapCurlyBrackets("{4-10}", 4, 10);
        testMinMaxGapCurlyBrackets("{0-0}", 0, 0);
        testMinMaxGapCurlyBrackets("{27-3}", 27, 3);
        testMinMaxGapCurlyBrackets("{100000-200000}", 100000, 200000);
    }

    private void testMinMaxGapCurlyBrackets(String expression, int first, int second) throws ParseException {
        ParseTree result = PARSER.parse(expression);
        assertEquals(ParseTreeType.SEQUENCE, result.getParseTreeType());
        assertEquals(1, result.getNumChildren());

        ParseTree gapNode = result.getChild(0);
        assertEquals(ParseTreeType.REPEAT_MIN_TO_MAX, gapNode.getParseTreeType());
        assertEquals(3, gapNode.getNumChildren());

        ParseTree firstNum = gapNode.getChild(0);
        assertEquals(ParseTreeType.INTEGER, firstNum.getParseTreeType());
        assertEquals(first, firstNum.getIntValue());

        ParseTree secondNum = gapNode.getChild(1);
        assertEquals(ParseTreeType.INTEGER, secondNum.getParseTreeType());
        assertEquals(second, secondNum.getIntValue());

        ParseTree nodeToRepeat = gapNode.getChild(2);
        assertEquals(ParseTreeType.ANY, nodeToRepeat.getParseTreeType());
    }

    @Test(expected=ParseException.class)
    public void testMissingCloseAfterMany() throws Exception {
        PARSER.parse("{10-*");
    }

    @Test(expected=ParseException.class)
    public void testWhitespaceNotAllowedInMinToMany() throws Exception {
        PARSER.parse("{10 - *}");
    }

    @Test
    public void testMinToManyGaps() throws Exception {
        testMinToManyGaps("{32-*}", 32);
        testMinToManyGaps("{0-*}", 0);
        testMinToManyGaps("{250000-*}", 250000);
    }

    private void testMinToManyGaps(String expression, int numToRepeat) throws ParseException {
        ParseTree result = PARSER.parse(expression);
        assertEquals(ParseTreeType.SEQUENCE, result.getParseTreeType());
        assertEquals(1, result.getNumChildren());

        ParseTree gapNode = result.getChild(0);
        assertEquals(ParseTreeType.REPEAT_MIN_TO_MANY, gapNode.getParseTreeType());
        assertEquals(1, gapNode.getNumChildren());

        ParseTree repeatNum = gapNode.getChild(0);
        assertEquals(ParseTreeType.INTEGER, repeatNum.getParseTreeType());
        assertEquals(numToRepeat, repeatNum.getIntValue());
    }

    @Test(expected = ParseException.class)
    public void testIncorrectCharInSet() throws ParseException {
        PARSER.parse("[0102 03-04 % FF]");
    }


    @Test(expected = ParseException.class)
    public void testEmptySet() throws ParseException {
        PARSER.parse("[ ]");
    }

    @Test(expected = ParseException.class)
    public void testNoFirstRangeValue() throws ParseException {
        PARSER.parse("[:04]");
    }

    @Test(expected = ParseException.class)
    public void testFirstStringRangeValueTooBig() throws ParseException {
        PARSER.parse("['aa':'z'");
    }


    @Test(expected = ParseException.class)
    public void testSecondStringRangeValueTooBig() throws ParseException {
        PARSER.parse("['a':'zz'");
    }

    @Test(expected = ParseException.class)
    public void testFirstCharRangeValueTooBig() throws ParseException {
        PARSER.parse("['" + (char) 256 + "':'z'");
    }

    @Test(expected = ParseException.class)
    public void testSecondCharRangeValueTooBig() throws ParseException {
        PARSER.parse("['a':" + (char) 256 + ']');
    }

    @Test(expected = ParseException.class)
    public void testFirstRangeValueNotByteOrString() throws ParseException {
        PARSER.parse("[&01:FF]");
    }

    @Test(expected = ParseException.class)
    public void testSecondRangeValueNotByteOrString() throws ParseException {
        PARSER.parse("[01:&01]");
    }

    @Test
    public void testParsesAllPRONOMSignaturesWithoutError() throws IOException {
        testParseSignaturesWithoutError("/allPRONOMByteSequenceValues.txt");
    }

    @Test
    public void testParsesAllContainerSignaturesWithoutError() throws IOException {
        testParseSignaturesWithoutError("/allContainerSequenceValues.txt");
    }

    private void testParseSignaturesWithoutError(String filename) throws IOException {
        File file = getFile(filename);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String sequence = "";
        String failureMessages = "FAILURE: the following signatures could not be parsed from file: " + filename + "\n";
        boolean failed = false;
        while ((sequence = reader.readLine()) != null) {
            try {
                PARSER.parse(sequence);
            } catch (ParseException e) {
                failed = true;
                failureMessages += " > " + e.getMessage() + "\n";
            }
        }
        assertFalse(failureMessages, failed);
    }



    private File getFile(String resourceName) {
        return new File(getClass().getResource(resourceName).getPath());
    }

    private ParseTree testHeader(String expression) throws ParseException {
        ParseTree result = PARSER.parse(expression);
        assertEquals(ParseTreeType.SEQUENCE, result.getParseTreeType());
        assertEquals(1, result.getNumChildren());
        return result.getChild(0);
    }


}