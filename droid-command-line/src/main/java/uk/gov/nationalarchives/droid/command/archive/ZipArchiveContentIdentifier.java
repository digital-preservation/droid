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
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import uk.gov.nationalarchives.droid.command.ResultPrinter;
import uk.gov.nationalarchives.droid.command.action.CommandExecutionException;
import uk.gov.nationalarchives.droid.container.ContainerSignatureDefinitions;
import uk.gov.nationalarchives.droid.core.BinarySignatureIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;
import uk.gov.nationalarchives.droid.core.interfaces.resource.ZipEntryIdentificationRequest;

/**
 * Identifier for files held in a ZIP archive
 * 
 * @author rbrennan
 */
public class ZipArchiveContentIdentifier {

    private BinarySignatureIdentifier binarySignatureIdentifier;
    private ContainerSignatureDefinitions containerSignatureDefinitions;
    private String path;
    private String slash;
    private File tmpDir;
    
    public ZipArchiveContentIdentifier (BinarySignatureIdentifier binarySignatureIdentifier,
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
    public void identify(URI uri, IdentificationRequest request)
            throws CommandExecutionException, IOException {
        
        String newPath = "zip:" + path + request.getFileName() + "!" + slash;
        InputStream zipIn = request.getSourceInputStream(); 
        try {
            ZipArchiveInputStream in = new ZipArchiveInputStream(zipIn);
            try {
                ZipArchiveEntry entry = null; 
                while ((entry = (ZipArchiveEntry)in.getNextZipEntry()) != null) {
                    String name = entry.getName();
                    if (entry.isDirectory()) {
//                        System.out.println(".......found directory: " + name);
                    } else {
//                        System.out.println(".......found file: " + name);
                        RequestMetaData metaData = new RequestMetaData(1L, 2L, name);
                        RequestIdentifier identifier = new RequestIdentifier(uri);
                        ZipEntryIdentificationRequest zipRequest =
                            new ZipEntryIdentificationRequest(metaData, identifier, tmpDir);
                        try {
                            zipRequest.open(in);
                            IdentificationResultCollection zipResults =
                                binarySignatureIdentifier.matchBinarySignatures(zipRequest);
//                            System.out.println("zipRequest results: " + zipResults.getResults().size());
                            ResultPrinter resultPrinter =
                                new ResultPrinter(binarySignatureIdentifier,
                                    containerSignatureDefinitions, newPath, slash);
                            resultPrinter.print(zipResults, zipRequest);
                        } catch (IOException e) {
                            throw new CommandExecutionException(e);
                        }
                    }
                }
            } finally {
                if (in != null) {
                    in.close();
                }
            }
        } finally {
            if (zipIn != null) {
                zipIn.close();
            }
        }
    }
}
