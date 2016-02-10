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
package uk.gov.nationalarchives.droid.command.filter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionOperator;
import uk.gov.nationalarchives.droid.core.interfaces.filter.FilterCriterion;

/**
 * @author boreilly
 * Parses a command line filter condition, throwing exceptions for various invalid filter patterns.
 * Uses Java code without relying on Antlr (see AntlrDqlParser) which may have dependency issues with particular 
 * Java versions and environments.
 */
//CHECKSTYLE:OFF  Too complex and some don't agree with all the suggestions.  But layout issues all OK!
public class SimpleDqlFilterParser implements DqlFilterParser {

    private static final String INVALID_ARGUMENT_COUNT = "The filter condition \"%s\" is invalid, since it has only %d arguments - at least 3 are required (are you missing an operator, or a space?)";
    private static final String INVALID_ARGUMENT_COUNT_FOR_STRING_OPERATOR = "The filter condition \"%s\" is invalid, since it has  %d arguments.  String based filter conditions must supply one string value enclosed in single quotes for the final argument";
    private static final String INVALID_USE_OF_NOT = "The filter condition \"%s\" is invalid. The \"not\" operator can only be used with string operators \"starts\", \"ends\" and \"contains\"";
    private static final String MISSING_SINGLE_QUOTES = "The filter condition \"%s\" is invalid. Queries with \"starts\", \"ends\" and \"contains\" must be followed by a  value enclosed in single quotes after the operator.";
    private static final String INVALID_COMPARISON_FILTER = "The filter condition \"%s\" is invalid.  Filters using a comparison operator must supply a single integer numeric operand, or (for the \"=\" operator only) a string surrounded by single quotes. Dates must use the format yyyy-mm-dd, e.g. 2010-01-23 for 23rd Jan 2010";
    private static final String SINGLE_QUOTE = "'";
    private static final int MIMIMUM_FILTER_COMPONENTS = 3;
    private static final int VALUES_START_INDEX_WITH_NOT_OPERATOR = 3;
    private static final int VALUES_START_INDEX_SANS_NOT_OPERATOR = 2;
    
    @Override
    public FilterCriterion parse(String dql) {
        try {
            Pattern p = Pattern.compile("(?<!(\\\\'|\"|'[a-zA-Z0-9\"]{1,1000})) (?!(\\\\|\").*)");
            String[] filterComponents = dql.split(p.pattern());
          
            String dqlOperator = null;
            String value = null;
            int valuesStartIndex = 0;
            FilterCriterion criterion = null;
             
            //We must always have at least 3 components i.e. field, operator and one operand 
            //E.g. "file_name contains 'london'"
            if (filterComponents.length < MIMIMUM_FILTER_COMPONENTS) {
                throw new DqlParseException(String.format(INVALID_ARGUMENT_COUNT , dql, filterComponents.length));
            }

            String dqlField = filterComponents[0];

            // Currently the operator is always a single token, unless preceded by "not" 
            //e.g. in "file_name not contains 'london'", the operator is "not contains"
            boolean operatorIsTwoPart = filterComponents[1].toLowerCase().equals("not") ? true : false;

            if (operatorIsTwoPart) {
                String[] operators = Arrays.copyOfRange(filterComponents, 1, VALUES_START_INDEX_WITH_NOT_OPERATOR);
                dqlOperator = operators[0] + " " + operators[1];
                //dqlOperator =  String.join(" ", operators); -- Only works with Java 8 - we have to support 7!!
                valuesStartIndex = VALUES_START_INDEX_WITH_NOT_OPERATOR;
            } else {
                dqlOperator = filterComponents[1];
                valuesStartIndex = VALUES_START_INDEX_SANS_NOT_OPERATOR;
            }

            if (isStringOperator(dqlOperator)) {
                // Check that we have the right number of arguments, with "starts", "ends" or "contains", we should
                // only have a single value, So there should be 3 arguments, or 4 if preceded by "not"
                if ((operatorIsTwoPart && filterComponents.length != (MIMIMUM_FILTER_COMPONENTS + 1)) || ((!operatorIsTwoPart) && filterComponents.length != MIMIMUM_FILTER_COMPONENTS)) {
                    throw new DqlParseException(String.format(INVALID_ARGUMENT_COUNT_FOR_STRING_OPERATOR,  dql , filterComponents.length));
                }
                // We have the correct number of arguments - check that the value is enclosed in quotes - this is required  
                // with the string operators i.e. "starts", "ends" or "contains"
                value = filterComponents[valuesStartIndex];
                if (!(value.startsWith(SINGLE_QUOTE) && value.endsWith(SINGLE_QUOTE))) {
                    throw new DqlParseException(String.format(MISSING_SINGLE_QUOTES, dql));
                }
                criterion = DqlCriterionFactory.newCriterion(dqlField, dqlOperator, fromDqlString(value));
            } else {
                if (operatorIsTwoPart) {
                    // The "not" prefix is only valid with string operators - so throw an exception if we reach here.
                    throw new DqlParseException(String.format(INVALID_USE_OF_NOT, dql));
                }

                if (isComparisonOperator(dqlOperator)) {
                    // With a comparison operator, we should have only one operand which should be either 
                    // a long integer or a string which can be converted to a Boolean
                    if (filterComponents.length != MIMIMUM_FILTER_COMPONENTS) {
                        throw new DqlParseException(String.format(INVALID_COMPARISON_FILTER, dql));
                    }

                    value = filterComponents[valuesStartIndex];
                    if (!isLongInteger(value) && !isBoolean(value) &&  !isDateValue(value) && (!(isQuotedString(value) && isEqualsOperator(dqlOperator)))) {
                        throw new DqlParseException(String.format(INVALID_COMPARISON_FILTER, dql));
                    }
                    criterion = DqlCriterionFactory.newCriterion(dqlField, dqlOperator, fromDqlString(value));
                } else {
                    //We're using the "any" or "none" operator with a list of values, which may or may not be quoted...
                    Collection<String> dqlValues = new ArrayList<String>();

                    for (int i = valuesStartIndex; i < filterComponents.length; i++) {
                        // Strip the single quotes when using "any" or "none" (previously this would fail if  
                        // quotes were used as these are not handled in the Criterion code).
                        dqlValues.add(fromDqlString(filterComponents[i]));
                    }
                    criterion = DqlCriterionFactory.newCriterion(dqlField, dqlOperator, dqlValues);
                }
            }

            return criterion;
        } catch (IllegalArgumentException e) {
            throw new DqlParseException(e);
        } catch (DqlParseException e) {
            throw (DqlParseException) e;
        }
    }

