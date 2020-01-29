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
package uk.gov.nationalarchives.droid.report;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.itextpdf.text.DocumentException;

import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalConfig;
import uk.gov.nationalarchives.droid.util.FileUtil;


/**
 * @author rflitcroft
 *
 */
public class ReportTransformerImpl implements ReportTransformer {

    private static final String UTF8 = "UTF-8";

    private DroidGlobalConfig globalConfig;
    private Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Empty bean constructor.
     */
    public ReportTransformerImpl() {
    }

    /**
     * Parameterized constructor.
     * @param config The global config to use.
     */
    public ReportTransformerImpl(DroidGlobalConfig config) {
        this.globalConfig = config;
    }

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
    public void transformUsingXsl(final Reader sourceReader, final String xslScriptLocation, final Writer out)
        throws TransformerException {
        try (final InputStream transform = new BufferedInputStream(getClass().getClassLoader().getResourceAsStream(xslScriptLocation))) {
            transform(sourceReader, transform, out);
        } catch (final IOException e) {
            throw new TransformerException(e);
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
    public void transformUsingXsl(final Reader sourceReader, final Path xslFile, final Writer out)
        throws TransformerException {
        try (final InputStream transform = new BufferedInputStream(Files.newInputStream(xslFile))) {
            transform(sourceReader, transform, out);
        } catch (final IOException e) {
            throw new TransformerException(e);
        }
    }
    
    
    private void transform(Reader sourceReader, InputStream xsl, Writer out) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Source transformSource = new StreamSource(new BufferedReader(new InputStreamReader(xsl, UTF_8)));
        Transformer transformer = transformerFactory.newTransformer(transformSource);
        Source source = new StreamSource(sourceReader);
        Result result = new StreamResult(out);
        transformer.setParameter("reportDir", getReportDir());
        transformer.transform(source, result);        
    }
    
    private String getReportDir() {
        String dir = "";
        if (globalConfig != null) {
            final Path reportDir = globalConfig.getReportDefinitionDir();
            dir = reportDir == null ? "" : reportDir.toAbsolutePath().toString();
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
    public void transformToPdf(final Reader in, final String transformLocation, final OutputStream out)
        throws ReportTransformException {

        try {
            final Path tmpXhtml = Files.createTempFile(globalConfig.getTempDir(), "xhtml~", null);
            tmpXhtml.toFile().deleteOnExit();

            try (final Writer buffer = Files.newBufferedWriter(tmpXhtml, UTF_8))  {
                transformUsingXsl(in, transformLocation, buffer);
                
                final ITextRenderer renderer = new ITextRenderer();
                renderer.setDocument(tmpXhtml.toFile());
                renderer.layout();
                renderer.createPDF(out);
            } catch (TransformerException | DocumentException e) {
                throw new ReportTransformException(e);
            } finally {
                FileUtil.deleteQuietly(tmpXhtml);
            }
        } catch (final IOException e) {
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
