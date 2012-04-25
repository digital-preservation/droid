/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */


package uk.gov.nationalarchives.droid.core.signature.droid6;

/**
 * A utility class to transform DROID 4 expression fragments
 * into net.domesdaybook regular expressions. 
 *
 * <p/>The only syntactic different between a DROID 4 fragment and
 * net.domesdaybook expressions are in the definitions of sets.
 *
 * <p/>DROID 4 uses:
 *  * a ! to indicate an inverted set, whereas net.domesdaybook uses the more standard ^
 *  * a : to indicate a range of values, whereas net.domesdaybook uses the more standard -
 * 
 * <p/>net.domesdaybook regular expressions permit case sensitive
 * strings delimited by single quotes ('), and case insensitive
 * strings delimited by back-ticks (`).  In case these are passed
 * in to the rewriter, it will ignore text between these delimiters.
 *  
 * @author Matt Palmer
 */
public final class FragmentRewriter {

    /**
     * Inverted sets use ! in the droid syntax.
     */
    private static final char INVERTED_OLD = '!';
    /**
     * Inverted sets use ^ in the net.domesdaybook syntax.
     */
    private static final char INVERTED_NEW = '^';
    /**
     * Set ranges use : in the droid syntax.
     */
    private static final char RANGE_OLD = ':';
    /**
     * Set ranges use - in the net.domesbook syntax.
     */
    private static final char RANGE_NEW = '-';
    /**
     * Defines a case sensitive string delimiter.
     */
    private static final char QUOTE = '\'';
    /**
     * Defines a case insensitive string delimiter.
     */
    private static final char BACKTICK = '`';
    /**
     * Square brackets open a set definition.
     */
    private static final char OPENSET = '[';
    /**
     * Square brackets close a set definition.
     */
    private static final char CLOSESET = ']';

    /**
     * Private constructor - this is a static utility class.
     */
    private FragmentRewriter() {
    }

    /**
     * 
     * @param fragment The DROID 4 syntax fragment to rewrite.
     * @return A fragment compatible with net.domesdaybook regular expressions.
     */
    //CHECKSTYLE:OFF - cyclomatic complexity is too high.
    public static String rewriteFragment(final String fragment) {
    //CHECKSTYLE:ON
        StringBuilder builder = new StringBuilder();
        final int length = fragment.length();
        boolean inCaseSensitiveString = false;
        boolean inCaseInsensitiveString = false;
        int inSet = 0;
        for (int charIndex = 0; charIndex < length; charIndex++) {
            char theChar = fragment.charAt(charIndex);

            // substitute characters if needed, or just add them:
            if (inSet > 0 && !inCaseSensitiveString && !inCaseInsensitiveString) {
                if (theChar == INVERTED_OLD) {
                    builder.append(INVERTED_NEW);
                } else if (theChar == RANGE_OLD) {
                    builder.append(RANGE_NEW);
                } else {
                    builder.append(theChar);
                }
            } else {
                builder.append(theChar);
            }

            // Determine if we are in sets or strings
            if (theChar == QUOTE && !inCaseInsensitiveString) {
                inCaseSensitiveString = !inCaseSensitiveString;
            } else if (theChar == BACKTICK && !inCaseSensitiveString) {
                inCaseInsensitiveString = !inCaseInsensitiveString;
            } else if (!inCaseSensitiveString && !inCaseInsensitiveString) {
                if (theChar == OPENSET) {
                    inSet++;
                } else if (theChar == CLOSESET) {
                    inSet--;
                }
            }
        }
        
        return builder.toString();
    }

}
