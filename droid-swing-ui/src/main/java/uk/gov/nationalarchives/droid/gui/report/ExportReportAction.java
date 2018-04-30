/**
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gsi.gov.uk>
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
package uk.gov.nationalarchives.droid.gui.report;

import java.awt.Window;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.FilenameUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.nationalarchives.droid.gui.widgetwrapper.SaveAsFileChooser;
import uk.gov.nationalarchives.droid.report.ReportTransformException;
import uk.gov.nationalarchives.droid.report.ReportTransformer;

/**
 * @author rflitcroft
 *
 */
public class ExportReportAction {

    private static final String XHTML_TRANSFORM_LOCATION = "Web page.html.xsl";

    private ReportTransformer reportTransformer;
    private SaveAsFileChooser exportFileChooser;
    
    private Path droidReportXml;
    private List<ExportType> exportTypes;
    private Path lastSelectedDir;
    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    /**
     * Executes the export of a report.
     * 
     * @param parent the parent GUI component.
     * @param xslTransforms a list of xsl transform files which can be applied to this report.
     */
    public void execute(final Window parent, final List<Path> xslTransforms) {
        setExportTypes(xslTransforms);
        setFileFilters();
        int result = exportFileChooser.showSaveDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            ExportType exporter = (ExportType) exportFileChooser.getFileFilter();
            exporter.handleExport(this);
            lastSelectedDir = exportFileChooser.getSelectedFile().toPath().getParent();
        }
    }
    
    /**
     * Initialises the Report Action.
     */
    public void init() {
        /*

        /*
        FileFilter droidXmlFileFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || FilenameUtils.isExtension(f.getName(), ExportType.RAW_XML.extension);
            }
            @Override
            public String getDescription() {
                return String.format(descPattern, ExportType.RAW_XML.description, ExportType.RAW_XML.extension);
            }
        };

        FileFilter textFileFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || FilenameUtils.isExtension(f.getName(), ExportType.RAW_XML.extension);
            }
            @Override
            public String getDescription() {
                return String.format(descPattern, ExportType.TEXT.description, ExportType.TEXT.extension);
            }
        };
        
        exportFileChooser.addChoosableFileFilterWithDefaultExtension(pdfFileFilter, ExportType.PDF.extension);
        exportFileChooser.addChoosableFileFilterWithDefaultExtension(droidXmlFileFilter, ExportType.RAW_XML.extension);
        exportFileChooser.addChoosableFileFilterWithDefaultExtension(textFileFilter, ExportType.TEXT.extension);
        exportFileChooser.setAcceptAllFileFilterUsed(false);
        */
    }

    private void initialiseSaveAsDialog() {
        exportFileChooser = new SaveAsFileChooser();
        exportFileChooser.setSelectedFile(lastSelectedDir.toFile());
        exportFileChooser.setAcceptAllFileFilterUsed(false);
        final String dialogTitle = "Export Report";
        exportFileChooser.setWarningDialogTitle(dialogTitle);
        exportFileChooser.setWarningMessage("%s already exists.\nDo you want to replace it?");
        exportFileChooser.setWrongExtensionMessage("%s does not have the correct extension %s.\n"
            + "Do you want to append the right extension?");
        exportFileChooser.setDialogTitle(dialogTitle);
    }
    
    private void setExportTypes(final List<Path> xslTransforms) {
        exportTypes = new ArrayList<ExportType>();
        for (final Path xslFile : xslTransforms) {
            final String baseName = FilenameUtils.getBaseName(xslFile.getFileName().toString());
            final int stop = baseName.indexOf('.');
            if (stop > -1) {
                final String description = baseName.substring(0, stop);
                final String extension = baseName.substring(stop + 1);
                exportTypes.add(new XSLExportType(extension, description, xslFile));                
            }
        }
        exportTypes.add(new DroidXMLExportType());
        exportTypes.add(new PDFExportType());
    }
    
    private void setFileFilters() {
        initialiseSaveAsDialog();
        for (ExportType exporter : exportTypes) {
            exportFileChooser.addChoosableFileFilterWithDefaultExtension(exporter, exporter.getFileExtension());
        }
        if (exportFileChooser.getFileFilter() == null && exportTypes.size() > 0) {
            exportFileChooser.setFileFilter(exportTypes.get(0));
        }
    }
    
    /**
     * @param reportTransformer the reportTransformer to set
     */
    public void setReportTransformer(ReportTransformer reportTransformer) {
        this.reportTransformer = reportTransformer;
    }
    
    /**
     * @param droidReportXml the droidReportXml to set
     */
    public void setDroidReportXml(final Path droidReportXml) {
        this.droidReportXml = droidReportXml;
    }
    
    
    private List<ExportType> getExportTypes() {
        return null;
    }
    
    private abstract class ExportType extends FileFilter {
        static final String DESC_PATTERN = "%s (*.%s)";
        private final String fileExtension;
        private final String exportDescription;
        
        public ExportType(final String fileExtension, final String exportDescription) {
            this.fileExtension = fileExtension;
            this.exportDescription = exportDescription;
        }        
        
        @Override
        public String getDescription() {
            return String.format(DESC_PATTERN, exportDescription, fileExtension);
        }
        
        public String getFileExtension() {
            return fileExtension;
        }
        
        @Override
        public boolean accept(final File f) {
            return f.isDirectory() || FilenameUtils.isExtension(f.getName(), fileExtension);
        }

        public abstract void handleExport(ExportReportAction action);
        
        protected void logReportExport(final String filename) {
            final String message = String.format("Exporting report as [%s] to: [%s]", exportDescription, filename);
            log.info(message);
        }
    }
    
    private class PDFExportType extends ExportType {
        public PDFExportType() {
            super("pdf", "Adobe Portable Document Format");
        }
        
        @Override
        public void handleExport(final ExportReportAction action) {
            final Path target = action.exportFileChooser.getSelectedFile().toPath();
            logReportExport(target.toAbsolutePath().toString());
            try (final Reader reader = Files.newBufferedReader(action.droidReportXml, UTF_8);
                    final OutputStream out = new BufferedOutputStream(Files.newOutputStream(target))) {
                action.reportTransformer.transformToPdf(reader, XHTML_TRANSFORM_LOCATION, out);
            } catch (final IOException | ReportTransformException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }        
    }
    
    private class DroidXMLExportType extends ExportType {
        public DroidXMLExportType() {
            super("xml", "DROID Report XML");
        }

        @Override
        public void handleExport(final ExportReportAction action) {
            final Path target = action.exportFileChooser.getSelectedFile().toPath();
            logReportExport(target.toAbsolutePath().toString());
            try {
                Files.copy(action.droidReportXml, target);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }        
    }
    
    private class XSLExportType extends ExportType {
        private Path xslFile;
        public XSLExportType(final String fileExtension, final String exportDescription, final Path xslFile) {
            super(fileExtension, exportDescription);
            this.xslFile = xslFile;
        }
        @Override
        public void handleExport(final ExportReportAction action) {
            final Path target = action.exportFileChooser.getSelectedFile().toPath();
            logReportExport(target.toAbsolutePath().toString());
            try (final Reader reader = Files.newBufferedReader(action.droidReportXml, UTF_8);
                final Writer out = Files.newBufferedWriter(target, UTF_8)) {
                action.reportTransformer.transformUsingXsl(reader, xslFile, out);
            } catch (final TransformerException | IOException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }        
    }
}
