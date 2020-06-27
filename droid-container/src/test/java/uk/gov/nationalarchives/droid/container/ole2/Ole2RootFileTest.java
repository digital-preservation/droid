/**
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
package uk.gov.nationalarchives.droid.container.ole2;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import uk.gov.nationalarchives.droid.container.ContainerFile;
import uk.gov.nationalarchives.droid.container.ContainerSignature;
import uk.gov.nationalarchives.droid.container.ContainerSignatureFileReader;
import uk.gov.nationalarchives.droid.container.ContainerSignatureSaxParser;
import uk.gov.nationalarchives.droid.container.FileFormatMapping;
import uk.gov.nationalarchives.droid.core.interfaces.DroidCore;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.archive.ArchiveFormatResolver;
import uk.gov.nationalarchives.droid.core.interfaces.archive.ContainerIdentifierFactory;
import uk.gov.nationalarchives.droid.core.interfaces.archive.IdentificationRequestFactory;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;

/**
 * @author rflitcroft
 *
 */
public class Ole2RootFileTest {

    private Ole2Identifier ole2Identifier;
    
    
    @Before
    public void setup() {
        ole2Identifier = new Ole2Identifier();
    }
    
    
    @Test
    public void testIdentifyWordDocumentFromRootFile() throws IOException {
        
        ContainerSignature sig = new ContainerSignature();
        sig.setId(100);
        sig.setDescription("Word 97 OLE2");
        
        ContainerFile containerFile = new ContainerFile();
        containerFile.setPath("WordDocument");
        sig.setFiles(Arrays.asList(new ContainerFile[] {containerFile}));
        
        Map<Integer, List<FileFormatMapping>> formats = new HashMap<Integer, List<FileFormatMapping>>();
        FileFormatMapping fileFormat = new FileFormatMapping();
        fileFormat.setPuid("fmt/666");
        List<FileFormatMapping> formatMapping = new ArrayList<FileFormatMapping>();
        formatMapping.add(fileFormat);
        formats.put(100, formatMapping);
        
        
        ole2Identifier.addContainerSignature(sig);
        ole2Identifier.setFormats(formats);
        
        InputStream word97Stream = getClass().getClassLoader().getResourceAsStream("word97.doc");
        
        RequestMetaData metaData = mock(RequestMetaData.class);
        IdentificationRequest request = mock(IdentificationRequest.class);
        when(request.getSourceInputStream()).thenReturn(word97Stream);
        when(request.getRequestMetaData()).thenReturn(metaData);
        RequestIdentifier requestIdentifier = mock(RequestIdentifier.class);
        when(request.getIdentifier()).thenReturn(requestIdentifier);
        
        IdentificationResultCollection results = ole2Identifier.submit(request);
        
        assertEquals("fmt/666", results.getResults().iterator().next().getPuid());
    }

    @Test
    public void testIdentifyWordDocumentFromRootFileAndCompObj() throws IOException {
        
        ContainerSignature sig = new ContainerSignature();
        sig.setId(100);
        sig.setDescription("Word 97 OLE2");
        
        ContainerFile rootFile = new ContainerFile();
        rootFile.setPath("WordDocument");

        ContainerFile compObj = new ContainerFile();
        compObj.setPath("CompObj");

        sig.setFiles(Arrays.asList(new ContainerFile[] {rootFile, compObj}));
        
        Map<Integer, List<FileFormatMapping>> formats = new HashMap<Integer, List<FileFormatMapping>>();
        FileFormatMapping fileFormat = new FileFormatMapping();
        fileFormat.setPuid("fmt/666");
        List<FileFormatMapping> formatMapping = new ArrayList<FileFormatMapping>();
        formatMapping.add(fileFormat);
        formats.put(100, formatMapping);
        
        
        ole2Identifier.addContainerSignature(sig);
        ole2Identifier.setFormats(formats);
        
        InputStream word97Stream = getClass().getClassLoader().getResourceAsStream("word97.doc");
        
        RequestMetaData metaData = mock(RequestMetaData.class);
        IdentificationRequest request = mock(IdentificationRequest.class);
        when(request.getSourceInputStream()).thenReturn(word97Stream);
        when(request.getRequestMetaData()).thenReturn(metaData);
        RequestIdentifier requestIdentifier = mock(RequestIdentifier.class);
        when(request.getIdentifier()).thenReturn(requestIdentifier);
        
        IdentificationRequestFactory requestFactory = mock(IdentificationRequestFactory.class);
        when(requestFactory.newRequest(null, null))
            .thenReturn(request);

        IdentificationResultCollection results = ole2Identifier.submit(request);
        
        assertEquals("fmt/666", results.getResults().iterator().next().getPuid());
    }

