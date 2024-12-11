/*
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gov.uk>
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import uk.gov.nationalarchives.droid.core.SignatureParseException;

import jakarta.xml.bind.JAXBException;

/**
 * @author rflitcroft
 *
 */
public class ContainerSignatureSaxParserTest {

    @Test
    public void testParseSignatureNoPath() throws Exception {
        String xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                        + "<ContainerSignatureMapping>"
                        + "    <ContainerSignatures>"
                        + "        <ContainerSignature Id=\"9\">"
                        + "            <Files>"
                        + "                <File>"
                        + "                </File>"
                        + "            </Files>"
                        + "        </ContainerSignature>"
                        + "    </ContainerSignatures>"
                        + "</ContainerSignatureMapping>";
        ContainerSignatureSaxParser parser = new ContainerSignatureSaxParser();
        InputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));

        List<ContainerSignature> signatures = parser.parse(in).getContainerSignatures();

        assertEquals(1, signatures.size());

        ContainerSignature signatureNoPath = signatures.get(0);

        Map<String, ContainerFile> files = signatureNoPath.getFiles();
        assertEquals(1, files.size());
        assertTrue(files.containsKey("."));
    }

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

    @Test
    public void folderBasedContainerSignaturesShouldKeepThePathsAsPresentedInSignatureFileForContainerFileMap() throws JAXBException, UnsupportedEncodingException, SignatureParseException {
        String xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                        + "<ContainerSignatureMapping>"
                        + "    <ContainerSignatures>"
                        + "        <ContainerSignature ContainerType=\"ZIP\" Id=\"31020\">"
                        + "            <Description>SIARD 2.1</Description>"
                        + "            <Files>"
                        + "                <File>"
                        + "                   <Path>header/siardversion/2.1/</Path>"
                        + "                </File>"
                        + "            </Files>"
                        + "        </ContainerSignature>"
                        + "        <ContainerSignature ContainerType=\"ZIP\" Id=\"31010\">"
                        + "            <Description>SIARD 2.0</Description>"
                        + "            <Files>"
                        + "                <File>"
                        + "                    <Path>header/metadata.xml</Path>"
                        + "                    <BinarySignatures>"
                        + "                         <InternalSignatureCollection>"
                        + "                             <InternalSignature ID=\"31010\">"
                        + "                                 <ByteSequence Reference=\"BOFoffset\">"
                        + "                                     <SubSequence Position=\"1\" SubSeqMaxOffset=\"256\"\n SubSeqMinOffset=\"50\">"
                        + "                                         <Sequence>'xmlns=\"http://www.bar.admin.ch/xmlns/siard/2.0/metadata.xsd\"'</Sequence>"
                        + "                                     </SubSequence>"
                        + "                                 </ByteSequence>"
                        + "                             </InternalSignature>"
                        + "                         </InternalSignatureCollection>"
                        + "                    </BinarySignatures>"
                        + "                </File>"
                        + "            </Files>"
                        + "        </ContainerSignature>"
                        + "    </ContainerSignatures>"
                        + "</ContainerSignatureMapping>";
        ContainerSignatureSaxParser parser = new ContainerSignatureSaxParser();
        InputStream in = new ByteArrayInputStream(xml.getBytes("UTF-8"));
        List<ContainerSignature> signatures = parser.parse(in).getContainerSignatures();

        Optional<ContainerSignature> siard2_0 = signatures.stream().filter(sig -> sig.getId() == 31010).findFirst();
        assertTrue("siard20 signature not found", siard2_0.isPresent());
        Map<String, ContainerFile> fileMap = siard2_0.get().getFiles();
        assertTrue("expected an entry for 2_0 in file map but found none", fileMap.containsKey("header/metadata.xml"));

        Optional<ContainerSignature> siard2_1 = signatures.stream().filter(sig -> sig.getId() == 31020).findFirst();
        assertTrue("siard20 signature not found", siard2_1.isPresent());
        fileMap = siard2_1.get().getFiles();
        assertTrue("expected an entry for 2_1 in file map but found none", fileMap.containsKey("header/siardversion/2.1/"));
    }
}
