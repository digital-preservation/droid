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
package uk.gov.nationalarchives.droid.core;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import uk.gov.nationalarchives.droid.core.signature.droid6.FFSignatureFile;
import uk.gov.nationalarchives.droid.core.signature.xml.SAXModelBuilder;

/**
 * @author rflitcroft
 *
 */
public class SignatureFileParser {

    /** Namespace for the xml file format signatures file. */
    public static final String SIGNATURE_FILE_NS = "http://www.nationalarchives.gov.uk/pronom/SignatureFile";

    /**
     * Create a new signature file object based on a signature file.
     *
     * @param theFileName the file name
     * @return sig file
     * @throws SignatureParseException if there is a problem parsing the signature file.
     */
    FFSignatureFile parseSigFile(String theFileName) throws SignatureParseException {

        SAXModelBuilder mb = new SAXModelBuilder();
        XMLReader parser = getXMLReader(mb);

        //read in the XML file
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(theFileName), "UTF-8"));
            parser.parse(new InputSource(in));
        } catch (IOException e) {
            throw new SignatureParseException(e.getMessage(), e);
        } catch (SAXException e) {
            throw new SignatureParseException(e.getMessage(), e);
        }
        return (FFSignatureFile) mb.getModel();
    }
    
    /**
     * Create the XML parser for the signature file.
     *
     * @param mb sax builder
     * @return XMLReader
     * @throws SignatureParseException on error
     */
    private XMLReader getXMLReader(SAXModelBuilder mb) throws SignatureParseException {

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        SAXParser saxParser;
        try {
            saxParser = factory.newSAXParser();
            XMLReader parser = saxParser.getXMLReader();
            mb.setupNamespace(SIGNATURE_FILE_NS, true);
            parser.setContentHandler(mb);
            return parser;
        } catch (ParserConfigurationException e) {
            throw new SignatureParseException(e.getMessage(), e);
        } catch (SAXException e) {
            throw new SignatureParseException(e.getMessage(), e);
        }
    }

}
