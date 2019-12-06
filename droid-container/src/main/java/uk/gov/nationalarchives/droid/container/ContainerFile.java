/**
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gsi.gov.uk>
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
package uk.gov.nationalarchives.droid.container;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

//import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Element;

import uk.gov.nationalarchives.droid.core.SignatureParseException;
import uk.gov.nationalarchives.droid.core.signature.droid6.InternalSignatureCollection;
import uk.gov.nationalarchives.droid.core.signature.droid6.InternalSignatureComparator;

/**
 * @author rflitcroft
 *
 */
@XmlAccessorType(XmlAccessType.NONE)
public class ContainerFile {

    @XmlTransient
    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    @XmlTransient
    private boolean compileError;
    
    @XmlElement(name = "Path")
    private String path;

    //@XmlElement(name = "TextSignature")
    //private String textSignature;
    
    @XmlElement(name = "BinarySignatures")
    private XmlFragment binarySignatures;
    
    @XmlTransient
    private InternalSignatureCollection signatures;
    
    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    
    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }
    
   
    /**
     * @return the textSignature
     */
    /*
    public String getTextSignature() {
        return StringUtils.trimToNull(textSignature);
    }
    */
    
    /**
     * @param textSignature the textSignature to set
     */
    /*
    public void setTextSignature(String textSignature) {
        this.textSignature = textSignature;
    }
    */

    /**
     * Set a collection of binary signatures directly, rather than parsing from XML.
     * This is useful when programmatically constructing ContainerFiles, rather than parsing from XML.
     * @param binarySigs The binary signatures to set.
     */
    public void setBinarySignatures(InternalSignatureCollection binarySigs) {
        signatures = binarySigs;
    }
    
    
    /**
     * @return The XML fragment defining the binary signature.
     */
    public XmlFragment getBinarySignature() {
        return binarySignatures;
    }
    
    /**
     * @return the compiled binarySignature
     */
    public InternalSignatureCollection getCompiledBinarySignatures() {
        if (signatures == null && binarySignatures != null && !compileError) {      //TODO potential race condition
            BinarySignatureXMLParser<InternalSignatureCollection> signatureParser =
                new BinarySignatureXMLParser<InternalSignatureCollection>();
            Element element = binarySignatures.getElement();
            try {
                InternalSignatureCollection signaturesCollection = signatureParser.fromXmlElement(element);
                signaturesCollection.prepareForUse();
                signaturesCollection.sortSignatures(new InternalSignatureComparator());
                this.signatures = signaturesCollection;
            } catch (SignatureParseException e) {
                compileError = true;
                this.signatures = null;
                String message = String.format("Could not parse signature:\n%s", element.getTextContent());
                log.warn(message);
            }
        }
        return this.signatures;
    }
    
}
