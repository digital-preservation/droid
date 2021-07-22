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
package uk.gov.nationalarchives.droid.core.interfaces.filter;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses a list of values in a string which are space separated, unless they are enclosed in single quotes.
 * For example:  one two three four five gives the list [one, two, three, four, five] with 5 members.
 *               'one two' three 'four five' gives the list [one two, three, four five] with 3 members.
 * Since there is no escaping of quotes, you cannot put a quote inside a quoted string (or it will think the
 * quote "inside" is the closing quote of the string.  You can put quotes inside a space separated string.
 */
public final class StringListParser {

    /**
     * A static instance of the String list parser - there is no state, so there is no need to instantiate new ones.
     */
    public static final StringListParser STRING_LIST_PARSER = new StringListParser();

    private static final char SINGLE_QUOTE_CHAR = '\'';
    private static final char SPACE_CHAR = ' ';

    /**
     * Parses a list of values in a string which are space separated, unless they are enclosed in single quotes.
     * For example:  one two three four five gives the list [one, two, three, four, five] with 5 members.
     *               'one two' three 'four five' gives the list [one two, three, four five] with 3 members.
     * @param stringList The value to parse containing a space separated list of (possibly single quoted) entries.
     * @return A list of strings, or an empty list if no list items were found.
     */
    public List<String> parseListValues(String stringList) {
        List<String> values = new ArrayList<>();
        int charPos = 0;
        while (charPos < stringList.length()) {
            switch (stringList.charAt(charPos)) {

                // ignore spaces, just move on.
                case SPACE_CHAR : {
                    charPos++;
                    break;
                }

                // opening quote - find the closing quote and add an entry.
                case SINGLE_QUOTE_CHAR: {
                    int closingQuoteIndex = stringList.indexOf(SINGLE_QUOTE_CHAR, charPos + 1);
                    if (closingQuoteIndex < 0) {
                        closingQuoteIndex = stringList.length();
                    }
                    String entry = stringList.substring(charPos + 1, closingQuoteIndex);
                    if (!entry.isEmpty()) {
                        values.add(entry);
                    }
                    charPos = closingQuoteIndex + 1;
                    break;
                }

                // an unquoted entry, find the next space or end of string and add an entry.
                default: {
                    int endOfEntry = stringList.indexOf(SPACE_CHAR, charPos + 1);
                    if (endOfEntry < 0) { // if no further spaces, we go to the end of the value.
                        endOfEntry = stringList.length();
                    }
                    String entry = stringList.substring(charPos, endOfEntry);
                    if (!entry.isEmpty()) {
                        values.add(entry);
                    }
                    charPos = endOfEntry + 1;
                    break;
                }
            }
        }
        return values;
    }
}
