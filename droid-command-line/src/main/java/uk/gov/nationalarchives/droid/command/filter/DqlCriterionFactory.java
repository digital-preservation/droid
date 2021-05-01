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

import java.util.*;
import java.util.regex.Pattern;

import org.joda.time.format.ISODateTimeFormat;

import uk.gov.nationalarchives.droid.core.interfaces.IdentificationMethod;
import uk.gov.nationalarchives.droid.core.interfaces.NodeStatus;
import uk.gov.nationalarchives.droid.core.interfaces.ResourceType;
import uk.gov.nationalarchives.droid.core.interfaces.filter.BasicFilterCriterion;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionFieldEnum;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionOperator;
import uk.gov.nationalarchives.droid.core.interfaces.filter.FilterCriterion;

/**
 * A static class providing two factory functions to create FilterCriterion objects from a String representation
 * of fields, operators and values, or collections of values.
 *
 * @author rflitcroft, mpalmer
 */
public final class DqlCriterionFactory {

    private static final Map<CriterionFieldEnum, CriterionFactory> factories =
        new EnumMap<>(CriterionFieldEnum.class);
    
    static {
        factories.put(CriterionFieldEnum.FILE_EXTENSION, new UppercaseStringCriterionFactory());
        factories.put(CriterionFieldEnum.LAST_MODIFIED_DATE, new DateCriterionFactory());
        factories.put(CriterionFieldEnum.FILE_FORMAT, new UppercaseStringCriterionFactory());
        factories.put(CriterionFieldEnum.FILE_NAME, new UppercaseStringCriterionFactory());
        factories.put(CriterionFieldEnum.IDENTIFICATION_COUNT, new IntegerCriterionFactory());
        factories.put(CriterionFieldEnum.FILE_SIZE, new LongCriterionFactory());
        factories.put(CriterionFieldEnum.MIME_TYPE, new StringCriterionFactory());
        factories.put(CriterionFieldEnum.PUID, new StringCriterionFactory());
        factories.put(CriterionFieldEnum.IDENTIFICATION_METHOD, new EnumCriterionFactory<>(IdentificationMethod.class));
        factories.put(CriterionFieldEnum.JOB_STATUS, new EnumCriterionFactory<>(NodeStatus.class));
        factories.put(CriterionFieldEnum.RESOURCE_TYPE, new EnumCriterionFactory<>(ResourceType.class));
        factories.put(CriterionFieldEnum.EXTENSION_MISMATCH , new BooleanCriterionFactory());
    }

    private static final String VALUE_CANNOT_BE_NULL_OR_EMPTY = "The value provided cannot be null or empty.";
    private static final String FIELD_OPERATOR_VALUE_ERROR = "Error parsing values for filter field '%s' with operator '%s': %s";

    private DqlCriterionFactory() { }

    /**
     * Create a new criterion.
     * @param dqlValue the dql value
     * @param dqlField the field
     * @param dqlOperator the operator.
     * @return a new criterion.
     */
    public static FilterCriterion newCriterion(String dqlField, String dqlOperator, String dqlValue) {
        CriterionFieldEnum field = DqlCriterionMapper.forField(dqlField);
        CriterionOperator op = DqlCriterionMapper.forOperator(dqlOperator);
        return factories.get(field).newCriterion(field, op, dqlValue);
    }
    
    /**
     * Create a new criterion.
     * @param dqlValues the dql values
     * @param dqlField the field
     * @param dqlOperator the operator.
     * @return a new criterion.
     */
    public static FilterCriterion newCriterion(String dqlField, String dqlOperator, Collection<String> dqlValues) {
        CriterionFieldEnum field = DqlCriterionMapper.forField(dqlField);
        CriterionOperator op = DqlCriterionMapper.forOperator(dqlOperator);
        return factories.get(field).newCriterion(field, op, dqlValues);
    }


    /*
     * Private implementation
     */

