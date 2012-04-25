/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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
