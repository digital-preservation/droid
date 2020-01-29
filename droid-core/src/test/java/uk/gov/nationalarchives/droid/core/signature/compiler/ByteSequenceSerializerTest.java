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
    public void toXML() throws Exception {
        toXML("{40-1024} 10 00 00 00 'Word.Document.8' 00");
    }

    private void toXML(String expression) throws CompileException {
       toXML(expression, ByteSequenceAnchor.BOFOffset, DROID, BINARY);
       toXML(expression, ByteSequenceAnchor.BOFOffset, DROID, CONTAINER);
       toXML(expression, ByteSequenceAnchor.BOFOffset, PRONOM, BINARY);
       toXML(expression, ByteSequenceAnchor.BOFOffset, PRONOM, CONTAINER);
    }

    private void toXML(String expression, ByteSequenceAnchor anchor, ByteSequenceCompiler.CompileType compileType,
                       SignatureType sigType) throws CompileException {
        String xml = ByteSequenceSerializer.SERIALIZER.toXML(expression, anchor, compileType, sigType);
        System.out.println(xml);
    }

    @Test
    public void testBinaryDroidCompileSetsToAlternatives() throws Exception {
        testRenderExpressions("[01 'abc']");
        testRenderExpressions("02{2}[01:1C][01:1F]{2}[00:03]([41:5A]|[61:7A]){10}(43|4c|4e)");
    }

    private void testRenderExpressions(String expression) throws Exception {
        ByteSequence seq = ByteSequenceCompiler.COMPILER.compile(expression, ByteSequenceAnchor.BOFOffset, DROID);
        String regex = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(seq, BINARY, false);
        String regex2 = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(seq, CONTAINER, false);
        System.out.println(expression + "\t" + regex + "\t"+ regex2);
    }

    @Test
    public void toPRONOMRegex() throws Exception {
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
                } catch (CompileException e) {
                    failureMessages += e.getMessage();
                    failed = true;
                } catch (ParseException e) {
                    failureMessages += e.getMessage();
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