/**
 * Copyright (c) 2012, The National Archives <pronom@nationalarchives.gsi.gov.uk>
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
