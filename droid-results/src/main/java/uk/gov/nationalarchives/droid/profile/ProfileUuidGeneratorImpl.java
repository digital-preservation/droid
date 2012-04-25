/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.profile;

import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 * @author rflitcroft
 *
 */
public class ProfileUuidGeneratorImpl implements ProfileUuidGenerator {

    /**
     * Generates a UUID of the format &lt;hostname&gt;-&lt;timestamp&gt;.
     * @return the UUID
     */
    @Override
    public String generateUuid() {
        try {
            return InetAddress.getLocalHost().getCanonicalHostName() + "-" + System.currentTimeMillis();
        } catch (UnknownHostException e) {
            return "unknownhost-" + System.currentTimeMillis();
        }
    }
}
