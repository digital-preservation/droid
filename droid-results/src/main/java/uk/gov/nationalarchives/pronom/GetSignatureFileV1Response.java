
package uk.gov.nationalarchives.pronom;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import uk.gov.nationalarchives.pronom.signaturefile.XmlFragment;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="SignatureFile" type="{http://www.nationalarchives.gov.uk/pronom/SignatureFile}SigFile"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "signatureFile"
})
@XmlRootElement(name = "getSignatureFileV1Response")
public class GetSignatureFileV1Response {

    @XmlElement(name = "SignatureFile", required = true)
    protected XmlFragment signatureFile;

    /**
     * Gets the value of the signatureFile property.
     * 
     * @return
     *     possible object is
     *     {@link SigFile }
     *     
     */
    public XmlFragment getSignatureFile() {
        return signatureFile;
    }

    /**
     * Sets the value of the signatureFile property.
     * 
     * @param value
     *     allowed object is
     *     {@link SigFile }
     *     
     */
    public void setSignatureFile(XmlFragment value) {
        this.signatureFile = value;
    }

}
