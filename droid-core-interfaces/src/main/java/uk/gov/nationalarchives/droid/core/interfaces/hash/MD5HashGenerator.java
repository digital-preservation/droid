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

import org.apache.commons.codec.digest.DigestUtils;

/**
 * @author rflitcroft
 *
 */
public class MD5HashGenerator implements HashGenerator {

    /**
     * {@inheritDoc}
     * @throws IOException 
     */
    @Override
    public String hash(InputStream in) throws IOException {
        return DigestUtils.md5Hex(in);
    }

}
