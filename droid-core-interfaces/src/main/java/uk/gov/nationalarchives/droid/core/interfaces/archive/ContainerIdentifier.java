/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.core.interfaces.archive;

import java.io.IOException;

import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;

/**
 * @author rflitcroft
 *
 */
public interface ContainerIdentifier {

    /**
     * Submits an input stream for container identification.
     * @param request the request to be identified.
     * @return an Identification result
     * @throws IOException if the input stream could not be read
     */
    IdentificationResultCollection submit(IdentificationRequest request) throws IOException;
    
    /**
     * Sets the maximum number of bytes to scan from the
     * beginning or end of a file.  If negative, scanning
     * is unlimited. 
     * @param maxBytes The number of bytes to scan, or negative meaning unlimited.
     */
    void setMaxBytesToScan(long maxBytes);
   
}
