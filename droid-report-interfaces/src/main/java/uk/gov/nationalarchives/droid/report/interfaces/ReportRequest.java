/**
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gov.uk>
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
package uk.gov.nationalarchives.droid.report.interfaces;

import java.util.List;

import uk.gov.nationalarchives.droid.core.interfaces.filter.Filter;

/**
 * Encapsulates a reuest for a report.
 * @author rflitcroft
 *
 */
public class ReportRequest {

    private ReportSpec reportSpec;
    private Filter filter;
    private List<String> profileIds;

    /**
     * Empty bean constructor.
     */
    public ReportRequest() {
    }

    /**
     * Parameterized constructor.
     * @param reportSpec The report spec to use.
     * @param filter the filter to use.
     * @param profileIds The list of profile ids to use.
     */
    public ReportRequest(ReportSpec reportSpec, Filter filter, List<String> profileIds) {
        setReportSpec(reportSpec);
        setFilter(filter);
        setProfileIds(profileIds);
    }

    /**
     * @return the filter
     */
    public Filter getFilter() {
        return filter;
    }

    /**
     * @param filter the filter to set
     */
    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    /**
     * @return the profileIds
     */
    public List<String> getProfileIds() {
        return profileIds;
    }

    /**
     * @param profileIds the profileIds to set
     */
    public void setProfileIds(List<String> profileIds) {
        this.profileIds = profileIds;
    }
    
    /**
     * @param reportSpec the reportSpec to set
     */
    public void setReportSpec(ReportSpec reportSpec) {
        this.reportSpec = reportSpec;
    }
    
    /**
     * @return the reportSpec
     */
    public ReportSpec getReportSpec() {
        return reportSpec;
    }
    
    
}
