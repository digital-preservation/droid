/**
 * Copyright (c) 2012, The National Archives <pronom@nationalarchives.gsi.gov.uk>
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
/*
 * The National Archives 2005-2006.  All rights reserved.
 * See Licence.txt for full licence details.
 *
 * Developed by:
 * Tessella Support Services plc
 * 3 Vineyard Chambers
 * Abingdon, OX14 3PX
 * United Kingdom
 * http://www.tessella.com
 *
 * Tessella/NPD/4305
 * PRONOM 4
 *
 * $History: FileFormat.java $
 * 
 * *****************  Version 3  *****************
 * User: Walm         Date: 5/04/05    Time: 18:07
 * Updated in $/PRONOM4/FFIT_SOURCE/signatureFile
 * review headers
 *
 */
package uk.gov.nationalarchives.droid.core.signature;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import uk.gov.nationalarchives.droid.core.signature.xml.SimpleElement;

/**
 * holds details of a file format.
 * 
 * @author Martin Waller
 * @version 4.0.0
 */
public class FileFormat extends SimpleElement {
    private int identifier;
    private String name;
    private String version;
    private String puid;
    private final List<Integer> internalSigIDs = new ArrayList<Integer>();
    private final List<String> extensions = new ArrayList<String>();
    private final Set<String> extensionLookup = new HashSet<String>();
    private final List<Integer> hasPriorityOver = new ArrayList<Integer>();
    private String mimeType;

    /* setters */

    /**
     * @param theID
     *            the id of the internal signature.
     *            As a string, as that is how the XML parser sets the value.
     */
    public final void setInternalSignatureID(final String theID) {
        this.internalSigIDs.add(Integer.parseInt(theID));
    }
    
    /**
     * 
     * @param id The id of the signature to remove.
     */
    public final void removeInternalSignatureID(int id) {
        this.internalSigIDs.remove(id);
    }
    
    /**
     * Removes all internal signature ids from a file format.
     * @return a copy of the list of signature ids removed from the file format.
     */
    public final List<Integer> clearSignatures() {
        final List<Integer> oldSignatureIDs = new ArrayList<Integer>(internalSigIDs);
        this.internalSigIDs.clear();
        return oldSignatureIDs;
    }

    /**
     * 
     * @param theExtension
     *            the file extension.
     */
    public final void setExtension(final String theExtension) {
        this.extensions.add(theExtension);
        this.extensionLookup.add(theExtension.toUpperCase(Locale.ENGLISH));
    }

    /**
     * 
     * @param theID The signature the file format takes priority over.
     */
    public final void setHasPriorityOverFileFormatID(final String theID) {
        this.hasPriorityOver.add(Integer.parseInt(theID));
    }

    /**
     * 
     * @param mimeType The mime type of the file format.
     */
    public final void setMimeType(final String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public final void setAttributeValue(final String theName,
            final String theValue) {
        if ("ID".equals(theName)) {
            this.identifier = Integer.parseInt(theValue);
        } else if ("Name".equals(theName)) {
            this.name = theValue;
        } else if ("Version".equals(theName)) {
            this.version = theValue;
        } else if ("PUID".equals(theName)) {
            this.puid = theValue;
        } else if ("MIMEType".equals(theName)) {
            this.mimeType = theValue;
        } else {
            unknownAttributeWarning(name, this.getElementName());
        }
    }

    /* getters */
    
    /**
     * @return the number of internal signatures.
     */
    public final int getNumInternalSignatures() {
        return this.internalSigIDs.size();
    }

    /**
     * 
     * @return the number of file extensions.
     */
    public final int getNumExtensions() {
        return this.extensions.size();
    }

    /**
     * 
     * @return the number of file formats this format has priority over.
     */
    public final int getNumHasPriorityOver() {
        return this.hasPriorityOver.size();
    }
    
    /**
     * 
     * @return The list of format ids this format has priority over.
     */
    public List<Integer> getFormatIdsHasPriorityOver() {
        return this.hasPriorityOver;
    }

    /**
     * 
     * @param theIndex The index of the internal signature to get the id of.
     * @return the id of the internal signature.
     */
    public final int getInternalSignatureID(final int theIndex) {
        return this.internalSigIDs.get(theIndex);
    }

    /**
     * 
     * @return The mime type of the file format.
     */
    public final String getMimeType() {
        return this.mimeType == null ? "" : this.mimeType;
    }

    /**
     * 
     * @param theIndex The index of the file extension
     * @return the file extension.
     */
    public final String getExtension(final int theIndex) {
        return this.extensions.get(theIndex);
    }
    
    
    /**
     * 
     * @return A list of extensions defined against this file format.
     */
    public final List<String> getExtensions() {
        return extensions;
    }

    /**
     * 
     * @param theIndex The index of the format this format takes priority over.
     * @return The id of the file format which this format takes priority over.
     */
    public final int getHasPriorityOver(final int theIndex) {
        return this.hasPriorityOver.get(theIndex);
    }

    /**
     * 
     * @return The id of this file format.
     */
    public final int getID() {
        return identifier;
    }

    /**
     * 
     * @return the name of this file format.
     */
    public final String getName() {
        return name;
    }

    /**
     * 
     * @return The version of this file format.
     */
    public final String getVersion() {
        return version;
    }

    /**
     * 
     * @return the puid of this file format.
     */
    public final String getPUID() {
        return puid;
    }

    /**
     * Indicates whether the file extension given is listed against this file format.
     * 
     * @param theExtension file extension
     * @return whether this file format has a matching extension.
     * 
     */
    public final boolean hasMatchingExtension(final String theExtension) {
        return extensionLookup.contains(theExtension
                .toUpperCase(Locale.ENGLISH));
    }
    
    /**
     * Indicates whether the file extension given should result
     * in a mismatch warning.
     * 
     * If there are no extensions listed for this file format,
     * there should be no mismatch warning, whatever the given extension.
     * 
     * Otherwise, a warning should be issued if the extension 
     * is not listed against this file format.
     *  
     * @param theExtension The file extension to check.
     * @return Whether the file extension given should result in a mismatch warning.
     */
    public final boolean hasExtensionMismatch(final String theExtension) {
        return extensions.size() == 0 ? false : !hasMatchingExtension(theExtension);
    }

}
