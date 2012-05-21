/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import uk.gov.nationalarchives.droid.core.interfaces.IdentificationMethod;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.resource.FileSystemIdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;

import uk.gov.nationalarchives.droid.core.signature.FileFormat;
import uk.gov.nationalarchives.droid.core.signature.droid6.InternalSignature;


public class Droid4LegacyDroidTest {

    @Test
    public void testDroid4Indentification() throws Exception {
        
        BinarySignatureIdentifier droid = new BinarySignatureIdentifier();
        droid.setSignatureFile("test_sig_files/DROID_SignatureFile_V26.xml");
//        ResultHandler resultHandler = mock(ResultHandler.class);
//        droid.setResultHandler(resultHandler);
//        droid.setExecutorService(Executors.newFixedThreadPool(2));
//        droid.setProcessArchives(true);
        try {
            droid.init();
        } catch (SignatureParseException x) {
            assertEquals("Can't parse signature file", x.getMessage());
        }
//        ArchiveFormatResolver archiveFormatResolver = mock(ArchiveFormatResolver.class);
//        ArchiveHandlerFactory archiveHandlerFactory = mock(ArchiveHandlerFactory.class);
//        droid.setArchiveFormatResolver(archiveFormatResolver);
//        droid.setArchiveHandlerFactory(archiveHandlerFactory);
        
        File file = new File("test_sig_files/sample.pdf");
        assertTrue(file.exists());
        URI resourceUri = file.toURI();
  
        InputStream in = new FileInputStream(file);
        RequestMetaData metaData = new RequestMetaData(file.length(), file.lastModified(), "sample.pdf");
        RequestIdentifier identifier = new RequestIdentifier(resourceUri);
        identifier.setParentId(1L);
        
        IdentificationRequest request = new FileSystemIdentificationRequest(metaData, identifier);
        request.open(in);
//        IdentificationRequest request = ResourceWrapperFactoryImpl.newResourceWrapper(resourceUri, 1L);
        
        IdentificationResultCollection results = droid.matchBinarySignatures(request);
        
//        ArgumentCaptor<IdentificationResultCollection> resultCaptor = 
//            ArgumentCaptor.forClass(IdentificationResultCollection.class);
//        verify(resultHandler).handle(resultCaptor.capture());
        
        IdentificationResult result = results.getResults().iterator().next();
        assertEquals("fmt/18", result.getPuid());
        assertEquals(1L, results.getCorrelationId().getId());
        assertEquals(IdentificationMethod.BINARY_SIGNATURE, result.getMethod());
        assertEquals("application/pdf", result.getMimeType());
        assertEquals("Acrobat PDF 1.4 - Portable Document Format", result.getName());
        
    }
    
    @Test
    public void testRemovePuidRemovesInterbalSignaturesWithThisPuid() throws Exception {
        
        BinarySignatureIdentifier droid = new BinarySignatureIdentifier();
        droid.setSignatureFile("test_sig_files/DROID_SignatureFile_V26.xml");
        try {
            droid.init();
        } catch (SignatureParseException x) {
            assertEquals("Can't parse signature file", x.getMessage());
        }
        
        List<InternalSignature> signatures = droid.getSigFile().getSignatures();
        
        Set<String> puidsWithSignatures = new HashSet<String>();
        
        for (InternalSignature sig : signatures) {
            for (int i = 0; i < sig.getNumFileFormats(); i++) {
                FileFormat format = sig.getFileFormat(i);
                puidsWithSignatures.add(format.getPUID());
            }
        }
        
        assertEquals(193, puidsWithSignatures.size());
        
        for (String puidToRemove : puidsWithSignatures) {
            droid.removeSignatureForPuid(puidToRemove);
        }
        
        assertEquals(1, signatures.size());
        
    }
    
}
