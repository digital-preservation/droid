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
package uk.gov.nationalarchives.droid.command.action;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.gov.nationalarchives.droid.command.filter.CommandLineFilter;
import uk.gov.nationalarchives.droid.command.filter.DqlFilterParser;
import uk.gov.nationalarchives.droid.command.filter.SimpleFilter;
import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalConfig;
import uk.gov.nationalarchives.droid.core.interfaces.filter.Filter;
import uk.gov.nationalarchives.droid.profile.ProfileInstance;
import uk.gov.nationalarchives.droid.profile.ProfileManager;
import uk.gov.nationalarchives.droid.report.ReportTransformException;
import uk.gov.nationalarchives.droid.report.ReportTransformerImpl;
import uk.gov.nationalarchives.droid.report.interfaces.Report;
import uk.gov.nationalarchives.droid.report.interfaces.ReportCancelledException;
import uk.gov.nationalarchives.droid.report.interfaces.ReportManager;
import uk.gov.nationalarchives.droid.report.interfaces.ReportRequest;
import uk.gov.nationalarchives.droid.report.interfaces.ReportSpec;
import uk.gov.nationalarchives.droid.report.interfaces.ReportXmlWriter;
import uk.gov.nationalarchives.droid.results.handlers.ProgressObserver;

/**
 * @author Alok Kumar Dash
 * 
 */
//CHECKSTYLE:OFF - too much class coupling
public class ReportCommand implements DroidCommand {
//CHECKSTYLE:ON
    
    private static final String DROID_REPORT_XML = "DROID Report XML";
    private static final String PDF_FORMAT = "PDF";
    private static final String XHTML_TRANSFORM_LOCATION = "Web page.html.xsl";
    
    private String[] profiles;
    private ReportManager reportManager;
    private ProfileManager profileManager;
    private ReportXmlWriter reportXmlWriter;
    private String destination;
    private String reportType;
    private DroidGlobalConfig config;
    private String reportOutputType = "pdf";

    private DqlFilterParser dqlFilterParser;    
    private CommandLineFilter cliFilter;    
    
    private Log log = LogFactory.getLog(this.getClass());
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() throws CommandExecutionException {
        List<String> profileIds = new ArrayList<String>();

        SimpleFilter filter = null;
        if (cliFilter != null) {
            filter = new SimpleFilter(cliFilter.getFilterType());
            
            for (String dql : cliFilter.getFilters()) {
                filter.add(dqlFilterParser.parse(dql));
            }
        }
        
        // load each profile
        for (String profileLocation : profiles) {
            ProfileInstance profile;
            try {
                profile = profileManager.open(new File(profileLocation),
                        new ProgressObserver() {
                            @Override
                            public void onProgress(Integer progress) {
                            }
                        });
                profileIds.add(profile.getUuid());
            } catch (IOException e) {
                throw new CommandExecutionException(e);
            }
        }

        ReportRequest request = new ReportRequest();
        request.setProfileIds(profileIds);
        
        List<ReportSpec> reportSpecs = reportManager.listReportSpecs();
        for (ReportSpec reportSpec : reportSpecs) {
            if (reportSpec.getName().equals(reportType)) {
                request.setReportSpec(reportSpec);
                break;
            }
        }
        
        if (request.getReportSpec() == null) {
            throw new CommandExecutionException(
                    String.format("Report [%s] not found.", reportType));
        }
        writeReport(request, filter);

        profileManager.closeProfile(profileIds.get(0));
    }

