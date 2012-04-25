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
 * MIME type metadata.
 * @author adash
 *
 */
public class MimeTypeMetadata extends GenericMetadata {

    private static final String DISPLAY_NAME = "Mime type";

    /**
     * 
     * @param data available formats
     */
    public MimeTypeMetadata(List<Format> data) {
        super(CriterionFieldEnum.MIME_TYPE);
        addOperation(CriterionOperator.ANY_OF);
        addOperation(CriterionOperator.NONE_OF);
        int index = 0;
        for (Format format : data) {

            if (StringUtils.isNotBlank(format.getMimeType())) {
                if (!isExists(getPossibleValues(), format.getMimeType())) {
                    addPossibleValue(new FilterValue(index++,
                            format.getMimeType(), format.getMimeType()));
                }
            }
        }
    }

    private boolean isExists(List<FilterValue> values, String mimeType) {
        for (FilterValue value : values) {
            if (value.getDescription().equals(mimeType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isFreeText() {
        return false;
    }

}
