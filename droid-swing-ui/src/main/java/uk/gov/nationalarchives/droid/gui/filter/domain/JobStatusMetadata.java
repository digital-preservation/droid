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

import uk.gov.nationalarchives.droid.core.interfaces.NodeStatus;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionFieldEnum;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionOperator;
import uk.gov.nationalarchives.droid.core.interfaces.filter.FilterValue;

/**
 * Identification status metatdata.
 * @author adash
 *
 */

public class JobStatusMetadata extends GenericMetadata {

    /**
     * Default Constructor.
     */
    public JobStatusMetadata() {
        super(CriterionFieldEnum.JOB_STATUS);
        addOperation(CriterionOperator.ANY_OF);
        addOperation(CriterionOperator.NONE_OF);
        int index = 0;
        for (NodeStatus nodeStatus : NodeStatus.values()) {
            addPossibleValue(new FilterValue(index++, nodeStatus.getStatus(),
                    String.valueOf(nodeStatus.ordinal())));
        }
    }

    @Override
    public boolean isFreeText() {
        return false;
    }
    @Override
    public void validate(String stringTovalidate) throws FilterValidationException {
        if (StringUtils.isBlank(stringTovalidate)) {
            throw new FilterValidationException("Job status can not be blank");
        }
    }
}
