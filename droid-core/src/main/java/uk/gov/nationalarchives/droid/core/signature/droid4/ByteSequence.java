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
import uk.gov.nationalarchives.droid.core.signature.xml.SimpleElement;

/**
 * holds details of a byte sequence
 *
 * @author Martin Waller
 * @version 4.0.0
 * @deprecated DROID 4 version.
 */
@Deprecated
public class ByteSequence extends SimpleElement {

    int parentSignature;
    List<SubSequence> subSequences = new ArrayList<SubSequence>();
    String reference = "";
    boolean bigEndian = true;               // Assume a signature is big-endian unless we are told to the contrary.
    int indirectOffsetLength = 0;
    int indirectOffsetLocation = 0;
    //int MaxOffset = 0;

    /* setters */


    public int getIndirectOffsetLength() {
        return indirectOffsetLength;
    }

    public int getIndirectOffsetLocation() {
        return indirectOffsetLocation;
    }

    public int getParentSignature() {
        return parentSignature;
    }

    public void setParentSignature(int parentSignature) {
        this.parentSignature = parentSignature;
    }

    public void addSubSequence(SubSequence sseq) {
        subSequences.add(sseq);
    }

    public void setSubSequences(List<SubSequence> SubSequences) {
        this.subSequences = SubSequences;
    }

    public void setReference(String theRef) {
        this.reference = theRef;
    }

    public void setEndianness(String endianness) {
        this.bigEndian = !endianness.equals("Little-endian");
    }

    public void setIndirectOffsetLength(String indirectOffsetLength) {
        this.indirectOffsetLength = Integer.parseInt(indirectOffsetLength);
    }

    public void setIndirectOffsetLocation(String indirectOffsetLocation) {
        this.indirectOffsetLocation = Integer.parseInt(indirectOffsetLocation);
    }


    public boolean isBigEndian() {
        return bigEndian;
    }

    @Override
    public void setAttributeValue(String name, String value) {
        if (name.equals("Reference")) {
            setReference(value);
        } else if (name.equals("Endianness")) {
            setEndianness(value);
        } else if (name.equals("IndirectOffsetLength")) {
            setIndirectOffsetLength(value);
        } else if (name.equals("IndirectOffsetLocation")) {
            setIndirectOffsetLocation(value);
        } else {
            unknownAttributeWarning(name, this.getElementName());
        }
    }


    /**
     * is this byte sequence anchored to either
     * BOF or EOF
     *
     * @return
     */
    public boolean isAnchored() {
        return getReference().endsWith("EOFoffset") || getReference().endsWith("BOFoffset");
    }

    /* getters */
    public List<SubSequence> getSubSequences() {
        return subSequences;
    }

    public int getNumSubSequences() {
        return subSequences.size();
    }

    public SubSequence getSubSequence(int theIndex) {
        return subSequences.get(theIndex);
    }

    public String getReference() {
        return reference;
    }
    //public int getMaxOffset() { return MaxOffset; }


    /**
     * checks whether the binary file specified by targetFile is compliant
     * with this byte sequence
     *
     * @param targetFile The binary file to be identified
     * @return boolean
     */
    public boolean isFileCompliant(ByteReader targetFile) {
        //System.out.println("Looking at new byte sequence with reference "+Reference);
        //initialise variables and start with the file marker at the beginning of the file
        boolean isCompliant = true;
        boolean reverseOrder = (getReference().equalsIgnoreCase("EOFoffset"));
        int ssLoopStart = reverseOrder ? getNumSubSequences() - 1 : 0;
        int ssLoopEnd = reverseOrder ? -1 : getNumSubSequences();
        int searchDirection = reverseOrder ? -1 : 1;
        if (reverseOrder) {
            targetFile.setFileMarker(targetFile.getNumBytes() - 1L);
        } else {
            targetFile.setFileMarker(0L);
        }

        //check whether each subsequence in turn is compliant
        for (int iSS = ssLoopStart; (searchDirection * iSS < searchDirection * ssLoopEnd) & isCompliant; iSS += searchDirection) {
            boolean isFixedStart = getReference().endsWith("EOFoffset") || getReference().endsWith("BOFoffset");
            if ((iSS == ssLoopStart) && (isFixedStart)) {
                isCompliant = getSubSequence(iSS).isFoundAtStartOfFile(targetFile, reverseOrder, bigEndian); //, MaxOffset);
            } else {
                isCompliant = getSubSequence(iSS).isFoundAfterFileMarker(targetFile, reverseOrder, bigEndian);
            }


        }
        return isCompliant;


    }

    @Override
    public String toString() {
        return reference + "{" + subSequences + "}";
    }
}
