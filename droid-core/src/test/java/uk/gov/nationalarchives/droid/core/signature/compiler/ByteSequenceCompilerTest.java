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
import net.byteseek.matcher.bytes.AllBitmaskMatcher;
import net.byteseek.matcher.bytes.AnyByteMatcher;
import net.byteseek.matcher.bytes.ByteMatcher;
import net.byteseek.matcher.bytes.ByteRangeMatcher;
import net.byteseek.matcher.bytes.InvertedByteMatcher;
import net.byteseek.matcher.bytes.OneByteMatcher;
import net.byteseek.matcher.bytes.SetBinarySearchMatcher;
import net.byteseek.matcher.bytes.TwoByteMatcher;
import net.byteseek.matcher.sequence.ByteSequenceMatcher;
import net.byteseek.matcher.sequence.SequenceMatcher;
import net.byteseek.utils.ByteUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import uk.gov.nationalarchives.droid.core.signature.droid6.ByteSequence;
import uk.gov.nationalarchives.droid.core.signature.droid6.SideFragment;
import uk.gov.nationalarchives.droid.core.signature.droid6.SubSequence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.*;
import static uk.gov.nationalarchives.droid.core.signature.compiler.ByteSequenceAnchor.BOFOffset;
import static uk.gov.nationalarchives.droid.core.signature.compiler.ByteSequenceAnchor.EOFOffset;
import static uk.gov.nationalarchives.droid.core.signature.compiler.ByteSequenceAnchor.VariableOffset;
import static uk.gov.nationalarchives.droid.core.signature.compiler.ByteSequenceCompiler.CompileType.DROID;
import static uk.gov.nationalarchives.droid.core.signature.compiler.ByteSequenceCompiler.CompileType.PRONOM;

@RunWith(Parameterized.class)
public class ByteSequenceCompilerTest {

    private static final int NUM_TESTS = 100;
    private static final int MAX_BYTE  = 256;

    private static final ByteSequenceCompiler COMPILER = new ByteSequenceCompiler();
    private static final SequenceMatcherCompiler BYTESEEKCOMPILER = new SequenceMatcherCompiler();

    @Parameterized.Parameters
    public static Collection params() {
        return Arrays.asList(BOFOffset, EOFOffset, VariableOffset);
    }

    private ByteSequenceAnchor compileType;

    public ByteSequenceCompilerTest(ByteSequenceAnchor anchor) {
        compileType = anchor;
    }

    /*******************************************************************************************************************
     * Bad input tests
     */

    @Test(expected = IllegalArgumentException.class)
    public void testNullSequence() throws Exception {
        COMPILER.compile(null);
    }

    @Test(expected = CompileException.class)
    public void testBadSyntaxSequence() throws Exception {
        COMPILER.compile("01 02 03 this doesn't make any sense");
    }

    @Test
    public void testEmptySequence() throws Exception {
        ByteSequence seq = COMPILER.compile("");
        assertTrue(seq.getNumberOfSubSequences() == 0);
    }

    @Test
    public void testWhitespaceOnlySequence() throws Exception {
        ByteSequence seq = COMPILER.compile("     \t\t  \n   \r    \t");
        assertTrue(seq.getNumberOfSubSequences() == 0);
    }

    /*******************************************************************************************************************
     * Test Reference attribute of compiled ByteSequence and whether it knows which end it's searching at correctly.
     */
    @Test
    public void testBOFReference() throws Exception {
        ByteSequence seq = COMPILER.compile("00", BOFOffset, DROID);
        assertEquals("BOFoffset", seq.getReference());
        assertTrue(seq.isAnchoredToBOF());
        assertFalse(seq.isAnchoredToEOF());

        ByteSequence newSeq = new ByteSequence();
        COMPILER.compile(newSeq, "00", BOFOffset, DROID);
        assertEquals("BOFoffset", newSeq.getReference());
        assertTrue(newSeq.isAnchoredToBOF());
        assertFalse(newSeq.isAnchoredToEOF());
    }

    @Test
    public void testEOFReference() throws Exception {
        ByteSequence seq = COMPILER.compile("00", EOFOffset, DROID);
        assertEquals("EOFoffset", seq.getReference());
        assertFalse(seq.isAnchoredToBOF());
        assertTrue(seq.isAnchoredToEOF());

        ByteSequence newSeq = new ByteSequence();
        COMPILER.compile(newSeq, "00", EOFOffset, DROID);
        assertEquals("EOFoffset", newSeq.getReference());
        assertFalse(newSeq.isAnchoredToBOF());
        assertTrue(newSeq.isAnchoredToEOF());
    }

