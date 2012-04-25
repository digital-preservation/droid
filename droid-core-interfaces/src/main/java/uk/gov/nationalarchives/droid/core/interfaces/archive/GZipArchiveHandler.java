/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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
