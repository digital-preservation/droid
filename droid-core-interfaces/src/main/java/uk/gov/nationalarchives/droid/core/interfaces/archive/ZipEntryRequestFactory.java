/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.core.interfaces.archive;

import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;
import uk.gov.nationalarchives.droid.core.interfaces.resource.ZipEntryIdentificationRequest;

/**
 * Generates requests for entries in a specific zip file.
 * An instance of this class is valid for one, and one only, zip file.
 * @author rflitcroft
 *
 */
public class ZipEntryRequestFactory extends AbstractArchiveRequestFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    public final ZipEntryIdentificationRequest newRequest(RequestMetaData metaData, RequestIdentifier identifier) {
        
        final ZipEntryIdentificationRequest request = new ZipEntryIdentificationRequest(
                metaData, identifier, getTempDirLocation());
        return request;
    }
    
}
