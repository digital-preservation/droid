/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.profile;

import java.io.File;
import java.io.InputStream;

/**
 * @author rflitcroft
 *
 */
public interface ProfileSpecDao {

    /**
     * Persists a new profile spec.
     * @param profileSpec the profile spec to persist
     * @param profileHomeDir the home directory of the profile
     */
    void saveProfile(ProfileInstance profileSpec, File profileHomeDir);

    /**
     * Loads a profile instabnce form the given input stream.
     * @param in the input stream containing the profile data.
     * @return the profile instance loaded
     */
    ProfileInstance loadProfile(InputStream in);
}
