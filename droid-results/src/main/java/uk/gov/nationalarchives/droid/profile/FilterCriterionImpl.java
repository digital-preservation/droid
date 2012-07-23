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
package uk.gov.nationalarchives.droid.profile;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import org.joda.time.format.ISODateTimeFormat;

import uk.gov.nationalarchives.droid.core.interfaces.IdentificationMethod;
import uk.gov.nationalarchives.droid.core.interfaces.NodeStatus;
import uk.gov.nationalarchives.droid.core.interfaces.ResourceType;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionFieldEnum;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionOperator;
import uk.gov.nationalarchives.droid.core.interfaces.filter.FilterCriterion;
import uk.gov.nationalarchives.droid.core.interfaces.filter.FilterValue;

/**
 * @author Alok  Kumar Dash
 *
 */
@XmlAccessorType(XmlAccessType.NONE)
public class FilterCriterionImpl implements FilterCriterion {

    @XmlElement(name = "FieldName", required = true)
    private CriterionFieldEnum fieldName;
    
    @XmlElement(name = "Operator", required = true)
    private CriterionOperator operator;

    @XmlElement(name = "Value", required = true)
    private String valueFreeText;

    @XmlElement(name = "Parameter", required = true)
    private List<FilterValue> selectedValues;

    @XmlElement(name = "RowNumber", required = true)
    private int rowNumber;
    
    
    /**
     * Default constructor.
     */
    public FilterCriterionImpl() { }
    
    /**
     * Getter method for row number.
     * @return row number.
     */
    public int getRowNumber() {
        return rowNumber;
    }

    /**
     * Setter method for row number.
     * @param rowNumber row position of this filter criteria.
     */
    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    /**
     * Add values to selected values list.
     * @param value reference data.
     */
    public void addSelectedValue(FilterValue value) {
        selectedValues.add(value);
    }

    /**
     * Remove values to selected values list.
     * @param value reference data.
     */
    public void removeSelectedValues(FilterValue value) {
        int index = 0;
        for (int i = 0; i < selectedValues.size(); i++) {

            if (selectedValues.get(i).getDescription().equals(
                    value.getDescription())) {
                index = i;
            }
            if (index >= 0) {
                selectedValues.remove(index);
            }
        }
    }

    /** Getter method for metadata name.
     * @return metadataname name of the metadata. 
     */
    public CriterionFieldEnum getField() {
        return fieldName;
    }

    /** Setter method for field.
     * @param field name of the metadata. 
     */
    public void setField(CriterionFieldEnum field) {
        this.fieldName = field;
    }

    /**
     * Getter method for operator string.
     * @return String representation of operator.
     */
    public CriterionOperator getOperator() {
        return operator;
    }

    /**
     * Setter method for operator string.
     * @param operator  String representation of the operator.
     */
    public void setOperator(CriterionOperator operator) {
        this.operator = operator;
    }

    /**
     * Getter method for value free text.
     * @return values text.
     */
    public String getValueFreeText() {
        return valueFreeText;
    }

    /**
     * Setter method for value free text.
     * @param valueFreeText selected value at filter.
     */
    public void setValueFreeText(String valueFreeText) {
        this.valueFreeText = valueFreeText;
    }

    /**
     * Getter method for selected values.
     * @return list of selected values.
     */
    public List<FilterValue> getSelectedValues() {
        return selectedValues;
    }

    /**
     *Setter method for selected values. 
     * @param selectedValues List of selected values.
     */
    public void setSelectedValues(List<FilterValue> selectedValues) {
        this.selectedValues = selectedValues;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue() {
        Object obj = null;
        if (selectedValues != null && !selectedValues.isEmpty()) {
            Object[] value = new Object[selectedValues.size()];
            int i = 0;
            for (FilterValue v : selectedValues) {
                value[i++] = convert(fieldName, v.getQueryParameter());
            }
            obj = value;
        } else if (valueFreeText != null) {
            obj = convert(fieldName, valueFreeText);
        }
        return obj;
    }
    
    // CHECKSTYLE:OFF
    private static Object convert(CriterionFieldEnum fieldType, String s) {
        // CHECKSTYLE:ON
        Object o;
        switch (fieldType) {
            case FILE_EXTENSION:
            case FILE_FORMAT:
            case FILE_NAME:
            case PUID:
            case MIME_TYPE:
                o = s;
                break;
            case FILE_SIZE:
                o = Long.valueOf(s);
                break;
            case LAST_MODIFIED_DATE:
                o = ISODateTimeFormat.date().parseDateTime(s).toDate();
                break;
            case IDENTIFICATION_METHOD:
                o = IdentificationMethod.values()[Integer.valueOf(s)];
                break;
            case JOB_STATUS:
                o = NodeStatus.values()[Integer.valueOf(s)];
                break;
            case RESOURCE_TYPE:
                o = ResourceType.values()[Integer.valueOf(s)];
                break;
            case IDENTIFICATION_COUNT:
                o = Integer.valueOf(s);
                break;
            default:
                throw new IllegalArgumentException(
                        String.format("Invalid argument [%s]", fieldType));
        }
        
        return o;
    }
}
