/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.core.interfaces.hash;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author rflitcroft
 *
 */
public interface HashGenerator {

    /**
     * Calculates the hash of an input stream.
     * @param in the input stream
     * @return the hash.
     * @throws IOException if there was an error reading the input stream.
     */
    String hash(InputStream in) throws IOException;

}
