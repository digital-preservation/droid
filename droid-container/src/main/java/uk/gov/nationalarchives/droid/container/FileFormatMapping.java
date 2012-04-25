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
import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author rflitcroft
 *
 */
@XmlAccessorType(XmlAccessType.NONE)
public class FileFormatMapping {

    @XmlAttribute(name = "signatureId")
    private int signatureId;
    
    @XmlAttribute(name = "Puid")
    private String puid;

    /**
     * @return the puid
     */
    public String getPuid() {
        return puid;
    }
    
    /**
     * @param puid the puid to set
     */
    public void setPuid(String puid) {
        this.puid = puid;
    }
    
    /**
     * @return the signatureId
     */
    public int getSignatureId() {
        return signatureId;
    }
    
}
