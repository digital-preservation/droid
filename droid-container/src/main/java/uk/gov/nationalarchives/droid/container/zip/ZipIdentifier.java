/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.container.zip;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;


import de.schlichtherle.util.zip.BasicZipFile;
import de.schlichtherle.util.zip.ZipEntry;

//import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
//import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import uk.gov.nationalarchives.droid.container.AbstractContainerIdentifier;
import uk.gov.nationalarchives.droid.container.ContainerSignatureMatch;
import uk.gov.nationalarchives.droid.container.ContainerSignatureMatchCollection;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.signature.ByteReader;

/**
 * @author rflitcroft
 *
 */
public class ZipIdentifier extends AbstractContainerIdentifier {
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final void process(IdentificationRequest request, 
        ContainerSignatureMatchCollection matches) throws IOException {
        BasicZipFile zipFile = new BasicZipFile(request.getSourceFile());
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
        
        // 
        
        
        
        /* old code using apache commons zip file processing.
         * very slow to process a ziparchiveinputstream across all entries
         * and is NOT recommended (even by apache).  Will sometimes not
         * return entries in the zip file correctly, as it has to infer
         * them as it scans the stream, rather than looking at the actual
         * zip entry directory (which is at the end of a zip file).
        ZipArchiveInputStream zipIn = new ZipArchiveInputStream(buf);
        
        // try to match against each ZIP signature
        for (ZipArchiveEntry entry = zipIn.getNextZipEntry(); 
            entry != null; 
            entry = zipIn.getNextZipEntry()) {
            
            String entryName = entry.getName();

            boolean needsBinaryMatch = false;

            for (ContainerSignatureMatch match : matches) {
                match.matchFileEntry(entryName);
                if (match.needsBinaryMatch(entryName)) {
                    needsBinaryMatch = true;
                    break;
                }
            }
            
            ByteReader byteReader = null;
            if (needsBinaryMatch) {
                byteReader = newByteReader(zipIn);
                for (ContainerSignatureMatch match : matches) {
                    match.matchBinaryContent(entryName, byteReader);
                }
            }
        }
        */
        
    }


}
