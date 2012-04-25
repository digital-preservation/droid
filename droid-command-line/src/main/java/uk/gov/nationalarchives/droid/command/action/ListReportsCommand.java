/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.command.action;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import uk.gov.nationalarchives.droid.command.i18n.I18N;


import uk.gov.nationalarchives.droid.report.interfaces.ReportManager;
import uk.gov.nationalarchives.droid.report.interfaces.ReportSpec;


/**
 * @author a-mpalmer
 *
 */
public class ListReportsCommand implements DroidCommand {

    private PrintWriter printWriter;
    private ReportManager reportManager;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() throws CommandExecutionException {
        List<ReportSpec> reports = reportManager.listReportSpecs();
        if (reports.isEmpty()) {
            printWriter.println(I18N.getResource(I18N.NO_REPORTS_DEFINED));
        } else {
            StringBuilder builder = new StringBuilder();
            for (ReportSpec report : reports) {
                String reportDescription = String.format("\nReport:\t'%s'\n\tFormats:", report.getName());
                builder.append(reportDescription);
                List<String> outputFormats = getReportOutputFormats(report);
                for (String format : outputFormats) {
                    builder.append("\t'");
                    builder.append(format);
                    builder.append("'");
                }
                builder.append("\t'Pdf'\t'DROID Report XML'");
                printWriter.println(builder.toString());
            }
        }
    }
    
    private List<String> getReportOutputFormats(ReportSpec report) {
        List<String> outputFormats = new ArrayList<String>();
        List<File> xslFiles = report.getXslTransforms();
        for (File xslFile : xslFiles) {
            final String baseName = FilenameUtils.getBaseName(xslFile.getName());
            final int stop = baseName.indexOf('.');
            if (stop > -1) {
                final String description = baseName.substring(0, stop);
                outputFormats.add(description);
            }
        }
        return outputFormats;
    }
    
    /**
     * @param printWriter the printWriter to set
     */
    public void setPrintWriter(PrintWriter printWriter) {
        this.printWriter = printWriter;
    }    
    
    /**
     * @param reportManager
     *            the reportManager to set
     */
    public void setReportManager(ReportManager reportManager) {
        this.reportManager = reportManager;
    }    

}
