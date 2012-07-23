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
package uk.gov.nationalarchives.droid.core.interfaces.archive;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FilenameUtils;

import uk.gov.nationalarchives.droid.core.interfaces.AsynchDroid;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;

/**
 * @author rflitcroft
 *
 */
public class GZipArchiveHandler implements ArchiveHandler {

    private IdentificationRequestFactory factory;
    private AsynchDroid droid;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final void handle(IdentificationRequest request) throws IOException {
        IdentificationRequest archiveRequest = null;
        InputStream in = request.getSourceInputStream(); 
        try {
            URI parent = request.getIdentifier().getUri(); 
            long correlationId = request.getIdentifier().getNodeId();
            final URI uri = ArchiveFileUtils.toGZipUri(parent);
    
            String path = uri.getSchemeSpecificPart();
            String fileName = FilenameUtils.getName(path);
            final RequestMetaData metaData = new RequestMetaData(null, null, fileName);
            
            RequestIdentifier identifier = new RequestIdentifier(uri);
            identifier.setAncestorId(request.getIdentifier().getAncestorId());
            identifier.setParentId(correlationId);
            
            archiveRequest = factory.newRequest(metaData, identifier);
            final InputStream gzin = new GZIPInputStream(in);
            try {
                archiveRequest.open(gzin);
            } finally {
                if (gzin != null) {
                    gzin.close();                
                }
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
        if (archiveRequest != null) {
            droid.submit(archiveRequest);
        }
    }
    
    /**
     * @param factory factory for generating identification requests.
     */
    public final void setFactory(IdentificationRequestFactory factory) {
        this.factory = factory;
    }

    /**
     * @param droidCore the droid core to subnmit requests to.
     */
    public final void setDroidCore(AsynchDroid droidCore) {
        droid = droidCore;
    }
}
