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

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import uk.gov.nationalarchives.droid.command.ResultPrinter;
import uk.gov.nationalarchives.droid.command.action.CommandExecutionException;
import uk.gov.nationalarchives.droid.container.ContainerSignatureDefinitions;
import uk.gov.nationalarchives.droid.core.BinarySignatureIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;
import uk.gov.nationalarchives.droid.core.interfaces.resource.TarEntryIdentificationRequest;

/**
 * Identifier for files held in a TAR archive.
 * 
 * @author rbrennan
 */
public class TarArchiveContentIdentifier {

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
    public TarArchiveContentIdentifier(final BinarySignatureIdentifier binarySignatureIdentifier,
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
    public void identify(final URI uri, final IdentificationRequest request)
        throws CommandExecutionException {
        
        final String newPath = "tar:" + path + request.getFileName() + "!" + slash;
        
        InputStream tarIn = null;
        try {
            tarIn = request.getSourceInputStream(); 
            final TarArchiveInputStream in = new TarArchiveInputStream(tarIn);
            try {
                TarArchiveEntry entry = null; 
                while ((entry = (TarArchiveEntry) in.getNextTarEntry()) != null) {
                    String name = entry.getName();
                    if (!entry.isDirectory()) {
                        final RequestMetaData metaData = new RequestMetaData(1L, 2L, name);
                        final RequestIdentifier identifier = new RequestIdentifier(uri);
                        final TarEntryIdentificationRequest tarRequest =
                            new TarEntryIdentificationRequest(metaData, identifier, tmpDir);
                        tarRequest.open(in);
                        final IdentificationResultCollection tarResults =
                            binarySignatureIdentifier.matchBinarySignatures(tarRequest);
                        final ResultPrinter resultPrinter =
                            new ResultPrinter(binarySignatureIdentifier,
                                containerSignatureDefinitions, newPath, slash);
                        resultPrinter.print(tarResults, tarRequest);
                    }
                    
                }
            } finally {
                if (in != null) {
                    in.close();
                }
            }
        } catch (IOException e) {
            throw new CommandExecutionException(e);
        } finally {
            if (tarIn != null) {
                try {
                    tarIn.close();
                } catch (IOException ioe) {
                    throw new CommandExecutionException(ioe);
                }
            }
        }
        
    }
}
