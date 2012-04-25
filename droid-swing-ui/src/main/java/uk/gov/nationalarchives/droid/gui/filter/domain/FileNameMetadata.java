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

import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionFieldEnum;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionOperator;

/**
 * File name metadata.
 * 
 * @author adash
 * 
 */
public class FileNameMetadata extends GenericMetadata {

    private static final String DISPLAY_NAME = "File name";

    /**
     * Default constructor.
     */
    public FileNameMetadata() {
        super(CriterionFieldEnum.FILE_NAME);

        addOperation(CriterionOperator.EQ);
        addOperation(CriterionOperator.STARTS_WITH);
        addOperation(CriterionOperator.NE);
        addOperation(CriterionOperator.ENDS_WITH);
        addOperation(CriterionOperator.CONTAINS);
        addOperation(CriterionOperator.NOT_STARTS_WITH);
        addOperation(CriterionOperator.NOT_ENDS_WITH);
        addOperation(CriterionOperator.NOT_CONTAINS);        
        
    }

    @Override
    public void validate(String stringTovalidate) throws FilterValidationException {
        if (StringUtils.isBlank(stringTovalidate)) {
            throw new FilterValidationException("File name can not be blank");
        }

    }

}
