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
package uk.gov.nationalarchives.droid.report;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalConfig;
import uk.gov.nationalarchives.droid.core.interfaces.filter.expressions.Criterion;
import uk.gov.nationalarchives.droid.core.interfaces.filter.Filter;
import uk.gov.nationalarchives.droid.planet.xml.dao.PlanetsXMLData;
import uk.gov.nationalarchives.droid.profile.ProfileContextLocator;
import uk.gov.nationalarchives.droid.profile.ProfileInstance;
import uk.gov.nationalarchives.droid.profile.ProfileInstanceManager;
import uk.gov.nationalarchives.droid.report.dao.ReportLineItem;
import uk.gov.nationalarchives.droid.report.interfaces.CancellableProgressObserver;
import uk.gov.nationalarchives.droid.report.interfaces.GroupedFieldItem;
import uk.gov.nationalarchives.droid.report.interfaces.Report;
import uk.gov.nationalarchives.droid.report.interfaces.ReportCancelledException;
import uk.gov.nationalarchives.droid.report.interfaces.ReportItem;
import uk.gov.nationalarchives.droid.report.interfaces.ReportManager;
import uk.gov.nationalarchives.droid.report.interfaces.ReportRequest;
import uk.gov.nationalarchives.droid.report.interfaces.ReportSpec;
import uk.gov.nationalarchives.droid.report.interfaces.ReportSpecDao;
import uk.gov.nationalarchives.droid.report.interfaces.ReportSpecItem;
import uk.gov.nationalarchives.droid.report.planets.xml.PlanetsXMLGenerator;
import uk.gov.nationalarchives.droid.results.handlers.ProgressObserver;

/**
 * @author Alok Kumar Dash
 */

//CHECKSTYLE:OFF - this class has a fan out complexity one more than allowed
// This is the number of classes this class depends on.
// Quick solution is to strip out the planets xml generator as a special case,  
// once we're happy the improved reporting engine can handle them too.
// But it's still probably too complex in some ways.
public class ReportManagerImpl implements ReportManager {
//CHECKSTYLE:ON
    private final Log log = LogFactory.getLog(getClass());

    private ProfileContextLocator profileContextLocator;
    private ReportSpecDao reportSpecDao;
    private ProgressObserver observer;
    private DroidGlobalConfig config;
    
