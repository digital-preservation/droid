/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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
package uk.gov.nationalarchives.droid.core.signature.droid6;

import uk.gov.nationalarchives.droid.core.signature.xml.SimpleElement;

/**
 * The current system calculates its own shifts, so this class
 * does nothing except preserve backwards compatibility with the
 * DROID XML parser, which will attempt to create Shift elements.
 *
 * @author Martin Waller
 * @author Matt Palmer
 * @version 6.0.0
 */
public class Shift extends SimpleElement {

    /**
     */
    @Override
    public final void completeElementContent() {
    }

    /**
     * @param theName  attribute name
     * @param theValue attribute value
     */
    @Override
    public final void setAttributeValue(final String theName, final String theValue) {
    }
    
}
