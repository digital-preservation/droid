/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.container;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

//import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    private Log log = LogFactory.getLog(this.getClass());
    
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
     * @return The XML fragment defining the binary signature.
     */
    public XmlFragment getBinarySignature() {
        return binarySignatures;
    }
    
    /**
     * @return the compiled binarySignature
     */
    public InternalSignatureCollection getCompiledBinarySignatures() {
        if (signatures == null && binarySignatures != null && !compileError) {
            BinarySignatureXMLParser<InternalSignatureCollection> signatureParser =
                new BinarySignatureXMLParser<InternalSignatureCollection>();
            Element element = binarySignatures.getElement();
            try {
                signatures = signatureParser.fromXmlElement(element);
                signatures.prepareForUse();
                signatures.sortSignatures(new InternalSignatureComparator());
            } catch (SignatureParseException e) {
                compileError = true;
                signatures = null;
                String message = String.format("Could not parse signature:\n%s", element.getTextContent());
                log.warn(message);
            }
        }
        return signatures;
    }
    
}
