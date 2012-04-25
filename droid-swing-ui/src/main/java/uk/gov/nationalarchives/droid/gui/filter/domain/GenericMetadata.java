/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui.filter.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionFieldEnum;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionOperator;
import uk.gov.nationalarchives.droid.core.interfaces.filter.FilterValue;

/**
 * Generic metadata.
 * @author adash
 *
 */
public class GenericMetadata implements Metadata {

    /** Equals. */
    public static final String EQUALS = "=";
    /** Not equals. */
    public static final String NOT_EQUALS = "<>";
    /** Like. */
    public static final String LIKE = "Begins with";
    /** Less than. */
    public static final String LESS_THAN = "<";
    /** Greater than. */
    public static final String GREATER_THAN = ">";
    /** Less than or equals. */
    public static final String LESS_THAN_OR_EQUALS = "<=";
    /** Greater than or equals. */
    public static final String GREATER_THAN_OR_EQUALS = ">=";
    
    
    /** Any of. */
    public static final String ANY_OF = "Any of";
    /** None of. */
    public static final String NONE_OF = "None of";
    /**  Contains. */
    public static final String CONTAINS = "Cointains";
    /** Ends with. */
    public static final String ENDS_WITH = "Ends with";
    
    
    /** Equal. */
    public static final String OP_EQUALS = EQUALS;
    /** Not equal. */
    public static final String OP_NOT_EQUALS = "!=";
    /** Like. */
    public static final String OP_LIKE = "like";
    /** Less than. */
    public static final String OP_LESS_THAN = LESS_THAN;
    /** Greater than. */
    public static final String OP_GREATER_THAN = GREATER_THAN;
    /** Less than or equal. */
    public static final String OP_LESS_THAN_OR_EQUALS = LESS_THAN_OR_EQUALS;
    /** Greater than or equal. */
    public static final String OP_GREATER_THAN_OR_EQUALS = GREATER_THAN_OR_EQUALS;
    
    /** Any of. */
    public static final String OP_ANY_OF = OP_EQUALS;
    /** None of. */
    public static final String OP_NONE_OF = OP_NOT_EQUALS;
    /**  Contains. */
    public static final String OP_CONTAINS = "%like%";
    /** Ends with. */
    public static final String OP_ENDS_WITH = "like%";
    
    
    

    private Map<String, String> operationMap = new HashMap<String, String>();
    
    private CriterionFieldEnum metadataName;

    private final List<CriterionOperator> operationList = new ArrayList<CriterionOperator>();

    private List<FilterValue> possibleValues = new ArrayList<FilterValue>();


    /**
     * 
     * @param metadataName the metadata name.
     */
    public GenericMetadata(CriterionFieldEnum metadataName) {
        this.metadataName = metadataName;
        operationMap.put(EQUALS, OP_EQUALS);
        operationMap.put(NOT_EQUALS, OP_NOT_EQUALS);
        operationMap.put(LIKE, OP_LIKE);
        operationMap.put(LESS_THAN, OP_LESS_THAN);
        operationMap.put(GREATER_THAN, OP_GREATER_THAN);
        operationMap.put(LESS_THAN_OR_EQUALS, OP_LESS_THAN_OR_EQUALS);
        operationMap.put(GREATER_THAN_OR_EQUALS, OP_GREATER_THAN_OR_EQUALS);
        
        operationMap.put(ANY_OF, OP_ANY_OF);
        operationMap.put(NONE_OF, OP_NONE_OF);
        operationMap.put(CONTAINS, OP_CONTAINS);
        operationMap.put(ENDS_WITH, OP_ENDS_WITH);
    }

    /**
     * 
     * @return the operations
     */
    public Map<String, String> getOperationMap() {
        return operationMap;
    }

    /**
     * Sets the operations.
     * @param operationMap the operations to set
     */
    public void setOperationMap(Map<String, String> operationMap) {
        this.operationMap = operationMap;
    }


    @Override
    public CriterionFieldEnum getMetadataName() {
        return metadataName;
    }

    @Override
    public final List<CriterionOperator> getOperationList() {
        return operationList;
    }

    @Override
    public final List<FilterValue> getPossibleValues() {
        return possibleValues;
    }
    
    /**
     * Adds a possible value.
     * @param value the value to add
     */
    void addPossibleValue(FilterValue value) {
        possibleValues.add(value);
    }
    
    /**
     * Adds an operation.
     * @param operation the operation to add
     */
    void addOperation(CriterionOperator operation) {
        operationList.add(operation);
    }

    @Override
    public boolean isFreeText() {
        return true;
    }

    @Override
    public void validate(String stringToValidate) throws FilterValidationException {
    }

    @Override
    public String toString() {
        return getMetadataName().toString();
    }

    /**
     * @return the field type
     */
    public CriterionFieldEnum getField() {
        return metadataName;
    }

}
