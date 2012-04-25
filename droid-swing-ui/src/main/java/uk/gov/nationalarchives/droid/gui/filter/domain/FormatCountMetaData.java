/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui.filter.domain;

import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionFieldEnum;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionOperator;

/**
 * @author rflitcroft
 *
 */
public class FormatCountMetaData extends GenericMetadata {

    private static final int ALLOWEDNOOFDIGITS = 18;
    
    /**
     * @param metadataName
     */
    public FormatCountMetaData() {
        super(CriterionFieldEnum.IDENTIFICATION_COUNT);
        addOperation(CriterionOperator.EQ);
        addOperation(CriterionOperator.NE);
        addOperation(CriterionOperator.LT);
        addOperation(CriterionOperator.GT);
        addOperation(CriterionOperator.LTE);
        addOperation(CriterionOperator.GTE);
    }
    
    @Override
    public void validate(String stringTovalidate) throws FilterValidationException {

        if (stringTovalidate.length() > ALLOWEDNOOFDIGITS) {
            throw new FilterValidationException("Format count cannot be more than 18 digits long");
        }
        try {
            Long.parseLong(stringTovalidate);
            // Integer.parseInt(stringTovalidate);
        } catch (NumberFormatException e) {
            throw new FilterValidationException("Format count must be numeric and can not be left blank");
        }

    }

}
