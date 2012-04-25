/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.lowagie.text.DocumentException;

import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalConfig;


/**
 * @author rflitcroft
 *
 */
public class ReportTransformerImpl implements ReportTransformer {

    private DroidGlobalConfig globalConfig;
    private Log log = LogFactory.getLog(this.getClass());
    
    /**
     * Transforms a report using xsl either for display or for use in a subsequent transformation.
     * 
     * @param sourceReader the location of the report to export.
     * @param xslScriptLocation the location of the xsl script as a resource.
     * @param out the destination of the export
     * @throws TransformerException if the transform failed
     * 
     */
    @Override
    public void transformUsingXsl(Reader sourceReader, String xslScriptLocation, Writer out) 
        throws TransformerException {
        InputStream transform = getClass().getClassLoader().getResourceAsStream(xslScriptLocation);
        transform(sourceReader, transform, out);
        if (transform != null) {
            try {
                transform.close();
            } catch (IOException e) {
                throw new TransformerException(e);
            }
        }
    }
    
    /**
     * Transforms a report using xsl either for display or for use in a subsequent transformation.
     * 
     * @param sourceReader the location of the report to export.
     * @param xslFile the xsl file to use to transform the report.
     * @param out the destination of the export
     * @throws TransformerException if the transform failed
     * 
     */
    @Override
    public void transformUsingXsl(Reader sourceReader, File xslFile, Writer out) 
        throws TransformerException {
        InputStream transform;
        try {
            transform = new FileInputStream(xslFile);
        } catch (FileNotFoundException e) {
            throw new TransformerException(e);
        }
        transform(sourceReader, transform, out);
    }
    
    
    private void transform(Reader sourceReader, InputStream xsl, Writer out) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Source transformSource = new StreamSource(xsl);
        Transformer transformer = transformerFactory.newTransformer(transformSource);
        Source source = new StreamSource(sourceReader);
        Result result = new StreamResult(out);
        transformer.setParameter("reportDir", getReportDir());
        transformer.transform(source, result);        
    }
    
    private String getReportDir() {
        String dir = "";
        if (globalConfig != null) {
            File reportDir = globalConfig.getReportDefinitionDir();
            dir = reportDir == null ? "" : reportDir.getAbsolutePath();
        }
        return dir;
    }
    
    /**
     * Transforms a report xml to PDF format.
     * 
     * @param in the XHTML file to transform
     * @param transformLocation the location of the XSLT transform
     * @param out the stream to write the PDF to
     * @throws ReportTransformException if the transform failed 
     */
    @Override
    public void transformToPdf(Reader in, String transformLocation, OutputStream out) 
        throws ReportTransformException {
        
        try {
            File tmpXhtml = File.createTempFile("xhtml~", null, globalConfig.getTempDir());
            tmpXhtml.deleteOnExit();
        
            try {
                FileWriter buffer = new FileWriter(tmpXhtml);
                transformUsingXsl(in, transformLocation, buffer);
                buffer.close();
                
                ITextRenderer renderer = new ITextRenderer();
                renderer.setDocument(tmpXhtml);
                renderer.layout();
                renderer.createPDF(out);
            } catch (TransformerException e) {
                throw new ReportTransformException(e);
            } catch (DocumentException e) {
                throw new ReportTransformException(e);
            } finally {
                if (!tmpXhtml.delete() && tmpXhtml.exists()) {
                    String message = String.format("Could not delete temporary XHTML report file:%s. "
                            + "Will try to delete on exit.", 
                            tmpXhtml.getAbsolutePath());
                    log.warn(message);
                    tmpXhtml.deleteOnExit();
                }
            }
        } catch (IOException e) {
            throw new ReportTransformException(e);
        }
    }
    
    /**
     * @param config the globalConfig to set
     */
    public void setConfig(DroidGlobalConfig config) {
        this.globalConfig = config;
    }

}
