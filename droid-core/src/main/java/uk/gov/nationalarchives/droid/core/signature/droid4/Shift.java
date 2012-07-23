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
 * $History: Shift.java $
 * 
 * *****************  Version 6  *****************
 * User: Walm         Date: 17/05/05   Time: 12:48
 * Updated in $/PRONOM4/FFIT_SOURCE/signatureFile
 * wait for end of element tag before setting its content via the
 * completeElementContent method
 * 
 * *****************  Version 5  *****************
 * User: Walm         Date: 5/04/05    Time: 18:07
 * Updated in $/PRONOM4/FFIT_SOURCE/signatureFile
 * review headers
 *
 */
package uk.gov.nationalarchives.droid.core.signature.droid4;

import uk.gov.nationalarchives.droid.core.signature.xml.SimpleElement;


/**
 * holds a shift function value
 *
 * @author Martin Waller
 * @version 4.0.0
 * @deprecated DROID 4 version. 
 */
@Deprecated
public class Shift extends SimpleElement {
    long myShiftValue;
    int myShiftByte = 999;

    /**
     * Set the shift distance when the end of element tag is reached.
     * This will have been stored in the text attribute by the setText method defined in SimpleElement
     */
    @Override
    public void completeElementContent() {
        String theElementValue = this.getText();   //((SimpleElement)this).getText();
        try {
            this.myShiftValue = Long.parseLong(theElementValue);
        } catch (Exception e) {
            generalWarning("The following non-numerical shift distance was found in the signature file: " + theElementValue);
            this.myShiftValue = 1;
        }

    }

    /**
     * Respond to an XML attribute
     *
     * @param theName  attribute name
     * @param theValue attribute value
     */
    @Override
    public void setAttributeValue(String theName, String theValue) {
        if (theName.equals("Byte")) {
            try {
                myShiftByte = Integer.parseInt(theValue, 16);
            } catch (Exception e) {
            }
        } else {
            unknownAttributeWarning(theName, this.getElementName());
        }
    }

    /* getters */
    public int getShiftByte() {
        return myShiftByte;
    }

    public long getShiftValue() {
        return myShiftValue;
    }
}
