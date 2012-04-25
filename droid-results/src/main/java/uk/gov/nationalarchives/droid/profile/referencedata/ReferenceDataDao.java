/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.profile.referencedata;

import java.util.List;



/**
 * Operations for persisting reference data.
 * @author Alok Kumar Dash
 */
public interface ReferenceDataDao {

    /**
     * Retrieves formats from database. 
     * @return List of formats.
     */
    List<Format> getFormats();
}
