/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.core.interfaces.text;

import java.io.IOException;

import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;

/**
 * @author mpalmer
 *
 */
public interface TextIdentifier {
    /**
     * Submits an input stream for container identification.
     * @param request the request to be identified.
     * @return an Identification result
     * @throws IOException if the input stream could not be read
     */
    IdentificationResultCollection submit(IdentificationRequest request) throws IOException;
}