    @Test
    public void testVariableReference() throws Exception {
        ByteSequence seq = COMPILER.compile("00", ByteSequenceAnchor.VariableOffset, DROID);
        assertEquals("Variable", seq.getReference());
        assertFalse(seq.isAnchoredToBOF());
        assertFalse(seq.isAnchoredToEOF());

        ByteSequence newSeq = new ByteSequence();
        COMPILER.compile(newSeq, "00", ByteSequenceAnchor.VariableOffset, DROID);
        assertEquals("Variable", newSeq.getReference());
        assertFalse(newSeq.isAnchoredToBOF());
        assertFalse(newSeq.isAnchoredToEOF());
    }

    /*******************************************************************************************************************
     * Tests that simple syntactic elements compile correctly.
     */

    @Test
    public void testByte() throws Exception {
        // Test all byte values compile correctly to the right byte values:
        for (int byteValue = 0; byteValue < MAX_BYTE; byteValue++) {
            String expression = String.format("%02x", byteValue);

            ByteMatcher bm = assertSingleByteMatcher(expression, OneByteMatcher.class);
            byte value = bm.getMatchingBytes()[0];
            assertEquals(value & 0xFF, byteValue);
        }
    }

    @Test
    public void testByteSequences() throws Exception {
        for (int testNumber = 0; testNumber < NUM_TESTS; testNumber++) {
            String expression = getRandomByteSequence();
            SequenceMatcher sm = assertSingleSequenceMatcher(expression, ByteSequenceMatcher.class);

            int length = expression.length() / 2;
            assertEquals(length, sm.length());
            for (int index = 0; index < length; index++) {
                int position = index * 2;
                final int value = Integer.parseInt(expression.substring(position, position + 2), 16);
                final byte matchingValue = sm.getMatcherForPosition(index).getMatchingBytes()[0];
                assertEquals(value, matchingValue & 0xFF);
            }
        }
    }

    @Test
    public void testStrings() throws Exception {
        for (int testNumber = 0; testNumber < NUM_TESTS; testNumber++) {
            String expression = getRandomString();
            SequenceMatcher sm = expression.length() == 3 ? // 3 is a single char string, two chars for quotes: 'X'
                    assertSingleSequenceMatcher(expression, OneByteMatcher.class) :
                    assertSingleSequenceMatcher(expression, ByteSequenceMatcher.class);

            int length = expression.length() - 2;
            assertEquals(length, sm.length());
            for (int index = 0; index < length; index++) {
                final int value = expression.charAt(index + 1);
                final byte matchingByte = sm.getMatcherForPosition(index).getMatchingBytes()[0];
                assertEquals(value, matchingByte & 0xFF);
            }
        }
    }

    @Test
    public void testByteSet() throws Exception {
        // Test all byte values compile correctly to the right byte values:
        for (int byteValue = 0; byteValue < MAX_BYTE; byteValue++) {
            String expression = String.format("[%02x]", byteValue);
            ByteMatcher bm   = assertSingleByteMatcher(expression, OneByteMatcher.class);

            byte value = bm.getMatchingBytes()[0];
            assertEquals(byteValue, value & 0xFF);
        }
    }

    @Test
    public void testInvertedByteSet() throws Exception {
        // Test all inverted byte values compile correctly to the right inverted byte values:
        for (int byteValue = 0; byteValue < MAX_BYTE; byteValue++) {
            String expression = String.format("[!%02x]", byteValue);
            ByteMatcher bm   = assertSingleByteMatcher(expression, InvertedByteMatcher.class);
            byte[] values = bm.getMatchingBytes();
            for (byte b : values) {
                assertNotEquals((byte) byteValue, b & 0xFF);
            }
        }
    }

    @Test
    public void testRange() throws Exception {
        Random rand = new Random();
        // Test may ranges compile correctly.
        for (int testNumber = 0; testNumber < NUM_TESTS; testNumber++) {
            int firstRangeNo = rand.nextInt(MAX_BYTE);
            int secondRangeNo = rand.nextInt(MAX_BYTE);

            String expression = String.format("[%02x:%02x]", firstRangeNo, secondRangeNo);
            ByteMatcher bm   = assertSingleByteMatcher(expression, ByteRangeMatcher.class);

            int rangeLength = Math.abs(firstRangeNo - secondRangeNo) + 1;
            assertEquals(rangeLength, bm.getNumberOfMatchingBytes());
        }
    }

    @Test
    public void testInvertedRange() throws Exception {
        Random rand = new Random();
        // Test may ranges compile correctly.
        for (int testNumber = 0; testNumber < NUM_TESTS; testNumber++) {

            int firstRangeNo = rand.nextInt(MAX_BYTE);
            int secondRangeNo = rand.nextInt(MAX_BYTE);

            String expression = String.format("[!%02x:%02x]", firstRangeNo, secondRangeNo);
            ByteMatcher bm   = assertSingleByteMatcher(expression, ByteRangeMatcher.class);

            int rangeLength = Math.abs(firstRangeNo - secondRangeNo) + 1;
            assertEquals(MAX_BYTE - rangeLength, bm.getNumberOfMatchingBytes());
        }
    }

