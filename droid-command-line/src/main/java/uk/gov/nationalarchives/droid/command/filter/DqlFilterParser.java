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
package uk.gov.nationalarchives.droid.command.filter;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.gov.nationalarchives.droid.core.interfaces.filter.FilterCriterion;
import uk.gov.nationalarchives.droid.core.interfaces.filter.StringListParser;

/**
 * @author mpalmer
 * Parses a command line filter condition, throwing exceptions for various invalid filter patterns.
 */
public class DqlFilterParser {

    private static final DqlFilterParser PARSER = new DqlFilterParser();

    private static final Pattern FIELD_PATTERN = Pattern.compile("[a-zA-Z_]+");
    private static final Pattern OPERATOR_PATTERN =
            Pattern.compile("( *)(<(?![=>])|<=|<>|=|>(?!=)|>=|starts|ends|contains|not starts|not ends|not contains|any|none)( *)",
                    Pattern.CASE_INSENSITIVE);
    private static final String SINGLE_QUOTE = "'";
    private static final String ANY_OPERATOR = "ANY";
    private static final String NONE_OPERATOR = "NONE";
    private static final String COULD_NOT_FIND_A_VALUE_FOR_THE_FILTER_CRITERIA = "Could not find a value for the filter criteria %s in %s";
    private static final String COULD_NOT_FIND_A_VALID_OPERATOR_IN_THE_FILTER_CRITERIA = "Could not find a valid operator in the filter criteria: ";
    private static final String COULD_NOT_FIND_A_FIELD_IN_THE_FILTER_CRITERIA = "Could not find a field in the filter criteria: ";

    /**
     * Creates a FilterCriterion given a DROID query language string: field operator value(s)
     * @param dql A DROID query language filter string
     * @return A FilterCriterion for the DQL string passed in.
     */
    public static FilterCriterion parseDql(String dql) {
        return PARSER.parse(dql);
    }

    /**
     * Parse a filter criterion string to split into the field, operator and value and returns a FilterCriterion.
     * Could be done more simply with a single regular expression, but would not let us throw
     * such useful parse error messages by doing it all in one.  We can identify which bit is
     * a problem by breaking it down into three matches, which gives better feedback to users.
     * <p>
     * Parsing is tolerant of additional whitespace where it can be, and will remove any
     * enclosing single quotes on the value component.  Single quotes are useful to define
     * string values if you have leading or trailing whitespace as part of the value.
     *
     * @param dql The filter criteria string to parse.
     * @return A FilterCriterion from parsing the string.
     * @throws DqlParseException if there was a problem parsing the string.
     */
    public FilterCriterion parse(String dql) {
        final String dqlToParse = dql.strip();

        final String fieldName;
        Matcher fieldMatcher = FIELD_PATTERN.matcher(dqlToParse);
        if (fieldMatcher.find()) {
            fieldName = fieldMatcher.group();
        } else {
            throw new DqlParseException(COULD_NOT_FIND_A_FIELD_IN_THE_FILTER_CRITERIA + dql);
        }

        final String operator;
        String remainingDql = dqlToParse.substring(fieldMatcher.end());
        Matcher operatorMatcher = OPERATOR_PATTERN.matcher(remainingDql);
        if (operatorMatcher.find()) {
            operator = operatorMatcher.group(2); // ignore any whitespace in the first or last group:
        } else {
            throw new DqlParseException(COULD_NOT_FIND_A_VALID_OPERATOR_IN_THE_FILTER_CRITERIA + dql);
        }

        final String value = remainingDql.substring(operatorMatcher.end());

        return createFilterCriterion(dql, fieldName, operator, value);
    }

    /**
     * Creates a filter criterion for a field, operator and value.
     * @param dql The full string to parse (in case of error to produce good error messages)
     * @param fieldName The field name
     * @param operator The operator
     * @param value The value
     * @return A FilterCriterion for the field, operator and value.
     * @throws DqlParseException if there's a problem creating a filter criterion from those values.
     */
    private FilterCriterion createFilterCriterion(String dql, String fieldName, String operator, String value) {
        try {
            return isListOperator(operator)
                    ? DqlCriterionFactory.newCriterion(fieldName, operator, parseListValues(dql, value))
                    : DqlCriterionFactory.newCriterion(fieldName, operator, parseSingleValue(dql, value));
        } catch (IllegalArgumentException ex) {
            throw new DqlParseException(ex);
        }
    }

    /**
     * Parses a single value, stripping any single quotes that surround it.
     * @param dql The full string to parse.
     * @param value The value to parse.
     * @return A single value, stripping any single surrounding quotes.
     * @throws DqlParseException if there is no value.
     */
    private String parseSingleValue(String dql, String value) {
        String result = value;
        if (result.startsWith(SINGLE_QUOTE) && result.endsWith(SINGLE_QUOTE)) {
            result = result.substring(1, result.length() - 1);
        }
        if (result.isEmpty()) {
            throw new DqlParseException(COULD_NOT_FIND_A_VALUE_FOR_THE_FILTER_CRITERIA + dql);
        }
        return result;
    }

    /**
     * Parses a list of values in a string which are space separated, unless they are enclosed in single quotes.
     * For example:  one two three four five gives the list [one, two, three, four, five] with 5 members.
     *               'one two' three 'four five' gives the list [one two, three, four five] with 3 members.
     * @param dql The full string to parse.
     * @param value The value to parse containing a space separated list of (possibly single quoted) entries.
     * @return A list of strings
     * @throws DqlParseException if the list of values is empty.
     */
    private List<String> parseListValues(String dql, String value) {
        List<String> values = StringListParser.STRING_LIST_PARSER.parseListValues(value);
        if (values.isEmpty()) {
            throw new DqlParseException(String.format(COULD_NOT_FIND_A_VALUE_FOR_THE_FILTER_CRITERIA, value, dql));
        }
        return values;
    }

    private boolean isListOperator(String operator) {
        return ANY_OPERATOR.equalsIgnoreCase(operator) || NONE_OPERATOR.equalsIgnoreCase(operator);
    }

}
