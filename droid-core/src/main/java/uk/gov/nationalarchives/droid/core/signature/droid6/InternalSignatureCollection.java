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
 * $History: InternalSignatureCollection.java $
 * 
 * *****************  Version 2  *****************
 * User: Walm         Date: 5/04/05    Time: 18:07
 * Updated in $/PRONOM4/FFIT_SOURCE/signatureFile
 * review headers
 *
 */
package uk.gov.nationalarchives.droid.core.signature.droid6;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import uk.gov.nationalarchives.droid.core.signature.ByteReader;
import uk.gov.nationalarchives.droid.core.signature.xml.SimpleElement;

/**
 * Holds a collection of internal signatures
 * used by the XML parsing code.
 *
 * @author Martin Waller
 * @author Matt Palmer.
 * @version 6.0.0
 */
public class InternalSignatureCollection extends SimpleElement {

    /**
     * Default size of signature collection.
     */
    private static final int DEFAULT_COLLECTION_SIZE = 10;
    
    private List<InternalSignature> intSigs = new ArrayList<InternalSignature>(DEFAULT_COLLECTION_SIZE);
    private Map<Integer, InternalSignature> sigsByID = new HashMap<Integer, InternalSignature>();
    
    /**
     * Runs all the signatures against the target file,
     * adding a hit for each of them, if any of them match.
     * 
     * @param targetFile The file to match the signatures against.
     * @param maxBytesToScan The maximum bytes to scan.
     * @return A list of the internal signatures which matched. 
     */
    public List<InternalSignature> getMatchingSignatures(ByteReader targetFile, long maxBytesToScan) {
        List<InternalSignature> matchingSigs = new ArrayList<InternalSignature>();
        if (targetFile.getNumBytes() > 0) {
            final int stop = intSigs.size();
            for (int sigIndex = 0; sigIndex < stop; sigIndex++) {
                final InternalSignature internalSig = intSigs.get(sigIndex);
                if (internalSig.matches(targetFile, maxBytesToScan)) {
                    matchingSigs.add(internalSig);
                }
            }
        }
        return matchingSigs;
    }
    
   
    /**
     * Prepares the internal signatures in the collection for use.
     */
    public void prepareForUse() {
        for (Iterator<InternalSignature> sigIterator = intSigs.iterator(); sigIterator.hasNext();) {
            InternalSignature sig = sigIterator.next();
            sig.prepareForUse();
            if (sig.isInvalidSignature()) {
                sigsByID.remove(sig.getID());
                getLog().warn(getInvalidSignatureWarningMessage(sig));
                sigIterator.remove();
            }
        }
    }
    
    private String getInvalidSignatureWarningMessage(InternalSignature sig) {
        return String.format("Removing invalid signature [id:%d]. " 
                + "Matches formats: %s", sig.getID(), sig.getFileFormatDescriptions());
    }

    
    /* setters */
    /**
     * @param iSig the signature to add.
     */
    public final void addInternalSignature(final InternalSignature iSig) {
        intSigs.add(iSig);
        sigsByID.put(iSig.getID(), iSig);
    }
    
    
    /**
     * 
     * @param iSig The signature to remove.
     */
    public final void removeInternalSignature(final InternalSignature iSig) {
        intSigs.remove(iSig);
        sigsByID.remove(iSig.getID());
    }
    
    
    /**
     * 
     * @param signatureID The id of the signature to get
     * @return The signature with the given id, or null if the signature does not exist.
     */
    public final InternalSignature getInternalSignature(int signatureID) {
        return sigsByID.get(signatureID);
    }

    
    /**
     * 
     * @param iSigs The list of signatures to add.
     */
    public final void setInternalSignatures(final List<InternalSignature> iSigs) {
        intSigs.clear();
        sigsByID.clear();
        for (InternalSignature signature : iSigs) {
            addInternalSignature(signature);
        }
    }

    /* getters */
    /**
     * A list of internal signatures in the collection.
     * @return A list of internal signatures in the collection.
     */
    public final List<InternalSignature> getInternalSignatures() {
        return intSigs;
    }

    /**
     * Sorts the signatures in an order which maximises performance.
     * @param compareWith the internal signature comparator to compare with.
     */
    public void sortSignatures(final Comparator<InternalSignature> compareWith) {
        Collections.sort(intSigs, compareWith);
    }

}
