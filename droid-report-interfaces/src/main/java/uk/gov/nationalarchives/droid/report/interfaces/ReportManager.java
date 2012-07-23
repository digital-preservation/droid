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
package uk.gov.nationalarchives.droid.report.interfaces;

import java.util.List;

import uk.gov.nationalarchives.droid.core.interfaces.filter.Filter;
import uk.gov.nationalarchives.droid.results.handlers.ProgressObserver;

/**
 * @author Alok Kumar Dash
 */
public interface ReportManager {
    /**
     * Generates planet xml and writes to path provided.
     * 
     * @param profileId
     *            Profile Id.
     * @param nameAndPathOfTheFile
     *            Path and name of the file.
     */
    void generatePlanetsXML(String profileId, String nameAndPathOfTheFile);

    /**
     * Generates planet xml and writes to path provided.
     * 
     * @return Map<ProfileInstance, PlanetsXMLData> map of results.
     * @param profileIds
     *            Profile Ids.
     * @param reportField
     *            reported field.
     * @param groupByField
     *            groupByField.
     * @param function
     *            Function like month/ year for last modified date field.
     * @param filter
     *            Filter to be applied to reporting.
     *
    Map<ProfileInstance, ReportData> getReportData(List<String> profileIds, Filter filter, ReportFieldEnum reportField,
            ReportFieldEnum groupByField, String function);
    */

    /**
     * Sets Observer.
     * 
     * @param observer
     *            Observer for the progress bar.
     */

    void setObserver(ProgressObserver observer);

    /**
     * FIXME: the only reason we pass in an optional filter
     * is so we can override any filters present in the profiles
     * using a different implementation of filter than FilterImpl,
     * which was made part of the profile interface, instead of
     * conforming to a general filter interface.
     * 
     * Generates a report.
     * @param request the report request
     * @param optionalFilter a filter to pass in
     * @param progressObserver a progress observer
     * @return the report
     * @throws ReportCancelledException if the report was cancelled
     */
    Report generateReport(ReportRequest request, Filter optionalFilter, 
            CancellableProgressObserver progressObserver) 
        throws ReportCancelledException;
    
    /**
     * Lists all available report definitions.
     * @return all available report definitions
     */
    List<ReportSpec> listReportSpecs();

}
