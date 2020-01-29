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

import java.util.Collection;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.joda.time.format.ISODateTimeFormat;

import uk.gov.nationalarchives.droid.core.interfaces.IdentificationMethod;
import uk.gov.nationalarchives.droid.core.interfaces.NodeStatus;
import uk.gov.nationalarchives.droid.core.interfaces.ResourceType;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionFieldEnum;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionOperator;
import uk.gov.nationalarchives.droid.core.interfaces.filter.FilterCriterion;

/**
 * @author rflitcroft
 *
 */
public final class DqlCriterionFactory {

    
    private static Map<CriterionFieldEnum, CriterionFactory> factories =
        new EnumMap<CriterionFieldEnum, CriterionFactory>(CriterionFieldEnum.class);
    
    static {
        factories.put(CriterionFieldEnum.FILE_EXTENSION, new StringCriterionFactory());
        factories.put(CriterionFieldEnum.LAST_MODIFIED_DATE, new DateCriterionFactory());
        factories.put(CriterionFieldEnum.FILE_EXTENSION, new StringCriterionFactory());
        factories.put(CriterionFieldEnum.FILE_FORMAT, new StringCriterionFactory());
        factories.put(CriterionFieldEnum.FILE_NAME, new StringCriterionFactory());
        factories.put(CriterionFieldEnum.FILE_SIZE, new LongCriterionFactory());
        factories.put(CriterionFieldEnum.MIME_TYPE, new StringCriterionFactory());
        factories.put(CriterionFieldEnum.PUID, new StringCriterionFactory());
        factories.put(CriterionFieldEnum.IDENTIFICATION_METHOD, 
                new EnumCriterionFactory<IdentificationMethod>(IdentificationMethod.class));
        factories.put(CriterionFieldEnum.JOB_STATUS, 
                new EnumCriterionFactory<NodeStatus>(NodeStatus.class));
        factories.put(CriterionFieldEnum.RESOURCE_TYPE, 
                new EnumCriterionFactory<ResourceType>(ResourceType.class));
        factories.put(CriterionFieldEnum.EXTENSION_MISMATCH , new BooleanCriterionFactory());
    }
    
    private DqlCriterionFactory() { }

    /**
     * Create a new criterion.
     * @param dqlValue the dql value
     * @param dqlField the field
     * @param dqlOperator the operator.
     * @return a new criterion.
     */
    static FilterCriterion newCriterion(String dqlField, String dqlOperator, String dqlValue) {
        CriterionFieldEnum field = DqlCriterionMapper.forField(dqlField);
        CriterionOperator op = DqlCriterionMapper.forOperator(dqlOperator);
        return factories.get(field).newCriterion(field, op, dqlValue);
    }
    
    /**
     * Create a new citerion.
     * @param dqlValues the dql values
     * @param dqlField the field
     * @param dqlOperator the operator.
     * @return a new criterion.
     */
    static FilterCriterion newCriterion(String dqlField, String dqlOperator, Collection<String> dqlValues) {
        CriterionFieldEnum field = DqlCriterionMapper.forField(dqlField);
        CriterionOperator op = DqlCriterionMapper.forOperator(dqlOperator);
        return factories.get(field).newCriterion(field, op, dqlValues);
    }

    private static final class StringCriterionFactory implements CriterionFactory {
        @Override
        public FilterCriterion newCriterion(CriterionFieldEnum field, 
                CriterionOperator operator, Collection<String> dqlValues) {
            StringCriterion criterion = new StringCriterion(field, operator);
            criterion.setValue(dqlValues);
            return criterion;
        };
        
        @Override
        public FilterCriterion newCriterion(CriterionFieldEnum field,
                CriterionOperator operator, String dqlValue) {
            StringCriterion criterion = new StringCriterion(field, operator);
            criterion.setValue(dqlValue);
            return criterion;
        }
    }
   
    private static final class LongCriterionFactory implements CriterionFactory {
        @Override
        public FilterCriterion newCriterion(CriterionFieldEnum field, 
                CriterionOperator operator, Collection<String> dqlValues) {
            LongCriterion criterion = new LongCriterion(field, operator);
            criterion.setValue(dqlValues);
            return criterion;
        };
        
        @Override
        public FilterCriterion newCriterion(CriterionFieldEnum field,
                CriterionOperator operator, String dqlValue) {
            LongCriterion criterion = new LongCriterion(field, operator);
            criterion.setValue(dqlValue);
            return criterion;
        }
    }

    private static final class DateCriterionFactory implements CriterionFactory {
        @Override
        public FilterCriterion newCriterion(CriterionFieldEnum field, 
                CriterionOperator operator, Collection<String> dqlValues) {
            DateCriterion criterion = new DateCriterion(field, operator);
            criterion.setValue(dqlValues);
            return criterion;
        };
        
        @Override
        public FilterCriterion newCriterion(CriterionFieldEnum field,
                CriterionOperator operator, String dqlValue) {
            DateCriterion criterion = new DateCriterion(field, operator);
            criterion.setValue(dqlValue);
            return criterion;
        }
    }