    @Test
    public void testMultiByteSet() throws Exception {
        // Test may ranges compile correctly.
        for (int testNumber = 0; testNumber < NUM_TESTS; testNumber++) {
            Set<Byte> bytes = getRandomBytes();
            String expression = getByteSetExpression(bytes, false);
            ByteMatcher bm   = assertSingleByteMatcher(expression, ByteMatcher.class);

            assertEquals(bytes.size(), bm.getNumberOfMatchingBytes());
            byte[] values = bm.getMatchingBytes();
            for (byte v : values) {
                assertTrue(bytes.contains(v));
            }
        }
    }

    @Test
    public void testInvertedMultiByteSet() throws Exception {
        // Test may ranges compile correctly.
        for (int testNumber = 0; testNumber < NUM_TESTS; testNumber++) {
            Set<Byte> bytes = getRandomBytes();
            String expression = getByteSetExpression(bytes, true);
            ByteMatcher bm   = assertSingleByteMatcher(expression, ByteMatcher.class);

            assertEquals(MAX_BYTE - bytes.size(), bm.getNumberOfMatchingBytes());
            byte[] values = bm.getMatchingBytes();
            for (byte v : values) {
                assertFalse(bytes.contains(v));
            }
        }
    }

    @Test
    public void testStringsInSets() throws Exception {
        for (int testNumber = 0; testNumber < NUM_TESTS; testNumber++) {
            String randomString = getRandomString();
            String expression = '[' + randomString + ']';
            ByteMatcher bm = assertSingleByteMatcher(expression, ByteMatcher.class);

            Set<Byte> bytes = getBytesInString(randomString);
            assertEquals(bytes.size(), bm.getNumberOfMatchingBytes());
            for (byte b : bm.getMatchingBytes()) {
                assertTrue(bytes.contains(b));
            }
        }
    }

    @Test
    public void testInvertedStringsInSets() throws Exception {
        for (int testNumber = 0; testNumber < NUM_TESTS; testNumber++) {
            String randomString = getRandomString();
            String expression = "[!" + randomString + ']';
            ByteMatcher bm = assertSingleByteMatcher(expression, ByteMatcher.class);

            Set<Byte> bytes = getBytesInString(randomString);
            assertEquals(MAX_BYTE - bytes.size(), bm.getNumberOfMatchingBytes());
            for (byte b : bm.getMatchingBytes()) {
                assertFalse(bytes.contains(b));
            }
        }
    }

    @Test
    public void testAny() throws Exception {
        ByteSequence seq = COMPILER.compile("??");
        SubSequence sub  = assertSingleSubSequenceNoFragments(seq);
        assertSingleByteMatcher(sub.getAnchorMatcher(), AnyByteMatcher.class);
    }

    @Test
    public void testBitmask() throws Exception {
        // Test all byte values compile correctly to the right byte values:
        for (int byteValue = 0; byteValue < MAX_BYTE; byteValue++) {
            String expression = String.format("[&%02x]", byteValue);
            ByteMatcher bm   = assertSingleByteMatcher(expression, AllBitmaskMatcher.class);

            int numMatching = ByteUtils.countBytesMatchingAllBits((byte) byteValue);
            assertEquals(numMatching, bm.getNumberOfMatchingBytes());
        }
    }

    @Test
    public void testInvertedBitmask() throws Exception {
        // Test all byte values compile correctly to the right byte values:
        for (int byteValue = 0; byteValue < MAX_BYTE; byteValue++) {
            String expression = String.format("[!&%02x]", byteValue);
            ByteMatcher bm   = assertSingleByteMatcher(expression, AllBitmaskMatcher.class);

            int numMatching = MAX_BYTE - ByteUtils.countBytesMatchingAllBits((byte) byteValue);
            assertEquals(numMatching, bm.getNumberOfMatchingBytes());
        }
    }

