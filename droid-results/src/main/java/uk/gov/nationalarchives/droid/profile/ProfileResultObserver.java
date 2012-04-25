/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.profile;


/**
 * @author rflitcroft
 * Interface for clients to implement in order to receive notifications of identifications.
 */
public interface ProfileResultObserver {

    /**
     * Triggered when DROID identified a file format.
     * @param uri the identification made
     */
    void onResult(ProfileResourceNode uri);
}
