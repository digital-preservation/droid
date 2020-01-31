/**
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gov.uk>
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
package uk.gov.nationalarchives.droid.core.interfaces.signature;

import java.nio.file.Path;
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
     * Installs a signature file in DROID.
     * @param type the type of signature file being installed
     * @param signatureFile the signature file to install
     * @param setDefault set this signature file as the new default
     * @return the siognature file info.
     * @throws SignatureFileException if the installation failed
     */
    SignatureFileInfo install(SignatureType type, Path signatureFile, boolean setDefault) throws SignatureFileException;

}
