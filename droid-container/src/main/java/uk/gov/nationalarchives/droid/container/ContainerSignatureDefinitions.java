/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.container;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author rflitcroft
 *
 */
@XmlRootElement(name = "ContainerSignatureMapping")
public class ContainerSignatureDefinitions {

    @XmlElementWrapper(name = "ContainerSignatures")
    @XmlElement(name = "ContainerSignature")
    private List<ContainerSignature> containerSignatures;
    
    @XmlElementWrapper(name = "FileFormatMappings")
    @XmlElement(name = "FileFormatMapping")
    private List<FileFormatMapping> formats;

    @XmlElementWrapper(name = "TriggerPuids")
    @XmlElement(name = "TriggerPuid")
    private List<TriggerPuid> tiggerPuids;
    
    /**
     * @return the containerSignatures
     */
    public List<ContainerSignature> getContainerSignatures() {
        return containerSignatures;
    }
    
    /**
     * @return the formats
     */
    public List<FileFormatMapping> getFormats() {
        return formats;
    }
    
    /**
     * @return the tiggerPuid
     */
    public List<TriggerPuid> getTiggerPuids() {
        return tiggerPuids;
    }
    
}
