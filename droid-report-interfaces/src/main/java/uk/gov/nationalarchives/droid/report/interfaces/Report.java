/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.report.interfaces;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import uk.gov.nationalarchives.droid.profile.ProfileInstance;

/**
 * @author rflitcroft
 *
 */
@XmlRootElement(name = "Report")
public class Report {

    @XmlElement(name = "Title")
    private String title;
    
    @XmlElementWrapper(name = "Profiles")
    @XmlElement(name = "Profile")
    private Set<ProfileInstance> profiles = new LinkedHashSet<ProfileInstance>();

    @XmlElementWrapper(name = "ReportItems")
    @XmlElement(name = "ReportItem")
    private List<ReportItem> reportItems = new ArrayList<ReportItem>();
    
    /**
     * 
     * @param title The title of the report.
     */
    public void setTitle(String title) {
        this.title = title;
    }
    
    /**
     * Adds an item to a report.
     * @param item the item to add
     */
    public void addItem(ReportItem item) {
        reportItems.add(item);
    }
    
    /**
     * Adds a profile to the report.
     * @param profile the profile to add
     */
    public void addProfile(ProfileInstance profile) {
        profiles.add(profile);
    }
}
