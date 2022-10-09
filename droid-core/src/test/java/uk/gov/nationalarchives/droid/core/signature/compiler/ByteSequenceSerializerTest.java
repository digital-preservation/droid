/*
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
import net.byteseek.parser.ParseException;
import org.junit.Test;
import uk.gov.nationalarchives.droid.core.signature.droid6.ByteSequence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import static org.junit.Assert.*;
import static uk.gov.nationalarchives.droid.core.signature.compiler.ByteSequenceCompiler.CompileType.DROID;
import static uk.gov.nationalarchives.droid.core.signature.compiler.ByteSequenceCompiler.CompileType.PRONOM;
import static uk.gov.nationalarchives.droid.core.signature.compiler.SignatureType.BINARY;
import static uk.gov.nationalarchives.droid.core.signature.compiler.SignatureType.CONTAINER;

public class ByteSequenceSerializerTest {

    @Test
    public void testSerializeToXML() {
        serializeWithoutException("{40-1024} 10 00 00 00 'Word.Document.8' 00");
        serializeWithoutException("AABBCC{4}??20??30??50*BBCC[!AA]");
        serializeWithoutException("'AA''BB'AABB'BB''AA'");
        serializeWithoutException("[!AABBCC]DDEE*30*AABBCC'end'??");
        serializeWithoutException("[!AA:CC]DDEE*30{5-*}AABBCC'end'??");
    }

    private void serializeWithoutException(String expression) {
        serializeWithoutException(expression, ByteSequenceAnchor.BOFOffset, DROID, BINARY);
        serializeWithoutException(expression, ByteSequenceAnchor.BOFOffset, DROID, CONTAINER);
        serializeWithoutException(expression, ByteSequenceAnchor.BOFOffset, PRONOM, BINARY);
        serializeWithoutException(expression, ByteSequenceAnchor.BOFOffset, PRONOM, CONTAINER);
        serializeWithoutException(expression, ByteSequenceAnchor.EOFOffset, DROID, BINARY);
        serializeWithoutException(expression, ByteSequenceAnchor.EOFOffset, DROID, CONTAINER);
        serializeWithoutException(expression, ByteSequenceAnchor.EOFOffset, PRONOM, BINARY);
        serializeWithoutException(expression, ByteSequenceAnchor.EOFOffset, PRONOM, CONTAINER);
        serializeWithoutException(expression, ByteSequenceAnchor.VariableOffset, DROID, BINARY);
        serializeWithoutException(expression, ByteSequenceAnchor.VariableOffset, DROID, CONTAINER);
        serializeWithoutException(expression, ByteSequenceAnchor.VariableOffset, PRONOM, BINARY);
        serializeWithoutException(expression, ByteSequenceAnchor.VariableOffset, PRONOM, CONTAINER);
    }

    private void serializeWithoutException(String expression, ByteSequenceAnchor anchor, ByteSequenceCompiler.CompileType compileType,
                                           SignatureType sigType) {
        try {
            ByteSequenceSerializer.SERIALIZER.toXML(expression, anchor, compileType, sigType);
        } catch (Exception ex) {
            fail("Exception caught compiling expression: " + expression);
        }
    }

    @Test
    public void testBinaryDroidCompileSetsToAlternatives() throws Exception {
        testRenderExpressionsWithoutExceptions("[01 'abc']");
        testRenderExpressionsWithoutExceptions("02{2}[01:1C][01:1F]{2}[00:03]([41:5A]|[61:7A]){10}(43|4c|4e)");
    }

    @Test
    public void testByte() throws Exception {
        String input = "FF";
        String expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, false);
        assertEquals(input, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, true);
        assertEquals(input, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, false);
        assertEquals(input, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, true);
        assertEquals(input, expression);

        input = "FFAA";
        String spaced = "FF AA";
        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, false);
        assertEquals(input, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, true);
        assertEquals(spaced, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, false);
        assertEquals(input, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, true);
        assertEquals(spaced, expression);
    }

    @Test
    public void testInvertedByte() throws Exception {
        String input = "[!FF]";
        String expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, false);
        assertEquals(input, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, false);
        assertEquals(input, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, true);
        assertEquals(input, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, true);
        assertEquals(input, expression);

        input = "[!FF][!AA]";
        String spaced = "[!FF] [!AA]";
        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, false);
        assertEquals(input, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, false);
        assertEquals(input, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, true);
        assertEquals(spaced, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, true);
        assertEquals(spaced, expression);
    }

    @Test
    public void testRange() throws Exception {
        String input = "[01:FE]";
        String expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, false);
        assertEquals(input, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, false);
        assertEquals(input, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, true);
        assertEquals(input, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, true);
        assertEquals(input, expression);

        input = "[01:FE][02:FD]";
        String spaced = "[01:FE] [02:FD]";
        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, false);
        assertEquals(input, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, false);
        assertEquals(input, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, true);
        assertEquals(spaced, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, true);
        assertEquals(spaced, expression);
    }

    @Test
    public void testInvertedRange() throws Exception {
        String input = "[!01:FE]";
        String expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, false);
        assertEquals(input, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, false);
        assertEquals(input, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, true);
        assertEquals(input, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, true);
        assertEquals(input, expression);

        input = "[!01:FE][!02:FD]";
        String spaced = "[!01:FE] [!02:FD]";
        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, false);
        assertEquals(input, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, false);
        assertEquals(input, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, true);
        assertEquals(spaced, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, true);
        assertEquals(spaced, expression);
    }

    @Test
    public void testString() throws Exception {
        String input = "'ABC'";
        String expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, false);
        assertEquals("414243", expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, false);
        assertEquals(input, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, true);
        assertEquals("41 42 43", expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, true);
        assertEquals(input, expression);

        input = "'ABC''DEF'";
        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, false);
        assertEquals("414243444546", expression);

        String singleString = "'ABCDEF'";
        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, false);
        assertEquals(singleString, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, true);
        assertEquals("41 42 43 44 45 46", expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, true);
        assertEquals(singleString, expression);
    }

    @Test
    public void testSet() throws Exception {
        String input = "(AA|BB|CC)";
        String expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, false);
        assertEquals(input, expression);

        String expected = "[AABBCC]";
        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, false);
        assertEquals(expected, expression);

        expected = "(AA | BB | CC)";
        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, true);
        assertEquals(expected, expression);

        expected = "[AA BB CC]";
        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, true);
        assertEquals(expected, expression);

        input = "(AA|BB|CC)(DD|EE|FF)";
        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, false);
        assertEquals(input, expression);

        expected = "[AABBCC][DDEEFF]";
        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, false);
        assertEquals(expected, expression);

        expected = "(AA | BB | CC) (DD | EE | FF)";
        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, true);
        assertEquals(expected, expression);

        expected = "[AA BB CC] [DD EE FF]";
        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, true);
        assertEquals(expected, expression);
    }

    @Test
    public void testSequence() throws Exception {
        String input = "AABBCCDDEEFF";
        String spaced = "AA BB CC DD EE FF";

        String expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, false);
        assertEquals(input, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, true);
        assertEquals(spaced, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, false);
        assertEquals(input, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, true);
        assertEquals(spaced, expression);
    }

    @Test(expected = CompileException.class)
    public void testAnyOnItsOwn() throws Exception {
        String input = "??";
        String expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, false);
    }

    @Test
    public void testAny() throws Exception {
        String input = "??AA";
        String spaced = "?? AA";
        String expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, false);
        assertEquals(input, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, false);
        assertEquals(input, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, true);
        assertEquals(spaced, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, true);
        assertEquals(spaced, expression);

        input = "????AA";
        String expected = "{2}AA";
        spaced = "{2} AA";

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, false);
        assertEquals(expected, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, false);
        assertEquals(expected, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, true);
        assertEquals(spaced, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, true);
        assertEquals(spaced, expression);

        input = "??????BB";
        expected = "{3}BB";
        spaced = "{3} BB";
        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, false);
        assertEquals(expected, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, false);
        assertEquals(expected, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, true);
        assertEquals(spaced, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, true);
        assertEquals(spaced, expression);

        input = "FF??????AA";
        expected = "FF{3}AA";
        spaced = "FF {3} AA";
        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, false);
        assertEquals(expected, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, false);
        assertEquals(expected, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, true);
        assertEquals(spaced, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, true);
        assertEquals(spaced, expression);

    }

    /**
     * DROID and PRONOM have a syntax edge case - they cannot represent ?? wildcards if they are at the opposite end
     * to which the bytesequence is anchored.  If anchored to the BOF, then any ?? at the end will be discarded.
     * Likewise, if anchored to the EOF, any ?? at the start will be discarded.
     *
     * This is because ?? wildcards are modelled as gaps *between* things.  If there is nothing beyond the last thing
     * away from the anchor, then there is nothing to hold the "gap".  So they are discarded.
     *
     * In practice, this is not a severe limitation, since being able to specify that some additional bytes should
     * match after your signature only rules out those which appear too close to the end (or beginning) of the file
     * being scanned.
     *
     * This tests that compiling expressions and serializing them results in the correct serialization depending
     * on the anchor type.
     *
     * @throws Exception
     */
    @Test
    public void testHangingWildcards() throws Exception {
        // Test hanging wildcards on the end anchored to BOF (should be stripped):
        String input = "AABB????????";
        String expected = "AABB";
        String spaced = "AA BB";

        String expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, ByteSequenceAnchor.BOFOffset, false);
        assertEquals(expected, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, ByteSequenceAnchor.BOFOffset, false);
        assertEquals(expected, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, ByteSequenceAnchor.BOFOffset, true);
        assertEquals(spaced, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, ByteSequenceAnchor.BOFOffset, true);
        assertEquals(spaced, expression);

        // Test hanging wildcards on the end anchored to EOF (should remain):
        input = "AABB{4}";
        expected = "AABB{4}";
        spaced   = "AA BB {4}";

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, ByteSequenceAnchor.EOFOffset, false);
        assertEquals(expected, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, ByteSequenceAnchor.EOFOffset, false);
        assertEquals(expected, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, ByteSequenceAnchor.EOFOffset, true);
        assertEquals(spaced, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, ByteSequenceAnchor.EOFOffset, true);
        assertEquals(spaced, expression);

        // Test hanging wildcards at the start anchored to BOF (should remain):
        input = "????????AABB";
        expected = "{4}AABB";
        spaced = "{4} AA BB";
        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, ByteSequenceAnchor.BOFOffset, false);
        assertEquals(expected, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, ByteSequenceAnchor.BOFOffset, false);
        assertEquals(expected, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, ByteSequenceAnchor.BOFOffset, true);
        assertEquals(spaced, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, ByteSequenceAnchor.BOFOffset, true);
        assertEquals(spaced, expression);

        // Test hanging wildcards at the start anchored to EOF (should be stripped):
        input = "????????AABB";
        expected = "AABB";
        spaced = "AA BB";

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, ByteSequenceAnchor.EOFOffset, false);
        assertEquals(expected, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, ByteSequenceAnchor.EOFOffset, false);
        assertEquals(expected, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, ByteSequenceAnchor.EOFOffset, true);
        assertEquals(spaced, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, ByteSequenceAnchor.EOFOffset, true);
        assertEquals(spaced, expression);
    }

    @Test
    public void testAllBitmask() throws Exception {
        String input = "[&DD]";
        String expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, false);
        assertEquals(input, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, false);
        assertEquals(input, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, true);
        assertEquals(input, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, true);
        assertEquals(input, expression);

        input = "[&DD][&AB]";
        String spaced = "[&DD] [&AB]";

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, false);
        assertEquals(input, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, false);
        assertEquals(input, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, true);
        assertEquals(spaced, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, true);
        assertEquals(spaced, expression);
    }

    @Test
    public void testRepeat() throws Exception {
        String input = "AA{4}BB";
        String expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, false);
        assertEquals(input, expression);

        String any = "AA??BB";
        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(any, BINARY, false);
        assertEquals(any, expression);

        String repeatedWild = "AA????????BB";
        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(repeatedWild, BINARY, false);
        assertEquals(input, expression);

        String spaced = "AA {4} BB";
        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, true);
        assertEquals(spaced, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, false);
        assertEquals(input, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, true);
        assertEquals(spaced, expression);

        input = "FF{4}AA{2}CC";
        spaced = "FF {4} AA {2} CC";
        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, false);
        assertEquals(input, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, true);
        assertEquals(spaced, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, false);
        assertEquals(input, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, true);
        assertEquals(spaced, expression);
    }

    @Test
    public void testRepeatMinToMany() throws Exception {
        String input = "AA{4-*}FF";
        String expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, false);
        assertEquals(input, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, false);
        assertEquals(input, expression);

        String spaced = "AA {4-*} FF";
        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, true);
        assertEquals(spaced, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, true);
        assertEquals(spaced, expression);
    }

    @Test
    public void testRepeatMinToMax() throws Exception {
        String input = "AA{4-8}FF";
        String expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, false);
        assertEquals(input, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, false);
        assertEquals(input, expression);

        String spaced = "AA {4-8} FF";
        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, true);
        assertEquals(spaced, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, true);
        assertEquals(spaced, expression);
    }

    @Test
    public void testZeroToMany() throws Exception {
        String input = "AA*BB";
        String spaced = "AA * BB";

        String expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, false);
        assertEquals(input, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, BINARY, true);
        assertEquals(spaced, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, false);
        assertEquals(input, expression);

        expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(input, CONTAINER, true);
        assertEquals(spaced, expression);
    }

    private void testRenderExpressionsWithoutExceptions(String expression) throws Exception {
        ByteSequence seq = ByteSequenceCompiler.COMPILER.compile(expression, ByteSequenceAnchor.BOFOffset, DROID);
        ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(seq, BINARY, false);
        ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(seq, CONTAINER, false);
    }

    @Test
    public void testAllPRONOMSignaturesWithoutException() throws Exception{
        testRenderSignatures("/allPRONOMByteSequenceValues.txt", BINARY);
    }

    @Test
    public void testAllContainerSignaturesWithoutException() throws Exception{
        testRenderSignatures("/allContainerSequenceValues.txt", CONTAINER);
    }

    private void testRenderSignatures(String fileName, SignatureType type) throws Exception {
        File file = getFile(fileName);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String sequence;
        String failureMessages = "FAILURE: the following signatures could not be compiled from file: " + fileName + "\n";
        boolean failed = false;
        while ((sequence = reader.readLine()) != null) {
            if (sequence.trim().length() > 0) { //ignore blank lines in the test input files.
                try {
                    compileExpression(sequence, type);
                } catch (CompileException | ParseException e) {
                    failureMessages += (sequence + " : " + e.getMessage());
                    failed = true;
                }
            }
        }
        assertFalse(failureMessages, failed);
    }

    private void compileExpression(String expression, SignatureType type) throws CompileException, ParseException {
        ByteSequence seq = ByteSequenceCompiler.COMPILER.compile(expression);
        ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(seq, type, false);
    }

    private File getFile(String resourceName) {
        return new File(getClass().getResource(resourceName).getPath());
    }
}