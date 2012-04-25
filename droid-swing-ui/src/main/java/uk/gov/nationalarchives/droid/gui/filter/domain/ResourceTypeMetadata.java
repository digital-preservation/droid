/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui.filter.domain;

import org.apache.commons.lang.StringUtils;

import uk.gov.nationalarchives.droid.core.interfaces.ResourceType;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionFieldEnum;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionOperator;
import uk.gov.nationalarchives.droid.core.interfaces.filter.FilterValue;

/**
 * Resource Type Metadata.
 * @author Alok Kumar Dash.
 *
 */

public class ResourceTypeMetadata extends GenericMetadata {

    /**
     * Default constructor. 
     */
    public ResourceTypeMetadata() {
        super(CriterionFieldEnum.RESOURCE_TYPE);
        addOperation(CriterionOperator.ANY_OF);
        addOperation(CriterionOperator.NONE_OF);
        int index = 0;
        for (ResourceType resourceType : ResourceType.values()) {
            addPossibleValue(new FilterValue(index++, resourceType.getResourceType(),
                    String.valueOf(resourceType.ordinal())));
        }
    }

    @Override
    public boolean isFreeText() {
        return false;
    }

    
    
    @Override
    public void validate(String stringTovalidate) throws FilterValidationException {
        if (StringUtils.isBlank(stringTovalidate)) {
            throw new FilterValidationException("Resource Type can not be blank");
        }
    }
}
