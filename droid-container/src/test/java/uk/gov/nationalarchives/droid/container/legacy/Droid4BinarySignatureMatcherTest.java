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
package uk.gov.nationalarchives.droid.container.legacy;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.xml.serialize.XMLSerializer;
import org.junit.Test;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import uk.gov.nationalarchives.droid.container.ContainerSignature;
import uk.gov.nationalarchives.droid.container.ContainerSignatureDefinitions;
import uk.gov.nationalarchives.droid.container.ContainerSignatureSaxParser;
import uk.gov.nationalarchives.droid.core.signature.droid6.InternalSignatureCollection;
import uk.gov.nationalarchives.droid.core.signature.droid6.InternalSignature;
import uk.gov.nationalarchives.droid.core.signature.xml.SAXModelBuilder;

/**
 * @author rflitcroft
 *
 */
public class Droid4BinarySignatureMatcherTest {

    @Test
    public void testParseBinarySignatureXmlToInternalSignature() throws Exception {
        
        String xml = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
            + "<ContainerSignatureMapping>" 
            + "  <ContainerSignatures>"
            + "    <ContainerSignature Id=\"9\">"
            + "      <Description>Open Office Text</Description>"
            + "      <Files>" 
            + "        <File Required=\"true\">"
            + "          <Path>META-INF/manifest.xml</Path>"
            + "          <BinarySignatures>"
            + "            <InternalSignatureCollection>"
            + "               <InternalSignature ID=\"150\" Specificity=\"Specific\">"
            + "                 <ByteSequence Reference=\"BOFoffset\">"
            + "                   <SubSequence MinFragLength=\"6\" Position=\"1\""
            + "                     SubSeqMaxOffset=\"0\" SubSeqMinOffset=\"0\">"
            + "                     <Sequence>45786966000049492A00</Sequence>"
            + "                     <LeftFragment MaxOffset=\"2\" MinOffset=\"2\" "
            + "                          Position=\"1\">FFD8FFE1</LeftFragment>"
            + "                   </SubSequence>"
            + "                   <SubSequence MinFragLength=\"0\" Position=\"2\" SubSeqMinOffset=\"0\">"
            + "                     <Sequence>009007000400000030323130</Sequence>"
            + "                   </SubSequence>"
            + "                 </ByteSequence>"
            + "                 <ByteSequence Reference=\"EOFoffset\">"
            + "                   <SubSequence MinFragLength=\"0\" Position=\"1\" "
            + "                        SubSeqMaxOffset=\"0\" SubSeqMinOffset=\"0\">"
            + "                     <Sequence>FFD9</Sequence>"
            + "                   </SubSequence>"
            + "                 </ByteSequence>"
            + "               </InternalSignature>"
            + "             </InternalSignatureCollection>"
            + "          </BinarySignatures>"
            + "        </File>"
            + "      </Files>" 
            + "    </ContainerSignature>" 
            + "  </ContainerSignatures>"
            + "</ContainerSignatureMapping>";
        
        ContainerSignatureSaxParser parser = new ContainerSignatureSaxParser();
        ContainerSignatureDefinitions sigDefs = parser.parse(new ByteArrayInputStream(xml.getBytes()));
        
        final List<ContainerSignature> containerSignatures = sigDefs.getContainerSignatures();
        assertEquals(1, containerSignatures.size());
        
        final Element internalSigXml = containerSignatures.get(0).getFiles().get("META-INF/manifest.xml")
            .getBinarySignature().getElement();
        assertNotNull(internalSigXml);
        
        
        final XMLSerializer serializer = new XMLSerializer();

        StringWriter writer = new StringWriter();
        
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        final XMLReader xmlReader = saxParser.getXMLReader();

        SAXModelBuilder mb = new SAXModelBuilder();
        xmlReader.setContentHandler(mb);
        serializer.setOutputCharStream(writer);

        serializer.serialize(internalSigXml);
        xmlReader.parse(new InputSource(new StringReader(writer.getBuffer().toString())));
        
        InternalSignatureCollection sigs = (InternalSignatureCollection) mb.getModel();
        assertEquals(1, sigs.getInternalSignatures().size());
        InternalSignature sig = sigs.getInternalSignatures().get(0);
        assertEquals(2, sig.getByteSequences().size());
    }
    
}
