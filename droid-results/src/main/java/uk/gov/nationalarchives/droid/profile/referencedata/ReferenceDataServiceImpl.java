/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.profile.referencedata;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Alok Kumar Dash
 * 
 */
public class ReferenceDataServiceImpl implements ReferenceDataService {
    private ReferenceData referenceData;

    @Autowired
    private ReferenceDataDao rerferenceDataDao;

    /**
     * {@inheritDoc}
     */
    @Override
    public ReferenceData getReferenceData() {
        if (referenceData == null) {
            referenceData = new ReferenceData();
            referenceData.setFormats(rerferenceDataDao.getFormats());
        }
        return referenceData;
    }

}
