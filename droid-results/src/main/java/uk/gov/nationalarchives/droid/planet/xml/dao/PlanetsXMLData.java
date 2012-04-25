/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.planet.xml.dao;

import java.util.List;

/**
 * Encapsulates Planets XML Data.
 * @author Alok Kumar Dash
 *
 */

public class PlanetsXMLData {
    
    //System dependent top level items. 
    private List<String> topLevelItems;
    
    private ProfileStat profileStat;
    
    private List<GroupByYearSizeAndCountRow> groupByYear;
    
    private List<GroupByPuidSizeAndCountRow> groupByPuid;

    /**
     * @return the topLevelItems
     */
    public List<String> getTopLevelItems() {
        return topLevelItems;
    }

    /**
     * @param topLevelItems the topLevelItems to set
     */
    public void setTopLevelItems(List<String> topLevelItems) {
        this.topLevelItems = topLevelItems;
    }

    /**
     * @return the profileStat
     */
    public ProfileStat getProfileStat() {
        return profileStat;
    }

    /**
     * @param profileStat the profileStat to set
     */
    public void setProfileStat(ProfileStat profileStat) {
        this.profileStat = profileStat;
    }

    /**
     * @return the groupByYear
     */
    public List<GroupByYearSizeAndCountRow> getGroupByYear() {
        return groupByYear;
    }

    /**
     * @param groupByYear the groupByYear to set
     */
    public void setGroupByYear(List<GroupByYearSizeAndCountRow> groupByYear) {
        this.groupByYear = groupByYear;
    }

    /**
     * @return the groupByPuid
     */
    public List<GroupByPuidSizeAndCountRow> getGroupByPuid() {
        return groupByPuid;
    }

    /**
     * @param groupByPuid the groupByPuid to set
     */
    public void setGroupByPuid(List<GroupByPuidSizeAndCountRow> groupByPuid) {
        this.groupByPuid = groupByPuid;
    }

}
