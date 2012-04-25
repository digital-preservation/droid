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
 * $History: RightFragment.java $
 * 
 * *****************  Version 3  *****************
 * User: Walm         Date: 20/06/05   Time: 9:58
 * Updated in $/PRONOM4/FFIT_SOURCE/signatureFile
 * Returns a blank string for toString()
 * 
 * *****************  Version 2  *****************
 * User: Walm         Date: 5/04/05    Time: 18:07
 * Updated in $/PRONOM4/FFIT_SOURCE/signatureFile
 * review headers
 *
 */
package uk.gov.nationalarchives.droid.core.signature.droid6;

/**
 * A right fragment is all the parts of a byte sequence to
 * the right of an anchoring subsequence.  These parts cannot
 * be searched for using the fast BoyerMooreHorspool algorithm
 * so their existence is checked for once an anchoring sequence 
 * has been found.
 * 
 * <p/>This subclass of SideFragment is only required so the XML
 * parser can build an instance of this class and assemble the
 * right fragments together.
 * 
 * <p/>Left and Right Fragments are otherwise identical in functionality.
 * 
 * @author Martin Waller
 * @author Matt Palmer
 * @version 6.0.0
 */
public class RightFragment extends SideFragment {

}
