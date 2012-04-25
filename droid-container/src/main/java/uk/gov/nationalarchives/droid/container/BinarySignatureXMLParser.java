/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.container;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import uk.gov.nationalarchives.droid.core.SignatureParseException;
import uk.gov.nationalarchives.droid.core.signature.xml.SAXModelBuilder;
import uk.gov.nationalarchives.droid.core.signature.xml.SimpleElement;

/**
 * @author rflitcroft
 * 
 * @param <T> the SimpleElement type to create.
 */
public class BinarySignatureXMLParser<T extends SimpleElement> {

    //private final Log log = LogFactory.getLog(getClass());
    
    private SAXParserFactory factory = SAXParserFactory.newInstance();
    
    /**
     * Parses a DOM element to s SimpleElement type.
     * @param element the element to parse
     * @return a SimpleElement
     * @throws SignatureParseException if there was a problem parsing the signature.
     */
    public T fromXmlElement(Element element) throws SignatureParseException {
        
        XMLSerializer serializer = new XMLSerializer();
        StringWriter writer = new StringWriter();
        
        try {
            SAXParser saxParser = factory.newSAXParser();
            final XMLReader xmlReader = saxParser.getXMLReader();

            SAXModelBuilder mb = new SAXModelBuilder();
            xmlReader.setContentHandler(mb);
            serializer.setOutputCharStream(writer);

            serializer.serialize(element);
            final String xml = writer.getBuffer().toString();
            xmlReader.parse(new InputSource(new StringReader(xml)));
            T sig = (T) mb.getModel();
            return sig;
        } catch (ParserConfigurationException e) {
            throw new SignatureParseException(e);
        } catch (SAXException e) {
            throw new SignatureParseException(e);
        } catch (IOException e) {
            throw new SignatureParseException(e);
        } catch (IllegalArgumentException e) {
            throw new SignatureParseException(e);
        }
    }
}
