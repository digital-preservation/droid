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
        
        getIdentifierEngine().process(request, matches);
    }


}
