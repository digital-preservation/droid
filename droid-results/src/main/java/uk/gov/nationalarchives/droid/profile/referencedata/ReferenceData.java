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
 * @author Alok Kumar Dash
 * ReferenceData Encapsulates all static data required for filtration. This is being populated for 
 * each profile and kept in ProfileInstanceManager. And retrieved as when required for loading
 * ValuesDialog with reference data.
 */
public class ReferenceData {

    private List<Format> formats;

    /**
     * @return the list of IdentificationMethods
     */

    /**
     * @return the list of Formats
     */
    public List<Format> getFormats() {
        return formats;
    }

    /**
     * @param formats
     *            the Formats to set
     */
    public void setFormats(List<Format> formats) {
        this.formats = formats;
    }

}