    private void writeReport(ReportRequest request, Filter optionalFilter) throws CommandExecutionException {
        try {
            // Build the report
            Report report = reportManager.generateReport(request, optionalFilter, null);
            //FIXME: the report transformer is defined as a singleton bean in the export report 
            // action configured through spring.  Here we are instantiating a new specific 
            // transformer - there was a bug in that this one did not have the droid config
            // object configured.  For the time being, just set up this transformer correctly.
            ReportTransformerImpl transformer = new ReportTransformerImpl();
            transformer.setConfig(config);
            String message = String.format("Exporting report as [%s] to: [%s]", reportOutputType, destination); 
            log.info(message);
            if (DROID_REPORT_XML.equalsIgnoreCase(reportOutputType)) {
                FileWriter tempReport = new FileWriter(destination);
                reportXmlWriter.writeReport(report, tempReport);
            } else {
                // Write the report xml to a temporary file:
                File tempFile = File.createTempFile("report~", ".xml", config.getTempDir());
                FileWriter tempReport = new FileWriter(tempFile);
                reportXmlWriter.writeReport(report, tempReport);
                FileReader reader = new FileReader(tempFile);
                try {
                    if (PDF_FORMAT.equalsIgnoreCase(reportOutputType)) {
                        FileOutputStream out = new FileOutputStream(destination);
                        transformer.transformToPdf(reader, XHTML_TRANSFORM_LOCATION, out);
                        out.close();
                    } else {
                        ReportSpec spec = request.getReportSpec();
                        File xslFile = getXSLFile(spec.getXslTransforms());
                        if (xslFile != null) {
                            FileWriter out = new FileWriter(destination);
                            transformer.transformUsingXsl(reader, xslFile, out);
                            out.close();
                        }
                    }
                } finally {
                    reader.close();
                }
            }
        } catch (ReportCancelledException e) {
            throw new CommandExecutionException(e.getMessage(), e);
        } catch (IOException e) {
            throw new CommandExecutionException(e.getMessage(), e);
        } catch (ReportTransformException e) {
            throw new CommandExecutionException(e.getMessage(), e);
        } catch (TransformerException e) {
            throw new CommandExecutionException(e.getMessage(), e);
        }
    }
    
    private File getXSLFile(List<File> xslTransforms) {
        for (File file : xslTransforms) {
            final String transformName = StringUtils.substringBefore(file.getName(), ".");
            if (transformName.equalsIgnoreCase(reportOutputType)) {
                return file;
            }
        }
        return null;
    }
    
    /**
     * @param profileList
     *            the list of profiles to export.
     */
    public void setProfiles(String[] profileList) {
        this.profiles = profileList;
    }

    /**
     * @return the profiles
     */
    String[] getProfiles() {
        return profiles;
    }

    /**
     * @param profileManager
     *            the profileManager to set
     */
    public void setProfileManager(ProfileManager profileManager) {
        this.profileManager = profileManager;
    }

    /**
     * @param destination
     *            the destination to set
     */
    public void setDestination(String destination) {
        this.destination = destination;
    }

    /**
     * @return the destination
     */
    String getDestination() {
        return destination;
    }

    /**
     * @param reportManager
     *            the reportManager to set
     */
    public void setReportManager(ReportManager reportManager) {
        this.reportManager = reportManager;
    }
    
    
    /**
     * @param reportType
     *            the reportType to set
     */
    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    /**
     * 
     * @param reportOutputType The report output type.
     */
    public void setReportOutputType(String reportOutputType) {
        this.reportOutputType = reportOutputType;
    }
    
    /**
     * @param reportXmlWriter the reportXmlWriter to set
     */
    public void setReportXmlWriter(ReportXmlWriter reportXmlWriter) {
        this.reportXmlWriter = reportXmlWriter;
    }
    
    /**
     * Sets the filter.
     * @param filter the filter to set
     */
    public void setFilter(CommandLineFilter filter) {
        this.cliFilter = filter;
    }
    
    /**
     * @param dqlFilterParser the dqlFilterParser to set
     */
    public void setDqlFilterParser(DqlFilterParser dqlFilterParser) {
        this.dqlFilterParser = dqlFilterParser;
    }    
    
    /**
     * 
     * @return The global config used in this report command.
     */
    public DroidGlobalConfig getConfig() {
        return config;
    }

    /**
     * 
     * @param config Sets the global config to use in this report command.
     */
    public void setConfig(DroidGlobalConfig config) {
        this.config = config;
    }
}