    //TODO: make set tests more complex by randomising the elements in them, not just ranges and random bytes.
    @Test
    public void testMultiByteAndRangeSet() throws Exception {
        Random rand = new Random();
        for (int testNumber = 0; testNumber < NUM_TESTS; testNumber++) {
            Set<Byte> bytes = getRandomBytes();

            int firstRangeNo = rand.nextInt(MAX_BYTE);
            int secondRangeNo = rand.nextInt(MAX_BYTE);
            int start = firstRangeNo < secondRangeNo ? firstRangeNo : secondRangeNo;
            int end   = firstRangeNo < secondRangeNo ? secondRangeNo : firstRangeNo;

            String expression = getMultiByteAndRangeSetExpression(bytes, start, end, false);
            ByteMatcher bm   = assertSingleByteMatcher(expression,  ByteMatcher.class);

            Set<Byte> allBytes = new HashSet<>(bytes);
            for (int value = start; value <= end; value++) {
                allBytes.add((byte) value);
            }

            assertEquals(allBytes.size(), bm.getNumberOfMatchingBytes());
            for (byte b : bm.getMatchingBytes()) {
                assertTrue(allBytes.contains(b));
            }
        }
    }

    @Test
    public void testInvertedMultiByteAndRangeSet() throws Exception {
        Random rand = new Random();
        for (int testNumber = 0; testNumber < NUM_TESTS; testNumber++) {
            Set<Byte> bytes = getRandomBytes();

            int firstRangeNo = rand.nextInt(MAX_BYTE);
            int secondRangeNo = rand.nextInt(MAX_BYTE);
            int start = firstRangeNo < secondRangeNo ? firstRangeNo : secondRangeNo;
            int end   = firstRangeNo < secondRangeNo ? secondRangeNo : firstRangeNo;

            String expression = getMultiByteAndRangeSetExpression(bytes, start, end, true);
            ByteMatcher bm   = assertSingleByteMatcher(expression,  ByteMatcher.class);

            Set<Byte> allBytes = new HashSet<>(bytes);
            for (int value = start; value <= end; value++) {
                allBytes.add((byte) value);
            }

            assertEquals(MAX_BYTE - allBytes.size(), bm.getNumberOfMatchingBytes());
            for (byte b : bm.getMatchingBytes()) {
                assertFalse(allBytes.contains(b));
            }
        }
    }

    /*******************************************************************************************************************
     * Test optimisation
     */
    @Test
    public void testSingleByteAlternativesOptimisation() throws Exception {
        // This would normally be a sequence of 01, with an alternative fragment (22|27), but it gets optimised
        // into a set, so we end up with no fragments and a single sequence of [22 27] 01
        SequenceMatcher sm = assertSingleSequenceMatcher("(22|27) 01", SequenceMatcher.class);
        ByteMatcher bm = sm.getMatcherForPosition(0);
        assertTrue(TwoByteMatcher.class.isInstance(bm));

        sm = assertSingleSequenceMatcher("01 (22|27|00) 01", SequenceMatcher.class);
        bm = sm.getMatcherForPosition(1);
        assertTrue(SetBinarySearchMatcher.class.isInstance(bm));

        //TODO: test strings in single byte alternative optimisation.

        //TODO: test other constructs in single byte alternative optimisation.
    }

    /*******************************************************************************************************************
     * Test expressions, and put them into three subsequences to ensure they each compile correctly.
     */

    @Test
    public void testAnchorsAndFragments() throws Exception {

        // Simple sequences with only byte values.
        testCompile("01", "01", 0, 0);
        testCompile("02", "02", 0, 0);
        testCompile("01 02 03 04 05", "0102030405", 0, 0);

        // Strings only:
        testCompile("'A'", "'A'", 0, 0);
        testCompile("'A longer bit of text'", "'A longer bit of text'", 0, 0);

        // Byte values and strings
        testCompile("01 'A stringy thing' 02", "01'A stringy thing'02", 0, 0);

        // Byte values with single byte alternatives
        testCompile("01 02 (01|02) 03 04 05", "0102 [01 02] 030405", 0, 0);
        testCompile("01 02 03 (04|05) 04 05", "010203 [0405] 04 05", 0, 0);

        // Byte values with alternatives with some single bytes, but not all:
        testCompile("01 02 (01|02|FF FE) 03 04 05", "030405", 2, 0);

        // ?? any byte matching
        testCompile("01 ?? 02", "01", 0, 1);

        // repeat
        testCompile("01 02 {4} 05", "0102", 0, 1);

        // GAP at start
        testCompile("{9} 01 02 03 04", "01020304", 0, 0);

        // repeat min to max
        testCompile("01 02 {4-12} 'a string'", "'a string'", 1, 0);
    }

    /*******************************************************************************************************************
     * Test how anchors change with different compilation strategies:
     */

    @Test(expected = CompileException.class)
    public void testPRONOMNoAnchorException() throws CompileException {
        testCompile(PRONOM, "??", ".", 0, 0);
    }

    @Test
    public void testPRONOMAnchorStrategy() throws CompileException {
        testCompile(PRONOM, "01 02 03 04 [00-16]", "01 02 03 04", 0, 1);
        testCompile(PRONOM, "01 02 [&F0] 03 04 [00-16]", "01 02", 0, 1);
        testCompile(PRONOM, "01 02 [&F0] 03 04 [00-16] (01|02) 03", "01 02", 0, 3);

    }

