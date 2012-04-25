/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui.report;

import java.awt.Window;
import java.io.File;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    
    private File droidReportXml;
    private List<ExportType> exportTypes;
    private File lastSelectedDir;
    private Log log = LogFactory.getLog(this.getClass());
    
    /**
     * Executes the export of a report.
     * 
     * @param parent the parent GUI component.
     * @param xslTransforms a list of xsl transform files which can be applied to this report.
     */
    public void execute(Window parent, List<File> xslTransforms) {
        setExportTypes(xslTransforms);
        setFileFilters();
        int result = exportFileChooser.showSaveDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            ExportType exporter = (ExportType) exportFileChooser.getFileFilter();
            exporter.handleExport(this);
            lastSelectedDir = exportFileChooser.getSelectedFile().getParentFile();
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
        exportFileChooser.setSelectedFile(lastSelectedDir);
        exportFileChooser.setAcceptAllFileFilterUsed(false);
        final String dialogTitle = "Export Report";
        exportFileChooser.setWarningDialogTitle(dialogTitle);
        exportFileChooser.setWarningMessage("%s already exists.\nDo you want to replace it?");
        exportFileChooser.setWrongExtensionMessage("%s does not have the correct extension %s.\n"
            + "Do you want to append the right extension?");
        exportFileChooser.setDialogTitle(dialogTitle);
    }
    
    private void setExportTypes(List<File> xslTransforms) {
        exportTypes = new ArrayList<ExportType>();
        for (File xslFile : xslTransforms) {
            final String baseName = FilenameUtils.getBaseName(xslFile.getName());
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
    public void setDroidReportXml(File droidReportXml) {
        this.droidReportXml = droidReportXml;
    }
    
    
    private List<ExportType> getExportTypes() {
        return null;
    }
    
    private abstract class ExportType extends FileFilter {
        static final String DESC_PATTERN = "%s (*.%s)";
        private String fileExtension;
        private String exportDescription;
        
        public ExportType(String fileExtension, String exportDescription) {
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
        public boolean accept(File f) {
            return f.isDirectory() || FilenameUtils.isExtension(f.getName(), fileExtension);
        }

        public abstract void handleExport(ExportReportAction action);
        
        protected void logReportExport(String filename) {
            String message = String.format("Exporting report as [%s] to: [%s]", exportDescription, filename); 
            log.info(message);
        }
    }
    
    private class PDFExportType extends ExportType {
        public PDFExportType() {
            super("pdf", "Adobe Portable Document Format");
        }
        
        @Override
        public void handleExport(ExportReportAction action) {
            try {
                File target = action.exportFileChooser.getSelectedFile();
                logReportExport(target.getAbsolutePath());
                FileOutputStream out = new FileOutputStream(target);
                FileReader reader = new FileReader(action.droidReportXml);
                action.reportTransformer.transformToPdf(reader, XHTML_TRANSFORM_LOCATION, out);
            } catch (FileNotFoundException e) {
                log.error(e);
                throw new RuntimeException(e);
            } catch (ReportTransformException e) {
                log.error(e);
                throw new RuntimeException(e);
            }
        }        
    }
    
    private class DroidXMLExportType extends ExportType {
        public DroidXMLExportType() {
            super("xml", "DROID Report XML");
        }
        
        @Override
        public void handleExport(ExportReportAction action) {
            File target = action.exportFileChooser.getSelectedFile();
            logReportExport(target.getAbsolutePath());
            try {
                FileWriter writer = new FileWriter(target);
                FileReader reader = new FileReader(action.droidReportXml);
                
                try {
                    IOUtils.copy(reader, writer);
                } finally {
                    reader.close();
                    writer.close();
                }
            } catch (IOException e) {
                log.error(e);
                throw new RuntimeException(e);
            }
        }        
    }
    
    private class XSLExportType extends ExportType {
        private File xslFile;
        public XSLExportType(String fileExtension, String exportDescription, File xslFile) {
            super(fileExtension, exportDescription);
            this.xslFile = xslFile;
        }
        @Override
        public void handleExport(ExportReportAction action) {
            try {
                File target = action.exportFileChooser.getSelectedFile();
                logReportExport(target.getAbsolutePath());
                FileWriter out = new FileWriter(target);
                FileReader reader = new FileReader(action.droidReportXml);
                try {
                    action.reportTransformer.transformUsingXsl(reader, xslFile, out);
                } finally {
                    out.close();
                    reader.close();
                }
            } catch (TransformerException e) {
                log.error(e);
                throw new RuntimeException(e);
            } catch (IOException e) {
                log.error(e);
                throw new RuntimeException(e);
            }
        }        
    }
    
  
    
}
