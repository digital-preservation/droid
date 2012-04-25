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
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import javax.xml.transform.TransformerException;

/**
 * @author rflitcroft
 *
 */
public interface ReportTransformer {

    /**
     * Transforms a report to XHTML either for display or for use in a subsequent transformation.
     * 
     * @param sourceReader the location of the report to export.
     * @param xslScriptLocation the location of the xsl script as a resource.
     * @param out the destination of the export
     * @throws TransformerException if the transform failed
     * 
     */
    void transformUsingXsl(Reader sourceReader, String xslScriptLocation, Writer out) 
        throws TransformerException;
    
    /**
     * 
     * @param sourceReader the location of the report to export.
     * @param xslFile an xsl file to transform with.
     * @param out the destination of the export
     * @throws TransformerException if the transform failed
     */
    void transformUsingXsl(Reader sourceReader, File xslFile, Writer out) 
        throws TransformerException;

    /**
     * Transforms a report xml to PDF format.
     * 
     * @param in the XHTML file to transform
     * @param transformLocation the location of the XSLT transform
     * @param out the stream to write the PDF to
     * @throws ReportTransformException if the transform failed 
     */
    void transformToPdf(Reader in, String transformLocation, OutputStream out) throws ReportTransformException;

}
