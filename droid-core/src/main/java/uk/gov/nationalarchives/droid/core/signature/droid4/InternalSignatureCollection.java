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
 * $History: InternalSignatureCollection.java $
 * 
 * *****************  Version 2  *****************
 * User: Walm         Date: 5/04/05    Time: 18:07
 * Updated in $/PRONOM4/FFIT_SOURCE/signatureFile
 * review headers
 *
 */
package uk.gov.nationalarchives.droid.core.signature.droid4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.gov.nationalarchives.droid.core.signature.xml.SimpleElement;

/**
 * holds a collection of internal signatures
 * used by the XML parsing code
 *
 * @author Martin Waller
 * @version 4.0.0
 * @deprecated DROID 4 version. 
 */
@Deprecated
public class InternalSignatureCollection extends SimpleElement {

    //List<InternalSignature> intSigs = Collections.synchronizedList(new ArrayList<InternalSignature>());
    List<InternalSignature> intSigs = new ArrayList<InternalSignature>();

    /* setters */
    public void addInternalSignature(InternalSignature iSig) {
        intSigs.add(iSig);
    }

    public void setInternalSignatures(List<InternalSignature> iSigs) {
        this.intSigs = iSigs;
    }

    /* getters */
    //public synchronized List<InternalSignature> getInternalSignatures() {
    public List<InternalSignature> getInternalSignatures() {
        return intSigs;
    }
}
