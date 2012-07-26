/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.command.archive;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipUtils;

import uk.gov.nationalarchives.droid.command.ResultPrinter;
import uk.gov.nationalarchives.droid.command.action.CommandExecutionException;
import uk.gov.nationalarchives.droid.container.ContainerSignatureDefinitions;
import uk.gov.nationalarchives.droid.core.BinarySignatureIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.resource.GZipIdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;

/**
 * Identifier for files held in a GZIP archive.
 * 
 * @author rbrennan
 */
public class GZipArchiveContentIdentifier {

    private static final long SIZE = 12L;
    private static final long TIME = 13L;
    
    private BinarySignatureIdentifier binarySignatureIdentifier;
    private ContainerSignatureDefinitions containerSignatureDefinitions;
    private String path;
    private String slash;
    private File tmpDir;
    
    /**
     * @param binarySignatureIdentifier The Binary Signature Identifier
     * @param containerSignatureDefinitions The Container Signature Definitions
     * @param path The Path to the archive
     * @param slash The slash character to use
     */
    public GZipArchiveContentIdentifier(final BinarySignatureIdentifier binarySignatureIdentifier,
            final ContainerSignatureDefinitions containerSignatureDefinitions, final String path, final String slash) {
    
        synchronized (this) {
            this.binarySignatureIdentifier = binarySignatureIdentifier;
            this.containerSignatureDefinitions = containerSignatureDefinitions;
            this.path = path;
            this.slash = slash;
            if (tmpDir == null) {
                tmpDir = new File(System.getProperty("java.io.tmpdir"));
            }
        }
    }
    
    /**
     * @param uri The URI of the file to identify
     * @param request The Identification Request
     * @throws CommandExecutionException When an exception happens during execution
     */
    public final void identify(final URI uri, final IdentificationRequest request)
        throws CommandExecutionException {
        
        final String newPath = "gzip:" + path + request.getFileName() + "!" + slash;
        
        final RequestIdentifier identifier =
            new RequestIdentifier(URI.create(GzipUtils.getUncompressedFilename(uri.toString())));
        final RequestMetaData metaData = new RequestMetaData(SIZE, TIME, uri.getPath());
        final GZipIdentificationRequest gzRequest = new GZipIdentificationRequest(
            metaData, identifier, tmpDir);
        GzipCompressorInputStream gzin = null;

        try {
            gzin = new GzipCompressorInputStream(new FileInputStream(request.getSourceFile()));
            gzRequest.open(gzin);
            final IdentificationResultCollection gzResults =
                binarySignatureIdentifier.matchBinarySignatures(gzRequest);
            
            final ResultPrinter resultPrinter = new ResultPrinter(binarySignatureIdentifier,
                containerSignatureDefinitions, newPath, slash);
            resultPrinter.print(gzResults, gzRequest);
        } catch (IOException ioe) {
            throw new CommandExecutionException(ioe);
        } finally {
            if (gzin != null) {
                try {
                    gzin.close();
                } catch (IOException ioe) {
                    throw new CommandExecutionException(ioe);
                }
            }
        }
    }
}