    @Test
    public void testDROIDAnchorStrategy() throws Exception {
        testCompile(DROID, "01 02 03 04 [00-16]", "01 02 03 04 [00-16]", 0, 0);
        testCompile(DROID, "01 02 [&F0] 03 04 [00-16]", "01 02 [&F0] 03 04 [00-16]", 0, 0);
        testCompile(DROID, "01 02 [&F0] 03 04 [00-16] ?? 03", "01 02 &F0 03 04 [00-16]", 0, 1);
    }

    @Test
    public void testAllowAllAnchorStrategy() throws Exception {
        testCompile(DROID, "[&01]", "[&01]", 0, 0);
        testCompile(DROID, "??", ".", 0, 0);
        testCompile(DROID, "?? [00:F9] ?? [&01]", ". [00-F9] . [&01]", 0, 0);
    }

    /*******************************************************************************************************************
     * Test that all known real-world sequences can be compiled without an exception
     */

    @Test
    public void testCompilesAllPRONOMSignaturesWithoutExceptions() throws IOException {
        testCompilesSignaturesWithoutError("/allPRONOMByteSequenceValues.txt");
    }

    @Test
    public void testCompilesAllContainerSignaturesWithoutExceptions() throws IOException {
        testCompilesSignaturesWithoutError("/allContainerSequenceValues.txt");
    }

    /*******************************************************************************************************************
     * Test gaps and offsets in fragments, subsequences and bytesequences are calculated correctly.
     */
    @Test
    public void testFragmentGapCalculations() throws Exception {
        //TODO: this models the ?? as the sequence ?? 05 instead of 05 with a min offset of 1.
        //testFragmentGaps("01 02 03 ?? 05", 0, 1, 1, 1);

        // Test fixed gaps on the right:
        testFragmentGaps("01 02 03 {5} 05", 0, 1, 5, 5);
        testFragmentGaps("01 02 03 {5} 05 ('one'|'two')", 0, 2, 5, 5, 0, 0);
        testFragmentGaps("01 02 03 {5} 05 {7} ('one'|'two')", 0, 2, 5, 5, 7, 7);

        // Test fixed gaps on the left:
        testFragmentGaps("05 {5} 01 02 03", 1, 0, 5, 5);
        testFragmentGaps("05 ('one'|'two') {5} 01 02 03", 2, 0, 5, 5, 0, 0);
        testFragmentGaps("('one'|'two') {7} 05 {5} 01 02 03", 2, 0, 5, 5, 7, 7);

        // Test double fixed gaps on the right:
        testFragmentGaps("01 02 03 {5} {4} 05", 0, 1, 9, 9);
        testFragmentGaps("01 02 03 {5} {3} 05 ('one'|'two')", 0, 2, 8, 8, 0, 0);
        testFragmentGaps("01 02 03 {5} {2} 05 {7} {3} ('one'|'two')", 0, 2, 7, 7, 10, 10);

        // Test double fixed gaps on the left:
        testFragmentGaps("05 {5} {200} 01 02 03", 1, 0, 205, 205);
        testFragmentGaps("05 ('one'|'two') {5} {1} 01 02 03", 2, 0, 6, 6, 0, 0);
        testFragmentGaps("('one'|'two') {7} {13} 05 {5} {67} 01 02 03", 2, 0, 72, 72, 20, 20);

        // Test gaps from min to max range on right:
        testFragmentGaps("01 02 03 {5-10} 05", 0, 1, 5, 10);
        testFragmentGaps("01 02 03 {5-9} 05 ('one'|'two')", 0, 2, 5, 9, 0, 0);
        testFragmentGaps("01 02 03 {5-9} 05 {7-14} ('one'|'two')", 0, 2, 5, 9, 7, 14);

        // Test double gaps from min to max range on right:
        testFragmentGaps("01 02 03 {100} {5-10} 05", 0, 1, 105, 110);
        testFragmentGaps("01 02 03 {5-9} {60} 05 ('one'|'two')", 0, 2, 65, 69, 0, 0);
        testFragmentGaps("01 02 03 {5-9} {32} 05 {32} {7-14} ('one'|'two')", 0, 2, 37, 41, 39, 46);

        // Test gaps from min to max range on left:
        testFragmentGaps("05 {5-10} 01 02 03", 1, 0, 5, 10);
        testFragmentGaps("05 ('one'|'two') {4-9} 01 02 03", 2, 0, 4, 9, 0, 0);
        testFragmentGaps("('one'|'two') {7-15} 05 {5-6} 01 02 03", 2, 0, 5, 6, 7, 15);

        // Test double gaps from min to max range on left:
        testFragmentGaps("05 {5-10} {55} 01 02 03", 1, 0, 60, 65);
        testFragmentGaps("05 ('one'|'two') {29} {4-9} {21} 01 02 03", 2, 0, 54, 59, 0, 0);
        testFragmentGaps("('one'|'two') {7-15} {100} 05 {5-6} {62} {38} 01 02 03", 2, 0, 105, 106, 107, 115);
    }

