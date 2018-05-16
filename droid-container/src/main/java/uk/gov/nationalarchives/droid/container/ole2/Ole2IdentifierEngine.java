/**
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gsi.gov.uk>
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
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import uk.gov.nationalarchives.droid.container.AbstractIdentifierEngine;
import uk.gov.nationalarchives.droid.container.ContainerSignatureMatch;
import uk.gov.nationalarchives.droid.container.ContainerSignatureMatchCollection;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.FileSystemIdentificationRequest;
import uk.gov.nationalarchives.droid.core.signature.ByteReader;

/**
 *
 * @author rbrennan, boreilly
 */
public class Ole2IdentifierEngine extends AbstractIdentifierEngine {

    private static final String NO_READER_ERROR =
            "No reader was obtained for %s. This may be due to low memory conditions. "
                    + "Try running with a larger heap size!";
    private final Logger log = LoggerFactory.getLogger(Ole2IdentifierEngine.class);

    //CHECKSTYLE:OFF - cyclomatic complexity too high.
    @Override
    public void process(IdentificationRequest request, ContainerSignatureMatchCollection matches) throws IOException {
        //CHECKSTYLE:ON
        final InputStream in = request.getSourceInputStream();
        POIFSFileSystem reader = null;
        try {
            try {
                if (FileSystemIdentificationRequest.class.isAssignableFrom(request.getClass())) {
                    FileSystemIdentificationRequest req = FileSystemIdentificationRequest.class.cast(request);
                    reader = new POIFSFileSystem(req.getFile().toFile());
                } else {
                    reader = new POIFSFileSystem(in);
                }
            } finally {
                // We can get Out Of Memory errors when attempting to instantiate the POIFSFileSystem.  However, these
                // are handled internally by POIFS and not propogated to the calling code.  Therefore we check here
                // whether the reader has been assigned - if not, this is probably due to an Out Of Memory error,
                // possibly caused by a low heap size.
                if (reader == null) {
                    //request.getIdentifier() is null when running in command line 'no profile' mode.
                    String identifier = request.getIdentifier() != null
                            ? request.getIdentifier().getUri().toString() : "the current container file";
                    throw new IOException(String.format(NO_READER_ERROR, identifier));
                }
            }
            DirectoryEntry root = reader.getRoot();
            for (Iterator<Entry> it = root.getEntries(); it.hasNext();) {
                Entry entry = it.next();
                String entryName = entry.getName().trim();

                boolean needsBinaryMatch = false;

                for (ContainerSignatureMatch match : matches.getContainerSignatureMatches()) {
                    match.matchFileEntry(entryName);
                    if (match.needsBinaryMatch(entryName)) {
                        needsBinaryMatch = true;
                    }
                }

                if (needsBinaryMatch) {
                    DocumentInputStream docIn = null;
                    ByteReader byteReader = null;
                    try {
                        docIn = reader.createDocumentInputStream(entry.getName());
                        byteReader = newByteReader(docIn);
                        for (ContainerSignatureMatch match : matches.getContainerSignatureMatches()) {
                            match.matchBinaryContent(entryName, byteReader);
                        }
                    } finally {
                        if (byteReader != null) {
                            byteReader.close();
                        }
                        if (docIn != null) {
                            docIn.close();
                        }
                    }
                }
            }
        } catch (IOException e) {
            //System.out.println(e.getMessage());
            log.error(e.getMessage());
        } finally {
            if (reader != null) {
                reader.close();
            }

            if (in != null) {
                in.close();
            }
        }
    }
}
