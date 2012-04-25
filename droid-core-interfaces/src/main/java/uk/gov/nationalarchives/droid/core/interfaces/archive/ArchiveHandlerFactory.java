/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.core.interfaces.archive;


/**
 * Factory for archive handlers.
 * @author rflitcroft
 *
 */
public interface ArchiveHandlerFactory {

    /**
     * Returns the archive handler appropriate for the format supplied.
     * @param format an archive format
     * @return an archive handlers
     */
    ArchiveHandler getHandler(String format);

}