    private void testFragmentGaps(String expression, int leftFragments, int rightFragments, int... minMaxGaps) throws Exception {
        ByteSequence seq = COMPILER.compile(expression, compileType);
        SubSequence sub = assertSingleSubSequenceWithFragments(seq, leftFragments, rightFragments);
        List<List<SideFragment>> fragments = sub.getLeftFragments();
        int fragPos = 0;
        for (int minMaxPos = 0; minMaxPos < minMaxGaps.length; minMaxPos += 2) {
            if (fragPos >= fragments.size()) {
                fragments = sub.getRightFragments();
                fragPos = 0;
            }
            int min = minMaxGaps[minMaxPos];
            int max = minMaxGaps[minMaxPos + 1];
            List<SideFragment> currentFragList = fragments.get(fragPos++);
            SideFragment frag = currentFragList.get(0);
            assertEquals(min, frag.getMinOffset());
            assertEquals(max, frag.getMaxOffset());
        }
    }

    @Test
    public void testStartEndSubSequenceGapCalculations() throws Exception {
        testSubSequenceGaps("{10} 01 02 03 04", 10, 10, 0, 0);
        testSubSequenceGaps("01 02 03 04 {10}", 0, 0, 10, 10);

        testSubSequenceGaps("{10-256} 01 02 03 04", 10, 256, 0, 0);
        testSubSequenceGaps("01 02 03 04 {10-256}", 0, 0, 10, 256);

        testSubSequenceGaps("{10} 01 02 04 05 * {30} A1 A2 A3 A4 * {40} D1 D2 D3", BOFOffset, 10, 10, 30, 30, 40, 40);
        testSubSequenceGaps("{10} 01 02 04 05 * {30} A1 A2 A3 A4 * {40} D1 D2 D3", EOFOffset, 0, 0, 30, 30, 40, 40);

        testSubSequenceGaps("{10-256} 01 02 04 05 * {30} A1 A2 A3 A4 * {40} D1 D2 D3", BOFOffset, 10, 256, 30, 30, 40, 40);
        testSubSequenceGaps("{10-256} 01 02 04 05 * {30} A1 A2 A3 A4 * {40} D1 D2 D3", EOFOffset, 0, 0, 30, 30, 40, 40);

        testSubSequenceGaps("01 02 04 05 * {30} A1 A2 A3 A4 * {40} D1 D2 D3 {10}", BOFOffset, 0, 0, 30, 30, 40, 40);
        testSubSequenceGaps("01 02 04 05 * {30} A1 A2 A3 A4 * {40} D1 D2 D3 {10}", EOFOffset, 10, 10, 30, 30, 40, 40);

        testSubSequenceGaps("01 02 04 05 * {30} A1 A2 A3 A4 * {40} D1 D2 D3 {10-256}", BOFOffset, 0, 0, 30, 30, 40, 40);
        testSubSequenceGaps("01 02 04 05 * {30} A1 A2 A3 A4 * {40} D1 D2 D3 {10-256}", EOFOffset, 10, 256, 30, 30, 40, 40);
    }

    @Test
    public void testSubsequenceGapCalculations() throws Exception {
        // No gaps between subsequences
        testSubSequenceGaps("'no gaps' * 'between these'", 00, 00, 00, 00);

        // Fixed gap between subsequences:
        testSubSequenceGaps("01 02 03 {10} * 04 05 06", 0, 0, 10, 10);
        testSubSequenceGaps("01 02 03 * {10} 04 05 06", 0, 0, 10, 10);
        testSubSequenceGaps("01 02 03 {10} * 04 05 06 {100} * {256} 02 03 04 05", 0, 0, 10, 10, 356, 356);

        // Min Max gaps between subsequences:
        testSubSequenceGaps("01 02 03 {9-14} * 04 05 06", 0, 0, 9, 14);
        testSubSequenceGaps("01 02 03 * {9-14} 04 05 06", 0, 0, 9, 14);
        testSubSequenceGaps("01 02 03 {32-64} * 04 05 06 {100-105} * {256-356} 02 03 04 05", 0, 0, 32, 64, 356, 461);

        // Min Max and fixed gaps between subsequences:
        testSubSequenceGaps("01 02 03 {9-14} * {32} 04 05 06", 0, 0, 41, 46);
        testSubSequenceGaps("01 02 03 {32} * {9-14} 04 05 06", 0, 0, 41, 46);
        testSubSequenceGaps("01 02 03 {32-64} {10} * {20} 04 05 06 {100} * {256-356} 02 03 04 05", 0, 0, 62, 94, 356, 456);
    }


