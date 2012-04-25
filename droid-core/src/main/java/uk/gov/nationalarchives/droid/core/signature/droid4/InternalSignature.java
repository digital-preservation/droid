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
 * $Id: InternalSignature.java,v 1.5 2006/03/13 15:15:29 linb Exp $
 *
 * $Log: InternalSignature.java,v $
 * Revision 1.5  2006/03/13 15:15:29  linb
 * Changed copyright holder from Crown Copyright to The National Archives.
 * Added reference to licence.txt
 * Changed dates to 2005-2006
 *
 * Revision 1.4  2006/02/08 16:06:35  gaur
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
 * $History: InternalSignature.java $
 * 
 * *****************  Version 3  *****************
 * User: Walm         Date: 5/04/05    Time: 18:07
 * Updated in $/PRONOM4/FFIT_SOURCE/signatureFile
 * review headers
 *
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