    /**
     * {@inheritDoc}
     * @deprecated PLANETS XML is now built using XSLT to transform normal report XML.
     */
    @Override
    @Deprecated
    public void generatePlanetsXML(String profileId, String nameAndPathOfTheFile) {

        long startTime = System.currentTimeMillis();
        if (!profileContextLocator.hasProfileContext(profileId)) {
            throw new RuntimeException("Profile not available");
        }
        ProfileInstance profile = profileContextLocator.getProfileInstance(profileId);
        ProfileInstanceManager profileInstancemanager = profileContextLocator.openProfileInstanceManager(profile);

        PlanetsXMLData planetsData = profileInstancemanager.getPlanetsData();

        planetsData.getProfileStat().setProfileStartDate(profile.getProfileStartDate());
        planetsData.getProfileStat().setProfileEndDate(profile.getProfileEndDate());
        planetsData.getProfileStat().setProfileSaveDate(profile.getDateCreated());

        planetsData.setTopLevelItems(ReportUtils.toResourcePaths(profile.getProfileSpec().getResources()));

        PlanetsXMLGenerator planetXMLGenerator = new PlanetsXMLGenerator(observer, nameAndPathOfTheFile, planetsData);
        planetXMLGenerator.generate();

        long stopTime = System.currentTimeMillis();
        log.info(String.format("Time for profile [%s]: %s ms", profileId, stopTime - startTime));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Report generateReport(ReportRequest request, Filter optionalFilter,
            CancellableProgressObserver progressObserver) 
        throws ReportCancelledException {
        log.info(String.format("Generating report: %s", request.getReportSpec().getName()));
        final int totalSteps = request.getProfileIds().size() * request.getReportSpec().getItems().size();
        int stepCount = 0;

        Report report = new Report();
        report.setTitle(request.getReportSpec().getName());
        for (ReportSpecItem specItem : request.getReportSpec().getItems()) {
            ReportItem item = new ReportItem();
            item.setReportSpecItem(specItem);
            report.addItem(item);
            
            Map<String, GroupedFieldItem> groups = new LinkedHashMap<String, GroupedFieldItem>();
            
            for (String profileId : request.getProfileIds()) {
                ProfileInstance profile = profileContextLocator.getProfileInstance(profileId);
                report.addProfile(profile);
                
                ProfileInstanceManager profileInstanceManager = 
                    profileContextLocator.openProfileInstanceManager(profile);
                
                Filter filterToUse = optionalFilter == null ? profile.getFilter() : optionalFilter; 
                
                Criterion filter = ReportUtils.buildFilter(filterToUse, specItem.getFilter());
                
                List<ReportLineItem> reportData = profileInstanceManager.getReportData(
                        filter, specItem.getField(), specItem.getGroupByFields());
    
                for (ReportLineItem reportLineItem : reportData) {
                    final List<String> groupByValues = reportLineItem.getGroupByValues();
                    final String groupKey = getStringListKey(groupByValues);
                    if (!groups.containsKey(groupKey)) {
                        GroupedFieldItem newGroup = new GroupedFieldItem();
                        newGroup.setValues(groupByValues);
                        item.addGroupedFieldItem(newGroup);
                        groups.put(groupKey, newGroup);
                    }
                    
                    GroupedFieldItem groupedFieldItem = groups.get(groupKey);
                    groupedFieldItem.addProfileData(
                            ReportUtils.buildProfileReportData(profile, reportLineItem));
                }
                
                if (progressObserver != null) {
                    progressObserver.onProgress((ProgressObserver.UNITY_PERCENT * stepCount++) / totalSteps);
                    if (progressObserver.isCancelled()) {
                        throw new ReportCancelledException();
                    }
                }
            }
        }
        
        return report;
    }
    
    
    private String getStringListKey(List<String> values) {
        String result = "";
        for (String value : values) {
            result += value + "@";
        }
        return result;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<ReportSpec> listReportSpecs() {
        
        File reportDefDir = config.getReportDefinitionDir();
        
        FileFilter xslFileFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                // Only accept files with names conforming to the pattern:
                // {DESCRIPTION}.{EXTENSION}.XSL
                boolean result = false;
                final String name = f.getName();
                if (FilenameUtils.isExtension(name, "xsl")) {
                    final String baseName = FilenameUtils.getBaseName(name);
                    result = baseName.indexOf('.') > -1;
                }
                return result;
            }
        };
        
        List<File> globalTransforms = new ArrayList<File>();
        for (File transform : reportDefDir.listFiles(xslFileFilter)) {
            globalTransforms.add(transform);
        }

        List<ReportSpec> reportSpecs = new ArrayList<ReportSpec>();
        
        FileFilter xmlFileFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                return FilenameUtils.isExtension(f.getName(), "xml");
            }
        };
        
        for (File reportDir : reportDefDir.listFiles()) {
            if (reportDir.isDirectory()) {
                // Get any local transforms in the directory:
                List<File> reportTransforms = new ArrayList<File>();
                for (File transform : reportDir.listFiles(xslFileFilter)) {
                    reportTransforms.add(transform);
                }
                reportTransforms.addAll(globalTransforms);
                
                // Get any reports in the directory:
                for (File reportDef : reportDir.listFiles(xmlFileFilter)) {
                    if (reportDef.isFile()) {
                        ReportSpec reportSpec;
                        reportSpec = reportSpecDao.readReportSpec(reportDef.getPath());
                        reportSpec.setXslTransforms(reportTransforms);
                        reportSpecs.add(reportSpec);
                    }
                }
            }
        }
        
        return reportSpecs;
    }
    
    /**
     * @param profileContextLocator
     *            the profileContextLocator to set
     */
    public void setProfileContextLocator(ProfileContextLocator profileContextLocator) {
        this.profileContextLocator = profileContextLocator;
    }

    /**
     * @param observer
     *            the observer to set
     */
    public void setObserver(ProgressObserver observer) {
        this.observer = observer;
    }
    
    /**
     * @param reportSpecDao the reportSpecDao to set
     */
    public void setReportSpecDao(ReportSpecDao reportSpecDao) {
        this.reportSpecDao = reportSpecDao;
    }
    
    /**
     * Initialises the bean's working directories, if they don't already exist.
     * @throws IOException if a report definition resource could not be copied to the report def directory.
     */
    public void init() throws IOException {
        ReportUtils.populateReportDefinitionsDirectory(config.getReportDefinitionDir(), getClass().getClassLoader());
    }

    
    /**
     * @param config the config to set
     */
    public void setConfig(DroidGlobalConfig config) {
        this.config = config;
    }

    
}
