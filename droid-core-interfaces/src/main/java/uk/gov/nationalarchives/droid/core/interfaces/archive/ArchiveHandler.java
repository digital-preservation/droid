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

/**
 * Interface for archive handlers.
 * @author rflitcroft
 *
 */
public interface ArchiveHandler {

    /**
     * Handles an archive input stream.
     * @param request the originator request.
     * @throws IOException if there was an error reading from the input stream
     */
    void handle(IdentificationRequest request) throws IOException;

}
