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
package uk.gov.nationalarchives.droid.container.zip;

import net.java.truevfs.comp.zip.ZipEntry;
import net.java.truevfs.comp.zip.ZipOutputStream;
import org.junit.Test;
import uk.gov.nationalarchives.droid.container.*;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.archive.AbstractArchiveRequestFactory;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;
import uk.gov.nationalarchives.droid.core.interfaces.resource.ZipEntryIdentificationRequest;
import uk.gov.nationalarchives.droid.core.signature.droid6.ByteSequence;
import uk.gov.nationalarchives.droid.core.signature.droid6.InternalSignature;
import uk.gov.nationalarchives.droid.core.signature.droid6.InternalSignatureCollection;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.*;

public class ZipFailureFallbackTest {

    @Test
    public void testFallbackWithCorruptedZipFile() throws IOException {
        String fileName = "file.txt";
        byte[] zipBytes = createZipBytes(fileName);

        //14 bytes from the end is the file count. Setting it to zero causes truevfs to fail but commons-compress succeeds.
        zipBytes[zipBytes.length - 14] = 0x00;

        List<ContainerSignatureMatch> containerSignatureMatches = getContainerSignatureMatches(zipBytes, fileName);

        assertEquals(containerSignatureMatches.size(), 1);
        assertEquals(containerSignatureMatches.getFirst().getSignature().getId(), 1);
        assertTrue(containerSignatureMatches.getFirst().isMatch());
    }

    @Test
    public void testValidZipFileMatches() throws IOException {
        String fileName = "file.txt";
        byte[] zipBytes = createZipBytes(fileName);

        List<ContainerSignatureMatch> containerSignatureMatches = getContainerSignatureMatches(zipBytes, fileName);

        assertEquals(containerSignatureMatches.size(), 1);
        assertEquals(containerSignatureMatches.getFirst().getSignature().getId(), 1);
        assertTrue(containerSignatureMatches.getFirst().isMatch());
    }

    @Test
    public void testNoMatchesIfFileNotFound() throws IOException {
        String fileName = "file.txt";
        String missingFileName = "missingFile.txt";
        byte[] zipBytes = createZipBytes(fileName);

        List<ContainerSignatureMatch> containerSignatureMatches = getContainerSignatureMatches(zipBytes, missingFileName);

        assertEquals(containerSignatureMatches.size(), 1);
        assertFalse(containerSignatureMatches.getFirst().isMatch());
    }

    private static List<ContainerSignatureMatch> getContainerSignatureMatches(byte[] zipBytes, String filePath) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(zipBytes);

        RequestMetaData metaData = new RequestMetaData(1L, 1L, "");
        IdentificationRequest<InputStream> request = new ZipEntryIdentificationRequest(metaData, null, Files.createTempDirectory("test"));
        request.open(inputStream);

        ContainerSignature containerSignature = new ContainerSignature();
        containerSignature.setId(1);
        ContainerFile containerFile = new ContainerFile();
        containerFile.setPath(filePath);

        InternalSignatureCollection internalSignatureCollection = new InternalSignatureCollection();
        InternalSignature internalSignature = new InternalSignature();
        ByteSequence byteSequence = new ByteSequence();
        byteSequence.setSequence("");
        internalSignature.addByteSequence(byteSequence);
        internalSignatureCollection.setInternalSignatures(List.of(internalSignature));
        containerFile.setBinarySignatures(internalSignatureCollection);
        containerSignature.setFiles(List.of(containerFile));

        ContainerSignatureMatchCollection matchCollection = new ContainerSignatureMatchCollection(List.of(containerSignature), List.of(filePath), 1024);
        AbstractArchiveRequestFactory<InputStream> requestFactory = new AbstractArchiveRequestFactory<>() {

            @Override
            public IdentificationRequest<InputStream> newRequest(RequestMetaData metaData, RequestIdentifier identifier) {
                return request;
            }
        };
        ZipIdentifierEngine zipIdentifierEngine = new ZipIdentifierEngine();
        zipIdentifierEngine.setRequestFactory(requestFactory);
        zipIdentifierEngine.process(request, matchCollection);
        return matchCollection.getContainerSignatureMatches();
    }

    private static byte[] createZipBytes(String filePath) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry1 = new ZipEntry(filePath);
            zos.putNextEntry(entry1);
            zos.write("Test".getBytes());
            zos.closeEntry();
        }

        return baos.toByteArray();
    }
}