    private static final class BooleanCriterionFactory implements CriterionFactory {
        @Override
        public FilterCriterion newCriterion(CriterionFieldEnum field,
                                            CriterionOperator operator, Collection<String> dqlValues) {
            BooleanCriterion criterion = new BooleanCriterion(field, operator);
            criterion.setValue(dqlValues);
            return criterion;
        };

        @Override
        public FilterCriterion newCriterion(CriterionFieldEnum field,
                                            CriterionOperator operator, String dqlValue) {
            BooleanCriterion criterion = new BooleanCriterion(field, operator);
            criterion.setValue(dqlValue);
            return criterion;
        }
    }


    private static final class EnumCriterionFactory<T extends Enum<T>> implements CriterionFactory {

        private Class<T> type;
        
        public EnumCriterionFactory(Class<T> type) {
            this.type = type;
        }
        
        @Override
        public FilterCriterion newCriterion(CriterionFieldEnum field, 
                CriterionOperator operator, Collection<String> dqlValues) {
            EnumCriterion<T> criterion = new EnumCriterion<T>(field, operator, type);
            criterion.setValue(dqlValues);
            return criterion;
        };
        
        @Override
        public FilterCriterion newCriterion(CriterionFieldEnum field,
                CriterionOperator operator, String dqlValue) {
            StringCriterion criterion = new StringCriterion(field, operator);
            criterion.setValue(dqlValue);
            return criterion;
        }
    }

    private static class StringCriterion extends AbstractFilterCriterion<String> {

        public StringCriterion(CriterionFieldEnum field, CriterionOperator operator) {
            super(field, operator);
        }
        
        @Override
        protected String toTypedValue(String s) {
            return s;
        }

        @Override
        public void setValue(String value) {

            CriterionFieldEnum cfe = this.getField();

            if (cfe != null && (cfe == CriterionFieldEnum.FILE_FORMAT
                    || cfe == CriterionFieldEnum.FILE_NAME
                    || cfe == CriterionFieldEnum.FILE_EXTENSION)) {
                super.setValue(toTypedValue(value).toUpperCase());
            } else {
                super.setValue(toTypedValue(value));
            }
        }

    }

    private static class LongCriterion extends AbstractFilterCriterion<Long> {

        public LongCriterion(CriterionFieldEnum field, CriterionOperator operator) {
            super(field, operator);
        }
        
        @Override
        protected Long toTypedValue(String s) {
            return Long.valueOf(s);
        }
    }

    private static class DateCriterion extends AbstractFilterCriterion<Date> {

        public DateCriterion(CriterionFieldEnum field, CriterionOperator operator) {
            super(field, operator);
        }
        
        @Override
        protected Date toTypedValue(String s) {
            return ISODateTimeFormat.date().parseDateTime(s).toDate();
        }
    }

    private static class EnumCriterion<T extends Enum<T>> extends AbstractFilterCriterion<T> {

        private Class<T> type;
        
        public EnumCriterion(CriterionFieldEnum field, CriterionOperator operator, Class<T> type) {
            super(field, operator);
            this.type = type;
        }
        
        @Override
        protected T toTypedValue(String s) {
            return Enum.valueOf(type, s);
        }
    }

    /**
    * @author Brian O'Reilly
    * @date 14 March 2014
    */
    private static class BooleanCriterion extends AbstractFilterCriterion<Boolean> {

        public  static final String INVALID_BOOLEAN =  "The supplied value %s cannot be converted to a Boolean value";
        private static final Pattern BOOLEAN_TRUE_STR_REGEX = Pattern.compile("true|yes", Pattern.CASE_INSENSITIVE);
        private static final Pattern BOOLEAN_FALSE_STR_REGEX = Pattern.compile("false|no", Pattern.CASE_INSENSITIVE);
        private static final Matcher BOOLEAN_TRUE_MATCHER = BOOLEAN_TRUE_STR_REGEX.matcher("");
        private static final Matcher BOOLEAN_FALSE_MATCHER = BOOLEAN_FALSE_STR_REGEX.matcher("");
     
        public BooleanCriterion(CriterionFieldEnum field, CriterionOperator operator) {
             super(field, operator);
        }
      
        /**
        * Convert String input of [true|yes|false|no]i to Boolean
        * @param inputString textual value (may be user entered from CLI)
        * @return boolean
        * @throws IllegalArgumentException on unrecognised string
        */
        @Override
        protected Boolean toTypedValue(String inputString) {

            BOOLEAN_TRUE_MATCHER.reset(inputString);
            if (BOOLEAN_TRUE_MATCHER.matches()) {
                return Boolean.TRUE;
            }
            BOOLEAN_FALSE_MATCHER.reset(inputString);
            if (BOOLEAN_FALSE_MATCHER.matches()) {
                return Boolean.FALSE;
            }
            
            throw new IllegalArgumentException(String.format(INVALID_BOOLEAN, inputString));

        }

    
    }
}