    private static void errorOnNullOrEmpty(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(VALUE_CANNOT_BE_NULL_OR_EMPTY);
        }
    }

    private interface CriterionFactory {
        FilterCriterion newCriterion(CriterionFieldEnum field, CriterionOperator operator, String dqlValue);
        FilterCriterion newCriterion(CriterionFieldEnum field, CriterionOperator operator, Collection<String> dqlValues);
    }

    private static abstract class BasicCriterionFactory implements CriterionFactory {
        @Override
        public FilterCriterion newCriterion(CriterionFieldEnum field,
                                            CriterionOperator operator, Collection<String> dqlValues) {
            try {
                return new BasicFilterCriterion(field, operator, toArray(dqlValues));
            } catch (IllegalArgumentException ia) {
                throw new IllegalArgumentException(String.format(FIELD_OPERATOR_VALUE_ERROR, field, operator, ia.getMessage(), ia));
            }
        }

        @Override
        public FilterCriterion newCriterion(CriterionFieldEnum field,
                                            CriterionOperator operator, String dqlValue) {
            try {
                return new BasicFilterCriterion(field, operator, parseValue(dqlValue));
            } catch (IllegalArgumentException ia) {
                throw new IllegalArgumentException(String.format(FIELD_OPERATOR_VALUE_ERROR, field, operator, ia.getMessage(), ia));
            }
        }

        private Object[] toArray(Collection<String> values) {
            Object[] result = new Object[values.size()];
            int numResults = 0;
            for (String value : values) {
                result[numResults++] = parseValue(value);
            }
            return result;
        }

        protected abstract Object parseValue(String value);
    }

    private static final class StringCriterionFactory extends BasicCriterionFactory {
        @Override
        protected String parseValue(String value) {
            errorOnNullOrEmpty(value);
            return value;
        }
    }

    private static final class UppercaseStringCriterionFactory extends BasicCriterionFactory {
        @Override
        protected String parseValue(String value) {
            errorOnNullOrEmpty(value);
            return value.toUpperCase();
        }
    }

    private static final class LongCriterionFactory extends BasicCriterionFactory {
        @Override
        protected Long parseValue(String value) {
            return Long.valueOf(value);
        }
    }

    private static final class IntegerCriterionFactory extends BasicCriterionFactory {
        @Override
        protected Integer parseValue(String value) {
            return Integer.valueOf(value);
        }
    }

    private static final class DateCriterionFactory extends BasicCriterionFactory {
        @Override
        protected Date parseValue(String value) {
            return ISODateTimeFormat.date().parseDateTime(value).toDate();
        }
    }

    private static final class BooleanCriterionFactory extends BasicCriterionFactory {
        private static final String INVALID_BOOLEAN =  "The supplied value %s cannot be converted to a Boolean value.\n" +
                "Valid (case insensitive) values are true or yes, or false or no.";
        private static final Pattern BOOLEAN_TRUE_STR_REGEX = Pattern.compile("true|yes", Pattern.CASE_INSENSITIVE);
        private static final Pattern BOOLEAN_FALSE_STR_REGEX = Pattern.compile("false|no", Pattern.CASE_INSENSITIVE);
        @Override
        protected Boolean parseValue(String value) {
            if (BOOLEAN_TRUE_STR_REGEX.matcher(value).matches()) {
                return Boolean.TRUE;
            }
            if (BOOLEAN_FALSE_STR_REGEX.matcher(value).matches()) {
                return Boolean.FALSE;
            }
            throw new IllegalArgumentException(String.format(INVALID_BOOLEAN, value));
        }
    }

    private static final class EnumCriterionFactory<T extends Enum<T>> extends BasicCriterionFactory {
        private final Class<T> type;
        public EnumCriterionFactory(Class<T> type) {
            this.type = type;
        }

        @Override
        protected T parseValue(String value) {
            return Enum.valueOf(type, value);
        }

    }



}
