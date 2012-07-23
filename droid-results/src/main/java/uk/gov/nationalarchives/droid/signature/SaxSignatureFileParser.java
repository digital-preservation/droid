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
package uk.gov.nationalarchives.droid.signature;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import uk.gov.nationalarchives.droid.core.interfaces.signature.ErrorCode;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileException;
import uk.gov.nationalarchives.droid.profile.referencedata.Format;

/**
 * @author rflitcroft
 * 
 */
public class SaxSignatureFileParser implements SignatureParser {

    private static final String INVALID_SIGNATURE_FILE = "Invalid signature file [%s]";
    private Log log = LogFactory.getLog(this.getClass());
    
    private File file;

    /**
     * @param filePath
     *            the URI of the signature file to parse
     * @throws SignatureFileException
     *             if the Signature file could not be parsed
     */
    public SaxSignatureFileParser(URI filePath) throws SignatureFileException {
        file = openFile(filePath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void formats(FormatCallback callback) throws SignatureFileException {

        FileFormatHandler handler = new FileFormatHandler(callback);

        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        try {
            SAXParser saxParser = saxParserFactory.newSAXParser();
            saxParser.parse(file, handler);
        } catch (SAXException e) {
            throw new SignatureFileException(String.format(
                    INVALID_SIGNATURE_FILE, file.toURI()), e,
                    ErrorCode.INVALID_SIGNATURE_FILE);
        } catch (ParserConfigurationException e) {
            log.error(e);
            throw new RuntimeException(e.getMessage(), e);
        } catch (IOException e) {
            log.error(e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Opens a signature file for parsing.
     * 
     * @param filePath
     *            the file location (relative or absolute) of the signature file
     *            to parse.
     * @throws SignatureFileException
     *             if the path specified was not a valid signature file.
     */
    private File openFile(URI filePath) throws SignatureFileException {
        File f = new File(filePath);
        if (!f.exists()) {
            throw new SignatureFileException(String.format(
                    "Signature file does not exist [%s]", filePath),
                    ErrorCode.FILE_NOT_FOUND);
        }

        if (!f.isFile()) {
            throw new SignatureFileException(String.format(
                    INVALID_SIGNATURE_FILE, filePath),
                    ErrorCode.INVALID_SIGNATURE_FILE);
        }

        return f;
    }

    /**
     * Handler for &lt;FileFormat&gt; elements.
     * 
     */
    private static final class FileFormatHandler extends DefaultHandler {

        private FormatCallback callback;

        public FileFormatHandler(FormatCallback callback) {
            this.callback = callback;
        }

        @Override
        public void startElement(String uri, String localName, String qName,
                Attributes attributes) {

            if ("FileFormat".equals(qName)) {
                Format format = new Format();
                format.setPuid(notNull(attributes.getValue("PUID")));
                format.setName(notNull(attributes.getValue("Name")));
                format.setMimeType(notNull(attributes.getValue("MIMEType")));
                format.setVersion(notNull(attributes.getValue("Version")));
                callback.onFormat(format);
            }
        }
        
        private String notNull(final String value) {
            return value == null ? "" : value;
        }

    }

}
