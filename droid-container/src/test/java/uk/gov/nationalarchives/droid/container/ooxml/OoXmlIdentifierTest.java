/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.container.ooxml;

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
@Ignore
public class OoXmlIdentifierTest {

    private ZipIdentifier ooXmlIdentifier;
    
    @Before
    public void setup() {
        ooXmlIdentifier = new ZipIdentifier();
    }

    @Test
    public void testIdentifyWordDocument() throws IOException {
        
        ContainerSignature sig = new ContainerSignature();
        sig.setId(100);
        sig.setDescription("Word 97 OOXML");
        
        ContainerFile containerFile = new ContainerFile();
        containerFile.setPath("[Content_Types].xml");
        //containerFile.setTextSignature(
        //      ".*ContentType=\"application/vnd\\.openxmlformats-"
        //    + "officedocument\\.wordprocessingml\\.document\\.main\\+xml\".*");
        sig.setFiles(Arrays.asList(new ContainerFile[] {containerFile}));

        Map<Integer, List<FileFormatMapping>> formats = new HashMap<Integer, List<FileFormatMapping>>();
        FileFormatMapping fileFormat = new FileFormatMapping();
        fileFormat.setPuid("fmt/666");
        List<FileFormatMapping> formatMapping = new ArrayList<FileFormatMapping>();
        formatMapping.add(fileFormat);
        formats.put(100, formatMapping);
        
        
        ooXmlIdentifier.addContainerSignature(sig);
        ooXmlIdentifier.setFormats(formats);
        
        InputStream word97Stream = getClass().getClassLoader().getResourceAsStream("word_ooxml.docx");
        
        RequestMetaData metaData = mock(RequestMetaData.class);
        IdentificationRequest request = mock(IdentificationRequest.class);
        when(request.getSourceInputStream()).thenReturn(word97Stream);
        when(request.getRequestMetaData()).thenReturn(metaData);
        RequestIdentifier requestIdentifier = mock(RequestIdentifier.class);
        when(request.getIdentifier()).thenReturn(requestIdentifier);

        IdentificationResultCollection results = ooXmlIdentifier.submit(request);
        
        assertEquals("fmt/666", results.getResults().iterator().next().getPuid());
    }
    
    @Test
    public void testInitialiseSignaturesAndFormatsFromXml() throws Exception {
        
        URL containerSignatureUrl = getClass().getClassLoader().getResource("container-signature.xml");
        String path = containerSignatureUrl.getPath();
        
        DroidCore droidCore = mock(DroidCore.class);
        ooXmlIdentifier.setDroidCore(droidCore);
        
        ContainerIdentifierFactory containerIdentifierFactory = mock(ContainerIdentifierFactory.class);
        ooXmlIdentifier.setContainerIdentifierFactory(containerIdentifierFactory);
        
        ArchiveFormatResolver containerFormatResolver = mock(ArchiveFormatResolver.class);
        ooXmlIdentifier.setContainerFormatResolver(containerFormatResolver);

        ooXmlIdentifier.setSignatureFileParser(new ContainerSignatureSaxParser());
        ooXmlIdentifier.setContainerType("ZIP");
        ooXmlIdentifier.setSignatureFilePath(path);
        ooXmlIdentifier.init();
        
        List<ContainerSignature> containerSignatures = ooXmlIdentifier.getContainerSignatures();
        assertEquals(3, containerSignatures.size());
        
        ContainerSignature sig = containerSignatures.get(0);
        
        assertEquals("Microsoft Word OOXML", sig.getDescription());
        assertEquals("[Content_Types].xml", sig.listFiles().get(0).getPath());
        //assertEquals(".*ContentType=\"application/vnd\\.openxmlformats-"
        //        + "officedocument\\.wordprocessingml\\.document\\.main\\+xml\".*", 
        //        sig.listFiles().get(0).getTextSignature());
    }
    
    @Test
    public void testInitialiseRegistersOle2ContainerIdentifierWithContainerIdentifierResolver() throws Exception {
        
        URL containerSignatureUrl = getClass().getClassLoader().getResource("container-signature.xml");
        String path = containerSignatureUrl.getPath();
        
        ContainerIdentifierFactory containerIdentifierFactory = mock(ContainerIdentifierFactory.class);
        ArchiveFormatResolver containerFormatResolver = mock(ArchiveFormatResolver.class);
        
        DroidCore droidCore = mock(DroidCore.class);
        ooXmlIdentifier.setDroidCore(droidCore);
        
        ooXmlIdentifier.setContainerIdentifierFactory(containerIdentifierFactory);
        ooXmlIdentifier.setContainerFormatResolver(containerFormatResolver);
        
        ooXmlIdentifier.setSignatureFileParser(new ContainerSignatureSaxParser());
        ooXmlIdentifier.setContainerType("ZIP");
        ooXmlIdentifier.setSignatureFilePath(path);

        ooXmlIdentifier.init();
        
        verify(containerIdentifierFactory, times(2)).addContainerIdentifier("ZIP", ooXmlIdentifier);
        
    }

    @Test
    public void testInitialiseRegistersOle2ContainerFormatsAgainstOoxmlPuid() throws Exception {
        
        URL containerSignatureUrl = getClass().getClassLoader().getResource("container-signature.xml");
        String path = containerSignatureUrl.getPath();
        
        ContainerIdentifierFactory containerIdentifierFactory = mock(ContainerIdentifierFactory.class);
        ArchiveFormatResolver containerFormatResolver = mock(ArchiveFormatResolver.class);
        
        DroidCore droidCore = mock(DroidCore.class);
        ooXmlIdentifier.setDroidCore(droidCore);

        ooXmlIdentifier.setContainerIdentifierFactory(containerIdentifierFactory);
        ooXmlIdentifier.setContainerFormatResolver(containerFormatResolver);
        
        ooXmlIdentifier.setSignatureFileParser(new ContainerSignatureSaxParser());
        ooXmlIdentifier.setContainerType("ZIP");
        ooXmlIdentifier.setSignatureFilePath(path);
        ooXmlIdentifier.init();
        
        verify(containerIdentifierFactory, times(2)).addContainerIdentifier("ZIP", ooXmlIdentifier);
        verify(containerFormatResolver).registerPuid("fmt/189", "ZIP");
        
    }
}
