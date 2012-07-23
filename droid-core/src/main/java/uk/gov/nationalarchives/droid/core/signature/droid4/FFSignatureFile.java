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
package uk.gov.nationalarchives.droid.core.signature.droid4;

import java.util.ArrayList;
import java.util.List;

import uk.gov.nationalarchives.droid.core.signature.ByteReader;
import uk.gov.nationalarchives.droid.core.signature.FileFormat;
import uk.gov.nationalarchives.droid.core.signature.FileFormatCollection;
import uk.gov.nationalarchives.droid.core.signature.FileFormatHit;
import uk.gov.nationalarchives.droid.core.signature.xml.SimpleElement;

/**
 * holds details of a signature file and uses it to identify binary files
 *
 * @author Martin Waller
 * @version 4.0.0
 * @deprecated DROID 4 version.
 */
@Deprecated
public class FFSignatureFile extends SimpleElement {

    private String version = "";
    private String dateCreated = "";
    private FileFormatCollection FFcollection;
    private InternalSignatureCollection intSigs;


    /* setters */
    public void setFileFormatCollection(FileFormatCollection coll) {
        this.FFcollection = coll;
    }


    public void setInternalSignatureCollection(InternalSignatureCollection col3) {
        this.intSigs = col3;
    }

    private void setVersion(String vers) {
        this.version = vers;
    }

    private void setDateCreated(String dc) {
        this.dateCreated = dc;
    }

    @Override
    public void setAttributeValue(String name, String value) {
        if (name.equals("Version")) {
            setVersion(value.trim());
        } else if (name.equals("DateCreated")) {
            setDateCreated(value);
        } else {
            unknownAttributeWarning(name, this.getElementName());
        }
    }

    /* getters */
    public int getNumInternalSignatures() {
        return this.intSigs.getInternalSignatures().size();
    }


    public List<InternalSignature> getSignatures() {
        return intSigs.getInternalSignatures();
    }

    public InternalSignature getInternalSignature(int theIndex) {
        return intSigs.getInternalSignatures().get(theIndex);
    }

    public int getNumFileFormats() {
        return this.FFcollection.getFileFormats().size();
    }

    public FileFormat getFileFormat(int theIndex) {
        return FFcollection.getFileFormats().get(theIndex);
    }
    
    public FileFormatCollection getFileFormatCollection() {
    	return FFcollection;
    }

    public String getVersion() {
        return version;
    }

    public String getDateCreated() {
        return dateCreated;
    }


    /**
     * This method must be run after the signature file data has been read
     * and before the FFSignatureFile class is used.
     * It points internal signatures to the fileFormat objects they identify,
     * and it ensures that the sequence fragments are in the correct order.
     */
    public void prepareForUse() {
        this.setAllSignatureFileFormats();
        this.reorderAllSequenceFragments();
        this.reorderByteSequences();
    }


    /**
     * Points all internal signatures to the fileFormat objects they identify.
     */
    private void setAllSignatureFileFormats() {
        for (int iFormat = 0; iFormat < this.getNumFileFormats(); iFormat++) {  //loop through file formats
            for (int iFileSig = 0; iFileSig < this.getFileFormat(iFormat).getNumInternalSignatures(); iFileSig++) {  //loop through internal signatures for each file format
                int iFileSigID = this.getFileFormat(iFormat).getInternalSignatureID(iFileSig);
                //loop through all internal signatures to find one with a matching ID
                for (int iIntSig = 0; iIntSig < this.getNumInternalSignatures(); iIntSig++) {
                    if (this.getInternalSignature(iIntSig).getID() == iFileSigID) {
                        this.getInternalSignature(iIntSig).addFileFormat(this.getFileFormat(iFormat));
                        break;
                    }
                }
            }
        }
    }

    /**
     * Run prepareSeqFragments on all subSequences within all ByteSequences within all internalSignatures.
     */
    private void reorderAllSequenceFragments() {
        for (int iSig = 0; iSig < this.getNumInternalSignatures(); iSig++) {
            for (int iBS = 0; iBS < this.getInternalSignature(iSig).getNumByteSequences(); iBS++) {
                for (int iSS = 0; iSS < this.getInternalSignature(iSig).getByteSequence(iBS).getNumSubSequences(); iSS++) {
                    this.getInternalSignature(iSig).getByteSequence(iBS).getSubSequence(iSS).prepareSeqFragments();
                }
            }
        }
    }

    /**
     * Ensure that the BOFs and EOFs are searched for before the variable position byte sequences
     */
    private void reorderByteSequences() {
        for (int iSig = 0; iSig < this.getNumInternalSignatures(); iSig++) {
            InternalSignature sig = this.getInternalSignature(iSig);
            List<ByteSequence> BOFoffsetByteSequences = new ArrayList<ByteSequence>();
            List<ByteSequence> EOFoffsetByteSequences = new ArrayList<ByteSequence>();
            List<ByteSequence> variableByteSequences = new ArrayList<ByteSequence>();
            for (int iBS = 0; iBS < sig.getNumByteSequences(); iBS++) {
                ByteSequence seq = sig.getByteSequence(iBS);
                if (seq.getReference().startsWith("BOF")) {
                    BOFoffsetByteSequences.add(seq);
                }
                if (seq.getReference().startsWith("EOF")) {
                    EOFoffsetByteSequences.add(seq);
                } else {
                    variableByteSequences.add(seq);
                }
            }
            List<ByteSequence> byteSequences = new ArrayList<ByteSequence>();
            byteSequences.addAll(BOFoffsetByteSequences);
            byteSequences.addAll(EOFoffsetByteSequences);
            byteSequences.addAll(variableByteSequences);
            sig.resetByteSequences(byteSequences);
        }
    }

