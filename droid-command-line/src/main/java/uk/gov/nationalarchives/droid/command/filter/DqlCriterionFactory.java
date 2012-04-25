/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.command.filter;

import java.util.Collection;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;

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
    }
    
    private DqlCriterionFactory() { }

    /**
     * Create a new citerion.
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
}