    private void testSubSequenceGaps(String expression, int... minMaxGaps) throws Exception {
        testSubSequenceGaps(expression, compileType, minMaxGaps);
    }

    private void testSubSequenceGaps(String expression, ByteSequenceAnchor anchor, int... minMaxGaps) throws Exception {
        ByteSequence seq = COMPILER.compile(expression, anchor);
        // sequences compiled from EOF calculate gaps differently - they work "backwards" from the last subsequence,
        // rather than forwards from the first subsequence.  This means the gaps are shifted from one subsequence to
        // the following subsequence.
        if (anchor == EOFOffset) {
            int minMaxPos = 2; // start at the second set of gaps (shifted one on) if EOFOffset.
            for (SubSequence sub : seq.getSubSequences()) {
                if (minMaxPos >= minMaxGaps.length) {
                    minMaxPos = 0;
                }
                int min = minMaxGaps[minMaxPos++];
                int max = minMaxGaps[minMaxPos++];
                assertEquals(min, sub.getMinSeqOffset());
                assertEquals(max, sub.getMaxSeqOffset());
            }
        } else {
            int minMaxPos = 0;
            for (SubSequence sub : seq.getSubSequences()) {
                int min = minMaxGaps[minMaxPos++];
                int max = minMaxGaps[minMaxPos++];
                assertEquals(min, sub.getMinSeqOffset());
                assertEquals(max, sub.getMaxSeqOffset());
            }
        }
    }

    /*******************************************************************************************************************
     * Private test implementations used by the public tests
     */

    private void testCompilesSignaturesWithoutError(String filename) throws IOException {
        File file = getFile(filename);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String sequence;
        String failureMessages = "FAILURE: the following signatures could not be compiled from file: " + filename + "\n";
        boolean failed = false;
        while ((sequence = reader.readLine()) != null) {
            if (sequence.trim().length() > 0) { //ignore blank lines in the test input files.
                try {
                    COMPILER.compile(sequence);
                } catch (CompileException e) {
                    failed = true;
                    failureMessages += " > " + e.getMessage() + "\t" + sequence + "\n";
                }
            }
        }
        assertFalse(failureMessages, failed);
    }

    private void testCompile(String expression, String anchorValue, int numLeft, int numRight) throws CompileException {
        testCompile(DROID, expression, anchorValue, numLeft, numRight);
    }

    private void testCompile(ByteSequenceCompiler.CompileType type, String expression, String anchorValue, int numLeft, int numRight) throws CompileException {
        String initialExpression = expression;
        for (int numSubsequences = 1; numSubsequences < 10; numSubsequences++) {
            testCompile(type, expression, anchorValue, numLeft, numRight, numSubsequences);
            expression = expression + '*' + initialExpression;
        }
    }

    private void testCompile(ByteSequenceCompiler.CompileType type, String expression, String anchorValue, int numLeft, int numRight, int numSubSequences) throws CompileException {
        ByteSequence sequence = COMPILER.compile(expression, compileType, type);
        List<SubSequence> subs = sequence.getSubSequences();

        assertEquals(numSubSequences, subs.size());
        SequenceMatcher anchor = BYTESEEKCOMPILER.compile(anchorValue);

        for (int i = 0; i < numSubSequences; i++) {
            SubSequence seq = subs.get(i);

            // Check anchor is correct:
            SequenceMatcher matcher = seq.getAnchorMatcher();
            assertMatchersEqual(expression, anchor, matcher);

            assertEquals(numLeft, seq.getLeftFragments().size());
            assertEquals(numRight, seq.getRightFragments().size());
        }
    }

    private ByteMatcher assertSingleByteMatcher(String expression, Class classType) throws Exception {
        ByteSequence seq = COMPILER.compile(expression, compileType);
        SubSequence sub  = assertSingleSubSequenceNoFragments(seq);
        return assertSingleByteMatcher(sub.getAnchorMatcher(), classType);
    }

    private SequenceMatcher assertSingleSequenceMatcher(String expression, Class classType) throws Exception {
        ByteSequence seq = COMPILER.compile(expression, compileType);
        SubSequence sub  = assertSingleSubSequenceNoFragments(seq);
        SequenceMatcher sm = sub.getAnchorMatcher();
        assertTrue(classType.getSimpleName() + " v " + sm.getClass().getSimpleName(), classType.isInstance(sm));
        return sm;
    }

