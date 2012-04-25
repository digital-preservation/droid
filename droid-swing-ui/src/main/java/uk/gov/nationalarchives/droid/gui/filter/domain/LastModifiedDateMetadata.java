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
 * Last modified date metadata.
 * @author adash
 *
 */
public class LastModifiedDateMetadata extends GenericMetadata {

    private static final String VALIDATION_ERROR_MESSAGE = " Last modified data must be a date of format dd/mm/yyyy";
    private static final String DISPLAY_NAME = "LastModifiedDate";

    /**
     * Default Constructor.
     */
    public LastModifiedDateMetadata() {
        super(CriterionFieldEnum.LAST_MODIFIED_DATE);

        addOperation(CriterionOperator.EQ);
        addOperation(CriterionOperator.NE);
        addOperation(CriterionOperator.LT);
        addOperation(CriterionOperator.GT);
        addOperation(CriterionOperator.LTE);
        addOperation(CriterionOperator.GTE);

    }

//    @Override
//    public void validate(String stringToValidate) throws FilterValidationException {
//
//        try {
//            DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
//            df.setLenient(false);
//            df.parse(stringToValidate);
//        } catch (ParseException e) {
//            throw new FilterValidationException(VALIDATION_ERROR_MESSAGE);
//        } catch (IllegalArgumentException e) {
//            throw new FilterValidationException(VALIDATION_ERROR_MESSAGE);
//        }
//
//    }

}
