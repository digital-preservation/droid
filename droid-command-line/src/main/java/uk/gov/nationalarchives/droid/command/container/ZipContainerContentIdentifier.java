package uk.gov.nationalarchives.droid.command.container;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import uk.gov.nationalarchives.droid.container.ContainerSignatureMatch;
import uk.gov.nationalarchives.droid.core.interfaces.archive.ZipArchiveHandler;
import uk.gov.nationalarchives.droid.core.signature.ByteReader;

import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;
import uk.gov.nationalarchives.droid.core.interfaces.resource.ZipEntryIdentificationRequest;

/**
 *
 * @author rbrennan
 */
public class ZipContainerContentIdentifier implements ContainerContentIdentifier {

    @Override
    public void process (InputStream in) throws IOException {
        System.out.println("WE NEED TO DO THIS YET");
/*        try {
            final ZipArchiveInputStream zin = new ZipArchiveInputStream(in);
            //ZipArchiveEntry zae = zin.getNextZipEntry();
            ArchiveEntry ae = zin.getNextEntry();
            RequestMetaData metaData = new RequestMetaData(ae.getSize(),
                     ae.getLastModifiedDate().getTime(), ae.getName());
            RequestIdentifier identifier = new RequestIdentifier("oojah");
            IdentificationRequest request = new ZipEntryIdentificationRequest
                    (metaData, identifier, "");
            
            for (ZipArchiveEntry zae : zin.getNextZipEntry()){}
            try {
                Iterable<ZipArchiveEntry> iterable = new Iterable<ZipArchiveEntry>(); // {
                    @Override
                    public final Iterator<ZipArchiveEntry> iterator() {
                        return new ZipArchiveHandler.ZipInputStreamIterator(zin);
                    }
                };
                 
        
                ZipArchiveHandler.ZipArchiveWalker walker = new ZipArchiveHandler.ZipArchiveWalker(zin, request.getIdentifier());  
                walker.walk(iterable);
            } finally {
                if (zin != null) {
                    zin.close();
                }
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }*/
    }
}