    @Test
    public void testIdentifyContainerUsingCompObj() throws IOException {
        
        ContainerSignature wordSig = new ContainerSignature();
        wordSig.setId(100);
        wordSig.setDescription("Word 97 OLE2");
        
        ContainerFile wordDocument = new ContainerFile();
        wordDocument.setPath("WordDocument");

        ContainerFile wordCompObj = new ContainerFile();
        wordCompObj.setPath("CompObj");

        wordSig.setFiles(Arrays.asList(new ContainerFile[] {wordDocument, wordCompObj}));
        
        ContainerSignature excelSig = new ContainerSignature();
        excelSig.setId(100);
        excelSig.setDescription("Word 97 OLE2");
        
        ContainerFile workbook = new ContainerFile();
        workbook.setPath("Workbook");

        ContainerFile excelCompObj = new ContainerFile();
        excelCompObj.setPath("CompObj");

        excelSig.setFiles(Arrays.asList(new ContainerFile[] {workbook, excelCompObj}));

        Map<Integer, List<FileFormatMapping>> formats = new HashMap<Integer, List<FileFormatMapping>>();
        FileFormatMapping fileFormat = new FileFormatMapping();
        fileFormat.setPuid("fmt/666");
        List<FileFormatMapping> formatMapping = new ArrayList<FileFormatMapping>();
        formatMapping.add(fileFormat);
        formats.put(100, formatMapping);
        
        
        ole2Identifier.addContainerSignature(wordSig);
        ole2Identifier.setFormats(formats);
        
        InputStream word97Stream = getClass().getClassLoader().getResourceAsStream("word97.doc");
        
        RequestMetaData metaData = mock(RequestMetaData.class);
        IdentificationRequest request = mock(IdentificationRequest.class);
        when(request.getSourceInputStream()).thenReturn(word97Stream);
        when(request.getRequestMetaData()).thenReturn(metaData);
        RequestIdentifier requestIdentifier = mock(RequestIdentifier.class);
        when(request.getIdentifier()).thenReturn(requestIdentifier);
        
        IdentificationRequestFactory requestFactory = mock(IdentificationRequestFactory.class);
        when(requestFactory.newRequest(null, null)).thenReturn(request);

        IdentificationResultCollection results = ole2Identifier.submit(request);
        
        assertEquals("fmt/666", results.getResults().iterator().next().getPuid());
    }

    @Test
    public void testIdentifyOLE2ContainersWithInternalPaths() throws IOException {

        ContainerSignature omnipageSig = new ContainerSignature();
        omnipageSig.setId(100);
        omnipageSig.setDescription("Omnipage 18 Document");

        ContainerFile docPage1 = new ContainerFile();
        docPage1.setPath("Document/Page1");

        ContainerFile docData = new ContainerFile();
        docData.setPath("Document/Data");

        omnipageSig.setFiles(Arrays.asList(new ContainerFile[] {docPage1, docData}));

        Map<Integer, List<FileFormatMapping>> formats = new HashMap<Integer, List<FileFormatMapping>>();
        FileFormatMapping fileFormat = new FileFormatMapping();
        fileFormat.setPuid("fmt/666");
        List<FileFormatMapping> formatMapping = new ArrayList<FileFormatMapping>();
        formatMapping.add(fileFormat);
        formats.put(100, formatMapping);

        ole2Identifier.addContainerSignature(omnipageSig);
        ole2Identifier.setFormats(formats);

        InputStream omnipageStream = getClass().getClassLoader().getResourceAsStream("OmniPagePro18-Sample2.opd");

        RequestMetaData metaData = mock(RequestMetaData.class);
        IdentificationRequest request = mock(IdentificationRequest.class);
        when(request.getSourceInputStream()).thenReturn(omnipageStream);
        when(request.getRequestMetaData()).thenReturn(metaData);
        RequestIdentifier requestIdentifier = mock(RequestIdentifier.class);
        when(request.getIdentifier()).thenReturn(requestIdentifier);

        IdentificationRequestFactory requestFactory = mock(IdentificationRequestFactory.class);
        when(requestFactory.newRequest(null, null)).thenReturn(request);

        IdentificationResultCollection results = ole2Identifier.submit(request);

        assertEquals("fmt/666", results.getResults().iterator().next().getPuid());
    }
    
