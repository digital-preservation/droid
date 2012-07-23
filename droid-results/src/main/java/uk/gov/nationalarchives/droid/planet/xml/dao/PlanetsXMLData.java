/**
 * Copyright (c) 2012, The National Archives <pronom@nationalarchives.gsi.gov.uk>
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
