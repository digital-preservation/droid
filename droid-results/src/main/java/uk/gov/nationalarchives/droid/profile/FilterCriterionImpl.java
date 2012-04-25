/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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