    @Test
    public void testInitialiseRegistersOle2ContainerIdentifierWithContainerIdentifierResolver() throws Exception {
        
        URL containerSignatureUrl = getClass().getClassLoader().getResource("container-signature-20200121.xml");
        final Path path = Paths.get(containerSignatureUrl.toURI());

        ContainerIdentifierFactory containerIdentifierFactory = mock(ContainerIdentifierFactory.class);
        ArchiveFormatResolver containerFormatResolver = mock(ArchiveFormatResolver.class);
        
        DroidCore droidCore = mock(DroidCore.class);
        ole2Identifier.setDroidCore(droidCore);

        ole2Identifier.setContainerIdentifierFactory(containerIdentifierFactory);
        ole2Identifier.setContainerFormatResolver(containerFormatResolver);
        ole2Identifier.setSignatureReader(new ContainerSignatureFileReader(path));
        ole2Identifier.setContainerType("OLE2");
        IdentificationRequestFactory requestFactory = mock(IdentificationRequestFactory.class);
        
        IdentificationRequest request = mock(IdentificationRequest.class);
        when(requestFactory.newRequest(null, null))
            .thenReturn(request);

        ole2Identifier.init();
        
        verify(containerIdentifierFactory).addContainerIdentifier("OLE2", ole2Identifier);
        
    }

    @Test
    public void testInitialiseRegistersOle2ContainerFormatsAgainstOoxmlPuid() throws Exception {
        
        URL containerSignatureUrl = getClass().getClassLoader().getResource("container-signature-20200121.xml");
        final Path path = Paths.get(containerSignatureUrl.toURI());
        
        ContainerIdentifierFactory containerIdentifierFactory = mock(ContainerIdentifierFactory.class);
        ArchiveFormatResolver containerFormatResolver = mock(ArchiveFormatResolver.class);
        
        DroidCore droidCore = mock(DroidCore.class);
        ole2Identifier.setDroidCore(droidCore);

        ole2Identifier.setContainerIdentifierFactory(containerIdentifierFactory);
        ole2Identifier.setContainerFormatResolver(containerFormatResolver);
        ole2Identifier.setContainerType("OLE2");
        ole2Identifier.setSignatureReader(new ContainerSignatureFileReader(path));
        ole2Identifier.init();
        
        verify(containerIdentifierFactory).addContainerIdentifier("OLE2", ole2Identifier);
        verify(containerFormatResolver).registerPuid("fmt/111", "OLE2");
        
    }

    @Test
    public void testInitialiseDeregistersOle2BinarySignaturesFromDroid() throws Exception {
        
        URL containerSignatureUrl = getClass().getClassLoader().getResource("container-signature-20200121.xml");
        final Path path = Paths.get(containerSignatureUrl.toURI());
        
        ContainerIdentifierFactory containerIdentifierFactory = mock(ContainerIdentifierFactory.class);
        ArchiveFormatResolver containerFormatResolver = mock(ArchiveFormatResolver.class);
        
        ole2Identifier.setContainerIdentifierFactory(containerIdentifierFactory);
        ole2Identifier.setContainerFormatResolver(containerFormatResolver);
        DroidCore droidCore = mock(DroidCore.class);
        ole2Identifier.setDroidCore(droidCore);
        ole2Identifier.setSignatureReader(new ContainerSignatureFileReader(path));
        ole2Identifier.init();
        
        verify(droidCore).removeSignatureForPuid("fmt/39");
    }
}
