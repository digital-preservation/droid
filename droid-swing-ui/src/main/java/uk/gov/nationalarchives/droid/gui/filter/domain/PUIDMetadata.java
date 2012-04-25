/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui.filter.domain;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionFieldEnum;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionOperator;
import uk.gov.nationalarchives.droid.core.interfaces.filter.FilterValue;
import uk.gov.nationalarchives.droid.profile.referencedata.Format;

/**
 * Format/PUID metadata.
 * @author adash
 */

public class PUIDMetadata extends GenericMetadata {

    private static final String DISPLAY_NAME = "PUID";

    /**
     * @param data
     *            a list of formats
     */
    public PUIDMetadata(List<Format> data) {
        super(CriterionFieldEnum.PUID);
        addOperation(CriterionOperator.ANY_OF);
        addOperation(CriterionOperator.NONE_OF);
        int index = 0;
        for (Format format : data) {

            String formatWithVersion = "";
            if (format.getVersion() != null) {
                formatWithVersion = " ( " + format.getName() + " - " + format.getVersion() + " )";
            } else {
                formatWithVersion = " (" + format.getName() + ")";
            }

            if (format.getPuid() != null) {
                addPossibleValue(new FilterValue(index++, format.getPuid() + formatWithVersion, format.getPuid()));
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
            throw new FilterValidationException("PUID can not be blank");
        }
    }
}
