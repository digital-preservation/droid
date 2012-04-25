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
 * @author rflitcroft
 *
 */
public interface ArchiveFormatResolver {

    /**
     * Resolves a PUID to its archive format.
     * @param puid the puid to resolve
     * @return an archive format
     */
    String forPuid(String puid);
    
    /**
     * Registers a format against a PUID.
     * @param puid the PUID to register
     * @param format the format
     */
    void registerPuid(String puid, String format);
}
