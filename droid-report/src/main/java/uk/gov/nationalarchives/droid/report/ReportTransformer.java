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
