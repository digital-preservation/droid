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
package uk.gov.nationalarchives.droid.container.zip;

import java.io.InputStream;
import java.io.IOException;
import java.util.List;

import de.schlichtherle.util.zip.BasicZipFile;
import de.schlichtherle.util.zip.ZipEntry;
import uk.gov.nationalarchives.droid.container.AbstractIdentifierEngine;
import uk.gov.nationalarchives.droid.container.ContainerSignatureMatch;
import uk.gov.nationalarchives.droid.container.ContainerSignatureMatchCollection;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.archive.ReaderReadOnlyFile;
import uk.gov.nationalarchives.droid.core.signature.ByteReader;

/**
 *
 * @author rbrennan
 */
public class ZipIdentifierEngine extends AbstractIdentifierEngine {


    // Use built in java zip processing.
    /*
    @Override
    public void process(IdentificationRequest request, ContainerSignatureMatchCollection matches) throws IOException {
        InputStream in = request.getSourceInputStream();
        try {
            final ZipInputStream zin = new ZipInputStream(in);
            try {
                // For each entry:
                for (String entryName : matches.getAllFileEntries()) {
                    //TODO:MP rewrite zip identifier engine to use built-in java processing.

                }
            } finally {
                zin.close();
            }
        } finally {
            in.close();
        }
    }
    */

    @Override
    public void process(IdentificationRequest request, ContainerSignatureMatchCollection matches) throws IOException {
        BasicZipFile zipFile = new BasicZipFile(new ReaderReadOnlyFile(request.getWindowReader()));
        try {
            // For each entry:
            for (String entryName : matches.getAllFileEntries()) {
                final ZipEntry entry = zipFile.getEntry(entryName);
                if (entry != null) {
                    // Get a stream for the entry and a byte reader over the stream:
                    InputStream stream = zipFile.getInputStream(entry);
                    ByteReader reader = null;
                    try {
                        reader = newByteReader(stream);
                        // For each signature to match:
                        List<ContainerSignatureMatch> matchList = matches.getContainerSignatureMatches();
                        for (ContainerSignatureMatch match : matchList) {
                            match.matchBinaryContent(entryName, reader);
                        }
                    } finally {
                        if (reader != null) {
                            reader.close();
                        }
                        if (stream != null) {
                            stream.close();
                        }
                    }
                }
            }
        } finally {
            zipFile.close();
        }
    }

}
