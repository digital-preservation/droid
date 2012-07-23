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
import uk.gov.nationalarchives.droid.core.signature.xml.SimpleElement;

/**
 * holds the details of an internal signature
 *
 * @author Martin Waller
 * @version 4.0.0
 * @deprecated DROID 4 version.
 */
@Deprecated
public class InternalSignature extends SimpleElement {

    private List<ByteSequence> byteSequences = new ArrayList<ByteSequence>();
    int intSigID;
    String specificity;
    List<FileFormat> fileFormatList = new ArrayList<FileFormat>();

    /* setters */
    public void addByteSequence(ByteSequence byteSequence) {
        byteSequence.setParentSignature(intSigID);
        for (int i = 0; i < byteSequence.getNumSubSequences(); i++) {
            SubSequence subSequence = byteSequence.getSubSequence(i);
            subSequence.setParentSignature(intSigID);
            subSequence.setReference(byteSequence.getReference());
            subSequence.setBigEndian(byteSequence.isBigEndian());
            subSequence.setByteSequence(byteSequence);
        }
        byteSequences.add(byteSequence);
    }

    /**
     * Reset the bytesequences after reordering (to ensure BOF and EOF sequences are checked first
     *
     * @param byteSequences sequence
     */
    public void resetByteSequences(List<ByteSequence> byteSequences) {
        this.byteSequences = byteSequences;
    }

    public void addFileFormat(FileFormat theFileFormat) {
        fileFormatList.add(theFileFormat);
    }

    public void setID(String theIntSigID) {
        this.intSigID = Integer.parseInt(theIntSigID);
    }

    public void setSpecificity(String Specificity) {
        this.specificity = Specificity;
    }

    @Override
    public void setAttributeValue(String name, String value) {
        if (name.equals("ID")) {
            setID(value);
        } else if (name.equals("Specificity")) {
            setSpecificity(value);
        } else {
            unknownAttributeWarning(name, this.getElementName());
        }
    }

    /* getters */
    public List<ByteSequence> getByteSequences() {
        return byteSequences;
    }

    public int getNumFileFormats() {
        return fileFormatList.size();
    }

    public FileFormat getFileFormat(int theIndex) {
        return fileFormatList.get(theIndex);
    }

    public ByteSequence getByteSequence(int theByteSeq) {
        return this.getByteSequences().get(theByteSeq);
    }

    public int getNumByteSequences() {
        return this.byteSequences.size();
    }

    public int getID() {
        return intSigID;
    }

    public String getSpecificity() {
        return specificity;
    }

    public boolean isSpecific() {
        return specificity.equalsIgnoreCase("specific");
    }


    /**
     * Indicates whether the file is compliant with this internal signature
     *
     * @param targetFile the binary file to be identified
     * @return boolean
     */
    public boolean isFileCompliant(ByteReader targetFile) {
        //initialise variable
        boolean isCompliant = true;
        //long start = System.currentTimeMillis();
        //check each byte sequence in turn - stop as soon as one is found to be non-compliant
        for (int i = 0; (i < this.byteSequences.size()) && isCompliant; i++) {
            isCompliant = this.getByteSequence(i).isFileCompliant(targetFile);
        }
        //long finish = System.currentTimeMillis();
        //System.out.println(String.format("Signature %d took %d milliseconds", intSigID, finish - start));
        return isCompliant;
    }

    @Override
    public String toString() {
        return intSigID + "(" + specificity + ")" + byteSequences;
    }
}
