/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.core.interfaces.signature;

import java.io.File;
import java.util.Map;
import java.util.SortedMap;

/**
 * Interface for signature file management.
 * @author rflitcroft
 *
 */
public interface SignatureManager {

    /**
     * 
     * @return a List of available signature files
     */
    Map<SignatureType, SortedMap<String, SignatureFileInfo>> getAvailableSignatureFiles();
    
    /**
     * Returns a signature file info if a later version is available, otherwise
     * returns null if we already have the latest.
     * @return a signature file info if a later version is available
     * @throws SignatureManagerException if the signature file could not be downloaded
     */
    Map<SignatureType, SignatureFileInfo> getLatestSignatureFiles() throws SignatureManagerException;
    
    /**
     * Downloads the latest file to local disk.
     * @param type the type of signature file to download
     * @return the signature file info of the downloaded file
     * @throws SignatureManagerException if the signature file could not be downloaded
     */
    SignatureFileInfo downloadLatest(SignatureType type) throws SignatureManagerException;

    /**
     * @return the default signature info
     * @throws SignatureFileException if the default signature setting was invalid
     */
    Map<SignatureType, SignatureFileInfo> getDefaultSignatures() throws SignatureFileException;
    
    /**
     * Uploads a signature file to DROID.
     * @param type the type of signature file being uploaded
     * @param signatureFile the signature file to upload
     * @param setDefault set this signature file as the new default
     * @return the siognature file info.
     * @throws SignatureFileException if the upload failed
     */
    SignatureFileInfo upload(SignatureType type, File signatureFile, boolean setDefault) throws SignatureFileException;

}
