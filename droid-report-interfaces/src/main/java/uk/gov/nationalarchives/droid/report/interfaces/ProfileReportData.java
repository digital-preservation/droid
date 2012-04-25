/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.report.interfaces;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author rflitcroft
 *
 */
public class ProfileReportData extends ReportData {

    @XmlElement(name = "Name")
    private String profileName;
    
    @XmlAttribute(name = "Id")
    private String profileId;
    
    /**
     * @param profileName the profileName to set
     */
    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }
    
    /**
     * @param profileId the profileId to set
     */
    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

}
