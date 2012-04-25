/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.core.interfaces;



/**
 * DROID core API.
 * Call submit() to submit identification requests.
 * 
 * This interface is somewhat artificial, reflecting the original split
 * between binary signature processing and later forms of identification
 * (container signatures).  Since these signatures interact, we require
 * "fix-up" methods here.
 * 
 * Specifically:
 * 
 * 1) If there is a container signature for a puid, we do not
 * want to run any defined binary signatures for the same puid, as this 
 * would be redundant.
 * 
 * 2) All file formats and their extensions are defined in the binary
 * signature file alone (so exist in implementations of this interface).
 * However, we need to be able to perform extension checking and 
 * extension mismatch checking on container signature results
 * as well as for binary signature results, so there are public methods
 * to allow this.
 * 
 * Eventually, it would be a good idea to have a single identification interface,
 * with separate interfaces / classes to perform these other individual functions
 * accessible behind the main identification interface.  
 * 
 * @author rflitcroft
 *
 */
public interface DroidCore {

    /**
     * Submits an identification request to identify files using
     * binary signatures.
     *  
     * @param request the identification request.
     * @return the identification result.
     */
    IdentificationResultCollection matchBinarySignatures(IdentificationRequest request);

    /**
     * 
     * matches a known file format extension.
     * 
     * @param request The identification request to identify files using
     * file extensions.
     * @param allExtensions check the extension against all known extensions.
     * If false, then only formats for which there is no other signature will
     * produce a file extension match (this is the default in DROID 5 and below).
     * @return the identification result.
     */
    IdentificationResultCollection matchExtensions(IdentificationRequest request, boolean allExtensions);
    
    
    /**
     * Sets the signature file for the DROID core to use.
     * @param sigFilename the signature file to use
     */
    void setSignatureFile(String sigFilename);

    /**
     * Removes binary Signatures which identify the PUID specified.
     * @param string a puid
     */
    void removeSignatureForPuid(String string);
    
    /**
     * Sets the maximum number of bytes to scan from the
     * beginning or end of a file.  If negative, scanning
     * is unlimited. 
     * @param maxBytes The number of bytes to scan, or negative meaning unlimited.
     */
    void setMaxBytesToScan(long maxBytes);
    
    
    /**
     * Removes hits from the collection where the file format is
     * flagged as lower priority than another in the collection.
     * 
     * @param results The results to remove lower priority hits for.
     */
    void removeLowerPriorityHits(IdentificationResultCollection results); 
    
    
    /**
     * Checks whether any of the results have a file extension mismatch.
     * 
     * @param results The collection to check for mismatches.
     * @param fileExtension The file extension to check against.
     */
    void checkForExtensionsMismatches(IdentificationResultCollection results, String fileExtension);
    
    
}
