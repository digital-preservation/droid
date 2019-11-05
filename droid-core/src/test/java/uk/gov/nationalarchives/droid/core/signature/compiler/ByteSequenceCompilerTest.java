package uk.gov.nationalarchives.droid.core.signature.compiler;

import net.byteseek.compiler.CompileException;
import net.byteseek.compiler.matcher.SequenceMatcherCompiler;
import net.byteseek.matcher.bytes.ByteMatcher;
import net.byteseek.matcher.sequence.SequenceMatcher;
import net.byteseek.utils.ByteUtils;
import org.junit.Test;
import uk.gov.nationalarchives.droid.core.signature.droid6.ByteSequence;
import uk.gov.nationalarchives.droid.core.signature.droid6.SubSequence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class ByteSequenceCompilerTest {

    private static final ByteSequenceCompiler TESTCOMPILER = new ByteSequenceCompiler();
    private static final SequenceMatcherCompiler COMPILER = new SequenceMatcherCompiler();

    @Test
    public void compileOneSubSequence() throws Exception {

        // Simple sequences with only byte values.
        testCompileBOF("01", "01", 0, 0);
        testCompileBOF("02", "02", 0, 0);
        testCompileBOF("01 02 03 04 05", "0102030405", 0, 0);

        // Strings only:
        testCompileBOF("'A'", "'A'", 0, 0);
        testCompileBOF("'A longer bit of text'", "'A longer bit of text'", 0, 0);

        // Byte values and strings
        testCompileBOF("01 'A stringy thing' 02", "01'A stringy thing'02", 0, 0);

        // Byte values with alternatives
        testCompileBOF("01 02 (01|02) 03 04 05", "030405", 2, 0);
        testCompileBOF("01 02 03 (04|05) 04 05", "010203", 0, 2);

        testCompileBOF("01 ?? 02", "01", 0, 1);

        // repeat
        testCompileBOF("01 02 {4} 05", "0102", 0, 1);

        testCompileBOF("{9} 01 02 03 04", "01020304", 0, 0);

        // repeat min to max
        testCompileBOF("01 02 {4-12} 'a string'", "'a string'", 1, 0);
    }

    @Test
    public void testCompilesAllPRONOMSignaturesWithoutError() throws IOException {
        testCompilesSignaturesWithoutError("/allPRONOMByteSequenceValues.txt");
    }

    @Test
    public void testCompilesAllContainerSignaturesWithoutError() throws IOException {
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
                    TESTCOMPILER.compile(sequence);
                } catch (CompileException e) {
                    failed = true;
                    failureMessages += " > " + e.getMessage() + "\t" + sequence + "\n";
                }
            }
        }
        assertFalse(failureMessages, failed);
    }

    private void testCompileBOF(String expression, String anchorValue, int numLeft, int numRight) throws CompileException {
       ByteSequence sequence = compileBOF(expression);
       List<SubSequence> subs = sequence.getSubSequences();

       // Contains one subsequence
       assertEquals(1, subs.size());
       SubSequence seq = subs.get(0);

       // Check anchor is correct:
        SequenceMatcher anchor = COMPILER.compile(anchorValue);
        SequenceMatcher matcher = seq.getAnchorMatcher();
        assertMatchersEqual(anchor, matcher);

        assertEquals(numLeft, seq.getLeftFragments().size());
        assertEquals(numRight, seq.getRightFragments().size());
    }

    private void assertMatchersEqual(SequenceMatcher one, SequenceMatcher two) {
        assertEquals(one.length(), two.length());
        int index = 0;
        for (ByteMatcher t : two) {
            ByteMatcher o = one.getMatcherForPosition(index++);
            assertEquals(o.getNumberOfMatchingBytes(), t.getNumberOfMatchingBytes());
            Set<Byte> obytes = ByteUtils.toSet(o.getMatchingBytes());
            for (Byte tb : t.getMatchingBytes()) {
                assertTrue(obytes.contains(tb));
            }
        }
    }

    private ByteSequence compileBOF(final String droidExpression) throws CompileException {
        return compile(droidExpression, ByteSequenceAnchor.BOFOffset);
    }

    private ByteSequence compileEOF(final String droidExpression) throws CompileException {
        return compile(droidExpression, ByteSequenceAnchor.EOFOffset);
    }

    private ByteSequence compileVar(final String droidExpression) throws CompileException {
        return compile(droidExpression, ByteSequenceAnchor.VariableOffset);
    }

    private ByteSequence compile(String droidExpression, ByteSequenceAnchor anchor) throws CompileException {
        return TESTCOMPILER.compile(droidExpression, anchor, ByteSequenceCompiler.CompileType.DROID);
    }

    private File getFile(String resourceName) {
        return new File(getClass().getResource(resourceName).getPath());
    }


}