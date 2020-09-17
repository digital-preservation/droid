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
package uk.gov.nationalarchives.droid.command.action;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;

import uk.gov.nationalarchives.droid.command.i18n.I18N;


import uk.gov.nationalarchives.droid.report.interfaces.ReportManager;
import uk.gov.nationalarchives.droid.report.interfaces.ReportSpec;


/**
 * @author a-mpalmer
 *
 */
public class ListReportsCommand implements DroidCommand {

    /**
     * SINGLE QUOTE string to be used in messages.
     */
    public static final String SINGLE_QUOTE = "'";

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
            for (ReportSpec report : reports) {
                StringBuilder builder = new StringBuilder();
                String reportDescription = String.format("\nReport:\t'%s'\n\tFormats:\t", report.getName());
                builder.append(reportDescription);

                List<String> outputFormats = getReportOutputFormats(report);
                if (outputFormats != null) {
                    builder.append(outputFormats.stream().map(s -> SINGLE_QUOTE + s + SINGLE_QUOTE).collect(Collectors.joining("\t")));
                }

                builder.append("\t'Pdf'\t'DROID Report XML'");
                printWriter.println(builder.toString());
            }
        }
    }
    
    private List<String> getReportOutputFormats(ReportSpec report) {
        final List<String> outputFormats = new ArrayList<>();
        final List<Path> xslFiles = report.getXslTransforms();
        for (final Path xslFile : xslFiles) {
            final String baseName = FilenameUtils.getBaseName(xslFile.getFileName().toString());
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
