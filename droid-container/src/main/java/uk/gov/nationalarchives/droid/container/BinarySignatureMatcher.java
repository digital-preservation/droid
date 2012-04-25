/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.container;

import uk.gov.nationalarchives.droid.core.signature.ByteReader;
import uk.gov.nationalarchives.droid.core.signature.droid6.InternalSignature;

/**
 * @author rflitcroft
 *
 */
public final class BinarySignatureMatcher {

    private BinarySignatureMatcher() { }

    /**
     * 
     * @param source a binary source
     * @param signature a binary signature
     * @param maxBytesToScan - maximum bytes to scan from either end of the file.
     * @return true if the source matches the signature; false otherwise
     */
    public static boolean matches(ByteReader source, InternalSignature signature, long maxBytesToScan) {
        
        //droid4: return signature.isFileCompliant(source);
        return signature.matches(source, maxBytesToScan);
        
    }
}
