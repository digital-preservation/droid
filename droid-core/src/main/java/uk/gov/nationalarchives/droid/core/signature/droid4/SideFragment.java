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
 * $Id: sideFragment.java,v 1.6 2006/03/13 15:15:29 linb Exp $
 *
 * $Log: sideFragment.java,v $
 * Revision 1.6  2006/03/13 15:15:29  linb
 * Changed copyright holder from Crown Copyright to The National Archives.
 * Added reference to licence.txt
 * Changed dates to 2005-2006
 *
 * Revision 1.5  2006/02/09 15:04:37  gaur
 * Corrected formatting
 *
 * Revision 1.4  2006/02/07 11:30:04  gaur
 * Added support for endianness of signature
 *
 * Revision 1.3  2006/02/03 16:54:42  gaur
 * We now allow general wildcards of arbitrary endianness: e.g., [!~A1B1:C1D1]
 *
 * Revision 1.2  2006/02/02 17:15:47  gaur
 * Started migration to being able to handle byte specifier wildcards.  This version should have the same functionality as the old one (but making use of the new ByteSeqSpecifier class).
 *
 *
 * $History: sideFragment.java $
 *
 * *****************  Version 4  *****************
 * User: Walm         Date: 17/05/05   Time: 12:48
 * Updated in $/PRONOM4/FFIT_SOURCE/signatureFile
 * wait for end of element tag before setting its content via the
 * completeElementContent method
 *
 * *****************  Version 3  *****************
 * User: Walm         Date: 5/04/05    Time: 18:07
 * Updated in $/PRONOM4/FFIT_SOURCE/signatureFile
 * review headers
 *
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
