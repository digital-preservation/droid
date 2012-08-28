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
package uk.gov.nationalarchives.droid.container.odf;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import uk.gov.nationalarchives.droid.container.ContainerFile;
import uk.gov.nationalarchives.droid.container.ContainerSignature;
import uk.gov.nationalarchives.droid.container.ContainerSignatureSaxParser;
import uk.gov.nationalarchives.droid.container.FileFormatMapping;
import uk.gov.nationalarchives.droid.container.zip.ZipIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.DroidCore;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.archive.ArchiveFormatResolver;
import uk.gov.nationalarchives.droid.core.interfaces.archive.ContainerIdentifierFactory;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;

/**
 * @author rflitcroft
 *
 */
public class OdfIdentifierTest {

    private ZipIdentifier odfIdentifier;
    public static final String CONTAINER_SIGNATUE_FILE = "container-signature-20120828.xml";
    
    
    @Before
    public void setup() {
        odfIdentifier = new ZipIdentifier();
    }

    @Test
    @Ignore
    public void testIdentifyTextDocument() throws IOException {
        
        ContainerSignature sig = new ContainerSignature();
        sig.setId(100);
        sig.setDescription("ODF Wrong text signature");
        
        ContainerFile containerFile = new ContainerFile();
        containerFile.setPath("META-INF/manifest.xml");
        //containerFile.setTextSignature(".*manifest:media-type=\"foo\".*");
        sig.setFiles(Arrays.asList(new ContainerFile[] {containerFile}));
        
        ContainerSignature sig2 = new ContainerSignature();
        sig2.setId(101);
        sig2.setDescription("ODF Text");

        ContainerFile containerFile2 = new ContainerFile();
        containerFile2.setPath("META-INF/manifest.xml");
        //containerFile2.setTextSignature(
        //  ".*manifest:media-type=\"application/vnd\\.oasis\\.opendocument\\.text\".{0,50}manifest:full-path=\"/\".*");
        sig2.setFiles(Arrays.asList(new ContainerFile[] {containerFile2}));

        Map<Integer, List<FileFormatMapping>> formats = new HashMap<Integer, List<FileFormatMapping>>();
        FileFormatMapping fileFormatText = new FileFormatMapping();
        fileFormatText.setPuid("fmt/666");
        List<FileFormatMapping> mappings = new ArrayList<FileFormatMapping>();
        mappings.add(fileFormatText);
        formats.put(100, mappings);
        
        FileFormatMapping fileFormatMath = new FileFormatMapping();
        fileFormatMath.setPuid("fmt/667");
        List<FileFormatMapping> mathmapping = new ArrayList<FileFormatMapping>();
        mathmapping.add(fileFormatMath);
        formats.put(101, mathmapping);
        
        odfIdentifier.addContainerSignature(sig);
        odfIdentifier.addContainerSignature(sig2);
        odfIdentifier.setFormats(formats);
        
        InputStream odfTextStream = getClass().getClassLoader().getResourceAsStream("odf_text.odt");
        
        RequestMetaData metaData = mock(RequestMetaData.class);
        IdentificationRequest request = mock(IdentificationRequest.class);
        when(request.getSourceInputStream()).thenReturn(odfTextStream);
        when(request.getRequestMetaData()).thenReturn(metaData);
        RequestIdentifier requestIdentifier = mock(RequestIdentifier.class);
        when(request.getIdentifier()).thenReturn(requestIdentifier);
        
        IdentificationResultCollection results = odfIdentifier.submit(request);
        
        assertEquals("fmt/667", results.getResults().iterator().next().getPuid());
    }
    
    @Test
    public void testInitialiseRegistersZipContainerIdentifierWithContainerIdentifierResolver() throws Exception {
        
        URL containerSignatureUrl = getClass().getClassLoader().getResource(CONTAINER_SIGNATUE_FILE);
        String path = containerSignatureUrl.getPath();
        
        ContainerIdentifierFactory containerIdentifierFactory = mock(ContainerIdentifierFactory.class);
        ArchiveFormatResolver containerFormatResolver = mock(ArchiveFormatResolver.class);
        
        DroidCore droidCore = mock(DroidCore.class);
        odfIdentifier.setDroidCore(droidCore);

        odfIdentifier.setContainerIdentifierFactory(containerIdentifierFactory);
        odfIdentifier.setContainerFormatResolver(containerFormatResolver);
        
        odfIdentifier.setSignatureFileParser(new ContainerSignatureSaxParser());
        odfIdentifier.setContainerType("ZIP");
        odfIdentifier.setSignatureFilePath(path);

        odfIdentifier.init();
        
        verify(containerIdentifierFactory, times(2)).addContainerIdentifier("ZIP", odfIdentifier);
        
    }

    @Test
    public void testInitialiseRegistersZipContainerFormatsAgainstOdfPuid() throws Exception {
        
        URL containerSignatureUrl = getClass().getClassLoader().getResource(CONTAINER_SIGNATUE_FILE);
        String path = containerSignatureUrl.getPath();
        
        ContainerIdentifierFactory containerIdentifierFactory = mock(ContainerIdentifierFactory.class);
        ArchiveFormatResolver containerFormatResolver = mock(ArchiveFormatResolver.class);
        
        DroidCore droidCore = mock(DroidCore.class);
        odfIdentifier.setDroidCore(droidCore);

        odfIdentifier.setContainerIdentifierFactory(containerIdentifierFactory);
        odfIdentifier.setContainerFormatResolver(containerFormatResolver);
        
        odfIdentifier.setSignatureFileParser(new ContainerSignatureSaxParser());
        odfIdentifier.setContainerType("ZIP");
        odfIdentifier.setSignatureFilePath(path);

        odfIdentifier.init();
        
        verify(containerIdentifierFactory, times(2)).addContainerIdentifier("ZIP", odfIdentifier);
        verify(containerFormatResolver).registerPuid("x-fmt/263", "ZIP");
        
    }
    
}
