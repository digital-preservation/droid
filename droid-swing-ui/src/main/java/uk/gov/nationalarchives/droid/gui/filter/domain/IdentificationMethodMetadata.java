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

import uk.gov.nationalarchives.droid.core.interfaces.IdentificationMethod;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionFieldEnum;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionOperator;
import uk.gov.nationalarchives.droid.core.interfaces.filter.FilterValue;


/**
 * Identification method metatdata.
 * @author adash
 *
 */
public class IdentificationMethodMetadata extends GenericMetadata {

    /**
     * Default constructor.
     */
    public IdentificationMethodMetadata() {
        super(CriterionFieldEnum.IDENTIFICATION_METHOD);
        addOperation(CriterionOperator.ANY_OF);
        addOperation(CriterionOperator.NONE_OF);
        int index = 0;
        for (IdentificationMethod identificationMethod : IdentificationMethod.values()) {
            if (!identificationMethod.equals(IdentificationMethod.NULL)) {
                addPossibleValue(new FilterValue(index++, identificationMethod
                        .getMethod(), String.valueOf(identificationMethod.ordinal())));
            }
        }
    }


    @Override
    public boolean isFreeText() {
        return false;
    }

    
    
    @Override
    public void validate(String stringTovalidate) throws FilterValidationException {
        if (StringUtils.isBlank(stringTovalidate)) {
            throw new FilterValidationException("Identification method can not be blank");
        }
    }
}
