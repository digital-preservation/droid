package uk.gov.nationalarchives.droid.core.signature.compiler;

import net.byteseek.compiler.CompileException;
import net.byteseek.compiler.matcher.SequenceMatcherCompiler;
import net.byteseek.matcher.bytes.AllBitmaskMatcher;
import net.byteseek.matcher.bytes.AnyByteMatcher;
import net.byteseek.matcher.bytes.ByteMatcher;
import net.byteseek.matcher.bytes.ByteRangeMatcher;
import net.byteseek.matcher.bytes.InvertedByteMatcher;
import net.byteseek.matcher.bytes.OneByteMatcher;
import net.byteseek.matcher.sequence.ByteSequenceMatcher;
import net.byteseek.matcher.sequence.SequenceMatcher;
import net.byteseek.utils.ByteUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import uk.gov.nationalarchives.droid.core.signature.droid6.ByteSequence;
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
        return Arrays.asList(new Object[] {BOFOffset, EOFOffset, VariableOffset});
    }

    private ByteSequenceAnchor compileType;

    public ByteSequenceCompilerTest(ByteSequenceAnchor anchor) {
        compileType = anchor;
    }

    /*******************************************************************************************************************
     * Blank input tests
     */

    @Test(expected = IllegalArgumentException.class)
    public void testNullSequence() throws Exception {
        COMPILER.compile(null);
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
        ByteMatcher bm   = assertSingleByteMatcher(sub.getAnchorMatcher(), AnyByteMatcher.class);
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

    @Test
    public void testComplexSet() {
        fail("TODO");

    }

    @Test
    public void testInvertedComplexSet() {
        fail("TODO");

    }

    @Test
    public void compileOneSubSequence() throws Exception {

        // Simple sequences with only byte values.
        testCompile("01", "01", 0, 0);
        testCompile("02", "02", 0, 0);
        testCompile("01 02 03 04 05", "0102030405", 0, 0);

        // Strings only:
        testCompile("'A'", "'A'", 0, 0);
        testCompile("'A longer bit of text'", "'A longer bit of text'", 0, 0);

        // Byte values and strings
        testCompile("01 'A stringy thing' 02", "01'A stringy thing'02", 0, 0);

        // Byte values with alternatives
        testCompile("01 02 (01|02) 03 04 05", "030405", 2, 0);
        testCompile("01 02 03 (04|05) 04 05", "010203", 0, 2);

        // ?? any byte matching
        testCompile("01 ?? 02", "01", 0, 1);

        // repeat
        testCompile("01 02 {4} 05", "0102", 0, 1);

        testCompile("{9} 01 02 03 04", "01020304", 0, 0);

        // repeat min to max
        testCompile("01 02 {4-12} 'a string'", "'a string'", 1, 0);
    }

    @Test
    public void compileTwoSubSequences() throws Exception {
        fail("TODO");

    }

    @Test
    public void compileMultipleSubSequences() {
        fail("TODO");

    }

    @Test(expected = CompileException.class)
    public void testPRONOMNoAnchorException() throws CompileException {
        testCompile(PRONOM, "??", "??", 0, 0);
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
        testCompile(DROID, "01 02 03 04 [&01]", "01 02 03 04", 0, 1);
        testCompile(DROID, "?? 01 02 03 04", "01 02 03 04", 1, 0);
    }

    @Test
    public void testCompilesAllPRONOMSignaturesWithoutExceptions() throws IOException {
        testCompilesSignaturesWithoutError("/allPRONOMByteSequenceValues.txt");
    }

    @Test
    public void testCompilesAllContainerSignaturesWithoutExceptions() throws IOException {
        testCompilesSignaturesWithoutError("/allContainerSequenceValues.txt");
    }

    private void testCompilesSignaturesWithoutError(String filename) throws IOException {
        File file = getFile(filename);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String sequence = "";
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
        ByteSequence sequence = COMPILER.compile(expression, compileType, type);
        List<SubSequence> subs = sequence.getSubSequences();

        // Contains one subsequence
        assertEquals(1, subs.size());
        SubSequence seq = subs.get(0);

       // Check anchor is correct:
        SequenceMatcher anchor = BYTESEEKCOMPILER.compile(anchorValue);
        SequenceMatcher matcher = seq.getAnchorMatcher();
        assertMatchersEqual(expression, anchor, matcher);

        assertEquals(numLeft, seq.getLeftFragments().size());
        assertEquals(numRight, seq.getRightFragments().size());
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
            while ((randChar = (char) rand.nextInt(128)) == '\'') {}
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

    private File getFile(String resourceName) {
        return new File(getClass().getResource(resourceName).getPath());
    }

}