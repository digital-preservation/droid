package uk.gov.nationalarchives.droid.command.container;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author rbrennan
 */
public class ZipContainerContentIdentifier extends AbstractContainerContentIdentifier {

    @Override
    public void process (File file, File tmpDir) throws IOException {
        System.out.println("WE NEED TO DO ZIP YET");
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