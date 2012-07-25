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
 * Identifier for files held in a GZIP archive
 * 
 * @author rbrennan
 */
public class GZipArchiveContentIdentifier {

    private BinarySignatureIdentifier binarySignatureIdentifier;
    private ContainerSignatureDefinitions containerSignatureDefinitions;
    private String path;
    private String slash;
    private File tmpDir;
    
/**
 * 
 * @param binarySignatureIdentifier
 * @param containerSignatureDefinitions 
 */
    public GZipArchiveContentIdentifier (BinarySignatureIdentifier binarySignatureIdentifier,
            ContainerSignatureDefinitions containerSignatureDefinitions, String path, String slash) {
    
        synchronized(this) {
            this.binarySignatureIdentifier = binarySignatureIdentifier;
            this.containerSignatureDefinitions = containerSignatureDefinitions;
            this.path = path;
            this.slash = slash;
            if(tmpDir == null) {
                tmpDir = new File(System.getProperty("java.io.tmpdir"));
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public final void identify(URI uri, IdentificationRequest request) throws CommandExecutionException, IOException {
        System.out.println(".......gzip uri: " + uri.toString());
        String newPath = "gzip:" + path + request.getFileName() + "!" + slash;
        URI newUri = URI.create(GzipUtils.getUncompressedFilename(uri.toString()));
        System.out.println(".......gzip new uri: " + newUri.toString());
        RequestIdentifier identifier =
                new RequestIdentifier(URI.create(GzipUtils.getUncompressedFilename(uri.toString())));
        RequestMetaData metaData = new RequestMetaData(12L, 13L, uri.getPath());
        GZipIdentificationRequest gzRequest = new GZipIdentificationRequest(
//                new RequestMetaData(12L, 13L, uri.getPath()), identifier, tmpDir);
                metaData, identifier, tmpDir);
        GzipCompressorInputStream gzin = new GzipCompressorInputStream(
                new FileInputStream(request.getSourceFile()));

        try {
            gzRequest.open(gzin);
            IdentificationResultCollection gzResults =
                     binarySignatureIdentifier.matchBinarySignatures(gzRequest);
            
            ResultPrinter resultPrinter = new ResultPrinter(binarySignatureIdentifier,
                    containerSignatureDefinitions, newPath, slash);
            resultPrinter.print(gzResults, gzRequest);
        } finally {
            if (gzin != null) {
                gzin.close();                
            }
        }
    }
}
