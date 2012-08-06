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
package uk.gov.nationalarchives.droid.container.ole2;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import uk.gov.nationalarchives.droid.container.AbstractIdentifierEngine;
import uk.gov.nationalarchives.droid.container.ContainerSignatureMatch;
import uk.gov.nationalarchives.droid.container.ContainerSignatureMatchCollection;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.signature.ByteReader;

/**
 *
 * @author rbrennan
 */
public class Ole2IdentifierEngine extends AbstractIdentifierEngine {

    @Override
    public void process(IdentificationRequest request, ContainerSignatureMatchCollection matches) throws IOException {
        final InputStream in = request.getSourceInputStream();
        try {
            POIFSFileSystem reader = new POIFSFileSystem(in);
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
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }   
}
