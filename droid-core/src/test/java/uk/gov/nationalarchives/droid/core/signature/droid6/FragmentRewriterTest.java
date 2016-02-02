/**
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gsi.gov.uk>
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
package uk.gov.nationalarchives.droid.core.signature.droid6;

import junit.framework.TestCase;
import org.junit.Test;

public class FragmentRewriterTest extends TestCase {



    @Test
    public void testRewriteNoTranslationNeeded() {

        testSame("00 01 02 03 04");
        testSame("01 02 [04 05]");
        testSame("'abcdefg'");
        testSame("`abcdefg`");
        testSame("00 01 02 [03] 'abcdefg' 01");
        testSame("00 01 02 [03] `abcdefg` 01");
        testSame("fffe['abcde']");
    }

    @Test
    public void testRewriteSetRange() {
        testDifferent("[00:7f]", "[00-7f]");
        testDifferent("00 01 [20:ff]", "00 01 [20-ff]");
    }

    @Test
    public void testRewriteInvertedSet() {
        testDifferent("[!00]", "^[00]");
        testDifferent("00 02 5c [! 00 01 02]", "00 02 5c ^[ 00 01 02]");
    }

    @Test
    public void testCombinedStringsAndSets() {
        testDifferent("00 [00:5c 'XYZ'] [!00:1f 01] 't[]est'", "00 [00-5c 'XYZ'] ^[00-1f 01] 't[]est'");
        testDifferent("00 [00:5c 'XYZ'] [!00:1f 01] 't[!00]est'", "00 [00-5c 'XYZ'] ^[00-1f 01] 't[!00]est'");


    }

    private void testSame(String expression) {
        assertEquals("Expression not rewritten: " + expression, expression, FragmentRewriter.rewriteFragment(expression));
    }

    private void testDifferent(String expression, String newExpression) {
        assertEquals("Expression " + expression + " to " + newExpression, newExpression, FragmentRewriter.rewriteFragment(expression));
    }

}