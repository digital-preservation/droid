/**
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gsi.gov.uk>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following
 * conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of the The National Archives nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package uk.gov.nationalarchives.droid.core.interfaces;

import java.io.IOException;

/**
 * DROID core API.
 *
 * An interface for classes which match binary signatures, container signatures and file extensions.
 * <p>
 * It provides separate methods for each of these types, but also a main match method
 * which implements all matching in one call.
 * 
 * @author rflitcroft, mpalmer
 */
public interface DroidCore {

    /**
     * Submits an identification request to identify files using all
     * available signatures and extensions defined.
     *
     * @param request the identification request
     * If false, then only formats for which there is no other signature will
     * produce a file extension match (this is the default in DROID 5 and below).
     * @return the identification result.
     * @throws IOException if there was a problem matching.
     */
    IdentificationResultCollection match(IdentificationRequest request) throws IOException;

    /**
     * Submits an identification request to identify files using
     * binary signatures.
     *  
     * @param request the identification request.
     * @return the identification result.
     */
    IdentificationResultCollection matchBinarySignatures(IdentificationRequest request);

    /**
     * Submits an identification request to identify files using
     * container signatures.
     * @param request the identification request.
     * @return the identification result.
     * @throws IOException if there was a problem matching a container signature.
     */
    IdentificationResultCollection matchContainerSignatures(IdentificationRequest request) throws IOException;

    /**
     * matches a known file format extension.
     * 
     * @param request The identification request to identify files using file extensions.
     * @return the identification result.
     */
    IdentificationResultCollection matchExtensions(IdentificationRequest request);

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
