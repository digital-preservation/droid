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
