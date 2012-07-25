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
package uk.gov.nationalarchives.droid.submitter;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import uk.gov.nationalarchives.droid.core.BinarySignatureIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationException;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.ResultHandler;
import uk.gov.nationalarchives.droid.core.interfaces.archive.ArchiveFormatResolver;
import uk.gov.nationalarchives.droid.core.interfaces.archive.ArchiveHandlerFactory;
import uk.gov.nationalarchives.droid.core.interfaces.archive.TrueZipArchiveHandler;
import uk.gov.nationalarchives.droid.core.interfaces.archive.ZipEntryRequestFactory;
import uk.gov.nationalarchives.droid.core.interfaces.resource.FileSystemIdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;
import uk.gov.nationalarchives.droid.core.SignatureParseException;

/**
 * @author rflitcroft
 * 
 */
public class SubmissionGatewayTest {

    @Test
    public void testDroid4IndentificationWithAZipFile() throws Exception {

        BinarySignatureIdentifier droid = new BinarySignatureIdentifier();
        SubmissionGateway submissionGateway = new SubmissionGateway();
        submissionGateway.setDroidCore(droid);
        
        droid.setSignatureFile("test_sig_files/DROID_SignatureFile_V26.xml");
        ResultHandler resultHandler = mock(ResultHandler.class);
        submissionGateway.setResultHandler(resultHandler);
        submissionGateway.setProcessArchives(true);
        submissionGateway.setExecutorService(Executors.newFixedThreadPool(2));
        
        SubmissionQueue submissionQueue = mock(SubmissionQueue.class);
        submissionGateway.setSubmissionQueue(submissionQueue);

        ArchiveFormatResolver archiveFormatResolver = mock(ArchiveFormatResolver.class);
        ArchiveHandlerFactory archiveHandlerFactory = mock(ArchiveHandlerFactory.class);
        when(archiveFormatResolver.forPuid("x-fmt/412")).thenReturn("ZIP");

        TrueZipArchiveHandler zipHandler = new TrueZipArchiveHandler();
        zipHandler.setDroidCore(submissionGateway);
        zipHandler.setFactory(new ZipEntryRequestFactory());
        zipHandler.setResultHandler(resultHandler);

        when(archiveHandlerFactory.getHandler("ZIP")).thenReturn(zipHandler);

        ArchiveFormatResolver containerFormatResolver = mock(ArchiveFormatResolver.class);
        
        submissionGateway.setArchiveFormatResolver(archiveFormatResolver);
        submissionGateway.setArchiveHandlerFactory(archiveHandlerFactory);
        submissionGateway.setContainerFormatResolver(containerFormatResolver);
        try {
            droid.init();
        } catch (SignatureParseException x) {
            assertEquals("Can't parse signature file", x.getMessage());
        }

        File file = new File("test_sig_files/persistence.zip");
        assertTrue(file.exists());

        ZipFile zipFile = new ZipFile(file);
        int entryCount = 0;
        final Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            if (!entries.nextElement().isDirectory()) {
                entryCount++;
            }
        }
        assertTrue(entryCount > 4);

        URI resourceUri = file.toURI();

        InputStream in = new FileInputStream(file);
        RequestMetaData metaData = new RequestMetaData(file.length(), file.lastModified(), "persistence.zip");
        
        RequestIdentifier identifier = new RequestIdentifier(resourceUri);
        identifier.setParentId(1L);
        identifier.setParentPrefix("X");
        identifier.setAncestorId(1L);
        
        IdentificationRequest request = new FileSystemIdentificationRequest(metaData, identifier);
        request.open(in);

        Future<IdentificationResultCollection> future = submissionGateway.submit(request);
        future.get();

        // Wait for the done() method to complete.
        Thread.sleep(2000);

        ArgumentCaptor<IdentificationResultCollection> resultCaptor = ArgumentCaptor
                .forClass(IdentificationResultCollection.class);
        verify(resultHandler, times(1)).handle(resultCaptor.capture());

        List<IdentificationResultCollection> allResults = resultCaptor.getAllValues();
        assertEquals(1, allResults.size());
    }

    @Test
    public void testResultAvailableWhenIdentificationFailed() throws InterruptedException {

        BinarySignatureIdentifier droid = new BinarySignatureIdentifier();
        
        SubmissionGateway submissionGateway = new SubmissionGateway();
        SubmissionQueue submissionQueue = mock(SubmissionQueue.class);
        submissionGateway.setSubmissionQueue(submissionQueue);
        submissionGateway.setDroidCore(droid);
        
        droid.setSignatureFile("test_sig_files/DROID_SignatureFile_V26.xml");
        ResultHandler resultHandler = mock(ResultHandler.class);
        submissionGateway.setResultHandler(resultHandler);
        submissionGateway.setExecutorService(Executors.newFixedThreadPool(2));
        try {
            droid.init();
        } catch (SignatureParseException e) {
            assertEquals("Can't parse signature file", e.getCause().getMessage());
        }

        File file = new File("test_sig_files/sample.pdf");
        assertTrue(file.exists());

        RequestIdentifier identifier = new RequestIdentifier(file.toURI());
        
        IdentificationRequest request = mock(IdentificationRequest.class);
        when(request.getIdentifier()).thenReturn(identifier);
        when(request.getRequestMetaData()).thenThrow(new RuntimeException("I failed"));

        Future<IdentificationResultCollection> future = submissionGateway.submit(request);

        try {
            future.get();
            fail("Expected Exception.");
        } catch (ExecutionException e) {
            assertTrue(future.isDone());
            assertEquals("I failed", e.getCause().getMessage());
        }

        Thread.sleep(50);

        ArgumentCaptor<IdentificationException> captor = ArgumentCaptor.forClass(IdentificationException.class);
        verify(resultHandler).handleError(captor.capture());

        IdentificationException e = captor.getValue();
        assertEquals("I failed", e.getMessage());
        assertSame(request, e.getRequest());
        assertEquals(RuntimeException.class, e.getCause().getClass());
        assertEquals("I failed", e.getCause().getMessage());

    }
}
