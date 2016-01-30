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