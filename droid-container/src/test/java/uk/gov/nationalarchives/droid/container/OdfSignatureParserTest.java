/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.container;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author rflitcroft
 *
 */
public class OdfSignatureParserTest {

    @Test
    public void testParseOdfManifestSignature() throws Exception {
        
        String xml = 
              "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
            + "<ContainerSignatureMapping>" 
            + "  <ContainerSignatures>"
            + "    <ContainerSignature Id=\"9\">"
            + "      <Description>Open Office Text</Description>"
            + "      <Files>" 
            + "        <File Required=\"true\">"
            + "          <Path>META-INF/manifest.xml</Path>"
            + "          <TextSignature>"
            + "             ^manifest:media-type=\"application\""
            + "          </TextSignature>"
            + "        </File>"
            + "      </Files>" 
            + "    </ContainerSignature>" 
            + "  </ContainerSignatures>"
            + "</ContainerSignatureMapping>";
        
        ContainerSignatureSaxParser parser = new ContainerSignatureSaxParser();
        InputStream in = new ByteArrayInputStream(xml.getBytes("UTF-8"));
        
        List<ContainerSignature> signatures = parser.parse(in).getContainerSignatures();
        assertEquals(1, signatures.size());
        
        ContainerSignature wordSignature = signatures.get(0);
        assertEquals("Open Office Text", wordSignature.getDescription());
        assertEquals(9, wordSignature.getId());
        assertEquals(1, wordSignature.getFiles().size());
        assertEquals("META-INF/manifest.xml", wordSignature.getFiles().get("META-INF/manifest.xml").getPath());
        //assertEquals("^manifest:media-type=\"application\"", 
        //        wordSignature.getFiles().get("META-INF/manifest.xml").getTextSignature());
    }
}
