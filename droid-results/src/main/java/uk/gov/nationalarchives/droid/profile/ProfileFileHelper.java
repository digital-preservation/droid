/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.profile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author rflitcroft
 *
 */
public final class ProfileFileHelper {
    
    private ProfileFileHelper() {
    }

    /**
     * Reads a profile.xml from a zipped file.
     * @param source the zip file
     * @return the Profile.
     * @throws IOException if the zip file copuld not be read
     */
    static InputStream getProfileXmlInputStream(ZipFile source) throws IOException {
        ZipEntry profileXml = source.getEntry("profile.xml");
        InputStream in = new BufferedInputStream(source.getInputStream(profileXml));
        return in;
    
    }
}
