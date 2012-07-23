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

import uk.gov.nationalarchives.droid.core.signature.xml.SimpleElement;

/**
 * holds the details of a left or right fragment associated with a subsequence
 *
 * @author Martin Waller
 * @version 4.0.0
 * @deprecated DROID 4 version. 
 */
@Deprecated
public class SideFragment extends SimpleElement {
    int myPosition;
    int myMinOffset;
    int myMaxOffset;
    int numBytes;
    String mySequenceFragment;
    ArrayList<ByteSeqSpecifier> myByteSpecifierSequence;

    /* setters */
    public void setPosition(int thePosition) {
        this.myPosition = thePosition;
    }

    public void setMinOffset(int theMinOffset) {
        this.myMinOffset = theMinOffset;
    }

    public void setMaxOffset(int theMaxOffset) {
        this.myMaxOffset = theMaxOffset;
    }

    @Override
    public void setAttributeValue(String name, String value) {
        if (name.equals("Position")) {
            setPosition(Integer.parseInt(value));
        } else if (name.equals("MinOffset")) {
            setMinOffset(Integer.parseInt(value));
        } else if (name.equals("MaxOffset")) {
            setMaxOffset(Integer.parseInt(value));
        } else {
            unknownAttributeWarning(name, this.getElementName());
        }
    }

    /* getters */
    public int getPosition() {
        return myPosition;
    }

    public int getMinOffset() {
        return myMinOffset;
    }

    public int getMaxOffset() {
        return myMaxOffset;
    }

    public int getNumByteSeqSpecifiers() {
        return myByteSpecifierSequence.size();
    }    // Number of byte sequence specifiers we hold (each of which specifies at least one byte)

    public ByteSeqSpecifier getByteSeqSpecifier(int index) {
        return myByteSpecifierSequence.get(index);
    }

    public int getNumBytes() {
        return numBytes;
    }                                      // Total number of bytes we hold

    public String getSequence() {
        return mySequenceFragment;
    }

    /**
     * Set the sideFragment sequence (this will have been stored in the text attribute by the setText method).
     * Then transforms the input string into an array of bytes
     */
    @Override
    public void completeElementContent() {
        numBytes = 0;
        String theElementValue = this.getText();
        this.mySequenceFragment = theElementValue;
        myByteSpecifierSequence = new ArrayList<ByteSeqSpecifier>();
        StringBuffer allSpecifiers = new StringBuffer(theElementValue);
        while (allSpecifiers.length() > 0) {
            try {
                ByteSeqSpecifier bss = new ByteSeqSpecifier(allSpecifiers);
                myByteSpecifierSequence.add(bss);
                numBytes += bss.getNumBytes();
            } catch (Exception e) {
            }
        }

    }

}
