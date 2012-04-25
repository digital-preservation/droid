/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui.filter.domain;


import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionFieldEnum;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionOperator;
import uk.gov.nationalarchives.droid.core.interfaces.filter.FilterValue;
import uk.gov.nationalarchives.droid.profile.referencedata.Format;

/**
 * File-Format Metadata.
 * @author Alok Kumar Dash. 
 */
public class FileFormatMetadata extends GenericMetadata {

    private static final String DISPLAY_NAME = "File format";

    /**
     * @param data Reference data format.
     * Default constructor.
     */
    public FileFormatMetadata(List<Format> data) {
        super(CriterionFieldEnum.FILE_FORMAT);
        addOperation(CriterionOperator.EQ);
        addOperation(CriterionOperator.STARTS_WITH);
        addOperation(CriterionOperator.NE);
        addOperation(CriterionOperator.ENDS_WITH);
        addOperation(CriterionOperator.CONTAINS);
        addOperation(CriterionOperator.NOT_STARTS_WITH);
        addOperation(CriterionOperator.NOT_ENDS_WITH);
        addOperation(CriterionOperator.NOT_CONTAINS);        
        
        // Convert in to unique values.
        Set<String> uniqueFormat = new LinkedHashSet<String>();
        int index = 0;
        for (Format formatFromDatabase : data) {
            uniqueFormat.add(formatFromDatabase.getName());
        }
        index = 0;
        for (String format : uniqueFormat) {
            addPossibleValue(new FilterValue(index++, format, format));
        }
    }

    @Override
    public boolean isFreeText() {
        return true;
    }

}
