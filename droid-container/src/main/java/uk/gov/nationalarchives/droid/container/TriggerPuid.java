/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.container;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author rflitcroft
 *
 */
public class TriggerPuid {
    
    @XmlAttribute(name = "Puid")
    private String puid;
    
    @XmlAttribute(name = "ContainerType")
    private String containerType;
    
    /**
     * @return the puid
     */
    public String getPuid() {
        return puid;
    }
    
    /**
     * @return the containerType
     */
    public String getContainerType() {
        return containerType;
    }
}
