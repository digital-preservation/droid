/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.signature;

import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileException;



/**
 * @author rflitcroft
 *
 */
public interface SignatureParser {

    /**
     * Parses the file specified and returns a collection of Format objects.
     * @param callback a callback to be executed whenver a format is found
     * @throws SignatureFileException if the signature file could not be parsed
     */
    void formats(FormatCallback callback) throws SignatureFileException;
    
}
