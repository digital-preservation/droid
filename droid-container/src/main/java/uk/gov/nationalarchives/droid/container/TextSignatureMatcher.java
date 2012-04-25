/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.container;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author rflitcroft
 *
 */
public final class TextSignatureMatcher {

    private TextSignatureMatcher() { }
    
    /**
     * Matches a String against a reglular expression.
     * @param controlRegExp the regular expression
     * @param actual the string to match
     * @return true if the string matched, false otherwise
     */
    public static boolean matches(String controlRegExp, String actual) {
        Pattern p = Pattern.compile(controlRegExp);
        Matcher m = p.matcher(actual);
        return m.matches();
    }
}
