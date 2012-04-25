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
public class ContainerSignatureSaxParserTest {

    @Test
    public void testParseSignatures() throws Exception {
        
        String xml = 
              "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
            + "<ContainerSignatureMapping>" 
            + "    <ContainerSignatures>"
            + "        <ContainerSignature Id=\"9\">"
            + "            <Description>Microsoft Word 97</Description>"
            + "            <Files>" 
            + "                <File Required=\"true\">"
            + "                   <Path>WordDocument</Path>"
            + "                </File>"
            + "            </Files>" 
            + "        </ContainerSignature>" 
            + "        <ContainerSignature Id=\"10\">"
            + "            <Description>Microsoft Excel 97</Description>"
            + "            <Files>" 
            + "                <File Required=\"false\">"
            + "                   <Path>Workbook</Path>"
            + "                </File>"
            + "            </Files>" 
            + "        </ContainerSignature>" 
            + "    </ContainerSignatures>"
            + "</ContainerSignatureMapping>";
        
        ContainerSignatureSaxParser parser = new ContainerSignatureSaxParser();
        InputStream in = new ByteArrayInputStream(xml.getBytes("UTF-8"));
        
        List<ContainerSignature> signatures = parser.parse(in).getContainerSignatures();
        assertEquals(2, signatures.size());
        
        ContainerSignature wordSignature = signatures.get(0);
        assertEquals("Microsoft Word 97", wordSignature.getDescription());
        assertEquals(9, wordSignature.getId());
        assertEquals(1, wordSignature.getFiles().size());
        assertEquals("WordDocument", wordSignature.listFiles().get(0).getPath());
        
        ContainerSignature excelSignature = signatures.get(1);
        assertEquals("Microsoft Excel 97", excelSignature.getDescription());
        assertEquals(10, excelSignature.getId());
        assertEquals(1, excelSignature.getFiles().size());
        assertEquals("Workbook", excelSignature.listFiles().get(0).getPath());
    }
}