    /**
     * Identify the current file
     *
     * @param targetFile The binary file to be identified
     */
    //public synchronized void runFileIdentification(ByteReader targetFile) {
    public void runFileIdentification(ByteReader targetFile) {

        List<InternalSignature> signatureList = getSignatures();
        //record all positive identifications
        for (InternalSignature internalSig : signatureList) {
            if (internalSig.isFileCompliant(targetFile)) {
                //File matches this internal signature
                targetFile.setPositiveIdent();
                for (int i = 0; i < internalSig.getNumFileFormats(); i++) {
                    FileFormatHit fileHit = new FileFormatHit(internalSig.getFileFormat(i), FileFormatHit.HIT_TYPE_POSITIVE_GENERIC_OR_SPECIFIC, internalSig.isSpecific(), "");
                    targetFile.addHit(fileHit);
                }
            }
        }

        //remove any hits for which there is a higher priority hit
        if (targetFile.getNumHits() > 1) {
            this.removeLowerPriorityHits(targetFile);
        }

        //carry out file extension checking
        this.checkExtension(targetFile);

        // if there are still no hits then classify as unidentified
        if (targetFile.getNumHits() == 0) {
            targetFile.setNoIdent();
        }
    }


    /**
     * Remove any hits for which there is a higher priority hit
     *
     * @param targetFile The binary file to be identified
     */
    private void removeLowerPriorityHits(ByteReader targetFile) {
        //loop through specific hits and list any hits which these have priority over
        List<Integer> hitsToRemove = new ArrayList<Integer>();
        for (int i = 0; i < targetFile.getNumHits(); i++) {
            for (int j = 0; j < targetFile.getHit(i).getFileFormat().getNumHasPriorityOver(); j++) {
                int formatID = targetFile.getHit(i).getFileFormat().getHasPriorityOver(j);
                for (int k = 0; k < targetFile.getNumHits(); k++) { //loop through hits to find any for this file format
                    if (targetFile.getHit(k).getFileFormat().getID() == formatID) {
                        hitsToRemove.add(k);  //use string representation as ArrayList won't take integers
                        break;
                    }
                }
            }
        }
        //Create sorted array of indexes for hits to be removed
        int[] indexesOfHits = new int[hitsToRemove.size()];
        int numHitsToRemove = 0;
        for (Integer aHitsToRemove : hitsToRemove) {   //loop through unsorted list of hits to be removed
            int j = numHitsToRemove;
            int indexOfHit = aHitsToRemove;
            while (j > 0 && indexesOfHits[j - 1] > indexOfHit) {
                indexesOfHits[j] = indexesOfHits[j - 1];
                --j;
            }
            indexesOfHits[j] = indexOfHit;
            ++numHitsToRemove;
        }
        //Delete hits in decreasing index order, ignorinmg any repetitions
        for (int i = indexesOfHits.length - 1; i >= 0; i--) {
            if (i == (indexesOfHits.length - 1)) {
                targetFile.removeHit(indexesOfHits[i]);
            } else if (indexesOfHits[i] != indexesOfHits[i + 1]) {
                targetFile.removeHit(indexesOfHits[i]);
            }
        }

    }

    /**
     * Determines the file extension
     * If the file has got some positive hits, then check these against this extension
     * If the file has not got any positive hits, then look for tentative hits
     * based on the extension only.
     *
     * @param targetFile The binary file to be identified
     */
    private void checkExtension(ByteReader targetFile) {

        //work out if file has an extension
        boolean hasExtension = true;
        int dotPos = targetFile.getFileName().lastIndexOf(".");
        if (dotPos < 0) {
            hasExtension = false;
        } else if (dotPos == targetFile.getFileName().length() - 1) {
            hasExtension = false;
        } else if (targetFile.getFileName().lastIndexOf("/") > dotPos) {
            hasExtension = false;
        } else if (targetFile.getFileName().lastIndexOf("\\") > dotPos) {
            hasExtension = false;
        }

        //
        if (hasExtension) {
            String fileExtension = targetFile.getFileName().substring(dotPos + 1);

            if (targetFile.getNumHits() > 0) {

                //for each file format which is a hit, check that it expects the given extension - if not give a warning
                for (int iHit = 0; iHit < targetFile.getNumHits(); iHit++) {
                    if (targetFile.getHit(iHit).getFileFormat().hasExtensionMismatch(fileExtension)) {
                        targetFile.getHit(iHit).setIdentificationWarning(FileFormatHit.FILEEXTENSIONWARNING);
                    }
                }//loop through hits

            } else {
                //no positive hits have been found, so search for tenative hits
                //loop through all file formats with no internal signature
                for (int iFormat = 0; iFormat < this.getNumFileFormats(); iFormat++) {
                    if (this.getFileFormat(iFormat).getNumInternalSignatures() == 0) {
                        if (this.getFileFormat(iFormat).hasMatchingExtension(fileExtension)) {
                            //add this as a tentative hit
                            FileFormatHit fileHit = new FileFormatHit(this.getFileFormat(iFormat), FileFormatHit.HIT_TYPE_TENTATIVE, false, "");
                            targetFile.addHit(fileHit);
                            targetFile.setTentativeIdent();
                        }
                    }
                }//loop through file formats
            }
        }//end of if(hasExtension)
        else {
            //if the file does not have an extension then add warning to all its hits
            for (int iHit = 0; iHit < targetFile.getNumHits(); iHit++) {
                targetFile.getHit(iHit).setIdentificationWarning(FileFormatHit.FILEEXTENSIONWARNING);
            }
        }
    }
}
