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
import net.java.truevfs.comp.zip.ZipFile;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.nationalarchives.droid.container.AbstractIdentifierEngine;
import uk.gov.nationalarchives.droid.container.ContainerSignatureMatch;
import uk.gov.nationalarchives.droid.container.ContainerSignatureMatchCollection;
import uk.gov.nationalarchives.droid.container.FileMatcher;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.archive.ByteseekWindowWrapper;
import uk.gov.nationalarchives.droid.core.signature.ByteReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipException;

/**
 *
 * @author rbrennan
 */
public class ZipIdentifierEngine extends AbstractIdentifierEngine {

    private static final FileMatcher FILE_MATCHER = new FileMatcher();

    private static final Logger LOG = LoggerFactory.getLogger(ZipIdentifierEngine.class);

    @Override
    public void process(IdentificationRequest<InputStream> request, ContainerSignatureMatchCollection matches) throws IOException {

        try (ZipFile zipFile = new ZipFile(new ByteseekWindowWrapper(request.getWindowReader()), ZipFile.DEFAULT_CHARSET, true, false)) {
            // For each entry:
            for (String entryName : matches.getAllFileEntries()) {
                final ZipEntry entry = getEntry(entryName, zipFile, request.getFileName());
                if (entry != null) {
                    // Get a stream for the entry and a byte reader over the stream:
                    InputStream stream = zipFile.getInputStream(entry.getName());
                    matchEntry(matches, entryName, stream);
                }
            }
        } catch (ZipException ze) {
            LOG.warn("Initial zip file parsing failed. Will try again with commons-compress {}", ze.getMessage());
            processFallback(request, matches);
        }
    }

    private void processFallback(IdentificationRequest<InputStream> request, ContainerSignatureMatchCollection matches) throws IOException {
        try (var zipFile = org.apache.commons.compress.archivers.zip.ZipFile.builder()
                .setIgnoreLocalFileHeader(true)
                .setSeekableByteChannel(new ByteseekWindowWrapper(request.getWindowReader()))
                .get()) {
            // For each entry:
            for (String entryName : matches.getAllFileEntries()) {
                final ZipArchiveEntry entry = getFallbackEntry(entryName, zipFile, request.getFileName());
                if (entry != null) {
                    // Get a stream for the entry and a byte reader over the stream:
                    InputStream stream = zipFile.getInputStream(entry);
                    matchEntry(matches, entryName, stream);
                }
            }
        }
    }

    private void matchEntry(ContainerSignatureMatchCollection matches, String entryName, InputStream stream) throws IOException {
        try (ByteReader reader = newByteReader(stream)) {
            // For each signature to match:
            List<ContainerSignatureMatch> matchList = matches.getContainerSignatureMatches();
            for (ContainerSignatureMatch match : matchList) {
                match.matchBinaryContent(entryName, reader);
            }
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    private static ZipEntry getEntry(String entryName, ZipFile zipFile, String containerFileName) {
        ZipEntry entry = zipFile.entry(entryName);
        if (entry == null) {
            for (Iterator<? extends ZipEntry> it = zipFile.entries().asIterator(); it.hasNext();) {
                ZipEntry eachEntry = it.next();
                if (!eachEntry.isDirectory()) {
                    if (FILE_MATCHER.fileMatches(entryName, eachEntry.getName(), containerFileName)) {
                        return eachEntry;
                    }

                }
            }
        }
        return entry;
    }

    private static ZipArchiveEntry getFallbackEntry(String entryName, org.apache.commons.compress.archivers.zip.ZipFile zipFile, String containerFileName) {
        ZipArchiveEntry entry = zipFile.getEntry(entryName);
        if (entry == null) {
            for (Iterator<? extends ZipArchiveEntry> it = zipFile.getEntries().asIterator(); it.hasNext();) {
                ZipArchiveEntry eachEntry = it.next();
                if (!eachEntry.isDirectory()) {
                    if (FILE_MATCHER.fileMatches(entryName, eachEntry.getName(), containerFileName)) {
                        return eachEntry;
                    }
                }
            }
        }
        return entry;
    }
}