    private static String fromDqlString(String dqlString) {
        return StringUtils.strip(dqlString, SINGLE_QUOTE).replace("\\'", SINGLE_QUOTE);
    }
    
    private static boolean isStringOperator(String operator) {

        CriterionOperator criterionOperator = DqlCriterionMapper.forOperator(operator);
        if (criterionOperator == CriterionOperator.STARTS_WITH  
                || criterionOperator == CriterionOperator.NOT_STARTS_WITH 
                || criterionOperator == CriterionOperator.ENDS_WITH  
                || criterionOperator == CriterionOperator.NOT_ENDS_WITH 
                || criterionOperator == CriterionOperator.CONTAINS 
                || criterionOperator == CriterionOperator.NOT_CONTAINS) {
            return true;
        }
        return false;
    }
    
    private static boolean isComparisonOperator(String operator) {

        CriterionOperator criterionOperator = DqlCriterionMapper.forOperator(operator);
        if (criterionOperator == CriterionOperator.EQ
                || criterionOperator == CriterionOperator.GT
                || criterionOperator == CriterionOperator.GTE
                || criterionOperator == CriterionOperator.LT
                || criterionOperator == CriterionOperator.LTE
                || criterionOperator == CriterionOperator.NE
        ) {
            return true;
        }
        return false;
    }
    
    private static boolean isEqualsOperator(String operator) {
        CriterionOperator criterionOperator = DqlCriterionMapper.forOperator(operator);
        return criterionOperator  == CriterionOperator.EQ;
    }
    
    private static boolean isQuotedString(String value) {
        return value.startsWith(SINGLE_QUOTE) && value.endsWith(SINGLE_QUOTE);
    }
    
    private static boolean isLongInteger(String str) {  
        try {  
            Long.parseLong(str);  
        } catch (NumberFormatException nfe) {  
            return false;  
        }  
        return true;  
    }
    
    private static boolean isDateValue(String value) {

        Date date = null;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        try {
            date = sdf.parse(value);
        } catch (ParseException e) {

        }

        return (date != null);
    }
    
    private static boolean isBoolean(String value) {  
        try {  
            if (fromDqlString(value).equalsIgnoreCase("true") || fromDqlString(value).equalsIgnoreCase("false")) {
                return true;   
            } else {
                return false;
            }
        } catch (NumberFormatException nfe) {  
            return false;  
        }  
    }
}
