/*
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
package uk.gov.nationalarchives.pronom;

import jakarta.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the uk.gov.nationalarchives.pronom package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: uk.gov.nationalarchives.pronom.
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetSignatureFileVersionV1Response}.
     * @return SignatureFileVersionV1Response 
     */
    public GetSignatureFileVersionV1Response createGetSignatureFileVersionV1Response() {
        return new GetSignatureFileVersionV1Response();
    }

    /**
     * Create an instance of {@link Version}.
     *
     * @return Version
     * 
     */
    public Version createVersion() {
        return new Version();
    }

    /**
     * Create an instance of {@link GetSignatureFileV1}.
     * 
     * @return GetSignatureFileV1
     */
    public GetSignatureFileV1 createGetSignatureFileV1() {
        return new GetSignatureFileV1();
    }

    /**
     * Create an instance of {@link GetSignatureFileVersionV1}.
     * 
     * @return GetSignatureFileVersionV1
     */
    public GetSignatureFileVersionV1 createGetSignatureFileVersionV1() {
        return new GetSignatureFileVersionV1();
    }

    /**
     * Create an instance of {@link GetSignatureFileV1Response}.
     * 
     * @return GetSignatureFileV1Response
     */
    public GetSignatureFileV1Response createGetSignatureFileV1Response() {
        return new GetSignatureFileV1Response();
    }

}
