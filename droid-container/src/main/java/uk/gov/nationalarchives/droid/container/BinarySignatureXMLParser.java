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
