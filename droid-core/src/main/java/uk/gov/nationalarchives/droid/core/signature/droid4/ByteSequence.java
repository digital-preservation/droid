/*
 * � The National Archives 2005-2006.  All rights reserved.
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
 * $Id: ByteSequence.java,v 1.7 2006/03/13 15:15:29 linb Exp $
 *
 * $Log: ByteSequence.java,v $
 * Revision 1.7  2006/03/13 15:15:29  linb
 * Changed copyright holder from Crown Copyright to The National Archives.
 * Added reference to licence.txt
 * Changed dates to 2005-2006
 *
 * Revision 1.6  2006/02/09 15:04:37  gaur
 * Corrected formatting
 *
 * Revision 1.5  2006/02/08 16:14:01  gaur
 * Corrected error in merge
 *
 * Revision 1.4  2006/02/08 16:06:29  gaur
 * Moved endianness from internal signatures to byte sequences
 *
 * Revision 1.3  2006/02/07 17:16:22  linb
 * - Change fileReader to ByteReader in formal parameters of methods
 * - use new static constructors
 * - Add detection of if a filePath is a URL or not
 *
 * Revision 1.2  2006/02/07 11:30:04  gaur
 * Added support for endianness of signature
 *
 *
 * $History: ByteSequence.java $
 *
 * *****************  Version 5  *****************
 * User: Walm         Date: 5/04/05    Time: 18:07
 * Updated in $/PRONOM4/FFIT_SOURCE/signatureFile
 * review headers
 *
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
