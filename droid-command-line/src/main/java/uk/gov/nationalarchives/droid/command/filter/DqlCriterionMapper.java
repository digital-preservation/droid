/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.command.filter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionFieldEnum;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionOperator;

/**
 * This maps DQL fields to Filter Criterion fiels.
 * This should NOT be necessary if the Query Generator used type-safe objects for fields rather than strings.
 * @author rflitcroft
 *
 */
public final class DqlCriterionMapper {

    private static Map<String, CriterionFieldEnum> fieldMapping = new TreeMap<String, CriterionFieldEnum>();
    private static Map<String, CriterionOperator> operatorMapping = new LinkedHashMap<String, CriterionOperator>();
    
    static {
        fieldMapping.put("file_ext", CriterionFieldEnum.FILE_EXTENSION);
        fieldMapping.put("file_name", CriterionFieldEnum.FILE_NAME);
        fieldMapping.put("file_size", CriterionFieldEnum.FILE_SIZE);
        fieldMapping.put("format_name", CriterionFieldEnum.FILE_FORMAT);
        fieldMapping.put("format_count", CriterionFieldEnum.IDENTIFICATION_COUNT);
        fieldMapping.put("last_modified", CriterionFieldEnum.LAST_MODIFIED_DATE);
        fieldMapping.put("type", CriterionFieldEnum.RESOURCE_TYPE);
        fieldMapping.put("method", CriterionFieldEnum.IDENTIFICATION_METHOD);
        fieldMapping.put("status", CriterionFieldEnum.JOB_STATUS);
        fieldMapping.put("puid", CriterionFieldEnum.PUID);
        fieldMapping.put("mime_type", CriterionFieldEnum.MIME_TYPE);
        
        operatorMapping.put("<", CriterionOperator.LT);
        operatorMapping.put("<=", CriterionOperator.LTE);
        operatorMapping.put("=", CriterionOperator.EQ);
        operatorMapping.put(">=", CriterionOperator.GTE);
        operatorMapping.put(">", CriterionOperator.GT);
        operatorMapping.put("<>", CriterionOperator.NE);
        operatorMapping.put("starts", CriterionOperator.STARTS_WITH);
        operatorMapping.put("ends", CriterionOperator.ENDS_WITH);
        operatorMapping.put("contains", CriterionOperator.CONTAINS);
        operatorMapping.put("any", CriterionOperator.ANY_OF);
        operatorMapping.put("none", CriterionOperator.NONE_OF);
        operatorMapping.put("not contains", CriterionOperator.NOT_CONTAINS);
        operatorMapping.put("not starts", CriterionOperator.NOT_STARTS_WITH);
        operatorMapping.put("not ends", CriterionOperator.NOT_ENDS_WITH);
    }
    
    private DqlCriterionMapper() { }
    
    /**
     * Resolves a DQL field to a CriterionField.
     * @param field the DQL field to resolve
     * @return a Criterion Field
     */
    public static CriterionFieldEnum forField(String field) {
        if (!fieldMapping.containsKey(field.toLowerCase())) {
            throw new IllegalArgumentException(String.format("Invalid DQL field [%s]", field));
        }
        return fieldMapping.get(field.toLowerCase());
    }
    
    /**
     * Resolves a DQL operator to a CriterionOperator.
     * @param operator the DQL operator to resolve
     * @return a Criterion OPerator
     */
    public static CriterionOperator forOperator(String operator) {
        if (!operatorMapping.containsKey(operator.toLowerCase())) {
            throw new IllegalArgumentException(String.format("Invalid DQL operator [%s]", operator));
        }
        return operatorMapping.get(operator.toLowerCase());
    }

    /**
     * @return all the DQL field names;
     */
    public static String[] allDqlFields() {
        return fieldMapping.keySet().toArray(new String[fieldMapping.keySet().size()]);
    }
    
    /**
     * @return all the DQL field names;
     */
    public static String[] allDqlOperators() {
        return operatorMapping.keySet().toArray(new String[operatorMapping.keySet().size()]);
    }

}
