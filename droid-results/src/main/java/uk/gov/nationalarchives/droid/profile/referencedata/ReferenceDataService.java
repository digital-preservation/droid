/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.profile.referencedata;

/**
 * @author adash Reference data service is responsible for loading all the
 *         reference data like Formats, Puids, statuses, Identification methods,
 *         mime types etc in to memory from database.
 */
public interface ReferenceDataService {

    /**
     * Retrieves reference data for the profile.
     * @return ReferenceData, Entire reference data for the profile.
     */

    ReferenceData getReferenceData();

}
