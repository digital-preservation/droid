/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.submitter;

import java.io.File;
import java.io.IOException;

import uk.gov.nationalarchives.droid.core.interfaces.ResourceId;
import uk.gov.nationalarchives.droid.submitter.FileWalker.ProgressEntry;

/**
 * @author rflitcroft
 *
 */
public interface FileWalkerHandler {

    /**
     * Handles a file walk event.
     * @param file the file or directory being handled
     * @param depth the depth n the hierarchy
     * @param parent the parent of the file or directory
     * @return the ID assigned to the handled file or directory
     * @throws IOException if the file IO failed.
     */
    ResourceId handle(File file, int depth, ProgressEntry parent) throws IOException;
}
