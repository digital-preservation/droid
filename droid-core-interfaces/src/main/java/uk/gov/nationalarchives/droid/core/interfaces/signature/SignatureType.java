/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.core.interfaces.signature;

/**
 * @author rflitcroft
 *
 */
public enum SignatureType {

    /** Binary Signature. */
    BINARY("Binary"),
    
    /** Container Signature. */
    CONTAINER("Container"),

    /** Text heuristic signature. */
    TEXT("Text heuristics");
    
    private String description;
    
    private SignatureType(String description) {
        this.description = description;
    }
    
    @Override
    public String toString() {
        return description;
    };
}
