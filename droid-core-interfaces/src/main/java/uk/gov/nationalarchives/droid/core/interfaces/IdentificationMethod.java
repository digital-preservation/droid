/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.core.interfaces;

/**
 * @author Alok Kumar Dash
 */
public enum IdentificationMethod {

    /** No identification was performed.*/
    NULL(null, "No identification was performed."), 
    
    /** Binary Signature identification. */
    BINARY_SIGNATURE("Signature",
            "Identified by a PRONOM signature."),
    /** Identification by extension. */
    EXTENSION("Extension", "Identified by its filename extension."), 
    
    /** Identified by container inspection. */
    CONTAINER("Container", "Identified by looking inside a container format");

    private String method;
    private String methodDescription;
    
    
    /**
     * Constructor
     * @param method Identification method Binary or extension.
     * @param methodDescription Identification method description.
     */
    IdentificationMethod(String method, String methodDescription) {
        this.method = method;
        this.methodDescription = methodDescription;
    }

    /**
     * @return the method
     */
    public String getMethod() {
        return method;
    }

    /**
     * @return the methodDescription
     */
    public String getMethodDescription() {
        return methodDescription;
    }
    
    
    @Override
    public String toString() {
        return method;
    }

}
