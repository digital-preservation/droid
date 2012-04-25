/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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
