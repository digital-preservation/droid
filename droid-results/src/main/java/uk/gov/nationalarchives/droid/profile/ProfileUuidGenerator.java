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
 *
 */
public interface ProfileUuidGenerator {

    /** 
     * Generates a UUID.
     * @return the uuid
     */
    String generateUuid();
}
