/**
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gov.uk>
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
package uk.gov.nationalarchives.droid.core.interfaces;

/**
 * @author Alok Kumar Dash
 * BNO: Aug 2015: Added static method to retrieve enum by integer value
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

    /**
     * Returns the IdentificationMethod enum value for the corresponding ordinal.
     * @param  value - the ordinal value used for the identfication method.
     * @return the IdentificationMethodEnum corresponding to the value parameter.
     */
    public static IdentificationMethod getIdentifationMethodForOrdinal(int value)  {

        IdentificationMethod identificationMethod = null;
    // CHECKSTYLE:OFF --  Complaining about magic number, not an issue in this context...
        switch (value) {
            case 0:
                identificationMethod = IdentificationMethod.NULL;
                break;
            case 1:
                identificationMethod = IdentificationMethod.BINARY_SIGNATURE;
                break;
            case 2:
                identificationMethod = IdentificationMethod.EXTENSION;
                break;
            case 3:
                identificationMethod = IdentificationMethod.CONTAINER;
                break;
            default:
                throw new  IllegalArgumentException("Invalid identification " + value + "!");
        }
        // CHECKSTYLE:ON
        return identificationMethod;
    }
}