    private SubSequence assertSingleSubSequenceNoFragments(ByteSequence seq) {
        assertEquals(1, seq.getNumberOfSubSequences());
        List<SubSequence> subs = seq.getSubSequences();
        SubSequence sub = subs.get(0);
        assertEquals(0, sub.getLeftFragments().size());
        assertEquals(0, sub.getRightFragments().size());
        return subs.get(0);
    }

    private SubSequence assertSingleSubSequenceWithFragments(ByteSequence seq, int leftFragments, int rightFragments) {
        assertEquals(1, seq.getNumberOfSubSequences());
        List<SubSequence> subs = seq.getSubSequences();
        SubSequence sub = subs.get(0);
        assertEquals(leftFragments, sub.getLeftFragments().size());
        assertEquals(rightFragments, sub.getRightFragments().size());
        return subs.get(0);
    }

    private ByteMatcher assertSingleByteMatcher(SequenceMatcher match, Class classType) {
        assertEquals(1, match.length());
        ByteMatcher bm = match.getMatcherForPosition(0);
        assertTrue(classType.isInstance(bm));
        return bm;
    }

    private void assertMatchersEqual(String expression, SequenceMatcher one, SequenceMatcher two) {
        String description = expression + ' ' + one.toString() + ' ' + two.toString();
        assertEquals(description, one.length(), two.length());
        int index = 0;
        for (ByteMatcher t : two) {
            ByteMatcher o = one.getMatcherForPosition(index++);
            assertEquals(o.getNumberOfMatchingBytes(), t.getNumberOfMatchingBytes());
            Set<Byte> obytes = ByteUtils.toSet(o.getMatchingBytes());
            for (Byte tb : t.getMatchingBytes()) {
                assertTrue(description, obytes.contains(tb));
            }
        }
    }

    /*******************************************************************************************************************
     * Methods to generate test sets and expressions and parse them into data structures for testing.
     */

    private Set<Byte> getRandomBytes() {
        Random rand = new Random();
        int maxBytes = rand.nextInt(128) + 1;
        Set<Byte> bytes = new HashSet<>();
        for (int i = 0; i < maxBytes; i++) {
            bytes.add((byte) rand.nextInt(256));
        }
        return bytes;
    }

    private String getByteSetExpression(Set<Byte> bytes, boolean inverted) {
        StringBuilder builder = new StringBuilder((bytes.size() + 1 ) * 2);
        builder.append('[');
        if (inverted) builder.append('!');
        for (Byte b : bytes) {
            builder.append(String.format("%02x", b & 0xFF));
        }
        builder.append(']');
        return builder.toString();
    }

    private String getMultiByteAndRangeSetExpression(Set<Byte> bytes, int start, int end, boolean inverted) {
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        if (inverted) builder.append('!');
        int numBytes = bytes.size();

        Random rand = new Random();
        int splitSet = rand.nextInt(numBytes);

        Iterator<Byte> iterator = bytes.iterator();
        for (int i = 0; i < splitSet && iterator.hasNext(); i++) {
            builder.append(String.format("%02x", iterator.next() & 0xFF));
        }

        builder.append(String.format("%02x:%02x", start, end));

        while (iterator.hasNext()) {
            builder.append(String.format("%02x", iterator.next() & 0xFF));
        }

        builder.append(']');

        return builder.toString();
    }

    private String getRandomByteSequence() {
        Random rand = new Random();
        int length = rand.nextInt(256) + 2; //MUST have more than one byte to be a sequence.
        StringBuilder builder = new StringBuilder(length * 2);
        for (int i = 0; i < length; i++) {
            builder.append(String.format("%02x", rand.nextInt(256)));
        }
        return builder.toString();
    }

    private String getRandomString() {
        Random rand = new Random();
        int length = rand.nextInt(256) + 1;
        StringBuilder builder = new StringBuilder(length + 2);
        builder.append("'");
        for (int charPos = 0; charPos < length; charPos++) {
            char randChar;
            // Get an ASCII char which isn't the closing string quote '
            // We can't use full ISO-8559-1 charset, as there is a bug in the byteseek compiler 2.0.3.
            randChar = (char) rand.nextInt(128); // limit to ASCII 128 value as highest char.
            if (randChar == '\'') { // can't have a single quote inside a string, make it the next char.
                randChar++;
            }
            builder.append(randChar);
        }
        builder.append("'");
        return builder.toString();
    }

    private Set<Byte> getBytesInString(String expression) {
        Set<Byte> bytes = new HashSet<>();
        for (int i = 1; i < expression.length() - 1; i++) {
            char value = expression.charAt(i);
            bytes.add((byte) value);
        }
        return bytes;
    }

    /*******************************************************************************************************************
     * Helper functions
     */

    private File getFile(String resourceName) {
        return new File(getClass().getResource(resourceName).getPath());
    }

}